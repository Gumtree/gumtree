package au.gov.ansto.bragg.nbi.server.notebook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.ControlDB;
import org.gumtree.service.db.HtmlSearchHelper;
import org.gumtree.service.db.LoggingDB;
import org.gumtree.service.db.ProposalDB;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.db.SessionDB;
import org.gumtree.service.db.TextDb;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;

import au.gov.ansto.bragg.nbi.service.soap.ProposalDBSOAPService;
import au.gov.ansto.bragg.nbi.service.soap.ProposalDBSOAPService.ProposalItems;

public class NotebookRestlet extends Restlet implements IDisposable {

	public NotebookRestlet() {
		this(null);
	}

	private final static String SEG_NAME_SAVE = "save";
	private final static String SEG_NAME_LOAD = "load";
	private final static String SEG_NAME_HELP = "help";
	private final static String SEG_NAME_CURRENTPAGE = "currentpage";
	private final static String SEG_NAME_SEARCH = "search";
	private final static String SEG_NAME_MANAGEGUIDE = "manageguide";
	private final static String SEG_NAME_DB = "db";
	private final static String SEG_NAME_NEW = "new";
	private final static String SEG_NAME_PDF = "pdf";
	private final static String SEG_NAME_DOWNLOAD = "download";
	private final static String SEG_NAME_ARCHIVE = "archive";
	private final static String SEG_NAME_TEMPLATE = "template";
	private final static String STRING_CONTENT_START = "content=";
	private final static String PREFIX_NOTEBOOK_FILES = "Page_";
	private final static String PROP_NOTEBOOK_SAVEPATH = "gumtree.notebook.savePath";
	private final static String PROP_DATABASE_SAVEPATH = "gumtree.loggingDB.savePath";
	private final static String PROP_PDF_FOLDER = "gumtree.notebook.pdfPath";
	private final static String NOTEBOOK_TEMPLATEFILENAME = "template.xml";
	private final static String NOTEBOOK_HELPFILENAME = "guide.xml";
	private final static String NOTEBOOK_MANAGEHELP_FILENAME = "ManagerUsersGuide.xml";
//	private final static String NOTEBOOK_DBFILENAME = "loggingDB.rdf";
	private static final String QUERY_ENTRY_START = "start";
	private static final String QUERY_ENTRY_LENGTH = "length";
	private final static String QUERY_SESSION_ID = "session";
	private final static String QUERY_PAGE_ID = "page";
	private final static String QUERY_EXTNAME_ID = "ext";
	private static final String QUERY_PATTERN = "pattern";
	private static final String QUERY_PROPOSAL_ID = "proposal_id";
	private static final String FILE_FREFIX = "<div class=\"class_div_search_file\" name=\"$filename\" session=\"$session\" proposal=\"$proposal\">";
	private static final String SPAN_SEARCH_RESULT_HEADER = "<h4>";
	private static final String DIV_END = "</div>";
	private static final String SPAN_END = "</h4>";
	private static final String EXPERIMENT_TABLE_HTML = "<div class=\"class_template_table class_template_object\" id=\"template_ec_1\">"
			+ "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" class=\"xmlTable\" style=\"table-layout:fixed; width:100%; word-wrap:break-word\"><caption>Experiment Setup</caption>"
			+ "<tbody><tr><th style=\"width: 40%;\">Start Date</th><td style=\"width: 60%;\">$START_DATE</td></tr><tr><th>End Date</th><td>$END_DATE</td></tr>"
			+ "<tr><th>Proposal #</th><td>$ID</td></tr><tr><th>Proposal Name</th><td>$EXPERIMENT_TITLE</td></tr>"
			+ "<tr><th>Principal Scientist</th><td>$PRINCIPAL_SCIENTIST</td></tr><tr><th>Email Address</th><td>$PRINCIPAL_EMAIL</td></tr>"
			+ "<tr><th>Local Contact</th><td>$LOCAL_CONTACT</td></tr>"
			+ "<tr><th>Experiment Description</th><td>$TEXT</td></tr><tr><th>Wavelength</th><td>A</td></tr>"
//			+ "<tr><th>Wavelength Resolution</th><td>0.10</td></tr><tr><th>Standard / High res&rsquo;n NVS</th><td>Standard</td></tr>"
//			+ "<tr><th>Apx softzero</th><td>&nbsp;</td></tr><tr><th>Samx softzero</th><td>&nbsp;</td></tr><tr><th>samy</th><td>&nbsp;</td></tr>"
//			+ "<tr><th>samz</th><td>&nbsp;</td></tr><tr><th>Sample Environment</th><td>&nbsp;</td></tr><tr><th>T / P / field set-point</th><td>&nbsp;</td></tr>"
//			+ "<tr><th>Cells used</th><td>&nbsp;</td></tr><tr><th>Sample alignment date</th><td>&nbsp;</td></tr><tr><th>Sensitivity file date</th><td>&nbsp;</td></tr>"
			+ "</tbody></table></div><p/>";
	
