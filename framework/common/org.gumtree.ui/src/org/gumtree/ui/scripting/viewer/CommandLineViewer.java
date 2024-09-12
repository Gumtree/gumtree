package org.gumtree.ui.scripting.viewer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.EvalChangeEvent;
import org.gumtree.scripting.IObservableComponent;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.IScriptingListener;
import org.gumtree.scripting.ObservableScriptContext;
import org.gumtree.scripting.ScriptExecutorEvent;
import org.gumtree.scripting.ScriptingChangeEvent;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.scripting.ICommandLineContentAssistProcessor;
import org.gumtree.ui.scripting.ICommandLineToolRegistry;
import org.gumtree.ui.scripting.support.ExportOutputAction;
import org.gumtree.ui.scripting.tools.ICommandLineTool;
import org.gumtree.ui.scripting.viewer.HistoryContentProvider.CommandHistory;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.workbench.AbstractPartControlProvider;
import org.gumtree.util.PlatformUtils;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineViewer extends AbstractPartControlProvider implements ICommandLineViewer {

	// TODO: Make this configurable or determine from the GUI width
//	private static final int DEFAULT_LINE_WRAP = 80;
	
	private static final String PROP_PREFIX_TOOLS = "gumtree.scripting.tools.";
	
	private static final int DEFAULT_LINE_LIMIT = 10000;
	
	private static final String PROP_CONSOLE_LINELIMIT = "gumtree.scripting.linelimit";
	
	// ID for default history tool
	private static final String ID_TOOL_HISTORY = "history";
	
	private static Logger logger = LoggerFactory.getLogger(CommandLineViewer.class);
	
	private int style = SWT.NONE;
	
	private boolean inputEnabled = true;
	
	private IScriptExecutor executor;
	
	// GumTree scripting API listener
	private IScriptingListener listener;
	
	// Script executor listener
	private IEventHandler<ScriptExecutorEvent> executorEventHandler;
	
	private ITextViewer consoleTextViewer;
	
	private SourceViewer inputTextViewer;
	
	private TableViewer historyTableViewer;
	
	private TableViewer commandTableViewer;
	
	private TabFolder tabFolder;
	
	private ICommandHistoryList historyList;
	
	private ProgressBar progressBar;
	
	private int previousDocLength = 0;
	
	private int readLineIndex = -1;
	
	private Font fontNormal;
	
	private Font fontBold;
	
	private Font fontItalic;
	
	private boolean contentAssistEnabled;
	
	private boolean scrollLocked;
	
	private MenuItem scrollLockItem;
	
	private ContentAssistant assistant;
	
	private UIResourceManager resourceManager;
	
	private boolean inLocalScale = false;
	
	private boolean inLineScale = false;
	
	private int defaultLineLimit = DEFAULT_LINE_LIMIT;
	
	private PrintWriter writer;
	
	private PrintWriter errorWriter;
	
	public IScriptExecutor getScriptExecutor() {
		return executor;
	}

	public CommandLineViewer() {
		super();
	}
	
	public CommandLineViewer(boolean isInputEnabled) {
		this();
		this.inputEnabled = isInputEnabled;
	}
	
	public class SharedPrintWriter extends PrintWriter {
		
		private OutputStream stream;
		
		public SharedPrintWriter(OutputStream stream) {
			super(stream, true);
			this.stream = stream;
		}
		
		public OutputStream getStream() {
			return stream;
		}
		
		public void write(String s) {
			super.write(s);
			// Hack to get Jepp to display text
			flush();
		}
	}

	public void setScriptExecutor(final IScriptExecutor executor) {
		this.executor = executor;
		// Wait for engine to be ready
		while (!executor.isInitialised()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { }
		}
		final ScriptEngine engine = executor.getEngine();
		if (engine instanceof IObservableComponent) {
			listener = new IScriptingListener() {
				public void handleChange(final ScriptingChangeEvent event) {
					if (event instanceof EvalChangeEvent) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (consoleTextViewer != null && !consoleTextViewer.getTextWidget().isDisposed()) {
									print("\n\n>> ", Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED), SWT.NORMAL);
									println(((EvalChangeEvent) event).getScript(), null, SWT.BOLD);
								}
							}
						});
					}
				}
			};
			((IObservableComponent) engine).addListener(listener);
		}
		// Print intro
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				println("Engine: " + engine.getFactory().getEngineName());
				println("Engine Version: " + engine.getFactory().getEngineVersion());
				println("Language: " + engine.getFactory().getLanguageName());
				println("Language Version: " + engine.getFactory().getLanguageVersion());
				println("");
			}
		});
		
		
		ScriptContext scriptContext = engine.getContext();
		if (scriptContext == null) {
			// Same engine (like Jepp) does not provide default context out of the box
			ScriptContext context = new ObservableScriptContext();
			engine.setContext(context);
			scriptContext = engine.getContext();
		}
		OutputStream writerStream = new ByteArrayOutputStream() {
			public synchronized void flush() throws IOException {
				final String text = toString();
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						print(text, Display.getDefault().getSystemColor(SWT.COLOR_BLUE), SWT.NORMAL);
					}
				});
				reset();
			}
		};
		
		writer = new SharedPrintWriter(writerStream);
		scriptContext.setWriter(writer);
		final OutputStream errorStream = new ByteArrayOutputStream() {
			public synchronized void flush() throws IOException {
				final String text = toString();
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						print(text, Display.getDefault().getSystemColor(SWT.COLOR_RED), SWT.NORMAL);
					}
				});
				reset();
			}
		};
		errorWriter = new SharedPrintWriter(errorStream);
		scriptContext.setErrorWriter(errorWriter);
		// Update context
		final ScriptContext updatedContext = scriptContext;
		executor.runTask(new Runnable() {
			public void run() {
				engine.setContext(updatedContext);
			}
		});
		
		// Register executor listener
		executorEventHandler = new IEventHandler<ScriptExecutorEvent>() {
			@Override
			public void handleEvent(final ScriptExecutorEvent event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						// Disable input text when busy
						if (inputTextViewer != null && !inputTextViewer.getTextWidget().isDisposed()) {
							if (executor.isBusy()) {
//								System.out.println("Disable console input");
								inputTextViewer.setEditable(false);
								Color gray = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
								inputTextViewer.getTextWidget().setBackground(gray);
								progressBar.setVisible(true);
							} else {
//								System.out.println("Enable console input");
								inputTextViewer.setEditable(true);
								Color gray = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
								inputTextViewer.getTextWidget().setBackground(gray);
								progressBar.setVisible(false);
							}
						}
					}
				});
			}			
		};
		PlatformUtils.getPlatformEventBus().subscribe(executor, executorEventHandler);
		
		
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (tabFolder != null) {
					// Setup Tools
					String[] toolIds = getToolProperty(engine).split(",");
					ICommandLineToolRegistry toolRegistry = ServiceUtils.getService(ICommandLineToolRegistry.class);
					boolean historyToolCreated = false;
					for (String toolId : toolIds) {
						toolId = toolId.trim();
						if (toolId.equalsIgnoreCase(ID_TOOL_HISTORY) && !historyToolCreated) {
							// Special case: create default history tool
							createHistoryArea(tabFolder);
							// Can't create history tool twice
							historyToolCreated = true;
						} else {
							ICommandLineTool tool = null;
							try {
								tool = toolRegistry.createCommandLineTool(toolId);
							} catch (CoreException e) {
								logger.error("Failed to instantiate tool for id " + toolId + ".", e);
							}
							String label = toolRegistry.getCommandLineToolLabel(toolId);
							if (tool != null) {
								tool.setScriptExecutor(executor);
								createToolArea(tabFolder, tool, label);
							} else {
								logger.info("Tool " + toolId + " is missing.");
							}
						}
					}
				}
				
				// Setup content assistant
				if (inputTextViewer != null) {
					final ICommandLineContentAssistProcessor processor = (ICommandLineContentAssistProcessor) Platform
							.getAdapterManager().loadAdapter(engine,
									ICommandLineContentAssistProcessor.class.getName());
					if (processor != null) {
						processor.setScriptExecutor(executor);
						SourceViewerConfiguration configuration= new SourceViewerConfiguration() {
							public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
								assistant = new ContentAssistant();
								assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
								// Use this to activate context information
								assistant.enableAutoActivation(isContentAssistEnabled());
								assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
								assistant.setProposalSelectorBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
								return assistant;
							}
						};
						// Install content assist
						inputTextViewer.configure(configuration);
					}
				}
				
				// Test drive the engine
				executor.runScript("", true);
			}
		});
		
