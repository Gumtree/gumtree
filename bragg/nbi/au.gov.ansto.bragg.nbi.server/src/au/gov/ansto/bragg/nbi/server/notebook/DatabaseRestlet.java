package au.gov.ansto.bragg.nbi.server.notebook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.ControlDB;
import org.gumtree.service.db.LoggingDB;
import org.gumtree.service.db.ProposalDB;
import org.gumtree.service.db.SessionDB;
import org.json.JSONArray;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.server.internal.UserSessionService;
import au.gov.ansto.bragg.nbi.server.login.UserSessionObject;

public class DatabaseRestlet extends Restlet implements IDisposable {

	public DatabaseRestlet() {
		this(null);
	}

	private final static String SEG_NAME_APPEND = "append";
	private final static String SEG_NAME_NEW = "new";
	private final static String SEG_NAME_CLOSE = "close";
	private final static String SEG_NAME_SEARCH = "search";
	private final static String SEG_NAME_SEARCH_ALL = "searchAll";
	private final static String SEG_NAME_SEARCH_MINE = "searchMine";
	private final static String QUERY_ID_KEY = "key";
	private final static String QUERY_ID_HTML = "html";
	private final static String QUERY_SESSION_ID = "session";
//	private final static String NOTEBOOK_DBFILENAME = "loggingDB.rdf";
//	private final static String PROP_DATABASE_SAVEPATH = "gumtree.loggingDB.savePath";
	private static final String PROPERTY_NOTEBOOK_DAVIP = "gumtree.notebook.dav";
	private static final String PROPERTY_NOTEBOOK_IPPREFIX = "137.157.";
	private static final String QUERY_PATTERN = "pattern";
	private static final String FILE_FREFIX = "<div class=\"class_div_search_db\" name=\"$filename\" session=\"$session\">";
	private static final String SPAN_SEARCH_RESULT_HEADER = "<h4>Proposal ";
	private static final String DIV_END = "</div>";
	private static final String SPAN_END = "</h4>";
	private static final Logger logger = LoggerFactory.getLogger(DatabaseRestlet.class);
//	private String currentDBPath;
	
	private SessionDB sessionDb;
	private ControlDB controlDb;
	private ProposalDB proposalDb;
	private String[] allowedDavIps;

	/**
	 * @param context
	 */
	public DatabaseRestlet(Context context) {
		super(context);
		sessionDb = SessionDB.getInstance();
		controlDb = ControlDB.getInstance();
		proposalDb = ProposalDB.getInstance();
		String ips = System.getProperty(PROPERTY_NOTEBOOK_DAVIP);
		if (ips != null) {
			allowedDavIps = ips.split(",");
			for (int i = 0; i < allowedDavIps.length; i++) {
				allowedDavIps[i] = PROPERTY_NOTEBOOK_IPPREFIX + allowedDavIps[i].replaceAll("/", ".").trim();
			}
		}
//		currentDBPath = System.getProperty(PROP_DATABASE_SAVEPATH) + "/" + NOTEBOOK_DBFILENAME;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {
	}
	
