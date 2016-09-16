package au.gov.ansto.bragg.nbi.server.notebook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.gumtree.core.object.IDisposable;
import org.gumtree.security.EncryptionUtils;
import org.gumtree.service.db.ControlDB;
import org.gumtree.service.db.HtmlSearchHelper;
import org.gumtree.service.db.ProposalDB;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.db.SessionDB;
import org.gumtree.service.db.TextDb;
import org.gumtree.service.httpclient.IHttpClient;
import org.gumtree.service.httpclient.IHttpClientCallback;
import org.gumtree.service.httpclient.IHttpClientFactory;
import org.gumtree.service.httpclient.support.HttpClientFactory;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.json.JSONArray;
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
import org.restlet.engine.util.Base64;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.server.git.GitService;
import au.gov.ansto.bragg.nbi.server.internal.UserSessionService;
import au.gov.ansto.bragg.nbi.server.login.UserSessionObject;
import au.gov.ansto.bragg.nbi.service.soap.ProposalDBSOAPService;
import au.gov.ansto.bragg.nbi.service.soap.ProposalDBSOAPService.ProposalItems;

public class NotebookRestlet extends Restlet implements IDisposable {

	public NotebookRestlet() {
		this(null);
	}

	private final static String SEG_NAME_SAVE = "save";
	private final static String SEG_NAME_LOAD = "load";
	private final static String SEG_NAME_HELP = "help";
	private final static String SEG_NAME_USER = "user";
	private final static String SEG_NAME_CURRENTPAGE = "currentpage";
	private final static String SEG_NAME_SEARCH = "search";
	private final static String SEG_NAME_SEARCHMINE = "searchMine";
	private final static String SEG_NAME_MANAGEGUIDE = "manageguide";
	private final static String SEG_NAME_MYGUIDE = "myguide";
	private final static String SEG_NAME_DB = "db";
	private final static String SEG_NAME_NEW = "new";
	private final static String SEG_NAME_PDF = "pdf";
	private final static String SEG_NAME_DOWNLOAD = "download";
	private final static String SEG_NAME_IMAGESERVICE = "imageService";
	private final static String SEG_NAME_ARCHIVE = "archive";
	private final static String SEG_NAME_MYARCHIVE = "myarchive";
	private final static String SEG_NAME_TEMPLATE = "template";
	private final static String SEG_NAME_LISTHISTORY = "history";
	private final static String STRING_CONTENT_START = "content=";
	private final static String PREFIX_NOTEBOOK_FILES = "Page_";
	private final static String PROP_INSTRUMENT_ID = "gumtree.instrument.id";
	private final static String PROP_NOTEBOOK_SAVEPATH = "gumtree.notebook.savePath";
	private final static String PROP_NOTEBOOK_TABLEEXTENSION = "gumtree.notebook.headerTableExtension";
	private final static String PROP_DATABASE_SAVEPATH = "gumtree.loggingDB.savePath";
	private final static String PROP_PDF_FOLDER = "gumtree.notebook.pdfPath";
	private final static String NOTEBOOK_TEMPLATEFILENAME = "template.xml";
	private final static String NOTEBOOK_HELPFILENAME = "guide.xml";
	private final static String NOTEBOOK_MANAGEHELP_FILENAME = "ManagerUsersGuide.xml";
	private final static String NOTEBOOK_MYGUIDE_FILENAME = "MyGuide.xml";
//	private final static String NOTEBOOK_DBFILENAME = "loggingDB.rdf";
	private static final String QUERY_ENTRY_START = "start";
	private static final String QUERY_ENTRY_LENGTH = "length";
	private final static String QUERY_SESSION_ID = "session";
	private final static String QUERY_PAGE_ID = "pageid";
	private final static String QUERY_EXTNAME_ID = "ext";
	private final static String QUERY_EXTERNAL_URL_ID = "url";
	private static final String QUERY_PATTERN = "pattern";
	private static final String QUERY_PROPOSAL_ID = "proposal_id";
	private static final String FILE_FREFIX = "<div class=\"class_div_search_file\" name=\"$filename\" session=\"$session\" proposal=\"$proposal\">";
	private static final String SPAN_SEARCH_RESULT_HEADER = "<h4>";
	private static final String DIV_END = "</div>";
	private static final String SPAN_END = "</h4>";
	private static final String ID_PROXY_HOST = "http.proxyHost";
	private static final String ID_PROXY_PORT = "http.proxyPort";
	private static final String ID_DAE_HOST = "gumtree.dae.host";
	private static final String ID_DAE_LOGIN = "gumtree.dae.login";
	private static final String ID_DAE_PASSWORD = "gumtree.dae.password";
	private static final String PROPERTY_NOTEBOOK_REPOSITORY_PATH = "gumtree.notebook.gitPath";
	private static final String PROPERTY_SERVER_REPORTPATH = "gumtree.server.reportPath";
	private static final String PROPERTY_NOTEBOOK_DAVIP = "gumtree.notebook.dav";
	private static final String PROPERTY_NOTEBOOK_ICSIP = "gumtree.notebook.ics";
	private static final String PROPERTY_NOTEBOOK_IPPREFIX = "137.157.";
	private static final String EXPERIMENT_TABLE_HTML_EXTENSION = "$EXTENSION";
	private static Logger logger = LoggerFactory.getLogger(NotebookRestlet.class);
	private static final String EXPERIMENT_TABLE_HTML = "<div class=\"class_template_table class_template_object\" id=\"template_ec_1\">"
			+ "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" class=\"xmlTable\" style=\"table-layout:fixed; width:100%; word-wrap:break-word\"><caption>Experiment Setup</caption>"
			+ "<tbody>"
			+ "<tr><th>Proposal #</th><td>$ID</td></tr><tr><th>Proposal Name</th><td>$EXPERIMENT_TITLE</td></tr>"
			+ "<tr><th style=\"width: 40%;\">Start Date</th><td style=\"width: 60%;\">$START_DATE</td></tr><tr><th>End Date</th><td>$END_DATE</td></tr>"
			+ "<tr><th>Principal Scientist</th><td>$PRINCIPAL_SCIENTIST</td></tr><tr><th>Email Address</th><td>$PRINCIPAL_EMAIL</td></tr>"
			+ "<tr><th>Local Contact</th><td>$LOCAL_CONTACT</td></tr>"
			+ "<tr><th>Experiment Description</th><td>$TEXT</td></tr>"
//			+ "<tr><th>Wavelength Resolution</th><td>0.10</td></tr><tr><th>Standard / High res&rsquo;n NVS</th><td>Standard</td></tr>"
//			+ "<tr><th>Apx softzero</th><td>&nbsp;</td></tr><tr><th>Samx softzero</th><td>&nbsp;</td></tr><tr><th>samy</th><td>&nbsp;</td></tr>"
//			+ "<tr><th>samz</th><td>&nbsp;</td></tr><tr><th>Sample Environment</th><td>&nbsp;</td></tr><tr><th>T / P / field set-point</th><td>&nbsp;</td></tr>"
//			+ "<tr><th>Cells used</th><td>&nbsp;</td></tr><tr><th>Sample alignment date</th><td>&nbsp;</td></tr><tr><th>Sensitivity file date</th><td>&nbsp;</td></tr>"
			+ "$EXTENSION"
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
	private IHttpClient externalHttpClient;
	private IHttpClient internalHttpClient;
	private String daeHost;
	private String daeLogin;
	private String daePassword;
	private GitService gitService;
	private String instrumentId;
	private String[] allowedDavIps;
	private String[] allowedIcsIps;
	
