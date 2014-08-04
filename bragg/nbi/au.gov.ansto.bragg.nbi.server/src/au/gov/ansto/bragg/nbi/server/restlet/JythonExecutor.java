/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.gumtree.scripting.EvalChangeEvent;
import org.gumtree.scripting.IObservableComponent;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.IScriptingListener;
import org.gumtree.scripting.ObservableScriptContext;
import org.gumtree.scripting.ScriptBlock;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.scripting.ScriptExecutorEvent;
import org.gumtree.scripting.ScriptingChangeEvent;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.PlatformUtils;

/**
 * @author nxi
 *
 */
public class JythonExecutor {

	public enum ExecutorStatus{
		IDLE, BUSY, INTERRUPTED, ERROR, 
	}
	
	private static ScriptExecutor executor;

	private static ExecutorStatus status;
	
	private static String recentMessage;
	
	private static String consoleHistory;
	
	private static String errorHistory;
	
	private static String recentError;
	
	private static IEventHandler<ScriptExecutorEvent> executorEventHandler;
	
	private static String currentScript;
	
	public static final String VAR_SILENCE_MODE = "slienceMode";
	
	/**
	 * 
	 */
	public JythonExecutor() {
	}

	public static ScriptExecutor getExecutor() {
		synchronized (JythonRestlet.class) {
			if (executor == null) {
				executor = new ScriptExecutor("jython");
				while (!executor.isInitialised()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) { }
				}
	    		final ScriptEngine engine = executor.getEngine();
	    		
//	    		if (engine instanceof IObservableComponent) {
//	    			IScriptingListener listener = new IScriptingListener() {
//	    				public void handleChange(final ScriptingChangeEvent event) {
//	    					if (event instanceof EvalChangeEvent) {
////	    						print("\n\n>> ", Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED), SWT.NORMAL);
//	    						appendText(((EvalChangeEvent) event).getScript());
//	    					}
//	    				}
//	    			};
//	    			((IObservableComponent) engine).addListener(listener);
//	    		}
	    		
	    		ScriptContext scriptContext = engine.getContext();
	    		if (scriptContext == null) {
	    			// Same engine (like Jepp) does not provide default context out of the box
	    			ScriptContext context = new ObservableScriptContext();
	    			engine.setContext(context);
	    			scriptContext = engine.getContext();
	    		}
	    		PrintWriter writer = new PrintWriter(new ByteArrayOutputStream() {
	    			public synchronized void flush() throws IOException {
	    				final String text = toString();
	    				appendText(text);
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
	    				appendError(text);
	    				setStatus(ExecutorStatus.ERROR);
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
	    		
	    		executorEventHandler = new IEventHandler<ScriptExecutorEvent>() {
	    			@Override
	    			public void handleEvent(final ScriptExecutorEvent event) {
	    				if (executor.isBusy()) {
	    					setStatus(ExecutorStatus.BUSY);
	    				} else {
	    					setStatus(ExecutorStatus.IDLE);
	    				}
	    			}			
	    		};
	    		PlatformUtils.getPlatformEventBus().subscribe(executor, executorEventHandler);
	    		
	    		recentMessage = "";
	    		recentError = "";
	    		consoleHistory = "";
	    		errorHistory = "";
	    		status = ExecutorStatus.IDLE;
			}
		}
		return executor;
	}

	public static void runScriptLine(final String scriptLine) {
		currentScript = scriptLine;
		getExecutor().getEngine().getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(VAR_SILENCE_MODE, false);
		setStatus(ExecutorStatus.BUSY);
		getExecutor().runScript(scriptLine);
	}
	
	public static void runScriptBlock(IScriptBlock scriptBlock){
		currentScript = scriptBlock.getScript();
		getExecutor().getEngine().getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(VAR_SILENCE_MODE, true);
		setStatus(ExecutorStatus.BUSY);
		getExecutor().runScript(scriptBlock);
	}
	
	public static void appendText(String text) {
		recentMessage += text;
		consoleHistory += text;
	}
	
	public static void appendError(String errText) {
		recentError += errText;
		errorHistory += errText;
		System.err.println("Error: " + errText);
	}
	
	public static String getRecentText(boolean reset){
		String message = recentMessage;
		if (reset) {
			recentMessage = "";
		}
		return message;
	}
	
	public static String getRecentError(boolean reset){
		String error = recentError;
		if (reset) {
			recentError = "";
		}
		return error;
	}
	
	public static ExecutorStatus getStatus(){
		return status;
	}
	
	private static void setStatus(ExecutorStatus newStatus) {
		status = newStatus;
	}
	
	public static void interrupt() {
		if (executor.isBusy()) {
			executor.interrupt();
			setStatus(ExecutorStatus.INTERRUPTED);
		}
	}
	
	public static String getCurrentScript(){
		return currentScript;
	}
	
	public static void resetConsoleMessage() {
		setStatus(ExecutorStatus.IDLE);
		recentMessage = "";
		recentError = "";
	}
}