	private String currentFileFolder;
	private String currentDBFolder;
	private String templateFilePath;
	private String helpFilePath;
	private SessionDB sessionDb;
	private ControlDB controlDb;
	private ProposalDB proposalDb;
	private String pdfFolder;
	private NotebookPDFService pdfService;
	
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
		proposalDb = ProposalDB.getInstance();
		pdfFolder = System.getProperty(PROP_PDF_FOLDER);
		pdfService = new NotebookPDFService(pdfFolder);
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
		List<String> segList = request.getResourceRef().getSegments();
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
				text = text == null ? "" : text;
				if (text.contains(STRING_CONTENT_START)) {
					int start = text.indexOf(STRING_CONTENT_START) + STRING_CONTENT_START.length();
					int stop = text.indexOf("&_method=");
					text = text.substring(start, stop);
					text = URLDecoder.decode(text, "UTF-8");
				}
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
		} else if (SEG_NAME_PDF.equals(seg)) {
			Form queryForm = request.getResourceRef().getQueryAsForm();
		    String sessionId = queryForm.getValues(QUERY_SESSION_ID);
		    String sourceFilename = null;
    		String sessionValue = null;
    		String targetFilename = null;
			String expName = String.valueOf(System.currentTimeMillis());
		    if (sessionId == null || sessionId.trim().length() == 0) {
				if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.")){
					response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
					return;
				}
		    	try {
		    		sessionId = controlDb.getCurrentSessionId();
		    		sessionValue = sessionDb.getSessionValue(sessionId);
		    		sourceFilename = currentFileFolder + "/" + sessionValue + ".xml";
		    		targetFilename = sessionValue + "_" + expName + ".pdf";
		    	} catch (Exception e) {
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		return;
		    	}
		    } else {
		    	try {
		    		sessionValue = sessionDb.getSessionValue(sessionId);
		    		sourceFilename = currentFileFolder + "/" + sessionDb.getSessionValue(sessionId) + ".xml";
		    		targetFilename = sessionValue + "_" + expName + ".pdf";
		    	} catch (Exception e) {
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		return;
		    	}
		    }
    		File current = new File(sourceFilename);
    		if (current.exists()) {
    			String targetPath = pdfFolder + "/" + targetFilename;
    			try {
    				boolean isSuccessful = pdfService.createPDF(sourceFilename, targetPath);
    				if (isSuccessful) {
//    					FileRepresentation representation = new FileRepresentation(targetPath, MediaType.APPLICATION_ZIP);
//    					Disposition disposition = new Disposition();
//    					disposition.setFilename(targetFilename);
//    					representation.setDisposition(disposition);
//    					response.setEntity(representation);
    					response.setEntity(sessionValue + ":" + expName, MediaType.TEXT_PLAIN);
    				} else {
    					response.setStatus(Status.SERVER_ERROR_INTERNAL, "Time out creating the PDF file.");
    					return;
        			}
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
    		}
		} else if (SEG_NAME_CURRENTPAGE.equals(seg)) {
			if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.")){
				response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
				return;
			}
			try {
				String sessionId = controlDb.getCurrentSessionId();
				String sessionValue = sessionDb.getSessionValue(sessionId);
				String proposalId = proposalDb.findProposalId(sessionId);
				String filename = currentFileFolder + "/" + sessionValue + ".xml";
				byte[] bytes = Files.readAllBytes(Paths.get(filename));
				response.setEntity(sessionId + ":" + sessionValue + ":" + proposalId + ":" + new String(bytes), MediaType.TEXT_PLAIN);
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
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
//			    String dbName = sessionDb.getSessionValue(sessionId);
				String dbName = proposalDb.findProposalId(sessionId);
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
				Form form = request.getResourceRef().getQueryAsForm();
		    	String proposalId = form.getValues(QUERY_PROPOSAL_ID);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
				String newName = PREFIX_NOTEBOOK_FILES + format.format(new Date());
				File newFile = new File(currentFileFolder + "/" + newName + ".xml");
				if (!newFile.createNewFile()) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, "failed to create new file");
					return;
				}
				PrintWriter pw = null;
				String html = "";
				try {
					pw = new PrintWriter(new FileWriter(newFile));
					if (proposalId != null) {
						html += "<h1>Quokka Notebook Page: " + proposalId + "</h1><p/>";
						int proposalInt = 0;
						try {
							proposalInt = Integer.valueOf(proposalId);
						} catch (Exception e) {
						}
						if (proposalInt > 0) {
							Map<ProposalItems, String> proposalInfo = ProposalDBSOAPService.getProposalInfo(proposalInt, "quokka");
							String space = "&nbsp;";
							if (proposalInfo != null) {
								String tableHtml = EXPERIMENT_TABLE_HTML.replace("$" + ProposalItems.ID.name(), proposalId);
								String user = proposalInfo.get(ProposalItems.PRINCIPAL_SCIENTIST);
								if (user == null) {
									user = space;
								}
								tableHtml = tableHtml.replace("$" + ProposalItems.PRINCIPAL_SCIENTIST.name(), user);
								String email = proposalInfo.get(ProposalItems.PRINCIPAL_EMAIL);
								if (email == null) {
									email = space;
								}
								tableHtml = tableHtml.replace("$" + ProposalItems.PRINCIPAL_EMAIL.name(), email);
								String title = proposalInfo.get(ProposalItems.EXPERIMENT_TITLE);
								if (title == null) {
									title = space;
								}
								tableHtml = tableHtml.replace("$" + ProposalItems.EXPERIMENT_TITLE.name(), title);
								String text = proposalInfo.get(ProposalItems.TEXT);
								if (text == null) {
									text = space;
								}
								tableHtml = tableHtml.replace("$" + ProposalItems.TEXT.name(), text);
								String start = proposalInfo.get(ProposalItems.START_DATE);
								if (start == null) {
									start = space;
								}
								tableHtml = tableHtml.replace("$" + ProposalItems.START_DATE.name(), start);
								String end = proposalInfo.get(ProposalItems.END_DATE);
								if (end == null) {
									end = space;
								}
								tableHtml = tableHtml.replace("$" + ProposalItems.END_DATE.name(), end);
								String contact = proposalInfo.get(ProposalItems.LOCAL_CONTACT);
								if (contact == null) {
									contact = space;
								}
								tableHtml = tableHtml.replace("$" + ProposalItems.LOCAL_CONTACT.name(), contact);
								html += tableHtml;
							}
						}
					} else {
						html += "<h1>Quokka Notebook</h1><p/>";						
					}
					pw.write(html);
					pw.close();					
				} finally {
					if (pw != null) {
						pw.close();
					}
				}
				String oldSession = "";
				String oldName = "";
				String oldProposal = "";
				try {
					oldSession = controlDb.getCurrentSessionId();
					oldName = sessionDb.getSessionValue(oldSession);
					oldProposal = proposalDb.findProposalId(oldSession);
//					LoggingDB db = LoggingDB.getInstance(oldName);
//					db.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				String sessionId = sessionDb.createNewSessionId(newName);
				controlDb.updateCurrentSessionId(sessionId);
				if (proposalId != null) {
					try {
						proposalDb.putSession(proposalId, sessionId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				response.setEntity(oldSession + ":" + oldName + ":" + oldProposal + ";" + sessionId + ":" + newName + ":" + proposalId + "=" + html, MediaType.TEXT_PLAIN);
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
					String currentSessionId = controlDb.getCurrentSessionId();
					List<String> proposalIds = proposalDb.listProposalIds();
					Collections.sort(proposalIds);
					Collections.reverse(proposalIds);
					List<String> sessionIds = sessionDb.listSessionIds();
					JSONObject jsonObject = new JSONObject(new LinkedHashMap<String, String>());
					for (String proposalId : proposalIds) {
						JSONObject proposalObject = new JSONObject(new LinkedHashMap<String, String>());
						String sessions = proposalDb.getSessionIds(proposalId);
						if (sessions != null && sessions.trim().length() > 0) {
							String[] sessionArray = sessions.split(":");
							for (String sessionId : sessionArray) {
								if (!sessionId.equals(currentSessionId)) {
									String pageId = sessionDb.getSessionValue(sessionId);
									proposalObject.put(sessionId, pageId);
								}
								sessionIds.remove(sessionId);
							}
							if (proposalObject.length() > 0) {
								jsonObject.put(proposalId, proposalObject);
							}
						}
					}
					if (sessionIds.size() > 0) {
						JSONObject standaloneObject = new JSONObject(new LinkedHashMap<String, String>());
						for (String sessionId : sessionIds) {
							String pageId = sessionDb.getSessionValue(sessionId);
							standaloneObject.put(sessionId, pageId);
						}
						jsonObject.put("Stand Alone Pages", standaloneObject);
					}
//					try {
//						sessionIds.remove(controlDb.getCurrentSessionId());
//					} catch (Exception e) {
//					}
					response.setEntity(jsonObject.toString(), MediaType.TEXT_PLAIN);
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
							String proposalId = proposalDb.findProposalId(pair[1]);
							if (proposalId == null) {
								proposalId = "N/A";
							}
							searchRes = FILE_FREFIX.replace("$filename", pair[0]).replace("$session", pair[1]).replace("$proposal", proposalId) 
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
		} else if (seg.toLowerCase().endsWith(".pdf") && segList.size() > 1 && segList.get(segList.size() - 2).equals(SEG_NAME_DOWNLOAD)) {
			Form queryForm = request.getResourceRef().getQueryAsForm();
		    String sessionId = queryForm.getValues(QUERY_SESSION_ID);
		    String pageId = seg.replaceAll(".pdf", "");
		    String extName = queryForm.getValues(QUERY_EXTNAME_ID);
		    String targetFilename = null;
		    if (sessionId == null || sessionId.trim().length() == 0) {
				if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.")){
					response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SUCCESS_OK);
					return;
				} else {
					targetFilename = pageId + "_" + extName + ".pdf";
				}
		    } else {
		    	try {
		    		String sessionValue = sessionDb.getSessionValue(sessionId);
		    		if (!sessionValue.equals(pageId)) {
		    			response.setStatus(Status.SERVER_ERROR_INTERNAL, "<span style=\"color:red\">Illigal session.</span>");
			    		return;
		    		}
		    		targetFilename = pageId + "_" + extName + ".pdf";
		    	} catch (Exception e) {
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		return;
		    	}
		    }
			String targetPath = pdfFolder + "/" + targetFilename;
    		File current = new File(targetPath);
    		if (current.exists()) {
    			try {
    				FileRepresentation representation = new FileRepresentation(targetPath, MediaType.APPLICATION_ZIP);
    				Disposition disposition = new Disposition();
    				disposition.setFilename(targetFilename);
    				representation.setDisposition(disposition);
    				response.setEntity(representation);
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
    		} else {
    			response.setStatus(Status.SERVER_ERROR_INTERNAL, "File not found.");
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