//		if (commandTableViewer != null && engine instanceof IGumShell) {
//			commandTableViewer.setInput(engine);
//		}
	}

	private String getToolProperty(ScriptEngine engine) {
		// Help to find the tool configurations for this engine
		String toolProperty = null;
		for (String engineName : engine.getFactory().getNames()) {
			toolProperty = System.getProperty(PROP_PREFIX_TOOLS + engineName);
			if (toolProperty != null) {
				break;
			}
		}
		if (toolProperty == null) {
			toolProperty = "history";
		}
		return toolProperty;
	}
	
	public PrintWriter getWriter() {
		return writer;
	}
	
	public PrintWriter getErrorWriter() {
		return errorWriter;
	}
	
	public String getConsoleText() {
		if (consoleTextViewer != null && !consoleTextViewer.getTextWidget().isDisposed()) {
			return consoleTextViewer.getTextWidget().getText();
		}
		return "";
	}
	
	public String[] getCommandHistory() {
		if (historyList != null) {
			return historyList.getCommands();
		}
		return new String[0];
	}
	
	public int getStyle() {
		return style;
	}

	public void createPartControl(Composite parent, int style) {
		this.style = style;
		try {
			defaultLineLimit = Integer.valueOf(System.getProperty(PROP_CONSOLE_LINELIMIT));
		} catch (Exception e) {
		}
		createPartControl(parent);
	}
	
	public void createControl(Composite parent) {
		parent.setLayout(new FillLayout());
		resourceManager = new UIResourceManager(Activator.PLUGIN_ID, parent);
		historyList = new HistoryContentProvider();
		
		if ((style & NO_UTIL_AREA) == 0) {
			SashForm sashForm = new SashForm(parent, SWT.NONE);
			createMainArea(sashForm, style);
			Composite utilArea = new Composite(sashForm, SWT.NONE);
			utilArea.setLayout(new FillLayout());
			tabFolder = new TabFolder(utilArea, SWT.NONE);
			sashForm.setWeights(new int[] {5, 3});
		} else {
			createMainArea(parent, style);
		}
	}

	private void createMainArea(Composite parent, int style) {
		Composite consoleArea = new Composite(parent, SWT.NONE);
		consoleArea.setLayout(new GridLayout(2, false));
		if ((style & NO_INPUT_TEXT) == 0) {
			createTerminalArea(consoleArea);
			createProgressArea(consoleArea);
			if (inputEnabled) {
				createInputArea(consoleArea);
			}
		} else {
			createTerminalArea(consoleArea);
			createProgressArea(consoleArea);
		}
	}
	
	private void createTerminalArea(Composite parent) {
		consoleTextViewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.WRAP | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(2, 1).grab(true, true).applyTo(consoleTextViewer.getTextWidget());
		consoleTextViewer.setDocument(new Document());
		// Add drop support
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DropTarget dropTarget = new DropTarget(consoleTextViewer.getTextWidget(), operations);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (FileTransfer.getInstance().isSupportedType(
						event.currentDataType)) {
					String[] files = (String[]) event.data;
					if (files.length == 1) {
						try {
							appendScript(new FileReader(files[0]));
						} catch (FileNotFoundException e) {
							logger.error("Cannot read from file " + files[0], e);
						}
					}
				}
			}
		});
		// Add context menu
		createContextMenu();
	}
	
	private void createContextMenu() {
		Menu menu = new Menu(consoleTextViewer.getTextWidget());
		consoleTextViewer.getTextWidget().setMenu(menu);
		/*********************************************************************
		 * Interrupt
		 *********************************************************************/
		final MenuItem interruptItem = new MenuItem(menu, SWT.PUSH);
		interruptItem.setText("Interrupt");
		interruptItem.setImage(InternalImage.INTERRUPT.getImage());
		interruptItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean isInterrupt = MessageDialog.openConfirm(interruptItem
						.getParent().getShell(), "Interrupt Script",
						"Do you want interrupt your running script?");
				if (isInterrupt) {
					getScriptExecutor().interrupt();
				}
			}
		});
		
		/*********************************************************************
		 * Scroll lock
		 *********************************************************************/
		scrollLockItem = new MenuItem(menu, SWT.CHECK);
		scrollLockItem.setText("Scroll Lock");
		scrollLockItem.setSelection(isScrollLocked());
		scrollLockItem.setImage(InternalImage.SCROLL_LOCK.getImage());
		scrollLockItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setScrollLocked(scrollLockItem.getSelection());
			}
		});

		/*********************************************************************
		 * Clear console text
		 *********************************************************************/
		final MenuItem clearItem = new MenuItem(menu, SWT.PUSH);
		clearItem.setText("Clear Console");
		clearItem.setImage(InternalImage.CLEAR.getImage());
		clearItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				clearConsole();
			}
		});
		
		/*********************************************************************
		 * Separator
		 *********************************************************************/
		new MenuItem(menu, SWT.SEPARATOR);
		
		/*********************************************************************
		 * Export console output
		 *********************************************************************/
		final Action exportOutputAction = new ExportOutputAction(this, menu.getShell());
		MenuItem exportOutputItem = new MenuItem(menu, SWT.PUSH);
		exportOutputItem.setText("Export Console Output");
		exportOutputItem.setImage(exportOutputAction.getImageDescriptor().createImage());
		exportOutputItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				exportOutputAction.run();
			}
		});
	}
	
	private void createProgressArea(Composite parent) {
		progressBar = new ProgressBar(parent, SWT.INDETERMINATE);
		progressBar.setVisible(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false).hint(SWT.DEFAULT, 10).applyTo(progressBar);
	}
	
	private void createInputArea(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		Font font = resourceManager.createRelativeFont(label.getFont(), 4, SWT.BOLD);
		label.setFont(font);
		label.setText("Command >");
		// Require SWT.MULTI to get tab working on LinkModelUI
		inputTextViewer = new SourceViewer(parent, null, SWT.MULTI | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(inputTextViewer.getTextWidget());
		font = resourceManager.createRelativeFont(inputTextViewer.getTextWidget().getFont(), 4, SWT.NONE);
		inputTextViewer.getTextWidget().setFont(font);
		inputTextViewer.setDocument(new Document());
		inputTextViewer.appendVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				if (event.keyCode == SWT.KEYPAD_CR || event.keyCode == SWT.CR) {
					String command = inputTextViewer.getTextWidget().getText();
					if (historyList != null) {
						historyList.appendCommand(command);
					}
					if (command.endsWith(":")){
						inLocalScale = true;
						return;
					}
					if (command.endsWith("\\")){
						inLineScale = true;
						return;
					}
					if (inLineScale) {
						if (!inLocalScale) {
							evaluateCommand(command.trim());
							// Reset readLineIndex
							readLineIndex = -1;
						} else {
							inLineScale = false;
						}
						return;
					}
					if (inLocalScale) {
						if (command.endsWith("\n")){
							evaluateCommand(command);
							// Reset readLineIndex
							readLineIndex = -1;
						} else {
							return;
						}
					} else {
						evaluateCommand(command.trim());
						// Reset readLineIndex
						readLineIndex = -1;
					}
				} else if (event.keyCode == SWT.ARROW_UP) {
					if(historyList.isEmpty())
						return;
					// Initialise
					if(readLineIndex == -1) {
						readLineIndex = historyList.size() - 1;
					} else if(readLineIndex != 0) {
						// If it does not reach top command
						readLineIndex = readLineIndex - 1;
					}
					updateTextInput(historyList.get(readLineIndex));
				} else if (event.keyCode == SWT.ARROW_DOWN) {
					if(historyList.isEmpty() || readLineIndex == -1) {
						return;
					}
					// If it does not reach last command
					if(readLineIndex !=historyList.size() - 1) {
						readLineIndex = readLineIndex + 1;
					}
					updateTextInput(historyList.get(readLineIndex));
				} else if (event.stateMask == SWT.CTRL && event.keyCode == 0x20) {
					// Enable content assistance trigger by ctrl + space
					 if (inputTextViewer.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS) &&
							 isContentAssistEnabled()) {
						 inputTextViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
						 event.doit = false;
					 }
				 }
			}
		});
	}
	
	public void setContentAssistEnabled(boolean enabled) {
		contentAssistEnabled = enabled;
		if (assistant != null) {
			assistant.enableAutoActivation(enabled);
		}
	}
	
	public boolean isContentAssistEnabled() {
		return contentAssistEnabled;
	}
	
	public boolean isScrollLocked() {
		return scrollLocked;
	}
	
	public void setScrollLocked(boolean locked) {
		scrollLocked = locked;
		// Update menu item state
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				if (scrollLockItem != null && !scrollLockItem.isDisposed()) {
					scrollLockItem.setSelection(isScrollLocked());
				}
			}			
		});
		// Notify viewer changes
		PlatformUtils.getPlatformEventBus().postEvent(new CommandlLineViewerEvent(this));
	}
	
	public void clearConsole() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (consoleTextViewer != null && !consoleTextViewer.getTextWidget().isDisposed()) {
					consoleTextViewer.getTextWidget().setText("");
					// [Tony] [2009-03-25] Fix scrolled bug after console is cleared
					previousDocLength = 0;
				}
			}
		});
	}
	
	private void updateTextInput(String text) {
		inputTextViewer.getTextWidget().setText(text);
		inputTextViewer.getTextWidget().setSelection(text.length());
	}
	
	private void createHistoryArea(TabFolder tabFolder) {
		TabItem item = new TabItem (tabFolder, SWT.NULL);
		item.setText("History");
		
		historyTableViewer = new TableViewer(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		item.setControl(historyTableViewer.getControl());
		historyTableViewer.setContentProvider((IStructuredContentProvider) historyList);
		historyTableViewer.setLabelProvider(new HistoryLableProvider());
		TableColumn timeColumn = new TableColumn(historyTableViewer.getTable(), SWT.LEFT);
		timeColumn.setText("Time");
		timeColumn.setWidth(120);
		TableColumn commandColumn = new TableColumn(historyTableViewer.getTable(), SWT.LEFT);
		commandColumn.setText("Command");
		commandColumn.setWidth(200);
		historyTableViewer.getTable().setHeaderVisible(true);
		historyTableViewer.getTable().setLinesVisible(true);
		historyTableViewer.setInput(historyList);
		historyTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((StructuredSelection) historyTableViewer.getSelection()).getFirstElement();
				if (selection instanceof CommandHistory) {
					String command = ((CommandHistory) selection).getCommand();
					if (inputTextViewer != null && !inputTextViewer.getTextWidget().isDisposed()) {
						inputTextViewer.getTextWidget().setText(command);
						inputTextViewer.setSelectedRange(0, command.length());
						inputTextViewer.getTextWidget().setFocus();
					}
				}
			}			
		});
	}
	
	private void createToolArea(TabFolder tabFolder, ICommandLineTool tool, String label) {
		TabItem item = new TabItem (tabFolder, SWT.NULL);
		item.setText(label);
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new FillLayout());
		tool.createPartControl(composite);
		item.setControl(composite);
	}

	public void setFocus() {
		if (inputTextViewer != null) {
			inputTextViewer.getTextWidget().forceFocus();
		} else if (consoleTextViewer != null){
			consoleTextViewer.getTextWidget().forceFocus();
		}
	}
	
	public void dispose() {
		if (executor!= null && listener != null) {
			((IObservableComponent) executor.getEngine()).removeListener(listener);
		}
		if (executorEventHandler != null) {
			PlatformUtils.getPlatformEventBus().unsubscribe(executor, executorEventHandler);
			executorEventHandler = null;
		}
		if (executor != null) {
			executor.shutDown();
		}
		fontNormal = null;
		fontBold = null;
		fontItalic = null;
		resourceManager = null;
		tabFolder = null;
		consoleTextViewer = null;
		inputTextViewer = null;
		historyTableViewer = null;
		historyList = null;
		commandTableViewer = null;
	}

	private void println(String text) {
		print(text + "\n");
	}
	
	private void print(final String text) {
		print(text, null, SWT.NORMAL);
	}
	
	private void println(final String text, final Color color, final int style) {
		print(text + "\n", color, style);
	}
	
	private void print(final String text, final Color color, final int style) {
//		SafeUIRunner.asyncExec(new ISafeRunnable() {
//			@Override
//			public void handleException(Throwable exception) {
//			}
//			@Override
//			public void run() throws Exception {
				String textToAppend = text;
				if (consoleTextViewer != null && !consoleTextViewer.getTextWidget().isDisposed()) {
					// Text wrap
//					if (textToAppend.length() > LINE_WRAP) {
//						StringBuilder builder = new StringBuilder();
//						while(textToAppend.length() > LINE_WRAP) {
//							builder.append(textToAppend.substring(0, LINE_WRAP));
//							builder.append('\n');
//							textToAppend = textToAppend.substring(LINE_WRAP);
//						}
//						builder.append(textToAppend);
//						textToAppend = builder.toString();
//					}
					
					// Set colour and style
					StyledText styledText = consoleTextViewer.getTextWidget();
					StyleRange styleRange = new StyleRange();
					styleRange.start = styledText.getCharCount();
					styleRange.length = textToAppend.length();
					if (style == SWT.BOLD || style == SWT.ITALIC) {
						styleRange.font = getFont(style);
					} else {
						styleRange.font = getFont(SWT.NORMAL);
					}
					if (color != null) {
						styleRange.foreground = color;
					} else {
						styleRange.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
					}
					
					// Append text
					consoleTextViewer.getTextWidget().append(textToAppend);
					styledText.setStyleRange(styleRange);
					autoScroll();
				}
//			}
//		});
	}
	
	private Font getFont(int style) {
		Font font = null;
		if (style == SWT.BOLD) {
			font = fontBold;
		} else if (style == SWT.ITALIC) {
			font = fontItalic;
		} else {
			font = fontNormal;
		}
		if (font == null && consoleTextViewer != null) {
			if (style == SWT.BOLD) {
				font = fontBold = resourceManager.createFont("Courier New", 12, style); 
			} else if (style == SWT.ITALIC) {
				font = fontItalic = resourceManager.createFont("Courier New", 12, style);
			} else {
				font = fontNormal = resourceManager.createFont("Courier New", 12, style);
			}
		}
		return font;
	}
	
	private void autoScroll() {
		// Don't scroll text if scroll is locked
		if (isScrollLocked()) {
			return;
		}
		// Delay to 200ms to avoid scrolling too often
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (consoleTextViewer != null && !consoleTextViewer.getTextWidget().isDisposed()) {
					StyledText styledText = consoleTextViewer.getTextWidget();
					StyledTextContent doc = styledText.getContent();
					int lineCount = doc.getLineCount();
					if (lineCount > defaultLineLimit){
						int startPoint = doc.getOffsetAtLine(lineCount - defaultLineLimit);
						doc.replaceTextRange(0, startPoint, "");
					}
					int docLength = doc.getCharCount();
					if (docLength > previousDocLength || lineCount > defaultLineLimit) {
						styledText.setCaretOffset(docLength);
						styledText.showSelection();
						previousDocLength = docLength;
					}
					styledText.redraw();
				}
			}
		}, 200);
	}

	protected void evaluateCommand(final String command) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				// Print command to console and clear input box
				inLineScale = false;
				inLocalScale = false;
				if (consoleTextViewer != null && !consoleTextViewer.getTextWidget().isDisposed()) {
					if (!(getScriptExecutor().getEngine() instanceof IObservableComponent)) {
						print("\n\n>> ", Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED), SWT.NORMAL);
						println(command, null, SWT.BOLD);
					}
					inputTextViewer.getTextWidget().setText("");
				}
				// Execute command
				if (getScriptExecutor() != null) {
//					try {
						if (command != null || command.length() != 0) {
							getScriptExecutor().runScript(command);
						}
						// Send empty string regardless the command is meaningful or not
						// in case the UI doesn't update itself correctly
						// Problem with Jepp
						// We always need to end the current command to test if there is any syntaxic problem
						getScriptExecutor().runScript("", true);
//					} catch (ScriptException e) {
//						print(e.getMessage(), Display.getDefault().getSystemColor(SWT.COLOR_RED), SWT.NORMAL);
////						logger.error("Error occured while evaluating command " + command, e);
//					}
				}
			}
		}, logger);
	}
	
	public void appendScript(final Reader reader) {
		if (getScriptExecutor() == null) {
			return;
		}
		getScriptExecutor().runScript(reader);
//		Job evalJob = new Job("Eval Job") {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
//		SafeUIRunner.asyncExec(new SafeRunnable() {
//
//			@Override
//			public void run() throws Exception {
//				getScriptEngine().eval(reader);
//			}
//		});
//				try {
//					getScriptEngine().eval(reader);
//				} catch (ScriptException e) {
//					logger.error("Failed to evaluate from reader.", e);
//				}
//				return Status.OK_STATUS;
//			}
//		};
//		evalJob.setThread(Thread.currentThread());
//		evalJob.setSystem(true);
//		evalJob.schedule(500);
	}
	
	public void exportConsoleText(Writer writer) throws IOException {
		try {
			String text = getConsoleText();
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				text = text.replace("\n", "\r\n");
			}
			writer.write(text);
		} catch (IOException e) {
			String errorMessage = "Failed to write console output to "
					+ writer.toString();
			logger.error(errorMessage, e);
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							IStatus.OK, errorMessage, e),
					StatusManager.SHOW);
		}
	}
	
	public static void main(String[] args) throws Exception {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(500, 500);
		shell.setLayout(new FillLayout());
		Composite clientArea = new Composite(shell, SWT.NONE);
		clientArea.setLayout(new FillLayout());
		CommandLineViewer viewer = new CommandLineViewer();
		viewer.createPartControl(clientArea);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
}
