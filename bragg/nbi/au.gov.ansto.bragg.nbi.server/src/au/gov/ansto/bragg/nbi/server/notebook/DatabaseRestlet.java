package au.gov.ansto.bragg.nbi.server.notebook;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.ControlDB;
import org.gumtree.service.db.LoggingDB;
import org.gumtree.service.db.SessionDB;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

public class DatabaseRestlet extends Restlet implements IDisposable {

	public DatabaseRestlet() {
		this(null);
	}

	private final static String SEG_NAME_APPEND = "append";
	private final static String SEG_NAME_NEW = "new";
	private final static String SEG_NAME_CLOSE = "close";
	private final static String QUERY_ID_KEY = "key";
	private final static String QUERY_ID_HTML = "html";
	private final static String QUERY_SESSION_ID = "session";
//	private final static String NOTEBOOK_DBFILENAME = "loggingDB.rdf";
//	private final static String PROP_DATABASE_SAVEPATH = "gumtree.loggingDB.savePath";
	
//	private String currentDBPath;
	
	private SessionDB sessionDb;
	private ControlDB controlDb;
	
	/**
	 * @param context
	 */
	public DatabaseRestlet(Context context) {
		super(context);
		sessionDb = SessionDB.getInstance();
		controlDb = ControlDB.getInstance();
//		currentDBPath = System.getProperty(PROP_DATABASE_SAVEPATH) + "/" + NOTEBOOK_DBFILENAME;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {
	}
	
	@Override
	public void handle(final Request request, final Response response) {
		
//		Form queryForm = request.getResourceRef().getQueryAsForm();
		String seg = request.getResourceRef().getLastSegment();
		if (SEG_NAME_APPEND.equals(seg)) {
		    Representation entity = request.getEntity();
	    	Form form = new Form(entity);
	    	String key = form.getValues(QUERY_ID_KEY);
	    	String html = form.getValues(QUERY_ID_HTML);
	    	try {
		    	String sessionId = controlDb.getCurrentSessionId();
		    	String dbName = sessionDb.getSessionValue(sessionId);
				LoggingDB db = LoggingDB.getInstance(dbName);
				db.appendHtmlEntry(key, html);
			} catch (Exception e) {
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
		} else if (SEG_NAME_CLOSE.equals(seg)) {
			try {
				String sessionId = controlDb.getCurrentSessionId();
		    	String dbName = sessionDb.getSessionValue(sessionId);
				LoggingDB db = LoggingDB.getInstance(dbName);
				db.close();
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
	    return;
	}
	
}
