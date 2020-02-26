/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.catalog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.CatalogDB;
import org.gumtree.service.db.ControlDB;
import org.gumtree.service.db.ProposalDB;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import au.gov.ansto.bragg.nbi.server.internal.AbstractUserControlRestlet;
import au.gov.ansto.bragg.nbi.server.internal.UserSessionService;
import au.gov.ansto.bragg.nbi.server.login.UserSessionObject;

/**
 * @author nxi
 *
 */
public class CatalogRestlet extends AbstractUserControlRestlet implements IDisposable {

	private static final String PROP_CATALOG_SAVEPATH = "gumtree.catalog.savePath";
	private static final String SEG_NAME_APPEND = "append";
	private static final String SEG_NAME_UPDATE = "update";
	private static final String SEG_NAME_READ = "read";
	private static final String SEG_NAME_LIST = "list";
	private final static String SEG_NAME_HELP = "help";
	private static final String QUERY_ENTRY_PROPOSALID = "proposal";
	private static final String QUERY_ENTRY_SESSIONID = "session";
	private static final String QUERY_ENTRY_VALUES = "values";
	private static final String QUERY_ENTRY_COLUMNS = "columns";
	private static final String QUERY_ENTRY_TIMESTAMP = "timestamp";
	private static final String QUERY_ENTRY_KEY = "key";
	private static final String QUERY_ENTRY_START = "start";
	private static final String CATALOG_HELPFILENAME = "guide.xml";
	
	
	private ProposalDB proposalDb;
	private ControlDB controlDb;
	private String helpFilePath;
	
	public CatalogRestlet(){
		this(null);
	}
	/**
	 * @param context
	 */
	public CatalogRestlet(Context context) {
		super(context);
		proposalDb = ProposalDB.getInstance();
		controlDb = ControlDB.getInstance();
		helpFilePath = System.getProperty(PROP_CATALOG_SAVEPATH) + "/" + CATALOG_HELPFILENAME;
	}

