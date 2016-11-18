package au.gov.ansto.bragg.nbi.server.login;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.MapDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

import au.gov.ansto.bragg.nbi.server.internal.UserSessionService;

public class UserHomeRestlet extends Restlet implements IDisposable{

	private static final String SEG_NAME_MENU = "menu";
	private static final String SEG_NAME_INFO = "info";
	private static final String SEG_NAME_USER = "user";
	private MapDatabase serviceDb;
	
	public UserHomeRestlet() {
		serviceDb = MapDatabase.getInstance(UserSessionService.ID_SESSION_SERVICE_DATABASE);
	}

	public UserHomeRestlet(Context context) {
		super(context);
		serviceDb = MapDatabase.getInstance(UserSessionService.ID_SESSION_SERVICE_DATABASE);
	}

	@Override
	public void handle(final Request request, final Response response) {

//			Form queryForm = request.getResourceRef().getQueryAsForm();
//			String seg = request.getResourceRef().getLastSegment();
//			List<String> segList = request.getResourceRef().getSegments();
//			String ip = request.getClientInfo().getUpstreamAddress();
	    
//		Representation entity = request.getEntity();
//    	Form form = new Form(entity);
//    	String username = form.getValues(QUERY_USER_ID);
    	
		UserSessionObject session = null;
		try {
			session = UserSessionService.getSession(request, response);
//			isSessionValid = UserSessionService.controlSession(request, response);
		} catch (Exception e1) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e1.toString());
			return;
		}
		if (session != null && session.isValid()) {
			String seg = request.getResourceRef().getLastSegment();
//			List<String> segList = request.getResourceRef().getSegments();
			if (SEG_NAME_MENU.equals(seg)) {
				JSONObject jsonObject = new JSONObject();
			    try {
			    	jsonObject.put("status", "OK");
			    	JSONObject menuJson = new JSONObject(new LinkedHashMap<String, Object>());
			    	JSONObject infoJson = new JSONObject(new LinkedHashMap<String, Object>());
			    	addInstrumentStatus(menuJson, infoJson);
//			    	String sessionId = UserSessionService.getSessionId(request);
//			    	if (sessionId != null) {
//			    		String serviceString = serviceDb.get(sessionId);
//			    		if (serviceString != null) {
//			    			JSONObject serviceList = new JSONObject(serviceString);
//			    			try {
//				    			if (Boolean.valueOf(String.valueOf(serviceList.get(
//				    					UserSessionService.NAME_SERVICE_NOTEBOOKMANAGER)))) {
//					    			addManageNotebook(menuJson, infoJson);
//				    			}
//							} catch (Exception e) {
//							}
//			    		}
//			    	}
			    	boolean allowCurrentCatalog = false;
			    	if (UserSessionService.verifyService(session, UserSessionService.NAME_SERVICE_CURRENTPAGE)) {
			    		addCurrentNotebook(menuJson, infoJson);
			    		allowCurrentCatalog = true;
			    	}
			    	if (UserSessionService.verifyService(session, UserSessionService.NAME_SERVICE_NOTEBOOKADMIN)) {
				    	JSONArray proposals = UserSessionService.getProposals(session);
				    	if (proposals != null) {
				    		addNotebookProposals(menuJson, infoJson, proposals);
				    	}
//			    		addPythonScript(menuJson, infoJson);
			    	}
			    	if (UserSessionService.verifyService(session, UserSessionService.NAME_SERVICE_NOTEBOOKMANAGER)){
			    		addManageNotebook(menuJson, infoJson);
			    		addManageCatalog(menuJson, infoJson);
			    	} else {
			    		if (allowCurrentCatalog) {
			    			addCurrentCatalog(menuJson, infoJson);
			    		}
			    	}
			    	jsonObject.put("menu", menuJson);
			    	jsonObject.put("info", infoJson);
			    	response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
			    	UserSessionService.renewCookie(session, response);
			    } catch (Exception e) {
			    	response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			    	return;
				}
			} else if (SEG_NAME_USER.equals(seg)) {
				try {
					JSONObject jsonObject = new JSONObject();
					if (session.isValid()){
						String username = session.getUserName();
						if (username.contains("@")){
							username = username.substring(0, username.indexOf("@"));
						}
						jsonObject.put("status", "OK");
						jsonObject.put("user", username);
				    	response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
					} else {
						jsonObject.put("status", "OK");
						jsonObject.put("user", "NONE");
				    	response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
					}
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, "internal error");
					return;
				}
			} 
		} else {
			try {
				JSONObject json = new JSONObject();
				if (session != null) {
					json.put("status", session.getMessage());
				} else {
			    	json.put("status", "please sign in");					
				}
				response.setEntity(json.toString(), MediaType.APPLICATION_JSON);				
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			}
		}
	    response.setStatus(Status.SUCCESS_OK);
	}

	private void addInstrumentStatus(JSONObject menuJson, JSONObject infoJson) throws JSONException {
    	menuJson.put("mobile.html", "Instrument Status");
    	infoJson.put("mobile.html", "Instrument Status: The online dashboard for the instrument.");		
	}
	
	private void addManageNotebook(JSONObject menuJson, JSONObject infoJson) throws JSONException, IOException {
    	menuJson.put("notebookAdmin.html", "Manage Notebook");
    	infoJson.put("notebookAdmin.html", "Manage Notebook: Instrument scientists can use this " +
    			"page to manage current and history notebook page.");		
	}
	
	private void addManageCatalog(JSONObject menuJson, JSONObject infoJson) throws JSONException, IOException {
    	menuJson.put("catalogAdmin.html", "Manage Data Catalog");
    	infoJson.put("catalogAdmin.html", "Manage Data Catalog: Instrument scientists can use this " +
    			"page to manage data catalogs of the current and history proposals.");		
	}
	
	private void addCurrentNotebook(JSONObject menuJson, JSONObject infoJson) throws JSONException, IOException {
    	menuJson.put("notebook.html", "Current Notebook Page");
    	infoJson.put("notebook.html", "Current Notebook Page: Access to edit current notebook page.");		
	}

	private void addCurrentCatalog(JSONObject menuJson, JSONObject infoJson) throws JSONException, IOException {
    	menuJson.put("catalog.html", "Data Catalog Page");
    	infoJson.put("catalog.html", "Data Catalog Page: Access to data catalog of the current proposal.");		
	}

	private void addPythonScript(JSONObject menuJson, JSONObject infoJson) throws JSONException {
    	menuJson.put("pyscript.html", "Data Treatment");
    	infoJson.put("pyscript.html", "Data Treatment: data downloading and online data reduciton.");		
	}

	private void addNotebookProposals(JSONObject menuJson, JSONObject infoJson, JSONArray proposals) 
			throws JSONException {
//		if (proposals != null) {
//			for (int i = 0; i < proposals.length(); i++) {
//				Object page = proposals.get(i);
//				menuJson.put("proposal" + String.valueOf(page), "Proposal " + page);
//		    	infoJson.put("proposal.html", "Page: " + page);		
//
//			}
//		}
		menuJson.put("myNotebook.html", "My Notebook Pages");
    	infoJson.put("myNotebook.html", "My Notebook Pages: Access to notebook pages of my proposals.");
	}
	
	@Override
	public void disposeObject() {

	}
	
}
