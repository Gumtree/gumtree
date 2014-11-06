package au.gov.ansto.bragg.nbi.server.jython;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.eclipse.core.runtime.FileLocator;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.ObservableScriptContext;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.scripting.ScriptExecutorEvent;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.PlatformUtils;

import au.gov.ansto.bragg.nbi.server.image.ChartImage;
import au.gov.ansto.bragg.nbi.server.internal.Activator;
import au.gov.ansto.bragg.nbi.server.restlet.JythonDataHandler;
import au.gov.ansto.bragg.nbi.server.restlet.JythonExecutor.ExecutorStatus;
import au.gov.ansto.bragg.nbi.server.restlet.JythonModelRegister;
import au.gov.ansto.bragg.nbi.server.restlet.JythonUIHandler;

public class JythonRunner {

	private UUID uuid;
	
	private ScriptExecutor executor;
	
	private ExecutorStatus status;
	
	private String recentMessage;
	
	private String consoleHistory;
	
	private String errorHistory;
	
	private String recentError;
	
	private String eventJs;
	
	private String filesForDownload;
	
	private IEventHandler<ScriptExecutorEvent> executorEventHandler;
	
	private String currentScript;
	
	private JythonUIHandler uiHandler;
	
	private JythonDataHandler dataHandler;
	
	private ChartImage plot1Cache;
	private ChartImage plot2Cache;
	private ChartImage plot3Cache;

	public static final String VAR_SILENCE_MODE = "slienceMode";

	private static final String INIT_SCRIPT = "/pyscripts/__init__.py";
	
	private final static int IMAGE_WIDTH = 640;
	private final static int IMAGE_HEIGHT = 320;


	public JythonRunner() {
		uuid = UUID.randomUUID();
		initialise();
	}

	public JythonRunner(UUID uuid) {
		this.uuid = uuid;
		initialise();
	}
	
	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}


	private void initialise() {
//		synchronized (JythonRestlet.class) {
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
	    		eventJs = "";
	    		status = ExecutorStatus.IDLE;
	    		
	    		uiHandler = new JythonUIHandler(this);
	    		JythonModelRegister register = new JythonModelRegister(this);
	    		JythonModelRegister.registPage(uiHandler.getScriptRegisterID(), register);
	    		
	    		dataHandler = new JythonDataHandler(this);
//	    		register.setDataPath(dataHandler.getDataPath());
//	    		register.setSavePath(dataHandler.getSavePath());
//	    		register.setCalibrationPath(dataHandler.getCalibrationPath());
//	    		register.setScriptPath(uiHandler.getScriptPath());
	    		plot1Cache = new ChartImage(IMAGE_WIDTH, IMAGE_HEIGHT);
	    		plot2Cache = new ChartImage(IMAGE_WIDTH, IMAGE_HEIGHT);
	    		plot3Cache = new ChartImage(IMAGE_WIDTH, IMAGE_HEIGHT);

	    		loadInitScript();
	    		executor.runScript(dataHandler.getLoadedDataCommand());
	    	
			}
//		}
	}

	public ScriptExecutor getExecutor() {
		return executor;
	}
	
	private void loadInitScript() {
		try {
			executor.runScript("__script_model_id__ = " + uiHandler.getScriptRegisterID());
			String fn = FileLocator.toFileURL(Activator.getContext().getBundle().getEntry(INIT_SCRIPT)).getFile();
			executor.runScript(new FileReader(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void runScriptLine(final String scriptLine) {
		currentScript = scriptLine;
		getExecutor().getEngine().getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(VAR_SILENCE_MODE, false);
		resetErrorStatus();
		setStatus(ExecutorStatus.BUSY);
		getExecutor().runScript(scriptLine);
	}
	
	public void runScriptBlock(IScriptBlock scriptBlock){
		currentScript = scriptBlock.getScript();
		getExecutor().getEngine().getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(VAR_SILENCE_MODE, true);
		resetErrorStatus();
		setStatus(ExecutorStatus.BUSY);
		getExecutor().runScript(scriptBlock);
	}
	
	public void runScriptFile(String filename) {
		FileReader reader = null;
		try {
//			currentScript = new String(Files.readAllBytes(Paths.get(URI.create(newName))), StandardCharsets.UTF_8);
			reader = new FileReader(filename);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		getExecutor().getEngine().getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(VAR_SILENCE_MODE, true);
		resetErrorStatus();
		setStatus(ExecutorStatus.BUSY);
		getExecutor().runScript(reader);
	}
	
	public void appendText(String text) {
		recentMessage += text;
		consoleHistory += text;
	}
	
	public void appendError(String errText) {
		recentError += errText;
		errorHistory += errText;
		System.err.println("Error: " + errText);
	}
	
	public String getRecentText(boolean reset){
		String message = recentMessage;
		if (reset) {
			recentMessage = "";
		}
		return message;
	}
	
	public String getRecentError(boolean reset){
		String error = recentError;
		if (reset) {
			recentError = "";
		}
		return error;
	}
	
	public ExecutorStatus getStatus(){
		return status;
	}
	
	private void setStatus(ExecutorStatus newStatus) {
		if (status != ExecutorStatus.ERROR) {
			status = newStatus;
		}
	}
	
	public void resetErrorStatus(){
		status = ExecutorStatus.IDLE;
	}
	
	public void interrupt() {
		if (executor.isBusy()) {
			executor.interrupt();
		}
	}
	
	public String getCurrentScript(){
		return currentScript;
	}
	
	public void resetConsoleMessage() {
		setStatus(ExecutorStatus.IDLE);
		recentMessage = "";
		recentError = "";
	}

	/**
	 * @return the consoleHistory
	 */
	public String getConsoleHistory() {
		return consoleHistory;
	}

	/**
	 * @return the errorHistory
	 */
	public String getErrorHistory() {
		return errorHistory;
	}

	public JythonUIHandler getUIHandler(){
		return uiHandler;
	}
	
	public String getScriptGUI(String script){
		if (executor == null) {
			getExecutor();
		}
		String guiHtml = uiHandler.getScriptGUIHtml(script);
		appendEventJs(uiHandler.getControlJs());
		return guiHtml;
	}

	/**
	 * @return the eventMessage
	 */
	public String getEventJs(boolean reset) {
		if (reset) {
			String script = eventJs;
			eventJs = "";
			return script;
		}
		return eventJs;
	}

	/**
	 * @param eventMessage the eventMessage to set
	 */
	public void appendEventJs(String script) {
		eventJs += script;
	}
	
	public String getAllDataHtml(){
		return dataHandler.getAllDataHtml();
	}
	
	public String getUserDataHtml(){
		return dataHandler.getUserDataHtml(uuid.toString());
	}
	
	public String getDefaultScript(){
		return uiHandler.getDefaultScript();
	}
	
	public JythonDataHandler getDataHandler(){
		return dataHandler;
	}
	
	public String getFilesForDownload(boolean reset) {
		if (reset) {
			String files = filesForDownload;
			filesForDownload = "";
			return files;
		}
		return filesForDownload;
	}
	
	public void appendFilesForDownload(String file){
		filesForDownload += file + ";";
	}

	public ChartImage getPlot1(){
		return plot1Cache;
	}
	
	public ChartImage getPlot2(){
		return plot2Cache;
	}
	
	public ChartImage getPlot3(){
		return plot3Cache;
	}

	public String getUserPath(){
		return dataHandler.getUserPath(uuid.toString());
	}
}
