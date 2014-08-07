/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import org.gumtree.core.object.IDisposable;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.ScriptBlock;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import au.gov.ansto.bragg.nbi.server.restlet.JythonExecutor.ExecutorStatus;

/**
 * @author nxi
 *
 */
public class JythonRestlet extends Restlet implements IDisposable {

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
		case INTERRUPT:
			JythonExecutor.interrupt();
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
		ExecutorStatus status = JythonExecutor.getStatus();
		jsonObject.put("status", status);
		jsonObject.put("text", JythonExecutor.getRecentText(true));
		jsonObject.put("error", JythonExecutor.getRecentError(true));
//		if (status == ExecutorStatus.ERROR) {
//			JythonExecutor.resetErrorStatus();
//		}
		return jsonObject;
	}
	

	
}
