package org.gumtree.ui.terminal.support;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ICommandLineTerminal;
import org.gumtree.ui.terminal.ICommunicationAdapter;
import org.gumtree.ui.terminal.ICommunicationAdapterDescriptor;
import org.gumtree.ui.terminal.ICommunicationAdapterRegistry;
import org.gumtree.ui.terminal.ICommunicationConfigPart;

public class CommandLineTerminal extends ViewPart implements ICommandLineTerminal {

	private static int viewActivationCount = 1;

	private ICommunicationAdapterRegistry adapterRegistry;
	
	private TerminalText textDisplay;

	private Text textInput;

	private Composite adapterConfigComposite;

	private Group testDisplayGroup;

	private Button connectButton;

	private ComboViewer comboViewer;

	private ICommunicationAdapter adapter;

	private int readLineIndex;

	private boolean proposalAccepted;

	private boolean inputFocusLost;

	private boolean canCommit;

	private FilteredContentProposalProvider provider;

	private List<String> histories;

	public CommandLineTerminal() {
		super();
		readLineIndex = -1;
		proposalAccepted = false;
		inputFocusLost = false;
		adapter = null;
		textDisplay = null;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		testDisplayGroup = new Group(composite, SWT.NONE);
		testDisplayGroup.setText("Terminal");
		createMainGroup(testDisplayGroup);
		testDisplayGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Group textInputGroup = new Group(composite, SWT.NONE);
		textInputGroup.setText("Input");
		textInputGroup.setLayout(new FillLayout(SWT.VERTICAL));
		textInputGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Label label = new Label(textInputGroup, SWT.NONE);
		label.setText("Press Ctrl+Space to trigger command history");
		textInput = new Text(textInputGroup,  SWT.BORDER | SWT.SINGLE);
		textInput.setEnabled(false);

		// Set content assistance
		provider = new FilteredContentProposalProvider();
		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		proposalAccepted = false;
		ContentProposalAdapter adapter = new ContentProposalAdapter(textInput,
				new TextContentAdapter(), provider, keyStroke, null);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		adapter.addContentProposalListener(new IContentProposalListener() {
			public void proposalAccepted(IContentProposal proposal) {
				proposalAccepted = true;
				if(inputFocusLost) {
					canCommit = false;
					// add time delay to avoid fast key release that leads to commit
					new Thread(new Runnable() {
						public void run() {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							} finally {
								canCommit = true;
							}
						}
					}).start();
				}
			}
		});


