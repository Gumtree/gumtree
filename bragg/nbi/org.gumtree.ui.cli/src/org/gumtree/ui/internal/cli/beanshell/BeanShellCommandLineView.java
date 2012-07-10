/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *     Norman Xiong (Bragg Institute) - update and implementation
 *******************************************************************************/

package org.gumtree.ui.internal.cli.beanshell;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.ui.internal.cli.Activator;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellCommandLineView extends ViewPart {

	private static int lineLimit = 1000;
	private StyledText terminal;
	private Interpreter bsh;
	private int pointer;
	private Color red;
	private Color blue;
	private Color darkRed;
	private Color black;
	private Color green;
	private InterpreterRegistry interpreterRegistry = null;
	private InstrumentInterpreter interpreter = null;
	private static BeanShellCommandLineView beanShellCommandLineView;
	private KeyAdapter keyAdapter;
	private boolean isReturn;
	private String dialogArgument;
	private List<ActionListener> skipActionListenerList;
	private List<ActionListener> stopActionListenerList;
	private Action skipAction;
	private Action stopAction;
	public enum ColorEnum{red, blue, darkRed, black, green};
	private LinkedList<String> historyCommandList;
	private ListIterator<String> historyIterator;

	public BeanShellCommandLineView() {
		super();
		bsh = new Interpreter();
//		bsh.setClassLoader(this.getClass().getClassLoader());
		pointer = 0;
		Display display = PlatformUI.getWorkbench().getDisplay();
		blue = new Color(display, 0, 0, 255);
		darkRed = new Color(display, 128, 0, 0);
		red = new Color(display, 255, 0, 0);
		black = new Color(display, 0, 0, 0);
		green = new Color(display, 0, 255, 120);
		beanShellCommandLineView = this;
		historyCommandList = new LinkedList<String>();
	}

	public static BeanShellCommandLineView getInstance(){
		if (beanShellCommandLineView == null)
			try {
				showCommandLineView();
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return beanShellCommandLineView;
	}
	
	public static BeanShellCommandLineView getInstance(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
//		if (beanShellCommandLineView == null)
			try {
				showCommandLineView(window);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return beanShellCommandLineView;
	}
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		terminal = new StyledText(parent, SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.WRAP);
		terminal.setText("");
//		terminal.setTextLimit(1000);
		terminal.setFont(new Font(terminal.getDisplay(), new FontData("Courier New", 9, SWT.NORMAL)));
//		terminal.setWordWrap(true);
		createInstrumentInterpreter();
		appendText(">>>", darkRed);
		GridData terminalData = new GridData(SWT.FILL, SWT.FILL, true, true);
		terminal.setLayoutData(terminalData);
		bsh.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				appendText(new String(new byte[] {(byte)b}), blue);
			}
		}));
		bsh.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				appendText(new String(new byte[] {(byte)b}), blue);
			}
		}));
		terminal.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(terminal.getCharCount() < pointer)
					appendText(">", darkRed);
			}
		});
		keyAdapter = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(terminal.getCaretOffset() >= pointer)
					terminal.setEditable(true);
				else
					terminal.setEditable(false);
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
//					System.out.println(terminal.getCharCount());
					final String cmd = terminal.getText(pointer, terminal.getCharCount() - 1).trim();
					historyIterator = historyCommandList.listIterator(historyCommandList.size());
//					historyCommandList.add(cmd);
					if (cmd.length() == 0){ 
						appendText(">>>", darkRed);
						return;
					}
					Thread evaluateThread = new Thread(){
						public void run(){
								try {
									String ncmd = cmd;
									if (ncmd.contains("\r\n")){
										ncmd = cmd.replaceAll("\r\n", "");
									}
									ncmd = ncmd.replaceAll("\r", "");
									ncmd = ncmd.replaceAll("\n", "");
									evaluate(ncmd);
									addCommandToHistory(ncmd);
									historyIterator = historyCommandList.listIterator(historyCommandList.size());
								} catch (EvaluationFailedException e1) {
									appendText(e1.getMessage() + "\n\n", red);
									e1.printStackTrace();
								}
									pushOut();
//								appendTextSync(">>>", darkRed);
							}
					};
					evaluateThread.start();