	@Override
	public void handle(Request request, Response response) {
		String seg = request.getResourceRef().getLastSegment();
		List<String> segList = request.getResourceRef().getSegments();

		UserSessionObject session = null;
		
//		try {
//			session = UserSessionService.getSession(request, response);
//		} catch (Exception e1) {
//			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, e1.toString());
//			return;
//		}
//		
//		if (session == null || !session.isValid()) {
//			try {
//				session = checkDavSession(request);
//			} catch (Exception e) {
//			}
//		}
		try {
			session = UserSessionService.getUniversalSession(request, response);
		} catch (Exception e) {
		}
		
		if (session != null && session.isValid()) {
			if (SEG_NAME_APPEND.equalsIgnoreCase(seg)){
				try {
					Form form = request.getResourceRef().getQueryAsForm();
					String proposalId = form.getValues(QUERY_ENTRY_PROPOSALID);
					String sessionId = controlDb.getCurrentSessionId();
					String currentProposal = proposalDb.findProposalId(sessionId);
					boolean allowAccess = false;
					if (proposalId == null || proposalId.equals(currentProposal)) {
						if (allowEditHistoryProposal(session, currentProposal)) {
							allowAccess = true;
						}
						proposalId = currentProposal;
					} else {
						if (allowEditHistoryProposal(session, proposalId)) {
							allowAccess = true;
						}
					}
					if (allowAccess){
						CatalogDB catalogDb = CatalogDB.getInstance(proposalId);
						String values = form.getValues(QUERY_ENTRY_VALUES);
						String key = form.getValues(QUERY_ENTRY_KEY);
						String[] valueArray = values.split(",");
						catalogDb.updateEntry(key, valueArray);
						response.setEntity("Done", MediaType.TEXT_PLAIN);
						response.setStatus(Status.SUCCESS_OK);
					}
				} catch (Exception e) {
					e.printStackTrace();
					response.setEntity("<span style=\"color:red\">Error appending to catalogue database: " 
								+ e.getMessage() + ".</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			} if (SEG_NAME_UPDATE.equalsIgnoreCase(seg)){
				try {
					Representation entity = request.getEntity();
			    	Form form = new Form(entity);
			    	String proposalId = form.getValues(QUERY_ENTRY_PROPOSALID);
			    	if (proposalId == null) {
						String sessionId = controlDb.getCurrentSessionId();
						proposalId = proposalDb.findProposalId(sessionId);
			    	}
					boolean allowAccess = true;
//					if (proposalId == null || proposalId.equals(currentProposal)) {
//						if (allowAccessCurrentPage(session, sessionId, proposalDb)) {
//							allowAccess = true;
//						}
//						proposalId = currentProposal;
//					} else {
//						if (allowEditHistoryProposal(session, proposalId)) {
//							allowAccess = true;
//						}
//					}
					if (allowAccess){
						CatalogDB catalogDb = CatalogDB.getInstance(proposalId);
						String columns = form.getValues(QUERY_ENTRY_COLUMNS);
						String key = form.getValues(QUERY_ENTRY_KEY);
//						String[] valueArray = values.split(",");
						JSONObject json = new JSONObject(columns);
						catalogDb.updateEntry(key, json);
						response.setEntity("Done", MediaType.TEXT_PLAIN);
						response.setStatus(Status.SUCCESS_OK);
					}
				} catch (Exception e) {
					e.printStackTrace();
					response.setEntity("<span style=\"color:red\">Error appending to catalogue database: " 
								+ e.getMessage() + ".</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			} else if (SEG_NAME_READ.equalsIgnoreCase(seg)) {
				try {
					Form form = request.getResourceRef().getQueryAsForm();
					boolean allowAccess = false;
					String sessionId = form.getValues(QUERY_ENTRY_SESSIONID);
					String proposalId = form.getValues(QUERY_ENTRY_PROPOSALID);
					String currentSessionId = controlDb.getCurrentSessionId();
					String currentProposal = proposalDb.findProposalId(currentSessionId);
					if (proposalId != null && !proposalId.equals("undefined")) {
						if (allowReadHistoryProposal(session, proposalId, proposalDb)) {
							allowAccess = true;
						} else {
							if (proposalId.equals(currentProposal)) {
								if (allowAccessCurrentPage(session, sessionId, proposalDb)){
									allowAccess = true;
								}
							}
						}
					} else if (sessionId != null) {
						proposalId = proposalDb.findProposalId(sessionId);
						if (allowReadHistoryProposal(session, proposalId, proposalDb)) {
							allowAccess = true;
						}
					} else {
						sessionId = currentSessionId;
						proposalId = currentProposal;
//						if (allowReadHistoryProposal(session, proposalId, proposalDb)) {
//							allowAccess = true;
//						}
						if (allowAccessCurrentPage(session, sessionId, proposalDb)){
							allowAccess = true;
						}
					}
					if (allowAccess){
						JSONArray headerArray;
						JSONArray entryArray = new JSONArray();
						JSONObject jsonObject = new JSONObject();
						if (!CatalogDB.dbExist(proposalId)) {
							headerArray = new JSONArray(CatalogDB.getGenericColumnNames());
							jsonObject.put("header", headerArray);
							jsonObject.put("size", 0);
							jsonObject.put("body", entryArray);
						} else {
							CatalogDB catalogDb = CatalogDB.getInstance(proposalId);
							String key = form.getValues(QUERY_ENTRY_KEY);
							if (key != null) {
								List<String> columnNames = catalogDb.getColumnNames();
								headerArray = new JSONArray(columnNames);
								String value = catalogDb.getEntry(key);
								entryArray.put(value);
								jsonObject.put("header", headerArray);
								jsonObject.put("size", 1);
								jsonObject.put("body", entryArray);
							} else {
								String start = form.getValues(QUERY_ENTRY_START);
								if (start == null) {
									List<String> columnNames = catalogDb.getColumnNames();
									headerArray = new JSONArray(columnNames);
									LinkedHashMap<String, Object> items = catalogDb.getAll();
									for (String entryKey : items.keySet()) {
										//									html += item.toString();
										JSONObject json = new JSONObject(items.get(entryKey).toString());
										json.put("_key_", entryKey);
										entryArray.put(json);
									}
									jsonObject.put("size", items.size());
									jsonObject.put("header", headerArray);
									jsonObject.put("body", entryArray);
								} else {
									String timestamp = form.getValues(QUERY_ENTRY_TIMESTAMP);
									LinkedHashMap<String, Object> items = catalogDb.getNew(Integer.valueOf(start), timestamp);
									for (String entryKey : items.keySet()) {
										JSONObject json = new JSONObject(items.get(entryKey).toString());
										json.put("_key_", entryKey);
										entryArray.put(json);
									}
									jsonObject.put("size", items.size());
									jsonObject.put("body", entryArray);
								}
							}
						}
						jsonObject.put("timestamp", System.currentTimeMillis());
						jsonObject.put("status", "OK");
						jsonObject.put("proposal", proposalId);
						jsonObject.put("current_proposal", currentProposal);
						response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
//						response.setEntity(html, MediaType.TEXT_PLAIN);
						response.setStatus(Status.SUCCESS_OK);
					} else {
						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Error: invalid user session.</span>");
					}
				} catch (Exception e) {
					response.setEntity("<span style=\"color:red\">Error reading from catalogue database: " 
								+ e.getMessage() + ".</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			} else if (SEG_NAME_LIST.equalsIgnoreCase(seg)) {
				try {
					boolean allowAccess = isManager(session);
					if (allowAccess){
						String sessionId = controlDb.getCurrentSessionId();
						String currentProposal = proposalDb.findProposalId(sessionId);
						List<String> dbList = CatalogDB.listDbNames();
						List<String> copyList = new ArrayList<String>(dbList);
						Collections.sort(copyList, new Comparator<String>() {

							@Override
							public int compare(String o1, String o2) {
								try {
									return Integer.compare(Integer.valueOf(o2), Integer.valueOf(o1));
								} catch (Exception e) {
									return -1;
								}
							}
						});
						JSONArray dbArray = new JSONArray(dbList);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("size", dbList.size());
						jsonObject.put("lastModified", dbArray);
						jsonObject.put("valueOrdered", copyList);
						jsonObject.put("currentProposal", currentProposal);
						jsonObject.put("status", "OK");
						response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
						response.setStatus(Status.SUCCESS_OK);
					} else {
						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Error: invalid user session.</span>");
					}
				} catch (Exception e) {
					response.setEntity("<span style=\"color:red\">Error reading from catalogue database: " 
								+ e.getMessage() + ".</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
					return;
				}
			} else if (SEG_NAME_HELP.equals(seg)) {
				try {
					File helpFile = new File(helpFilePath);
					if (helpFile.exists()) {
						byte[] bytes = Files.readAllBytes(Paths.get(helpFilePath));
						response.setEntity(new String(bytes), MediaType.TEXT_HTML);
					} else {
						response.setEntity("can't find the help file.", MediaType.TEXT_PLAIN);
					}
				} catch (IOException e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			} 
		} else {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Error: invalid user session.</span>");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {
		// TODO Auto-generated method stub

	}

}
