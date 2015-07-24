package au.gov.ansto.bragg.nbi.server.notebook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.ControlDB;
import org.gumtree.service.db.HtmlSearchHelper;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.db.SessionDB;
import org.gumtree.service.db.TextDb;
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

public class NotebookRestlet extends Restlet implements IDisposable {

	public NotebookRestlet() {
		this(null);
	}

	private final static String SEG_NAME_SAVE = "save";
	private final static String SEG_NAME_LOAD = "load";
	private final static String SEG_NAME_HELP = "help";
	private final static String SEG_NAME_SEARCH = "search";
	private final static String SEG_NAME_MANAGEGUIDE = "manageguide";
	private final static String SEG_NAME_DB = "db";
	private final static String SEG_NAME_NEW = "new";
	private final static String SEG_NAME_ARCHIVE = "archive";
	private final static String SEG_NAME_TEMPLATE = "template";
	private final static String STRING_CONTENT_START = "content=";
	private final static String PREFIX_NOTEBOOK_FILES = "Page_";
	private final static String PROP_NOTEBOOK_SAVEPATH = "gumtree.notebook.savePath";
	private final static String PROP_DATABASE_SAVEPATH = "gumtree.loggingDB.savePath";
	private final static String NOTEBOOK_TEMPLATEFILENAME = "template.xml";
	private final static String NOTEBOOK_HELPFILENAME = "guide.xml";
	private final static String NOTEBOOK_MANAGEHELP_FILENAME = "ManagerUsersGuide.xml";
//	private final static String NOTEBOOK_DBFILENAME = "loggingDB.rdf";
	private static final String QUERY_ENTRY_START = "start";
	private static final String QUERY_ENTRY_LENGTH = "length";
	private final static String QUERY_SESSION_ID = "session";
	private static final String QUERY_PATTERN = "pattern";
	private static final String FILE_FREFIX = "<div class=\"class_div_search_file\" name=\"$filename\" session=\"$session\">";
	private static final String SPAN_SEARCH_RESULT_HEADER = "<h4>";
	private static final String DIV_END = "</div>";
	private static final String SPAN_END = "</h4>";
	
	private String currentFileFolder;
	private String currentDBFolder;
	private String templateFilePath;
	private String helpFilePath;
	private SessionDB sessionDb;
	private ControlDB controlDb;
	
