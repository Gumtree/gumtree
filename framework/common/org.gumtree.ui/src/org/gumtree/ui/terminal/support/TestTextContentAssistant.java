package org.gumtree.ui.terminal.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Testing content assistance in text input
 *
 */
public class TestTextContentAssistant {

	private ContentProposalProvider provider;

	private StyledText display;

	private Text input;

	private boolean proposalAccepted;

	private boolean inputFocusLost;

	private boolean canCommit;

	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());

		display = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		display.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		display.setCaretOffset(0);

		input = new Text(parent, SWT.BORDER | SWT.SINGLE);
		input.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		provider = new ContentProposalProvider();

		KeyStroke keyStroke = null;
		try {
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		ContentProposalAdapter adapter = new ContentProposalAdapter(input,
				new TextContentAdapter(), provider, keyStroke, null);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		adapter.setPropagateKeys(false);

		proposalAccepted = false;
		inputFocusLost = false;

		adapter.addContentProposalListener(new IContentProposalListener() {
			public void proposalAccepted(IContentProposal proposal) {
				proposalAccepted = true;
				if(inputFocusLost) {
					canCommit = false;
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
				System.out.println("proposalAccepted");
			}
		});

		input.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
					if(input.getText() != null && input.getText().length() > 0) {
						System.out.println("keyReleased");
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
						proposalAccepted = false;
						inputFocusLost = false;
						provider.addNewProposal(input.getText());
						display.append(">> " + input.getText() + "\n\n");
						input.setText("");
					}
				}
			}
		});

		input.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				System.out.println("focusLost");
				inputFocusLost = true;
			}
		});

		input.forceFocus();
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("My SWT GUI");
		shell.setSize(600, 600);
		shell.setLayout(new FillLayout());

		TestTextContentAssistant testObject = new TestTextContentAssistant();
		testObject.createPartControl(shell);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	class ContentProposalProvider implements IContentProposalProvider {

		private Map<String, IContentProposal> proposalMap;

		public ContentProposalProvider() {
			proposalMap = new TreeMap<String, IContentProposal>();
		}

		public IContentProposal[] getProposals(String content, int position) {
			if(content == null || content.length() == 0) {
				return proposalMap.values().toArray(new IContentProposal[proposalMap.values().size()]);
			}
			List<IContentProposal> matchedProposals = new ArrayList<IContentProposal>();
			for(Entry<String, IContentProposal> proposalEntry : proposalMap.entrySet()) {
				if(proposalEntry.getKey().startsWith(content)) {
					matchedProposals.add(proposalEntry.getValue());
				}
			}
			return matchedProposals.toArray(new IContentProposal[matchedProposals.size()]);
		}

		public void addNewProposal(final String content) {
			if(!proposalMap.containsKey(content)) {
				proposalMap.put(content, new IContentProposal() {
					public String getContent() {
						return content;
					}
					public int getCursorPosition() {
						return getContent().length();
					}
					public String getDescription() {
						return null;
					}
					public String getLabel() {
						return null;
					}
				});
			}
		}
	}
}
