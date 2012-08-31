package org.gumtree.ui.scripting.support;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.scripting.EvalChangeEvent;
import org.gumtree.scripting.IObservableComponent;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.IScriptingListener;
import org.gumtree.scripting.ObservableScriptContext;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.scripting.ScriptingChangeEvent;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.scripting.ICommandLineContentAssistProcessor;
import org.gumtree.ui.scripting.ICommandLineToolRegistry;
import org.gumtree.ui.scripting.IScriptConsole;
import org.gumtree.ui.scripting.tools.ICommandLineTool;
import org.gumtree.ui.scripting.viewer.HistoryContentProvider;
import org.gumtree.ui.scripting.viewer.HistoryContentProvider.CommandHistory;
import org.gumtree.ui.scripting.viewer.HistoryLableProvider;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.resource.UIResourceManager;
import org.gumtree.ui.widgets.ExtendedComposite;
import org.gumtree.util.messaging.EventBuilder;
import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class ScriptConsole extends ExtendedComposite implements IScriptConsole {

	private static final String PROP_PREFIX_TOOLS = "gumtree.scripting.tools.";

	private static final String ID_TOOL_HISTORY = "history";

	private static final Logger logger = LoggerFactory
			.getLogger(ScriptConsole.class);

	private IScriptExecutor scriptExecutor;

	private ICommandLineToolRegistry toolRegistry;

	private UIContext context;

	private String id;

	private int originalStyle;

	private boolean contentAssistEnabled;

	private boolean scrollLocked;

	public ScriptConsole(Composite parent, int style) {
		super(parent, SWT.NONE);
		id = UUID.randomUUID().toString();
		originalStyle = style;
	}

	/*************************************************************************
	 * Main user interface
	 *************************************************************************/

	@PostConstruct
	public void render() {
		setLayout(new FillLayout());
		context = new UIContext();
		context.resourceManager = new UIResourceManager(Activator.PLUGIN_ID,
				this);
		context.historyList = new HistoryContentProvider();

		if ((getOriginalStyle() & NO_UTIL_AREA) == 0) {
			SashForm sashForm = getWidgetFactory().createSashForm(this);
			createMainArea(sashForm);
			Composite utilArea = getWidgetFactory().createComposite(sashForm);
			utilArea.setLayout(new FillLayout());
			context.toolTabFolder = new TabFolder(utilArea, SWT.NONE);
			sashForm.setWeights(new int[] { 5, 3 });
		} else {
			createMainArea(this);
		}

		bindScriptExecutor(getScriptExecutor());
	}

	private void createMainArea(Composite parent) {
		Composite consoleArea = getWidgetFactory().createComposite(parent);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(consoleArea);
		if ((getOriginalStyle() & NO_INPUT_TEXT) == 0) {
			createTerminalArea(consoleArea);
			createProgressArea(consoleArea);
			createInputArea(consoleArea);
		} else {
			createTerminalArea(consoleArea);
			createProgressArea(consoleArea);
		}
	}

	private void createTerminalArea(Composite parent) {
		// Text viewer
		context.consoleTextViewer = new TextViewer(parent, SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.READ_ONLY | SWT.WRAP | SWT.BORDER);
		StyledText textWidget = context.consoleTextViewer.getTextWidget();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(2, 1)
				.grab(true, true).applyTo(textWidget);
		context.consoleTextViewer.setDocument(new Document());
		// Add drop support
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DropTarget dropTarget = new DropTarget(textWidget, operations);
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
		createContextMenu(textWidget);
	}

	private void createProgressArea(Composite parent) {
		context.progressBar = new ProgressBar(parent, SWT.INDETERMINATE);
		context.progressBar.setVisible(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false).hint(SWT.DEFAULT, 10)
				.applyTo(context.progressBar);
	}

	private void createInputArea(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		Font font = context.resourceManager.createRelativeFont(label.getFont(),
				4, SWT.BOLD);
		label.setFont(font);
		label.setText("Command >");
		// Require SWT.MULTI to get tab working on LinkModelUI
		context.inputTextViewer = new SourceViewer(parent, null, SWT.MULTI
				| SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false)
				.applyTo(context.inputTextViewer.getTextWidget());
		font = context.resourceManager.createRelativeFont(
				context.inputTextViewer.getTextWidget().getFont(), 4, SWT.NONE);
		context.inputTextViewer.getTextWidget().setFont(font);
		context.inputTextViewer.setDocument(new Document());
		context.inputTextViewer
				.appendVerifyKeyListener(new VerifyKeyListener() {
					public void verifyKey(VerifyEvent event) {
						if (event.keyCode == SWT.KEYPAD_CR
								|| event.keyCode == SWT.CR) {
							String command = context.inputTextViewer
									.getTextWidget().getText().trim();
							if (context.historyList != null) {
								context.historyList.appendCommand(command);
							}
							evaluateCommand(command);
							// Reset readLineIndex
							context.readLineIndex = -1;
						} else if (event.keyCode == SWT.ARROW_UP) {
							if (context.historyList.isEmpty())
								return;
							// Initialise
							if (context.readLineIndex == -1) {
								context.readLineIndex = context.historyList
										.size() - 1;
							} else if (context.readLineIndex != 0) {
								// If it does not reach top command
								context.readLineIndex = context.readLineIndex - 1;
							}
							updateTextInput(context.historyList
									.get(context.readLineIndex));
						} else if (event.keyCode == SWT.ARROW_DOWN) {
							if (context.historyList.isEmpty()
									|| context.readLineIndex == -1) {
								return;
							}
							// If it does not reach last command
							if (context.readLineIndex != context.historyList
									.size() - 1) {
								context.readLineIndex = context.readLineIndex + 1;
							}
							updateTextInput(context.historyList
									.get(context.readLineIndex));
						} else if (event.stateMask == SWT.CTRL
								&& event.keyCode == 0x20) {
							// Enable content assistance trigger by ctrl + space
							if (context.inputTextViewer
									.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)
									&& isContentAssistEnabled()) {
								context.inputTextViewer
										.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
								event.doit = false;
							}
						}
					}
				});
	}

	private void createHistoryArea(TabFolder tabFolder) {
		TabItem item = new TabItem(tabFolder, SWT.NULL);
		item.setText("History");

		context.historyTableViewer = new TableViewer(tabFolder, SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		item.setControl(context.historyTableViewer.getControl());
		context.historyTableViewer
				.setContentProvider((IStructuredContentProvider) context.historyList);
		context.historyTableViewer.setLabelProvider(new HistoryLableProvider());
		TableColumn timeColumn = new TableColumn(
				context.historyTableViewer.getTable(), SWT.LEFT);
		timeColumn.setText("Time");
		timeColumn.setWidth(120);
		TableColumn commandColumn = new TableColumn(
				context.historyTableViewer.getTable(), SWT.LEFT);
		commandColumn.setText("Command");
		commandColumn.setWidth(200);
		context.historyTableViewer.getTable().setHeaderVisible(true);
		context.historyTableViewer.getTable().setLinesVisible(true);
		context.historyTableViewer.setInput(context.historyList);
		context.historyTableViewer
				.addDoubleClickListener(new IDoubleClickListener() {
					public void doubleClick(DoubleClickEvent event) {
						Object selection = ((StructuredSelection) context.historyTableViewer
								.getSelection()).getFirstElement();
						if (selection instanceof CommandHistory) {
							String command = ((CommandHistory) selection)
									.getCommand();
							if (context != null
									&& context.inputTextViewer != null) {
								context.inputTextViewer.getTextWidget()
										.setText(command);
								context.inputTextViewer.setSelectedRange(0,
										command.length());
								context.inputTextViewer.getTextWidget()
										.setFocus();
							}
						}
					}
				});
	}

	private void createToolArea(TabFolder tabFolder, ICommandLineTool tool,
			String label) {
		TabItem item = new TabItem(tabFolder, SWT.NULL);
		item.setText(label);
		Composite composite = getWidgetFactory().createComposite(tabFolder);
		composite.setLayout(new FillLayout());
		tool.createPartControl(composite);
		item.setControl(composite);
	}

	@Override
	public boolean setFocus() {
		boolean result = super.setFocus();
		if (context != null) {
			if (context.inputTextViewer != null) {
				context.inputTextViewer.getTextWidget().forceFocus();
			} else if (context.consoleTextViewer != null) {
				context.consoleTextViewer.getTextWidget().forceFocus();
			}
		}
		return result;
	}

	@Override
	protected void disposeWidget() {
		if (context != null) {
			if (context.executorEventHandler != null) {
				context.executorEventHandler.deactivate();
			}
			if (context.scriptingListener != null) {
				if (scriptExecutor != null) {
					if (scriptExecutor.getEngine() instanceof IObservableComponent) {
						((IObservableComponent) scriptExecutor.getEngine())
								.removeListener(context.scriptingListener);
					}
				}
			}

			context = null;
		}
		scriptExecutor = null;
		toolRegistry = null;
	}

	/*************************************************************************
	 * Menu and actions
	 *************************************************************************/

	private void createContextMenu(Control parent) {
		Menu menu = new Menu(parent);
		parent.setMenu(menu);

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
		context.scrollLockItem = new MenuItem(menu, SWT.CHECK);
		context.scrollLockItem.setText("Scroll Lock");
		context.scrollLockItem.setSelection(isScrollLocked());
		context.scrollLockItem.setImage(InternalImage.SCROLL_LOCK.getImage());
		context.scrollLockItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setScrollLocked(context.scrollLockItem.getSelection());
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
		final Action exportOutputAction = new ExportConsoleAction(this,
				menu.getShell());
		MenuItem exportOutputItem = new MenuItem(menu, SWT.PUSH);
		exportOutputItem.setText("Export Console Output");
		exportOutputItem.setImage(exportOutputAction.getImageDescriptor()
				.createImage());
		exportOutputItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				exportOutputAction.run();
			}
		});
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	@Override
	public IScriptExecutor getScriptExecutor() {
		if (scriptExecutor == null) {
			scriptExecutor = new ScriptExecutor();
		}
		return scriptExecutor;
	}

	@Override
	@Inject
	@Optional
	public void setScriptExecutor(IScriptExecutor scriptExecutor) {
		if (this.scriptExecutor != null) {
			unbindScriptExecutor(this.scriptExecutor);
		}
		this.scriptExecutor = scriptExecutor;
		bindScriptExecutor(scriptExecutor);
	}

	@Override
	public ICommandLineToolRegistry getToolRegistry() {
		return toolRegistry;
	}

	@Override
	@Inject
	@Optional
	public void setToolRegistry(ICommandLineToolRegistry toolRegistry) {
		this.toolRegistry = toolRegistry;
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/
	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getOriginalStyle() {
		return originalStyle;
	}

	@Override
	public boolean isContentAssistEnabled() {
		return contentAssistEnabled;
	}

	@Override
	public void setContentAssistEnabled(boolean enabled) {
		contentAssistEnabled = enabled;
		if (context != null && context.assistant != null) {
			context.assistant.enableAutoActivation(enabled);
		}
	}

	@Override
	public boolean isScrollLocked() {
		return scrollLocked;
	}

	@Override
	public void setScrollLocked(boolean locked) {
		scrollLocked = locked;
		// Update menu item state
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				if (context != null && context.scrollLockItem != null) {
					context.scrollLockItem.setSelection(isScrollLocked());
				}
			}
		});
		// Update event broker
		new EventBuilder(EVENT_TOPIC_SCRIPT_CONSOLE_SCROLL_LOCK)
				.append(EVENT_PROP_CONSOLE_ID, getId())
				.append(EVENT_PROP_LOCKED, locked).post();
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

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
		String textToAppend = text;
		if (context != null && context.consoleTextViewer != null) {
			// Set colour and style
			StyledText styledText = context.consoleTextViewer.getTextWidget();
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
				styleRange.foreground = Display.getDefault().getSystemColor(
						SWT.COLOR_BLACK);
			}

			// Append text
			context.consoleTextViewer.getTextWidget().append(textToAppend);
			styledText.setStyleRange(styleRange);
			autoScroll();
		}
	}

	@Override
	public void appendScript(final Reader reader) {
		if (getScriptExecutor() == null) {
			return;
		}
		getScriptExecutor().runScript(reader);
	}

	private void autoScroll() {
		// Don't scroll text if scroll is locked
		if (isScrollLocked()) {
			return;
		}
		// Delay to 200ms to avoid scrolling too often
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (context != null && context.consoleTextViewer != null) {
					StyledText styledText = context.consoleTextViewer
							.getTextWidget();
					StyledTextContent doc = styledText.getContent();
					int docLength = doc.getCharCount();
					if (docLength > context.previousDocLength) {
						styledText.setCaretOffset(docLength);
						styledText.showSelection();
						context.previousDocLength = docLength;
					}
					styledText.redraw();
				}
			}
		}, 200);
	}

	@Override
	public String getConsoleText() {
		if (context != null && context.consoleTextViewer != null) {
			return context.consoleTextViewer.getTextWidget().getText();
		}
		return "";
	}

	@Override
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
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK,
							errorMessage, e), StatusManager.SHOW);
		}
	}

	@Override
	public void clearConsole() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (context != null && context.consoleTextViewer != null) {
					context.consoleTextViewer.getTextWidget().setText("");
					// [Tony] [2009-03-25] Fix scrolled bug after console is
					// cleared
					context.previousDocLength = 0;
				}
			}
		});
	}

	protected void evaluateCommand(final String command) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@SuppressWarnings("null")
			public void run() throws Exception {
				// Print command to console and clear input box
				if (context != null && context.consoleTextViewer != null) {
					if (!(getScriptExecutor().getEngine() instanceof IObservableComponent)) {
						print("\n\n>> ",
								Display.getDefault().getSystemColor(
										SWT.COLOR_DARK_RED), SWT.NORMAL);
						println(command, null, SWT.BOLD);
					}
					context.inputTextViewer.getTextWidget().setText("");
				}
				// Execute command
				if (getScriptExecutor() != null) {
					if (command != null || command.length() != 0) {
						getScriptExecutor().runScript(command);
					}
					// Send empty string regardless the command is meaningful or
					// not
					// in case the UI doesn't update itself correctly
					// Problem with Jepp
					// We always need to end the current command to test if
					// there is any syntaxic problem
					getScriptExecutor().runScript("", true);
				}
			}
		}, logger);
	}

	private void updateTextInput(String text) {
		if (context != null && context.inputTextViewer != null) {
			context.inputTextViewer.getTextWidget().setText(text);
			context.inputTextViewer.getTextWidget().setSelection(text.length());
		}
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

	@Override
	public String[] getCommandHistory() {
		if (context != null && context.historyList != null) {
			return context.historyList.getCommands();
		}
		return new String[0];
	}

	private void bindScriptExecutor(final IScriptExecutor executor) {
		if (context == null || executor == null
				|| context.consoleTextViewer == null) {
			return;
		}

		// Wait for engine to be ready
		while (!executor.isInitialised()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}

		// Bind engine output
		final ScriptEngine engine = executor.getEngine();
		if (engine instanceof IObservableComponent) {
			context.scriptingListener = new IScriptingListener() {
				public void handleChange(final ScriptingChangeEvent event) {
					if (event instanceof EvalChangeEvent) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (context.consoleTextViewer != null
										&& !context.consoleTextViewer
												.getTextWidget().isDisposed()) {
									print("\n\n>> ",
											Display.getDefault()
													.getSystemColor(
															SWT.COLOR_DARK_RED),
											SWT.NORMAL);
									println(((EvalChangeEvent) event)
											.getScript(), null, SWT.BOLD);
								}
							}
						});
					}
				}
			};
			((IObservableComponent) engine)
					.addListener(context.scriptingListener);
		}

		// Print intro
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				println("Engine: " + engine.getFactory().getEngineName());
				println("Engine Version: "
						+ engine.getFactory().getEngineVersion());
				println("Language: " + engine.getFactory().getLanguageName());
				println("Language Version: "
						+ engine.getFactory().getLanguageVersion());
				println("");
			}
		});

		// Set default script context to engine if necessary
		ScriptContext scriptContext = engine.getContext();
		if (scriptContext == null) {
			// Same engine (like Jepp) does not provide default context out of
			// the box
			ScriptContext context = new ObservableScriptContext();
			engine.setContext(context);
			scriptContext = engine.getContext();
		}

		// Set writers
		PrintWriter writer = new PrintWriter(new ByteArrayOutputStream() {
			public synchronized void flush() throws IOException {
				final String text = toString();
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						print(text,
								Display.getDefault().getSystemColor(
										SWT.COLOR_BLUE), SWT.NORMAL);
					}
				});
				reset();
			}
		}, true) {
			public void write(String s) {
				super.write(s);
				// Hack to get Jepp to display text
				flush();
			}
		};
		scriptContext.setWriter(writer);
		PrintWriter errorWriter = new PrintWriter(new ByteArrayOutputStream() {
			public synchronized void flush() throws IOException {
				final String text = toString();
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						print(text,
								Display.getDefault().getSystemColor(
										SWT.COLOR_RED), SWT.NORMAL);
					}
				});
				reset();
			}
		}, true) {
			public void write(String s) {
				super.write(s);
				// Hack to get Jepp to display text
				flush();
			}
		};
		scriptContext.setErrorWriter(errorWriter);
		// Update context
		final ScriptContext updatedContext = scriptContext;
		executor.runTask(new Runnable() {
			public void run() {
				engine.setContext(updatedContext);
			}
		});

		// Register executor listener
		context.executorEventHandler = new EventHandler(
				IScriptExecutor.EVENT_TOPIC_SCRIPT_EXECUTOR + "/*",
				IScriptExecutor.EVENT_PROP_EXECUTOR_ID, executor.getId()) {

			@Override
			public void handleEvent(Event event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						// Disable input text when busy
						if (context != null && context.inputTextViewer != null) {
							if (executor.isBusy()) {
								// System.out.println("Disable console input");
								context.inputTextViewer.setEditable(false);
								Color gray = Display.getDefault()
										.getSystemColor(SWT.COLOR_GRAY);
								context.inputTextViewer.getTextWidget()
										.setBackground(gray);
								context.progressBar.setVisible(true);
							} else {
								// System.out.println("Enable console input");
								context.inputTextViewer.setEditable(true);
								Color gray = Display.getDefault()
										.getSystemColor(SWT.COLOR_WHITE);
								context.inputTextViewer.getTextWidget()
										.setBackground(gray);
								context.progressBar.setVisible(false);
							}
						}
					}
				});
			}
		}.activate();

		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (context != null && context.toolTabFolder != null
						&& getToolRegistry() != null) {
					// Setup Tools
					String[] toolIds = getToolProperty(engine).split(",");
					boolean historyToolCreated = false;
					for (String toolId : toolIds) {
						toolId = toolId.trim();
						if (toolId.equalsIgnoreCase(ID_TOOL_HISTORY)
								&& !historyToolCreated) {
							// Special case: create default history tool
							createHistoryArea(context.toolTabFolder);
							// Can't create history tool twice
							historyToolCreated = true;
						} else {
							ICommandLineTool tool = null;
							try {
								tool = getToolRegistry().createCommandLineTool(
										toolId);
							} catch (CoreException e) {
								logger.error(
										"Failed to instantiate tool for id "
												+ toolId + ".", e);
							}
							String label = getToolRegistry()
									.getCommandLineToolLabel(toolId);
							if (tool != null) {
								tool.setScriptExecutor(executor);
								createToolArea(context.toolTabFolder, tool,
										label);
							} else {
								logger.info("Tool " + toolId + " is missing.");
							}
						}
					}
				}

				// Setup content assistant
				if (context.inputTextViewer != null) {
					final ICommandLineContentAssistProcessor processor = (ICommandLineContentAssistProcessor) Platform
							.getAdapterManager().loadAdapter(
									engine,
									ICommandLineContentAssistProcessor.class
											.getName());
					if (processor != null) {
						processor.setScriptExecutor(executor);
						SourceViewerConfiguration configuration = new SourceViewerConfiguration() {
							public IContentAssistant getContentAssistant(
									ISourceViewer sourceViewer) {
								context.assistant = new ContentAssistant();
								context.assistant.setContentAssistProcessor(
										processor,
										IDocument.DEFAULT_CONTENT_TYPE);
								// Use this to activate context information
								context.assistant
										.enableAutoActivation(isContentAssistEnabled());
								context.assistant
										.setInformationControlCreator(getInformationControlCreator(sourceViewer));
								context.assistant
										.setProposalSelectorBackground(Display
												.getDefault().getSystemColor(
														SWT.COLOR_WHITE));
								return context.assistant;
							}
						};
						// Install content assist
						context.inputTextViewer.configure(configuration);
					}
				}

				// Test drive the engine
				executor.runScript("", true);
			}
		});
	}

	private void unbindScriptExecutor(IScriptExecutor executor) {
		if (context == null || executor == null) {
			return;
		}
		// TODO
		// Remove scriptingListener from engine
		// reset event handler
	}

	private Font getFont(int style) {
		// Disposed
		if (context == null) {
			return null;
		}
		Font font = null;
		if (style == SWT.BOLD) {
			font = context.fontBold;
		} else if (style == SWT.ITALIC) {
			font = context.fontItalic;
		} else {
			font = context.fontNormal;
		}
		if (font == null && context.resourceManager != null) {
			if (style == SWT.BOLD) {
				font = context.fontBold = context.resourceManager.createFont(
						"Courier New", 12, style);
			} else if (style == SWT.ITALIC) {
				font = context.fontItalic = context.resourceManager.createFont(
						"Courier New", 12, style);
			} else {
				font = context.fontNormal = context.resourceManager.createFont(
						"Courier New", 12, style);
			}
		}
		return font;
	}

	protected class UIContext {
		UIResourceManager resourceManager;
		ITextViewer consoleTextViewer;
		SourceViewer inputTextViewer;
		ProgressBar progressBar;
		TabFolder toolTabFolder;
		MenuItem scrollLockItem;
		HistoryContentProvider historyList;
		TableViewer historyTableViewer;
		ContentAssistant assistant;
		int previousDocLength;
		int readLineIndex = -1;
		Font fontNormal;
		Font fontBold;
		Font fontItalic;
		IScriptingListener scriptingListener;
		EventHandler executorEventHandler;
	}

}