	private UserSessionObject checkDavSession(Request request) {
		if (allowedDavIps == null) {
			return null;
		}
		String directIp = request.getClientInfo().getUpstreamAddress();
		if (directIp != null) {
			if (directIp != null && directIp.equals("127.0.0.1")) {
				UserSessionObject session = new UserSessionObject();
				session.setDAV(true);
				session.setUserName(System.getenv("gumtree.instrument.id"));
				session.setValid(true);
				return session;
			}
			for (int i = 0; i < allowedDavIps.length; i++) {
				if (directIp.equals(allowedDavIps[i])) {
					UserSessionObject session = new UserSessionObject();
					session.setDAV(true);
					session.setUserName(System.getenv("gumtree.instrument.id"));
					session.setValid(true);
					return session;
				}
			}
		}
		Object header = request.getAttributes().get("org.restlet.http.headers");
		if (header != null) {
			Form qform = (Form) header;
			String forwardedIp = qform.getFirstValue("X-Forwarded-For");
			if (forwardedIp != null) {
				if (forwardedIp.contains(",")) {
					forwardedIp = forwardedIp.split(",")[0].trim();
				} else {
					forwardedIp = forwardedIp.split(" ")[0].trim();
				}
				for (int i = 0; i < allowedDavIps.length; i++) {
					if (forwardedIp.equals(allowedDavIps[i])) {
						UserSessionObject session = new UserSessionObject();
						session.setDAV(true);
						session.setUserName(System.getenv("gumtree.instrument.id"));
						session.setValid(true);
						return session;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public void handle(final Request request, final Response response) {
		
		UserSessionObject session = null;
		
		try {
			session = UserSessionService.getSession(request, response);
//			isSessionValid = UserSessionService.controlSession(request, response);
		} catch (Exception e1) {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, e1.toString());
			return;
		}
		
		if (session == null || !session.isValid()) {
			try {
				session = checkDavSession(request);
			} catch (Exception e) {
			}
		}

		if (session != null && session.isValid()) {
	//		Form queryForm = request.getResourceRef().getQueryAsForm();
			String seg = request.getResourceRef().getLastSegment();
			if (SEG_NAME_APPEND.equals(seg)) {
			    Representation entity = request.getEntity();
		    	Form form = new Form(entity);
		    	String key = form.getValues(QUERY_ID_KEY);
		    	String html = form.getValues(QUERY_ID_HTML);
		    	try {
			    	String sessionId = controlDb.getCurrentSessionId();
	//		    	String dbName = sessionDb.getSessionValue(sessionId);
			    	String dbName = proposalDb.findProposalId(sessionId);
					LoggingDB db = LoggingDB.getInstance(dbName);
					db.appendHtmlEntry(key, html);
				} catch (Exception e) {
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				}
			} else if (SEG_NAME_CLOSE.equals(seg)) {
				try {
					if (!NotebookRestlet.isManager(session)) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, "Error: your privilege does not allow closing this DB.");
						return;
					}
					String sessionId = controlDb.getCurrentSessionId();
	//		    	String dbName = sessionDb.getSessionValue(sessionId);
					String dbName = proposalDb.findProposalId(sessionId);
					LoggingDB db = LoggingDB.getInstance(dbName);
					db.close();
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			} else if (SEG_NAME_SEARCH.equals(seg)) {
				try {
					Form queryForm = request.getResourceRef().getQueryAsForm();
					String pattern = queryForm.getValues(QUERY_PATTERN);
					if (pattern.trim().length() == 0) {
						response.setEntity("Please input a valid pattern", MediaType.TEXT_PLAIN);
						response.setStatus(Status.SUCCESS_OK);
						return;
					}
					String sessionId = queryForm.getValues(QUERY_SESSION_ID);
					if (sessionId == null || sessionId.trim().length() == 0) {
				    	sessionId = controlDb.getCurrentSessionId();
				    	if (!NotebookRestlet.allowAccessCurrentPage(session, sessionId, proposalDb)){
							response.setStatus(Status.SERVER_ERROR_INTERNAL, "Error: your privilege does not allow editing the current page.");
							return;
						}
					} else {
						if (!NotebookRestlet.allowReadHistoryPage(session, sessionId, proposalDb)){
							response.setStatus(Status.SERVER_ERROR_INTERNAL, "<span style=\"color:red\">Error: your privilege does not allow accessing this page.</span>");
							return;
						}
					}
	//		    	String dbName = sessionDb.getSessionValue(sessionId);
					String dbName = proposalDb.findProposalId(sessionId);
					LoggingDB db = LoggingDB.getInstance(dbName);
					String searchRes = db.search(pattern);
					if (searchRes.length() > 0){
						searchRes = FILE_FREFIX.replace("$filename", dbName).replace("$session", sessionId) 
								+ searchRes + DIV_END;
					}
					response.setEntity(searchRes, MediaType.TEXT_PLAIN);
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			} else if (SEG_NAME_SEARCH_ALL.equals(seg)) {
				try {
					if (!NotebookRestlet.isManager(session)) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, "Error: your privilege does not allow this.");
						return;
					}
					Form queryForm = request.getResourceRef().getQueryAsForm();
					String pattern = queryForm.getValues(QUERY_PATTERN);
					if (pattern.trim().length() == 0) {
						response.setEntity("Please input a valid pattern", MediaType.TEXT_PLAIN);
						response.setStatus(Status.SUCCESS_OK);
						return;
					}
					
	//				List<String> sessionIds = sessionDb.listSessionIds();
	//				String[] sessionPairs = new String[sessionIds.size()];
	//				int index = 0;
	//				for (String id : sessionIds) {
	////					sessionPairs[index++] = sessionDb.getSessionValue(id) + ":" + id;
	//					sessionPairs[index++] = proposalDb.findProposalId(id) + ":" + id;
	//					//						sessionPairs[index++] = sessionDb.getSessionValue(id);
	//				}
					List<String> proposalIds = proposalDb.listProposalIds();
					String[] sessionPairs = new String[proposalIds.size()];
					int index = 0;
					for (String proposalId : proposalIds) {
						String sessionId = proposalDb.getLastSessionId(proposalId);
						if (sessionId != null) {
							sessionPairs[index++] = proposalId + ":" + proposalDb.getLastSessionId(proposalId);						
						}
					}
					Arrays.sort(sessionPairs);
					String responseText = "";
					for (int i = sessionPairs.length - 1; i >= 0; i--) {
						String[] pair = sessionPairs[i].split(":");
	
						LoggingDB db = LoggingDB.getInstance(pair[0]);
						
	//					HtmlSearchHelper helper = new HtmlSearchHelper(new File(filename));
						String searchRes = db.search(pattern);
						if (searchRes.length() > 0){
							searchRes = FILE_FREFIX.replace("$filename", pair[0]).replace("$session", pair[1]) 
									+ SPAN_SEARCH_RESULT_HEADER + pair[0] + SPAN_END + searchRes + DIV_END;
						}
						responseText += searchRes;
					}
					response.setEntity(responseText, MediaType.TEXT_PLAIN);
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			} else if (SEG_NAME_SEARCH_MINE.equals(seg)) {
				try {
					if (!NotebookRestlet.isManager(session)) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, "Error: your privilege does not allow this.");
						return;
					}
					Form queryForm = request.getResourceRef().getQueryAsForm();
					String pattern = queryForm.getValues(QUERY_PATTERN);
					if (pattern.trim().length() == 0) {
						response.setEntity("Please input a valid pattern", MediaType.TEXT_PLAIN);
						response.setStatus(Status.SUCCESS_OK);
						return;
					}
					
	//				List<String> sessionIds = sessionDb.listSessionIds();
	//				String[] sessionPairs = new String[sessionIds.size()];
	//				int index = 0;
	//				for (String id : sessionIds) {
	////					sessionPairs[index++] = sessionDb.getSessionValue(id) + ":" + id;
	//					sessionPairs[index++] = proposalDb.findProposalId(id) + ":" + id;
	//					//						sessionPairs[index++] = sessionDb.getSessionValue(id);
	//				}
					JSONArray proposalArray = UserSessionService.getProposals(session);
					List<String> proposalIds = new ArrayList<String>();
					for (int i = 0; i < proposalArray.length(); i++) {
						proposalIds.add(proposalArray.getString(i));
					}
					String[] sessionPairs = new String[proposalIds.size()];
					int index = 0;
					for (String proposalId : proposalIds) {
						String sessionId = proposalDb.getLastSessionId(proposalId);
						if (sessionId != null) {
							sessionPairs[index++] = proposalId + ":" + proposalDb.getLastSessionId(proposalId);						
						}
					}
					Arrays.sort(sessionPairs);
					String responseText = "";
					for (int i = sessionPairs.length - 1; i >= 0; i--) {
						String[] pair = sessionPairs[i].split(":");
	
						LoggingDB db = LoggingDB.getInstance(pair[0]);
						
	//					HtmlSearchHelper helper = new HtmlSearchHelper(new File(filename));
						String searchRes = db.search(pattern);
						if (searchRes.length() > 0){
							searchRes = FILE_FREFIX.replace("$filename", pair[0]).replace("$session", pair[1]) 
									+ SPAN_SEARCH_RESULT_HEADER + pair[0] + SPAN_END + searchRes + DIV_END;
						}
						responseText += searchRes;
					}
					response.setEntity(responseText, MediaType.TEXT_PLAIN);
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			}
//		else if (SEG_NAME_NEW.equals(seg)) {
//			try {
//				LoggingDB.getInstance().archive();
//			} catch (Exception e) {
//				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//				return;
//			}
//		}
		
//		else if (SEG_NAME_LOAD.equals(seg)) {
//			Form queryForm = request.getResourceRef().getQueryAsForm();
//		    String fileId = queryForm.getValues(QUERY_FILE_ID);
//		    if (fileId == null || fileId.trim().length() == 0) {
//		    	try {
//		    		File current = new File(currentFilePath);
//		    		if (current.exists()) {
//		    			byte[] bytes = Files.readAllBytes(Paths.get(currentFilePath));
//		    			response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
//		    		}
//		    	} catch (IOException e) {
//		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//		    		return;
//		    	}
//		    } else {
//		    	try {
//		    		File current = new File(currentFilePath);
//		    		if (current.exists()) {
//		    			String filename = current.getParent() + "/" + fileId + ".xml"; 
//		    			byte[] bytes = Files.readAllBytes(Paths.get(filename));
//		    			response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
//		    		}
//		    	} catch (IOException e) {
//		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//		    		return;
//		    	}
//		    }
//		} else if (SEG_NAME_DB.equals(seg)) {
////			try {
////				File current = new File(currentDBPath);
////				if (current.exists()) {
////					byte[] bytes = Files.readAllBytes(Paths.get(currentDBPath));
////					response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
////				}
////			} catch (IOException e) {
////				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
////				return;
////			}
//	    	Form form = request.getResourceRef().getQueryAsForm();
//	    	String startValue = form.getValues(QUERY_ENTRY_START);
//	    	int start = 0;
//	    	boolean isBeginning = false;
//	    	if (startValue != null) {
//		    	start = Integer.valueOf(startValue);
//	    	} else {
//	    		isBeginning = true;
//	    	}
//	    	final int length = Integer.valueOf(form.getValues(QUERY_ENTRY_LENGTH));
//	    	TextDb db = null;
//			try {
//				db = new TextDb(currentDBPath, "r");
//				String html = "";
//				if (isBeginning) {
//					html = db.getEntries(length);
//				} else {
//					html = db.getEntries(start, length);
//				}
//				response.setEntity(html, MediaType.TEXT_PLAIN);
//			} catch (Exception e) {
//				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//				return;
//			} finally {
//				if (db != null) {
//					try {
//						db.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					} catch (RecordsFileException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		} 
//			else if (SEG_NAME_ARCHIVE.equals(seg)) {
//			try {
//				File current = new File(currentFilePath);
//				if (current.exists()) {
//					File parent = current.getParentFile();
//					String[] fileList = parent.list(new FilenameFilter() {
//						
//						@Override
//						public boolean accept(File dir, String name) {
//							if (name.startsWith(PREFIX_NOTEBOOK_FILES)){
//								return true;
//							}
//							return false;
//						}
//					});
//					Arrays.sort(fileList);
//					String responseText = "";
//					for (int i = fileList.length - 1; i >= 0; i--) {
//						responseText += fileList[i].substring(0, fileList[i].length() - 4);
//						if (i > 0){
//							responseText += ":";
//						}
//					}
//					response.setEntity(responseText, MediaType.TEXT_PLAIN);
//				}
//			} catch (Exception e) {
//				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//				return;
//			}
//		} 
			response.setStatus(Status.SUCCESS_OK);
//	    String typeString = queryForm.getValues(QUERY_TYPE);
//	    JSONObject jsonObject = new JSONObject();
//	    try {
//	    	jsonObject.put("status", 1);
//	    	response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
//	    	response.setStatus(Status.SUCCESS_OK);
//	    } catch (JSONException e) {
//	    	e.printStackTrace();
//	    	response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//	    }
		} else {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, "<span style=\"color:red\">Error: invalid user session.</span>");
		}
	    return;
	}
	
}
