/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.channels.ReadableByteChannel;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.gumtree.core.object.IDisposable;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.ObservableScriptContext;
import org.gumtree.scripting.ScriptBlock;
import org.gumtree.scripting.ScriptExecutor;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

/**
 * @author nxi
 *
 */
public class JythonRestlet extends Restlet implements IDisposable {

	private static ScriptExecutor executor;
	private final static String QUERY_SCRIPT_TEXT = "script_text";
	private final static String QUERY_SCRIPT_INPUT_MODE = "script_input";
	private final static String QUERY_TYPE = "type";
	private final static String SCRIPT_START_FLAG = "Content-Type:";
	
	
	enum QueryType {
		START,
		STATUS,
		INTERRUPT,
		READSCRIPT
	}
	/**
	 * 
	 */
	public JythonRestlet() {
		super();
	}

	/**
	 * @param context
	 */
	public JythonRestlet(Context context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {
	}
	
	@Override
	public void handle(final Request request, final Response response) {
		
        Form queryForm = request.getResourceRef().getQueryAsForm();
	    String typeString = queryForm.getValues(QUERY_TYPE);
	    QueryType type = QueryType.valueOf(typeString);
	    
	    switch (type) {
		case START:
	    	//		Form form = request.getResourceRef().getQueryAsForm();
	    	Representation entity = request.getEntity();
	    	Form form = new Form(entity);
	    	final String value = form.getValues(QUERY_SCRIPT_TEXT);
	    	final String inputMode = form.getValues(QUERY_SCRIPT_INPUT_MODE);
	    	try {
	    		final JSONObject result = new JSONObject();
	    		result.put("script", value);
	    		//			response.setEntity(result.toString(), MediaType.TEXT_PLAIN);
	    		if ("textArea".equals(inputMode)) {
	    			IScriptBlock block = new ScriptBlock(){
	    				public String getScript() {
	    					return value;
	    				}
	    			};
	    			JythonExecutor.runScriptBlock(block);
	    		} else if ("textInput".equals(inputMode)){
		    		JythonExecutor.runScriptLine(value);
	    		}
	    		Thread.sleep(200);
	    		JSONObject jsonObject = getExecutorStatus();
	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
	    	}
			break;
		case STATUS:
//			ExecutorStatus status = JythonExecutor.getStatus();
//			if (status == ExecutorStatus.BUSY) {
//				response.setEntity("Status:" + status.name() + ",Text:" + JythonExecutor.getRecentText(true), MediaType.TEXT_PLAIN);
//			} else if (status == ExecutorStatus.ERROR){
//				response.setEntity("Status:" + status.name() + ",Text:" + JythonExecutor.getRecentError(true), MediaType.TEXT_PLAIN);
//			} else if (status == ExecutorStatus.IDLE) {
//				response.setEntity("Status:" + status.name() + ",Text:" + JythonExecutor.getRecentText(true), MediaType.TEXT_PLAIN);
//			} else {
//				response.setEntity("Status:" + status.name(), MediaType.TEXT_PLAIN);
//			}
			JSONObject jsonObject;
			try {
				jsonObject = getExecutorStatus();
	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
			} catch (JSONException e) {
	    		e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			break;
		case READSCRIPT:
			try {
		    	Representation formEntity = request.getEntity();
		    	
//		    	RestletFileUpload upload = new RestletFileUpload(factory);
//                List<FileItem> items;
//
//                // 3/ Request is parsed by the handler which generates a
//                // list of FileItems
//                items = upload.parseRequest(getRequest());
		    	String text = formEntity.getText();
		    	int start = text.indexOf(SCRIPT_START_FLAG);
		    	start = text.indexOf("\n", start) + 3;
		    	int end = text.lastIndexOf("\n", text.length() - 2) - 1;
		    	text = text.substring(start, end);
				response.setEntity(text, MediaType.TEXT_PLAIN);
			} catch (Exception e) {
				e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			break;
		default:
			break;
		}


	}
	
	private JSONObject getExecutorStatus() throws JSONException{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", JythonExecutor.getStatus());
		jsonObject.put("text", JythonExecutor.getRecentText(true));
		jsonObject.put("error", JythonExecutor.getRecentError(true));
		return jsonObject;
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
	    		ScriptContext scriptContext = engine.getContext();
	    		if (scriptContext == null) {
	    			// Same engine (like Jepp) does not provide default context out of the box
	    			ScriptContext context = new ObservableScriptContext();
	    			engine.setContext(context);
	    			scriptContext = engine.getContext();
	    		}
	    		PrintWriter writer = new ExecutorWriter(new ByteArrayOutputStream() {
	    			public synchronized void flush() throws IOException {
	    				final String text = toString();
//	    				response.setEntity(text, MediaType.TEXT_PLAIN);
	    				reset();
	    			}
	    		}, true);
	    		scriptContext.setWriter(writer);

			}
		}
		return executor;
	}
	
}