	/**
	 * @param context
	 */
	public NotebookRestlet(Context context) {
		super(context);
		currentFileFolder = System.getProperty(PROP_NOTEBOOK_SAVEPATH);
		currentDBFolder = System.getProperty(PROP_DATABASE_SAVEPATH);
		templateFilePath = System.getProperty(PROP_NOTEBOOK_SAVEPATH) + "/" + NOTEBOOK_TEMPLATEFILENAME;
		helpFilePath = System.getProperty(PROP_NOTEBOOK_SAVEPATH) + "/" + NOTEBOOK_HELPFILENAME;
		sessionDb = SessionDB.getInstance();
		controlDb = ControlDB.getInstance();
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
		String ip = request.getClientInfo().getUpstreamAddress();
		if (SEG_NAME_SAVE.equals(seg)) {
//			String content = rForm.getValues("content");
			Representation rep = request.getEntity();
			FileWriter writer = null;
			Form queryForm = request.getResourceRef().getQueryAsForm();
		    String sessionId = queryForm.getValues(QUERY_SESSION_ID);
		    if (sessionId == null || sessionId.trim().length() == 0) {
				if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.")){
					response.setStatus(Status.SERVER_ERROR_INTERNAL, "The notebook page is not available to the public.");
					return;
				}
				try {
					sessionId = controlDb.getCurrentSessionId();
				} catch (Exception e2) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e2.toString());
				}
		    }
			try {
				String text = rep.getText();
				int start = text.indexOf(STRING_CONTENT_START) + STRING_CONTENT_START.length();
				int stop = text.indexOf("&_method=");
				text = text.substring(start, stop);
				text = URLDecoder.decode(text, "UTF-8");
	    		String sessionValue = sessionDb.getSessionValue(sessionId);
				writer = new FileWriter(currentFileFolder + "/" + sessionValue + ".xml");
				writer.write(text);
				writer.flush();
			} catch (Exception e1) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e1.toString());
				return;
			}finally {
				if (writer != null){
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		    JSONObject jsonObject = new JSONObject();
		    try {
		    	jsonObject.put("status", "OK");
		    	response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
		    } catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (SEG_NAME_LOAD.equals(seg)) {
			Form queryForm = request.getResourceRef().getQueryAsForm();
		    String sessionId = queryForm.getValues(QUERY_SESSION_ID);
		    String pattern = queryForm.getValues(QUERY_PATTERN);
		    if (sessionId == null || sessionId.trim().length() == 0) {
				if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.")){
					response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
					return;
				}
		    	try {
		    		sessionId = controlDb.getCurrentSessionId();
		    		String sessionValue = sessionDb.getSessionValue(sessionId);
		    		String filename = currentFileFolder + "/" + sessionValue + ".xml";
	    			if (pattern == null || pattern.trim().length() == 0) {
	    				byte[] bytes = Files.readAllBytes(Paths.get(filename));
	    				response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
	    			} else {
	    				HtmlSearchHelper helper = new HtmlSearchHelper(new File(filename));
	    				response.setEntity(helper.highlightSearch(pattern), MediaType.TEXT_PLAIN);
	    			}
		    	} catch (Exception e) {
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		return;
		    	}
		    } else {
		    	try {
		    		String filename = currentFileFolder + "/" + sessionDb.getSessionValue(sessionId) + ".xml";
		    		File current = new File(filename);
		    		if (current.exists()) {
		    			if (pattern == null || pattern.trim().length() == 0) {
		    				byte[] bytes = Files.readAllBytes(Paths.get(filename));
		    				response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
		    			} else {
		    				HtmlSearchHelper helper = new HtmlSearchHelper(new File(filename));
		    				response.setEntity(helper.highlightSearch(pattern), MediaType.TEXT_PLAIN);
		    			}
		    		}
		    	} catch (Exception e) {
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		return;
		    	}
		    }
		} else if (SEG_NAME_DB.equals(seg)) {
	    	Form form = request.getResourceRef().getQueryAsForm();
	    	String startValue = form.getValues(QUERY_ENTRY_START);
//		    String fileId = form.getValues(QUERY_SESSION_ID);
		    String sessionId = form.getValues(QUERY_SESSION_ID);
		    if (sessionId == null || sessionId.trim().length() == 0) {
				if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.")){
					response.setEntity("<span style=\"color:red\">The database is not available to the public.</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
					return;
				}
				try {
					sessionId = controlDb.getCurrentSessionId();
				} catch (Exception e1) {
					response.setEntity("<span style=\"color:red\">Error loading current notebook page. Please ask instrument scientist for help.</span>", 
							MediaType.TEXT_PLAIN);
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
					return;
				}
		    } 
	    	int start = 0;
	    	boolean isBeginning = false;
	    	if (startValue != null) {
	    		try {
			    	start = Integer.valueOf(startValue);
				} catch (Exception e) {
					start = 0;
				}
	    	} else {
	    		isBeginning = true;
	    	}
	    	final int length = Integer.valueOf(form.getValues(QUERY_ENTRY_LENGTH));
	    	TextDb db = null;
			try {
			    String dbName = sessionDb.getSessionValue(sessionId);
			    dbName = currentDBFolder + "/" + dbName + ".rdf";
			    File dbFile = new File(dbName);
			    if (!dbFile.exists()) {
			    	response.setEntity("", MediaType.TEXT_PLAIN);
			    	response.setStatus(Status.SUCCESS_OK);
			    	return;
			    }
				db = new TextDb(dbName, "r");
				String html = "";
				if (isBeginning) {
					html = db.getEntries(length);
				} else {
					html = db.getEntries(start, length);
				}
				response.setEntity(html, MediaType.TEXT_PLAIN);
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			} finally {
				if (db != null) {
					try {
						db.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (RecordsFileException e) {
						e.printStackTrace();
					}
				}
			}

		} else if (SEG_NAME_NEW.equals(seg)) {
			if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.")){
				response.setStatus(Status.SERVER_ERROR_INTERNAL, "Notebook management is not available to the public.");
				return;
			}
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
				String newName = PREFIX_NOTEBOOK_FILES + format.format(new Date());
				File newFile = new File(currentFileFolder + "/" + newName + ".xml");
				if (!newFile.createNewFile()) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, "failed to create new file");
					return;
				}
				String oldSession = controlDb.getCurrentSessionId();
				String oldName = sessionDb.getSessionValue(oldSession);
				String sessionId = sessionDb.createNewSessionId(newName);
				controlDb.updateCurrentSessionId(sessionId);
				response.setEntity(oldName + ":" + oldSession + ";" + newName + ":" + sessionId, MediaType.TEXT_PLAIN);
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			}
		} else if (SEG_NAME_ARCHIVE.equals(seg)) {
			try {
				File current = new File(currentFileFolder);
				if (current.exists()) {
//					String[] fileList = current.list(new FilenameFilter() {
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
					List<String> sessionIds = sessionDb.listSessionIds();
					try {
						sessionIds.remove(controlDb.getCurrentSessionId());
					} catch (Exception e) {
					}
					String[] sessionPairs = new String[sessionIds.size()];
					int index = 0;
					for (String id : sessionIds) {
						sessionPairs[index++] = sessionDb.getSessionValue(id) + ":" + id;
					}
					Arrays.sort(sessionPairs);
					String responseText = "";
					for (int i = sessionPairs.length - 1; i >= 0; i--) {
						responseText += sessionPairs[i];
						if (i > 0){
							responseText += ";";
						}
					}
					response.setEntity(responseText, MediaType.TEXT_PLAIN);
				}
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			}
		} else if (SEG_NAME_TEMPLATE.equals(seg)) {
			try {
				File templateFile = new File(templateFilePath);
				if (templateFile.exists()) {
					byte[] bytes = Files.readAllBytes(Paths.get(templateFilePath));
					response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
				} else {
					response.setEntity("", MediaType.TEXT_PLAIN);
				}
			} catch (IOException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
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
		} else if (SEG_NAME_MANAGEGUIDE.equals(seg)) {
			try {
				String guidePath = currentFileFolder + "/" + NOTEBOOK_MANAGEHELP_FILENAME;
				File guideFile = new File(guidePath);
				if (guideFile.exists()) {
					byte[] bytes = Files.readAllBytes(Paths.get(guidePath));
					response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
				} else {
					response.setEntity("", MediaType.TEXT_PLAIN);
				}
			} catch (IOException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			}
		} else if (SEG_NAME_SEARCH.equals(seg)) {
			try {
//				String guidePath = currentFileFolder + "/" + NOTEBOOK_MANAGEHELP_FILENAME;
//				File guideFile = new File(guidePath);
//				if (guideFile.exists()) {
//					byte[] bytes = Files.readAllBytes(Paths.get(guidePath));
//					response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
//				} else {
//					response.setEntity("", MediaType.TEXT_PLAIN);
//				}
				Form queryForm = request.getResourceRef().getQueryAsForm();
				String pattern = queryForm.getValues(QUERY_PATTERN);
				if (pattern.trim().length() == 0) {
					response.setEntity("Please input a valid pattern", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
					return;
				}
				File current = new File(currentFileFolder);
				if (current.exists()) {
					List<String> sessionIds = sessionDb.listSessionIds();
					String[] sessionPairs = new String[sessionIds.size()];
					int index = 0;
					for (String id : sessionIds) {
						sessionPairs[index++] = sessionDb.getSessionValue(id) + ":" + id;
//						sessionPairs[index++] = sessionDb.getSessionValue(id);
					}
					Arrays.sort(sessionPairs);
					String responseText = "";
					for (int i = sessionPairs.length - 1; i >= 0; i--) {
//						responseText += sessionPairs[i];
//						if (i > 0){
//							responseText += ";";
//						}
						String[] pair = sessionPairs[i].split(":");
						String filename = currentFileFolder + "/" + pair[0] + ".xml";
						HtmlSearchHelper helper = new HtmlSearchHelper(new File(filename));
						String searchRes = helper.search(pattern);
						if (searchRes.length() > 0){
							searchRes = FILE_FREFIX.replace("$filename", pair[0]).replace("$session", pair[1]) 
									+ SPAN_SEARCH_RESULT_HEADER + pair[0] + SPAN_END + searchRes + DIV_END;
						}
						responseText += searchRes;
					}
					response.setEntity(responseText, MediaType.TEXT_PLAIN);
				}
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			}
		}
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