	/**
	 * @param context
	 */
	public NotebookRestlet(Context context) {
		super(context);
		instrumentId = System.getProperty(PROP_INSTRUMENT_ID);
		currentFileFolder = System.getProperty(PROP_NOTEBOOK_SAVEPATH);
		currentDBFolder = System.getProperty(PROP_DATABASE_SAVEPATH);
		templateFilePath = System.getProperty(PROP_NOTEBOOK_SAVEPATH) + "/" + NOTEBOOK_TEMPLATEFILENAME;
		helpFilePath = System.getProperty(PROP_NOTEBOOK_SAVEPATH) + "/" + NOTEBOOK_HELPFILENAME;
		sessionDb = SessionDB.getInstance();
		controlDb = ControlDB.getInstance();
		proposalDb = ProposalDB.getInstance();
		pdfFolder = System.getProperty(PROP_PDF_FOLDER);
		pdfService = new NotebookPDFService(pdfFolder);
		IHttpClientFactory clienntFactory = new HttpClientFactory();
		externalHttpClient = clienntFactory.createHttpClient(1);
		internalHttpClient = clienntFactory.createHttpClient(1);
		String proxyHost = System.getProperty(ID_PROXY_HOST);
		String proxyPort = System.getProperty(ID_PROXY_PORT);
		if (proxyHost != null && proxyPort != null) {
			externalHttpClient.setProxy(proxyHost, Integer.valueOf(proxyPort));
			internalHttpClient.setProxy(proxyHost, Integer.valueOf(proxyPort));
		}
		daeHost = System.getProperty(ID_DAE_HOST);
		daeLogin = System.getProperty(ID_DAE_LOGIN);
		daePassword = System.getProperty(ID_DAE_PASSWORD);
		String gitPath = System.getProperty(PROPERTY_NOTEBOOK_REPOSITORY_PATH);
		if (gitPath != null) {
			gitService = new GitService(gitPath);
		}
		String ips = System.getProperty(PROPERTY_NOTEBOOK_DAVIP);
		if (ips != null) {
			allowedDavIps = ips.split(",");
			for (int i = 0; i < allowedDavIps.length; i++) {
				allowedDavIps[i] = PROPERTY_NOTEBOOK_IPPREFIX + allowedDavIps[i].replaceAll("/", ".").trim();
			}
		}
		String icsips = System.getProperty(PROPERTY_NOTEBOOK_ICSIP);
		if (icsips != null) {
			allowedIcsIps = icsips.split(",");
			for (int i = 0; i < allowedIcsIps.length; i++) {
				allowedIcsIps[i] = PROPERTY_NOTEBOOK_IPPREFIX + allowedIcsIps[i].replaceAll("/", ".").trim();
			}
		}
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
	
	private UserSessionObject checkIcsSession(Request request) {
		if (allowedIcsIps == null) {
			return null;
		}
		String directIp = request.getClientInfo().getUpstreamAddress();
		if (directIp != null) {
			logger.error("direct ip = " + directIp);
			logger.error("allowed ips = " + Arrays.toString(allowedIcsIps));
			for (int i = 0; i < allowedIcsIps.length; i++) {
				if (directIp.equals(allowedIcsIps[i])) {
					UserSessionObject session = new UserSessionObject();
					session.setICS(true);
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
				logger.error("forwarded ip = " + forwardedIp);
				logger.error("header = " + header);
				if (forwardedIp.contains(",")) {
					forwardedIp = forwardedIp.split(",")[0].trim();
				} else {
					forwardedIp = forwardedIp.split(" ")[0].trim();
				}
				for (int i = 0; i < allowedIcsIps.length; i++) {
					if (forwardedIp.equals(allowedIcsIps[i])) {
						UserSessionObject session = new UserSessionObject();
						session.setICS(true);
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

		if (session == null || !session.isValid()) {
			try {
				session = checkIcsSession(request);
			} catch (Exception e) {
			}
		}

		if (session != null && session.isValid()) {


			//		Form queryForm = request.getResourceRef().getQueryAsForm();
			String seg = request.getResourceRef().getLastSegment();
			List<String> segList = request.getResourceRef().getSegments();
//			String ip = request.getClientInfo().getUpstreamAddress();
			if (SEG_NAME_SAVE.equals(seg)) {
				
				//			String content = rForm.getValues("content");
				Representation rep = request.getEntity();
				FileWriter writer = null;
				Form queryForm = request.getResourceRef().getQueryAsForm();
				String pageId = queryForm.getValues(QUERY_SESSION_ID);
				String pageName = queryForm.getValues(QUERY_PAGE_ID);
				if (pageId == null || pageId.trim().length() == 0) {
//					if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//						response.setStatus(Status.SERVER_ERROR_INTERNAL, "The notebook page is not available to the public.");
//						return;
//					} else {
						try {
							pageId = controlDb.getCurrentSessionId();
							if (!allowAccessCurrentPage(session, pageId, proposalDb)){
								response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow editing the current page.");
								return;
							}
							if (pageName == null) {
								pageName = sessionDb.getSessionValue(pageId);
							}
						} catch (Exception e) {
							response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
							return;
						}
//					}
				} else {
					try {
						if (!allowEditHistoryPage(session, pageId)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow editing this page.");
							return;
						}
						String getPageId = sessionDb.getSessionValue(pageId);
						if (pageName != null){
							if (!pageName.equals(getPageId)) {
								response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: page and session don't match.");
								return;
							}
						} else {
							pageName = getPageId;
						}
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
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
					writer = new FileWriter(currentFileFolder + "/" + pageName + ".xml");
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
				if (gitService != null) {
					try {
						gitService.applyChange();
						gitService.commit(pageName + ":" + System.currentTimeMillis());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("status", "OK");
					response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (QUERY_PAGE_ID.equals(seg)) {
				Form queryForm = request.getResourceRef().getQueryAsForm();
				String sessionId = queryForm.getValues(QUERY_SESSION_ID);
				if (sessionId == null || sessionId.trim().length() == 0) {
//					if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//						response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
//						response.setStatus(Status.SUCCESS_OK);
//						return;
//					}
					try {
						sessionId = controlDb.getCurrentSessionId();
						if (!allowAccessCurrentPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow access the current page.");
							return;
						}
						String sessionValue = sessionDb.getSessionValue(sessionId);
						response.setEntity(new String(sessionValue), MediaType.TEXT_PLAIN);
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					}
				} else {
					try {
						if (!allowReadHistoryPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow accessing this page.");
							return;
						}
						String sessionValue = sessionDb.getSessionValue(sessionId);
						response.setEntity(new String(sessionValue), MediaType.TEXT_PLAIN);
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					}
				}
			} else if (SEG_NAME_LISTHISTORY.equals(seg)) {
				Form queryForm = request.getResourceRef().getQueryAsForm();
				String sessionId = queryForm.getValues(QUERY_SESSION_ID);
				if (sessionId == null || sessionId.trim().length() == 0) {
//					if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//						response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
//						response.setStatus(Status.SUCCESS_OK);
//						return;
//					}
					try {
						sessionId = controlDb.getCurrentSessionId();
						if (!allowAccessCurrentPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow access the current page.");
							return;
						}
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					}
				} else {
					try {
						if (!allowReadHistoryPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow accessing this page.");
							return;
						}
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					}
				}
				try {
					String proposalId = proposalDb.findProposalId(sessionId);
					String sessions = "None";
					if (proposalId != null) {
						sessions = proposalDb.getSessionIds(proposalId);
						String sessionString = "";
						if (sessions != null && sessions.trim().length() > 0) {
							String[] sessionArray = sessions.split(":");
							for (int i = 0; i < sessionArray.length; i++) {
								String sId = sessionArray[i];
								if (i > 0) {
									sessionString += ",";
								}
								sessionString += sId + ":" + sessionDb.getSessionValue(sId);
							}
						}
						if (sessionString == "") {
							sessionString = "None";
						}
						response.setEntity(new String(proposalId + ";" + sessionString), MediaType.TEXT_PLAIN);
					} else {
						response.setEntity(new String("Unknown;None"), MediaType.TEXT_PLAIN);
					}
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			} else if (SEG_NAME_LOAD.equals(seg)) {
				Form queryForm = request.getResourceRef().getQueryAsForm();
				String sessionId = queryForm.getValues(QUERY_SESSION_ID);
				String pattern = queryForm.getValues(QUERY_PATTERN);
				if (sessionId == null || sessionId.trim().length() == 0) {
//					if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//						response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
//						response.setStatus(Status.SUCCESS_OK);
//						return;
//					}
					try {
						sessionId = controlDb.getCurrentSessionId();
						if (!allowAccessCurrentPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow loading the current page.");
							return;
						}
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
						if (!allowReadHistoryPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow accessing this page.");
							return;
						}
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
//					if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//						response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
//						response.setStatus(Status.SUCCESS_OK);
//						return;
//					}
					try {
						sessionId = controlDb.getCurrentSessionId();
						if (!allowAccessCurrentPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow access the current page.");
							return;
						}
						sessionValue = sessionDb.getSessionValue(sessionId);
						sourceFilename = currentFileFolder + "/" + sessionValue + ".xml";
						targetFilename = sessionValue + "_" + expName + ".pdf";
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					}
				} else {
					try {
						if (!allowReadHistoryPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow accessing this page.");
							return;
						}
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
//				if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//					response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
//					response.setStatus(Status.SUCCESS_OK);
//					return;
//				}
				try {
					String sessionId = controlDb.getCurrentSessionId();
					if (!allowAccessCurrentPage(session, sessionId, proposalDb)){
						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow access current page.");
						return;
					}
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
//					if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//						response.setEntity("<span style=\"color:red\">The database is not available to the public.</span>", MediaType.TEXT_PLAIN);
//						response.setStatus(Status.SUCCESS_OK);
//						return;
//					}
					try {
						sessionId = controlDb.getCurrentSessionId();
						if (!allowAccessCurrentPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow access the current database.");
							return;
						}
					} catch (Exception e1) {
						response.setEntity("<span style=\"color:red\">Error loading current notebook page. Please ask instrument scientist for help.</span>", 
								MediaType.TEXT_PLAIN);
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e1);
						return;
					}
				} else {
					try {
						if (!allowReadHistoryPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow accessing this database.");
							return;
						}						
					} catch (Exception e) {
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
//				if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//					response.setStatus(Status.SERVER_ERROR_INTERNAL, "Notebook management is not available to the public.");
//					return;
//				}
				try {
					Form form = request.getResourceRef().getQueryAsForm();
					String proposalId = form.getValues(QUERY_PROPOSAL_ID);
					if (!isManager(session)) {
						boolean allowAccess = false;
						String sessionId = controlDb.getCurrentSessionId();
						String currentProposal = proposalDb.findProposalId(sessionId);
						if (proposalId != null && proposalId.equals(currentProposal)) {
							if (allowAccessCurrentPage(session, sessionId, proposalDb)) {
								allowAccess = true;
							}
						} 
						if (!allowAccess) {
							if (session.isICS()) {
								allowAccess = true;
							}
						}
						if (!allowAccess) {
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow creating new page.");
							return;
						}
					}
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
					String newName = PREFIX_NOTEBOOK_FILES + format.format(new Date());
					File newFile = new File(currentFileFolder + "/" + newName + ".xml");
					if (!newFile.createNewFile()) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, "failed to create new file");
						return;
					}
					try {
						saveOldPagePdf();					
					} catch (Exception e) {
					}
					PrintWriter pw = null;
					String html = "";
					try {
						pw = new PrintWriter(new FileWriter(newFile));
						if (proposalId != null) {
							html += "<h1>" + instrumentId.substring(0, 1).toUpperCase() + instrumentId.substring(1) 
									+ " Notebook Page: " + proposalId + "</h1><p/>";
							int proposalInt = 0;
							try {
								proposalInt = Integer.valueOf(proposalId);
							} catch (Exception e) {
							}
							if (proposalInt > 0) {
								Map<ProposalItems, String> proposalInfo = ProposalDBSOAPService.getProposalInfo(proposalInt, instrumentId);
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
									String extension = System.getProperty(PROP_NOTEBOOK_TABLEEXTENSION);
									if (extension == null) {
										extension = "";
									}
									tableHtml = tableHtml.replace(EXPERIMENT_TABLE_HTML_EXTENSION, extension);
									html += tableHtml;
								}
							}
						} else {
							html += "<h1>" + instrumentId.substring(0, 1).toUpperCase() + instrumentId.substring(1) + " Notebook</h1><p/>";
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
					if (!isManager(session)) {
						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow listing history.");
						return;
					}
					File current = new File(currentFileFolder);
					if (current.exists()) {
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
						response.setEntity(jsonObject.toString(), MediaType.TEXT_PLAIN);
					}
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			}  else if (SEG_NAME_MYARCHIVE.equals(seg)) {
				try {
//					if (!isManager(session)) {
//						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow listing history.");
//						return;
//					}
					JSONArray proposalArray = UserSessionService.getProposals(session);
					File current = new File(currentFileFolder);
					if (current.exists()) {
						String currentSessionId = controlDb.getCurrentSessionId();
						List<String> proposalIds = new ArrayList<String>();
						for (int i = 0; i < proposalArray.length(); i++) {
							proposalIds.add(proposalArray.getString(i));
						}
						Collections.sort(proposalIds);
						Collections.reverse(proposalIds);
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
								}
								if (proposalObject.length() > 0) {
									jsonObject.put(proposalId, proposalObject);
								}
							}
						}
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
					if (!isManager(session)) {
						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow accessing this page.");
						return;
					}
					String guidePath = currentFileFolder + "/" + NOTEBOOK_MANAGEHELP_FILENAME;
					File guideFile = new File(guidePath);
					if (guideFile.exists()) {
						byte[] bytes = Files.readAllBytes(Paths.get(guidePath));
						response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
					} else {
						response.setEntity("", MediaType.TEXT_PLAIN);
					}
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			} else if (SEG_NAME_MYGUIDE.equals(seg)) {
				try {
					if (!isManager(session)) {
						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow accessing this page.");
						return;
					}
					String guidePath = currentFileFolder + "/" + NOTEBOOK_MYGUIDE_FILENAME;
					File guideFile = new File(guidePath);
					if (guideFile.exists()) {
						byte[] bytes = Files.readAllBytes(Paths.get(guidePath));
						response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
					} else {
						response.setEntity("", MediaType.TEXT_PLAIN);
					}
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					return;
				}
			} else if (SEG_NAME_SEARCH.equals(seg)) {
				try {
					if (!isManager(session)) {
						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow searching pages.");
						return;
					}
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
			}  else if (SEG_NAME_SEARCHMINE.equals(seg)) {
				try {
					if (!isManager(session)) {
						response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Error: your privilege does not allow searching pages.");
						return;
					}
					Form queryForm = request.getResourceRef().getQueryAsForm();
					String pattern = queryForm.getValues(QUERY_PATTERN);
					if (pattern.trim().length() == 0) {
						response.setEntity("Please input a valid pattern", MediaType.TEXT_PLAIN);
						response.setStatus(Status.SUCCESS_OK);
						return;
					}
					File current = new File(currentFileFolder);
					if (current.exists()) {
						JSONArray proposalArray = UserSessionService.getProposals(session);
						List<String> sessionPairList = new ArrayList<String>();
						for (int i = 0; i < proposalArray.length(); i++) {
							String proposalId = proposalArray.getString(i);
							String sessions = proposalDb.getSessionIds(proposalId);
							if (sessions != null && sessions.trim().length() > 0) {
								String[] sessionArray = sessions.split(":");
								for (String sessionId : sessionArray) {
									sessionPairList.add(sessionDb.getSessionValue(sessionId) + ":" + sessionId);
								}
							}
						}
						String[] sessionPairs = sessionPairList.toArray(new String[0]);
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
			} else if (SEG_NAME_IMAGESERVICE.equals(seg)) {
				Form queryForm = request.getResourceRef().getQueryAsForm();
				String externalUrl = queryForm.getValues(QUERY_EXTERNAL_URL_ID);
				if (externalUrl != null && externalUrl.trim().length() > 0) {
					final CallbackAdapter callback = new CallbackAdapter();
					//			    if (externalUrl.contains(daeHost)) {
					if (externalUrl.contains("das") && externalUrl.contains(".nbi.ansto.gov.au:808")) {
						try {
							internalHttpClient.performGet(URI.create(externalUrl), callback, daeLogin, EncryptionUtils.decryptBase64(daePassword));
						} catch (Exception e) {
							response.setStatus(Status.SERVER_ERROR_INTERNAL, "faied to convert image");
							return;
						}
					} else {
						externalHttpClient.performGet(URI.create(externalUrl), callback);			    	
					}
					LoopRunner.run(new ILoopExitCondition() {

						@Override
						public boolean getExitCondition() {
							return callback.isReady;
						}
					}, 200000, 100);
					if (callback.base64 == null) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, "faied to convert image");
						return;
					}
					response.setEntity(callback.base64, MediaType.TEXT_PLAIN);
				}
			} else if (SEG_NAME_USER.equals(seg)) {
				try {
					if (validateService(session, new String[] {UserSessionService.NAME_SERVICE_SIGNIN})){
						String username = session.getUserName();
						if (username.contains("@")){
							username = username.substring(0, username.indexOf("@"));
						}
						response.setEntity(username, MediaType.TEXT_PLAIN);
					} else {
						response.setEntity("NONE", MediaType.TEXT_PLAIN);
					}
				} catch (Exception e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, "faied to convert image");
					return;
				}
			} else if (seg.toLowerCase().endsWith(".pdf") && segList.size() > 1 && segList.get(segList.size() - 2).equals(SEG_NAME_DOWNLOAD)) {
				Form queryForm = request.getResourceRef().getQueryAsForm();
				String sessionId = queryForm.getValues(QUERY_SESSION_ID);
				String pageId = seg.replaceAll(".pdf", "");
				String extName = queryForm.getValues(QUERY_EXTNAME_ID);
				String targetFilename = null;
				if (sessionId == null || sessionId.trim().length() == 0) {
//					if (!ip.startsWith("137.157.") && !ip.startsWith("127.0.") && !ip.startsWith("0:0")){
//						response.setEntity("<span style=\"color:red\">The notebook page is not available to the public.</span>", MediaType.TEXT_PLAIN);
//						response.setStatus(Status.SUCCESS_OK);
//						return;
//					} else {
					try {
						sessionId = controlDb.getCurrentSessionId();
						if (!allowAccessCurrentPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Error: your privilege does not allow accessing this page.</span>");
							return;
						}
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					}
					targetFilename = pageId + "_" + extName + ".pdf";
//					}
				} else {
					try {
						if (!allowReadHistoryPage(session, sessionId, proposalDb)){
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Error: your privilege does not allow accessing this page.</span>");
							return;
						}
						String sessionValue = sessionDb.getSessionValue(sessionId);
						if (!sessionValue.equals(pageId)) {
							response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Illigal session.</span>");
							return;
						}
						targetFilename = pageId + "_" + extName + ".pdf";
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					}
				}
				String targetPath = pdfFolder + "/" + targetFilename;
				String baseFilename = pageId + ".pdf";
				File current = new File(targetPath);
				if (current.exists()) {
					try {
						//    				FileRepresentation representation = new FileRepresentation(targetPath, MediaType.register("application/x-pdf", "Adobe PDF document"));
						FileRepresentation representation = new FileRepresentation(targetPath, MediaType.APPLICATION_OCTET_STREAM);
						Disposition disposition = new Disposition();
						disposition.setFilename(baseFilename);
						disposition.setType(Disposition.TYPE_ATTACHMENT);
						representation.setDisposition(disposition);
						response.setEntity(representation);
						//    				Form responseHeaders = (Form) response.getAttributes().get("org.restlet.http.headers");
						//    	            if (responseHeaders == null) {
						//    	                responseHeaders = new Form();
						//    	                responseHeaders.add("Pragma", "no-cache");
						//    	                responseHeaders.add("Cache-Control", "max-age=1");
						//    	                response.getAttributes().put("org.restlet.http.headers", responseHeaders);
						//    	            }
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					}
				} else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, "<span style=\"color:red\">File not found.</span>");
					return;
				}
			} 
			response.setStatus(Status.SUCCESS_OK);
			//		response.release();
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
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "<span style=\"color:red\">Error: invalid user session.</span>");
		}
	    return;
	}
	
	
	public static boolean validateService(UserSessionObject session, String[] services) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		for (int i = 0; i < services.length; i++) {
			if (UserSessionService.verifyService(session, services[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isManager(UserSessionObject session) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		return validateService(session, new String[] {
				UserSessionService.NAME_SERVICE_NOTEBOOKADMIN,
				UserSessionService.NAME_SERVICE_NOTEBOOKMANAGER
		});
	}
	

//	public boolean verifyPageAccess(UserSessionObject session, String pageId) 
//			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
//		if (verifyService(session, NAME_SERVICE_NOTEBOOKADMIN)){
//			return true;
//		} else if (verifyService(session, NAME_SERVICE_NOTEBOOKMANAGER)) {
//			return true;
//		} else {
//
//		}
//		return false;
//	}
	
	public static boolean allowAccessCurrentPage(UserSessionObject session, String pageId, ProposalDB proposalDb) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		if (session != null && session.isDAV()) {
			return true;
		}
		if (isManager(session)) {
			return true;
		} else if (validateService(session, new String[]{UserSessionService.NAME_SERVICE_CURRENTPAGE})){
			return true;
		} else if (verifyPageAccess(session, pageId, proposalDb)) {
			return true;
		}
		return false;
	}
	
	public static boolean allowEditHistoryPage(UserSessionObject session, String pageId) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		if (isManager(session)) {
			return true;
		} 
		return false;
	}

	public static boolean allowReadHistoryPage(UserSessionObject session, String pageId, ProposalDB proposalDb) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		if (isManager(session)) {
			return true;
		} else if (verifyPageAccess(session, pageId, proposalDb)) {
			return true;
		}
		return false;
	}

	public static boolean verifyPageAccess(UserSessionObject session, String pageId, ProposalDB proposalDb) 
			throws ClassNotFoundException, RecordsFileException, IOException, JSONException {
		String proposalId = proposalDb.findProposalId(pageId);
		if (proposalId == null) {
			return false;
		}
		JSONArray proposals = UserSessionService.getProposals(session);
		if (proposals != null) {
			for (int i = 0; i < proposals.length(); i++) {
				if (proposals.get(i).equals(proposalId)) {
					return true;
				}
			}
		}
		return false;
	}

	private void saveOldPagePdf() throws Exception {
		String reportPath = System.getProperty(PROPERTY_SERVER_REPORTPATH);
		if (reportPath == null) {
			return;
		}
		try {
			String sessionId = controlDb.getCurrentSessionId();
			String pageId = sessionDb.getSessionValue(sessionId);
			String proposalId = proposalDb.findProposalId(sessionId);
			int lengthDiff = 5 - proposalId.length();
			for (int i = 0; i < lengthDiff; i ++) {
				proposalId = "0" + proposalId;
			}
			String sourceFilename = currentFileFolder + "/" + pageId + ".xml";
			String pageTime = pageId.substring(pageId.indexOf("_") + 1);
			String targetFilename = instrumentId + "_" + proposalId + "_" + pageTime + ".pdf";
			File current = new File(sourceFilename);
			if (current.exists()) {
				String targetPath = reportPath + "/" + targetFilename;
				boolean isSuccessful = pdfService.createPDF(sourceFilename, targetPath);
				if (!isSuccessful) {
					throw new Exception("failed to save pdf file for old page");
				}
			}
		}catch (Exception e) {
			throw new Exception("failed to save pdf file for old page", e);
		}
	}


	class CallbackAdapter implements IHttpClientCallback{
		boolean isReady = false;
    	String base64 = null;
    	
		@Override
		public void handleResponse(InputStream in) {
			byte[] byteArray;
			if (in == null) {
				isReady = true;
				return;
			}
			try {
				byteArray = IOUtils.toByteArray(in);
				base64 = Base64.encode(byteArray, false);
				isReady = true;
			} catch (IOException e) {
				System.err.println(e);
				isReady = true;
			}
		}
		
		@Override
		public void handleError() {
			// TODO Auto-generated method stub
			isReady = true;
		}
	}

}