//					evaluate(cmd);
//					System.out.println("Evaluating this: " + cmd);
//					bsh.eval(cmd);
				}
				if (e.keyCode == SWT.ARROW_UP){
//					if (historyIterator == null || !historyIterator.hasPrevious()){
//					terminal.setCaretOffset(terminal.getCharCount());
//					return;
//					}
					String lastCommand = "";
					if (historyIterator != null && historyIterator.hasPrevious()){
						lastCommand = historyIterator.previous();
					}
//					terminal.replaceTextRange(pointer, terminal.getCharCount() - 3 - pointer, "");
//					terminal.setCaretOffset(pointer);
//					terminal.setCaretOffset(terminal.getCharCount());
//					System.out.println(terminal.getCharCount());
					terminal.replaceTextRange(pointer, terminal.getCharCount() - pointer, "");
					terminal.append(lastCommand + "\n");
					terminal.setCaretOffset(terminal.getCharCount() - 1);
//					System.out.println(terminal.getCharCount());
//					System.out.println(pointer);
//					terminal.setCaretOffset(offset);
				}
				if (e.keyCode == SWT.ARROW_DOWN){
//					if (historyIterator == null || !historyIterator.hasPrevious()){
//					terminal.setCaretOffset(terminal.getCharCount());
//					return;
//					}
					String lastCommand = "";
					if (historyIterator != null && historyIterator.hasNext()){
						lastCommand = historyIterator.next();
					}
//					terminal.replaceTextRange(pointer, terminal.getCharCount() - 3 - pointer, "");
//					terminal.setCaretOffset(pointer);
//					terminal.setCaretOffset(terminal.getCharCount());
//					System.out.println(terminal.getCharCount());
					terminal.replaceTextRange(pointer, terminal.getCharCount() - pointer, "");
					terminal.append(lastCommand);
					terminal.setCaretOffset(terminal.getCharCount());
//					System.out.println(terminal.getCharCount());
//					System.out.println(pointer);
//					terminal.setCaretOffset(offset);
				}
			}

			private void addCommandToHistory(String cmd) {
				// TODO Auto-generated method stub
				String lastCommand = "";
				if (historyCommandList != null && historyCommandList.size() > 0)
					lastCommand = historyCommandList.getLast();
				if (!lastCommand.equals(cmd)) 
					historyCommandList.add(cmd);
			}

//			private void evaluate(String cmd) throws EvalError {
//			// TODO Auto-generated method stub
//			Thread currentThread = Thread.currentThread();
////			appendText(currentThread.getName() + " -- evaluation\n");
//			List<String> javaCmds = interpreter.interpret(cmd);
//			for (Iterator<?> iterator = javaCmds.iterator(); iterator.hasNext();) {
//			String command = (String) iterator.next();
////			appendText(command + "\n", blue);
//			Object evalResult = bsh.eval(command);
//			if (evalResult != null && evalResult instanceof String) appendTextSync(evalResult + "\n", blue);
//			}
////			bsh.eval(cmd);
//			}
		};
		terminal.addKeyListener(keyAdapter);
		contributeToActionBars();
	}

	public void contributeToActionBars() {

		IActionBars bars = getViewSite().getActionBars();
//		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		skipAction = new Action() {
			public void run() {
				for (Iterator iterator = skipActionListenerList.iterator(); iterator
				.hasNext();) {
					ActionListener listener = (ActionListener) iterator.next();
					listener.run();
				}
			}
		};
		skipAction.setText("skip current command");
		skipAction.setToolTipText("skip current running command and jump to then next command in the script queue");
		skipAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
		"icons/stepreturn.gif"));
		skipAction.setDisabledImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
		"icons/stepreturn_co.gif"));
		skipAction.setEnabled(false);
		stopAction = new Action() {
			public void run() {
				for (Iterator iterator = stopActionListenerList.iterator(); iterator
				.hasNext();) {
					ActionListener listener = (ActionListener) iterator.next();
					listener.run();
				}
			}
		};
		stopAction.setText("stop script");
		stopAction.setToolTipText("stop the script queue");
		stopAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
		"icons/terminate.gif"));
		stopAction.setDisabledImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
		"icons/terminate_co.gif"));
		stopAction.setEnabled(false);