		textInput.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				inputFocusLost = true;
			}
		});

		textInput.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(getAdapter() == null) {
					return;
				}
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
					// proposal was selected without focusing into the popup,
					// so textInput key listener will be trigger
					// to prevent command is committed, a special flag is risen
					if (proposalAccepted) {
						if(!inputFocusLost) {
							// proposal accepted in text box
							proposalAccepted = false;
							return;
						} else {
							if(!canCommit) {
								// proposal accepted in pop up menu with enter
								return;
							}
						}
					}
					commitCommand(textInput.getText());
				} else if (e.keyCode == SWT.ARROW_UP) {
					if(getHistories().isEmpty())
						return;
					// Initialise
					if(readLineIndex == -1) {
						readLineIndex = getHistories().size() - 1;
					} else if(readLineIndex != 0) {
						// If it does not reach top command
						readLineIndex = readLineIndex - 1;
					}
					updateTextInput(getHistories().get(readLineIndex));
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					if(getHistories().isEmpty() || readLineIndex == -1) {
						return;
					}
					// If it does not reach last command
					if(readLineIndex != getHistories().size() - 1) {
						readLineIndex = readLineIndex + 1;
					}
					updateTextInput(getHistories().get(readLineIndex));
				}
			}
		});

		// clear tool actions
		if (getViewSite() != null) {
			IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
			mgr.removeAll();
		}
	}

	private void updateTextInput(String text) {
		textInput.setText(text);
		textInput.setSelection(text.length());
	}

	private void createMainGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		Composite configComposite = new Composite(parent, SWT.NONE);
		configComposite.setLayout(new GridLayout());

		Label label = new Label(configComposite, SWT.NONE);
		label.setText("Select communication provider: ");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		comboViewer = new ComboViewer(configComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider());
		comboViewer.setInput(getAdapterRegistry().getAdapterDescriptors());
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if(selection instanceof ICommunicationAdapterDescriptor) {
					ICommunicationAdapterDescriptor desc = (ICommunicationAdapterDescriptor)selection;
					try {
						setAdapter(desc.createNewAdapter());
					} catch (CommunicationAdapterException e) {
						e.printStackTrace();
					}
				}
			}
		});
		adapterConfigComposite = new Composite(configComposite, SWT.NONE);
		adapterConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		connectButton = new Button(configComposite, SWT.PUSH);
		connectButton.setText("Connect");
		connectButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		connectButton.setEnabled(false);
		connectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					connect();
				} catch (CommunicationAdapterException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private void switchTextDisplay() {
		if(testDisplayGroup != null && !testDisplayGroup.isDisposed()) {
			for(Control control : testDisplayGroup.getChildren()) {
				control.dispose();
			}
			testDisplayGroup.setLayout(new FillLayout());
			textDisplay = new TerminalText(testDisplayGroup, SWT.MULTI | SWT.V_SCROLL
	                | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.READ_ONLY);
			testDisplayGroup.layout(true);
		}
	}

	private ICommunicationAdapter getAdapter() {
		return adapter;
	}

	private void setAdapter(ICommunicationAdapter adapter) {
//		if(this.adapter != null) {
//			System.out.println("Trying to change adapter!");
//		}
		this.adapter = adapter;
		ICommunicationConfigPart part = getAdapter().createConfigPart();
		if(part != null && adapterConfigComposite != null && !adapterConfigComposite.isDisposed()) {
			for(Control child : adapterConfigComposite.getChildren()) {
				child.dispose();
			}
			part.createControlPart(adapterConfigComposite);
			adapterConfigComposite.layout(true);
			connectButton.setEnabled(true);
			// clear tool actions
			if (getViewSite() != null) {
				IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
				mgr.removeAll();
				mgr.update(true);
			}
		}
	}

	@Override
	public void setFocus() {
	}

	public void dispose() {
		if(getAdapter() != null) {
			if(getAdapter().isConnected()) {
				adapter.disconnect();
			}
		}
		adapterRegistry = null;
		super.dispose();
	}

	private List<String> getHistories() {
		if(histories == null) {
			histories = new ArrayList<String>();
		}
		return histories;
	}

	protected void commitCommand(String text) {
		try {
			getHistories().add(text);
			provider.addNewProposal(text);
			getAdapter().send(text);
			textInput.setText("");
			readLineIndex = -1;
		} catch (CommunicationAdapterException e) {
			printError(e);
		}
	}

	public static int getViewActivationCount() {
		return viewActivationCount;
	}

	public static void addViewActivationCount() {
		viewActivationCount++;
	}

	public void selectCommunicationAdapter(String adapterId) {
		ICommunicationAdapterDescriptor desc = getAdapterRegistry().getAdapterDescriptor(adapterId);
		if(desc != null) {
			try {
				comboViewer.setSelection(new StructuredSelection(desc));
				setAdapter(desc.createNewAdapter());
			} catch (CommunicationAdapterException e) {
				e.printStackTrace();
			}
		}
	}

	public ICommunicationAdapterRegistry getAdapterRegistry() {
		if (adapterRegistry == null) {
			adapterRegistry = ServiceUtils
					.getService(ICommunicationAdapterRegistry.class);
		}
		return adapterRegistry;
	}

	public void setAdapaterRegistry(
			ICommunicationAdapterRegistry adapterRegistry) {
		this.adapterRegistry = adapterRegistry;
	}
	
	public void connect() throws CommunicationAdapterException {
		if(getAdapter() != null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						switchTextDisplay();
						getAdapter().connect(new TerminalOutputBuffer(textDisplay));
						textInput.setEnabled(true);
						textInput.setFocus();
						IAction[] toolActions = getAdapter().getToolActions();
						if(toolActions != null) {
							for(IAction action : toolActions) {
								if (getViewSite() != null) {
									IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
									mgr.add(action);
									mgr.update(true);
								}
							}
						}
					} catch (CommunicationAdapterException e) {
						printError(e);
					}
				}
			});
		} else {
			throw new CommunicationAdapterException("Communication adapter is missing.");
		}
	}

	private void printError(final Exception e) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				textDisplay.append("Cannot connect to selected adapter.\n");
				textDisplay.append(e.getMessage() + '\n');
				// Exception cause can be null 
				if(e.getCause() != null) {
					textDisplay.append(e.getCause().toString());
				}
			}
		});
	}

	public void disconnect() throws CommunicationAdapterException {
		if(getAdapter() != null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						if(getAdapter().isConnected()) {
							adapter.disconnect();
						}
						textDisplay.append("\nDisconnected\n");
						textInput.setEnabled(false);
//						textInput.setFocus();
//						IAction[] toolActions = getAdapter().getToolActions();
//						if(toolActions != null) {
//							for(IAction action : toolActions) {
//								if (getViewSite() != null) {
//									IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
//									mgr.add(action);
//									mgr.update(true);
//								}
//							}
//						}
					} catch (Exception e) {
						printError(e);
					}
				}
			});
		} else {
			throw new CommunicationAdapterException("Communication adapter is missing.");
		}
	}
	
}