//		Action runAlgorithmAction = new Action() {
//		public void run() {
////		runSelectedAlgorithm();
//		}
//		};
//		runAlgorithmAction.setText("Run Algorithm");
//		runAlgorithmAction.setToolTipText("Run selected algorithm");
//		runAlgorithmAction.setImageDescriptor(Activator.getImageDescriptor("icons/run_tool.gif"));
//		runAlgorithmAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/run_tool_dis.gif"));
//		runAlgorithmAction.setEnabled(false);
		manager.add(skipAction);
		manager.add(stopAction);
	}

	public void evaluate(String cmd) throws EvaluationFailedException{
		// TODO Auto-generated method stub
		Thread currentThread = Thread.currentThread();
//		appendText(currentThread.getName() + " -- evaluation\n");
		List<String> javaCmds = interpreter.interpret(cmd);
		for (Iterator<?> iterator = javaCmds.iterator(); iterator.hasNext();) {
			String command = (String) iterator.next();
//			appendText(command + "\n", blue);
			Object evalResult = null;
			try {
				evalResult = bsh.eval(command);
			} catch (EvalError e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				throw new EvaluationFailedException(e);
			}
			if (evalResult != null && evalResult instanceof String) 
				appendText(evalResult + "\n", blue);
		}
//		bsh.eval(cmd);
	}

	private void createInstrumentInterpreter() {
		// TODO Auto-generated method stub
		interpreterRegistry = InterpreterRegistry.getInstance();
		try {
			interpreter = InstrumentInterpreter.getInstance(
					interpreterRegistry.getDefaultInterpreterClassID());
			appendText("use " + interpreterRegistry.getDefaultInterpreterName() 
					+ " command space.\n", darkRed);
		} catch (CreateInterpreterInstanceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			interpreter = new InstrumentInterpreter();
			appendText("use default(java) interpreter.\n", darkRed);
		}
		Map<String, String> interpreterMap = interpreterRegistry.getInterpreterMap();
		if (interpreterMap != null && interpreterMap.size() > 0){
			String interpreterNames = "";
			Set<String> keySet = interpreterMap.keySet();
			for (Iterator<?> iterator = keySet.iterator(); iterator
			.hasNext();) {
				String key = (String) iterator.next();
				interpreterNames += key + "\n";
			}
			appendText("The following instruments are available, \n" 
					+ interpreterNames
					+ "default\n"
					+ "please type in 'use + instrument name' to " +
					"change command space.\n"
					+ "use 'help' to read manual\n" , darkRed);
			Thread currentThread = Thread.currentThread();
//			appendText(currentThread.getName() + " -- main\n", darkRed);
		}
	}

	public void pushOut(){
		terminal.getDisplay().syncExec (new Runnable () {
			public void run () {
				int lineCount = terminal.getLineCount();
				if (lineCount > lineLimit){
					int startPoint = terminal.getOffsetAtLine(lineCount - lineLimit);
					StyledTextContent content = terminal.getContent();
					content.replaceTextRange(0, startPoint, "");
//					terminal.replaceTextRange(0, startPoint, "");
					autoScroll();
				}
//				StyledTextContent doc = terminal.getContent();
//				int docLength = doc.getCharCount();
//				if (docLength > 0) {
//				terminal.setCaretOffset(docLength);
//				terminal.showSelection();
//				pointer = terminal.getCaretOffset();
////				System.out.println("BeanShellCommandLineView.autoScroll() = " + pointer);
//				}
				appendText(">>>", darkRed);
			}});
	}

	private void autoScroll() {


//		int docLength = terminal.getCharCount();
//		if (docLength > textLimit){
//		String newText = terminal.getTextRange(docLength - textLimit, textLimit);
//		terminal.setText(newText);
//		}
		StyledTextContent doc = terminal.getContent();
		int docLength = doc.getCharCount();
		if (docLength > 0) {
			terminal.setCaretOffset(docLength);
			terminal.showSelection();
			pointer = terminal.getCaretOffset();
//			System.out.println("BeanShellCommandLineView.autoScroll() = " + pointer);
		}
	}

	@Override
	public void setFocus() {
	}

	public void appendText(final String text, final Color color) {
		if (text == null)
			return;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				StyleRange styleRange = new StyleRange();
				styleRange.start = terminal.getCharCount();
				styleRange.length = text.length();
				styleRange.foreground = color;
				terminal.append(text);
				terminal.setStyleRange(styleRange);
				autoScroll();
			}
		});
	}

	public void appendText(final String text) {
		appendText(text, blue);
	}

	public void appendText(final String text, final ColorEnum color){
		switch (color) {
		case red:
			appendText(text, red);
			break;
		case blue:
			appendText(text, blue);
			break;
		case darkRed:
			appendText(text, darkRed);
			break;
		case black:
			appendText(text, black);
			break;
		case green:
			appendText(text, green);
			break;
		default:
			break;
		}
	}

//	public void appendTextSync(final String text, final Color color) {
//		terminal.getDisplay().asyncExec (new Runnable () {
//			public void run () {
//				appendText(text, color);
//			}});
//	}

	public void dispose() {
		if(blue != null) {
			blue.dispose();
			blue = null;
		}
		if(darkRed != null) {
			darkRed.dispose();
			darkRed = null;
		}
		if(red != null) {
			red.dispose();
			red = null;
		}
		if(black != null) {
			black.dispose();
			black = null;
		}
		if(green != null) {
			green.dispose();
			green = null;
		}
		super.dispose();
	}

	public void setInstrumentInterpreter(InstrumentInterpreter interpreter) {
		this.interpreter = interpreter;
	}

	public String dialog(final String message){
		final Thread currentThread = Thread.currentThread();

		final KeyAdapter argumentKeyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
//				if(terminal.getCaretOffset() >= pointer)
//				terminal.setEditable(true);
//				else
//				terminal.setEditable(false);
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
//					isReturn = true;
//					notify();
					isReturn = true;
				}
			}
		};

		terminal.getDisplay().syncExec (new Runnable () {
			public void run () {
				terminal.removeKeyListener(keyAdapter);
				isReturn = false;
				appendText(message + "\n");
				terminal.addKeyListener(argumentKeyListener);
//				System.out.println("pointer and length are " + pointer + "/" + terminal.getCharCount());
			}});
//		argument = terminal.getText(pointer, terminal.getCharCount());
//		appendText(currentThread.getName() + " -- dialog\n");
		while(!isReturn) {
			try {
				currentThread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
//				throw new SicsExecutionException("Interrupted Exception", e);
			}
		}
		isReturn = false;
//		terminal.removeKeyListener(argumentKeyListener);
		terminal.getDisplay().syncExec (new Runnable () {
			public void run () {
//				System.out.println("pointer and length are " + pointer + "/" + terminal.getCharCount());
				dialogArgument = terminal.getText(pointer, terminal.getCharCount() - 3);
				terminal.removeKeyListener(argumentKeyListener);
				terminal.addKeyListener(keyAdapter);
			}});		
		return dialogArgument;
	}

	public static IWorkbenchPage getWorkbenchPage(){
		return getInstance().getSite().getPage();
	}

	public void addSkipActionListener(ActionListener listener){
		if (skipActionListenerList == null) skipActionListenerList = new ArrayList<ActionListener>();
		skipActionListenerList.add(0, listener);
		if (skipAction != null) skipAction.setEnabled(true);
	}

	public void removeSkipActionListener(ActionListener listener){
		if (skipActionListenerList != null) 
			skipActionListenerList.remove(listener);
		if (skipActionListenerList == null || skipActionListenerList.size() == 0)
			skipAction.setEnabled(false);
	}

	public void addStopActionListener(ActionListener listener){
		if (stopActionListenerList == null) stopActionListenerList = new ArrayList<ActionListener>();
		stopActionListenerList.add(0, listener);
		if (stopAction != null) stopAction.setEnabled(true);
	}

	public void removeStopActionListener(ActionListener listener){
		if (stopActionListenerList != null) 
			stopActionListenerList.remove(listener);
		if (stopActionListenerList == null || stopActionListenerList.size() == 0)
			stopAction.setEnabled(false);
	}

	public void removeAllStopActionListener(){
		if (stopActionListenerList != null) 
			stopActionListenerList.removeAll(stopActionListenerList);
		if (stopActionListenerList == null || stopActionListenerList.size() == 0)
			stopAction.setEnabled(false);
	}

	public void removeAllSkipActionListener() {
		// TODO Auto-generated method stub
		if (skipActionListenerList != null) 
			skipActionListenerList.removeAll(skipActionListenerList);
		if (skipActionListenerList == null || skipActionListenerList.size() == 0)
			skipAction.setEnabled(false);
	}

	public LinkedList<String> getHistoryCommandList() {
		return historyCommandList;
	}

	public static void showCommandLineView() throws PartInitException  {
		IWorkbench workbench;
		
		workbench = PlatformUI.getWorkbench();
//		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
//		workbench.getWorkbenchWindows()
//		workbenchWindow = workbench.getActiveWorkbenchWindow();
		final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
//		final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
//		workbenchWindow.getActivePage().showView("org.gumtree.ui.cli.beanShellTerminalview");
		workbench.getDisplay().syncExec(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.gumtree.ui.cli.beanShellTerminalview");
				} catch (PartInitException e) {
					e.printStackTrace();
				}						
			}
			
		});
		while (beanShellCommandLineView == null){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		workbenchWindow.getActivePage().showView("org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView");
	}
	
	public static void showCommandLineView(final IWorkbenchWindow workbenchWindow) throws PartInitException  {
		IWorkbench workbench;
		
		workbench = workbenchWindow.getWorkbench();
		workbench.getDisplay().syncExec(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				try {
					workbenchWindow.getActivePage().showView("org.gumtree.ui.cli.beanShellTerminalview");
				} catch (PartInitException e) {
					e.printStackTrace();
				}						
			}
			
		});
		while (beanShellCommandLineView == null){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		workbenchWindow.getActivePage().showView("org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView");
	}
	
	public InstrumentInterpreter getInterpreter(){
		return interpreter;
	}


}
