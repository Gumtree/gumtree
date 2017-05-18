package au.gov.ansto.bragg.nbi.server.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.gumtree.service.db.MapDatabase;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.directory.LDAPService;
import org.gumtree.service.directory.TardisService;
import org.gumtree.service.directory.LDAPService.GroupLevel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.server.login.UserSessionObject;

public class UserSessionService {

	public final static String COOKIE_NAME_UUID = "nbi.cookie.uuid";
	public final static String COOKIE_COMMENT_UUID = "ANSTO NBI experiment uuid";
	public static final String PROPERTY_INSTRUMENT_ID = "gumtree.instrument.id";
	public static final int COOKIE_EXP_SECONDS = 3600;
	public static final String ID_USER_SESSION_DATABASE = "usession";
	public static final String ID_SESSION_SERVICE_DATABASE = "session_service";
	public static final String ID_SESSION_TIME_DATABASE = "session_time";
	public static final String NAME_SERVICE_SIGNIN = "signin_made";
	public static final String NAME_SERVICE_NOTEBOOKADMIN = "notebook_admin";
	public static final String NAME_SERVICE_NOTEBOOKMANAGER = "notebook_manager";
	public static final String NAME_SERVICE_NOTEBOOKPROPOSALS = "notebook_proposals";
	public static final String NAME_SERVICE_CURRENTPAGE = "current_page";
	private static final String PROP_REMOTE_USER = "X-REMOTE-USER";
	private static final String PROPERTY_NOTEBOOK_DAVIP = "gumtree.notebook.dav";
	private static final String PROPERTY_NOTEBOOK_ICSIP = "gumtree.notebook.ics";
	private static final String PROPERTY_NOTEBOOK_IPPREFIX = "137.157.";

	private static MapDatabase sessionDb = MapDatabase.getInstance(ID_USER_SESSION_DATABASE);
	private static MapDatabase serviceDb = MapDatabase.getInstance(ID_SESSION_SERVICE_DATABASE);
	private static MapDatabase timestampDb = MapDatabase.getInstance(ID_SESSION_TIME_DATABASE);
	
	private static Logger logger = LoggerFactory.getLogger(UserSessionService.class);

	private static String[] allowedDavIps;
	private static String[] allowedIcsIps;

	static {
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
	
	public static boolean signIn(Request request, Response response, String username, String password) {
		GroupLevel level = checkLogin(username, password);
		if (level != GroupLevel.INVALID) {
			try {
				String uuidString = UUID.randomUUID().toString();
				CookieSetting cookie = UserSessionService.createUUIDCookie(username, uuidString, sessionDb, timestampDb);

				UserSessionService.persistServiceList(username, uuidString, level);

				response.getCookieSettings().add(cookie);
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}

	}
	
	public static UserSessionObject validateCookie(String userCookie) throws ClassNotFoundException, RecordsFileException, IOException {
		UserSessionObject session = new UserSessionObject();
		String[] pair = userCookie.split(":");
		if (pair.length != 2) {
			session.appendMessage("sign in required");
		} else {
			session.setUserName(pair[0]);
			String uuidString = sessionDb.get(pair[0]);
			if (!pair[1].equals(uuidString)) {
				session.appendMessage("invalid user account");
			} else {
				session.setSessionId(pair[1]);
				String timeString = timestampDb.get(pair[1]);
				if (timeString == null) {
					session.appendMessage("incomplete login");
				} else {
					long time = Long.valueOf(timeString);
					if (System.currentTimeMillis() - time > UserSessionService.COOKIE_EXP_SECONDS * 1000) {
						session.appendMessage("login expired");
					} else {
//						CookieSetting cookieSetting = new CookieSetting(0, UserManagerRestlet.COOKIE_NAME_UUID, userCookie, 
//								"/", null, UserManagerRestlet.COOKIE_COMMENT_UUID, UserSessionService.COOKIE_EXP_SECONDS, false);
						//	    response.getCookieSettings().add(cookieSetting);
						//	    response.setEntity(json.toString(), MediaType.APPLICATION_JSON);
						session.setValid(true);
					}
				}
			}
		}
		return session;
	}

	public static UserSessionObject setUpRemoteUserSession(Request request, Response response, String remoteUser) 
			throws IOException, RecordsFileException, JSONException {
		UserSessionObject session = new UserSessionObject();
		session.setUserName(remoteUser);
		session.setRemoteUserFlag(true);
		session.setValid(true);
		String uuidString = UUID.randomUUID().toString();
		session.setSessionId(uuidString);
		CookieSetting cookie = createUUIDCookie(remoteUser, uuidString, sessionDb, timestampDb);

		persistServiceList(remoteUser, uuidString, checkUsername(remoteUser));

		response.getCookieSettings().add(cookie);
		return session;
	}

	public static void persistServiceList(String username, String uuidString, GroupLevel level) throws IOException, RecordsFileException, JSONException {
		JSONObject serviceList = new JSONObject();
//		if (verifyInstrumentManager(username)) {
//
//		}
		switch (level) {
		case ADMIN:
			serviceList.put(UserSessionService.NAME_SERVICE_NOTEBOOKADMIN, true);
			serviceList.put(UserSessionService.NAME_SERVICE_NOTEBOOKMANAGER, true);
			serviceList.put(UserSessionService.NAME_SERVICE_CURRENTPAGE, true);
			serviceList.put(UserSessionService.NAME_SERVICE_NOTEBOOKPROPOSALS, getUserProposals(username));				
			break;
		case MANAGER:
			serviceList.put(UserSessionService.NAME_SERVICE_NOTEBOOKMANAGER, true);
			serviceList.put(UserSessionService.NAME_SERVICE_CURRENTPAGE, true);
			serviceList.put(UserSessionService.NAME_SERVICE_NOTEBOOKPROPOSALS, getUserProposals(username));				
			break;
		case USER:
			serviceList.put(UserSessionService.NAME_SERVICE_NOTEBOOKPROPOSALS, getUserProposals(username));				
			break;
		default:
			break;
		}
		serviceList.put(UserSessionService.NAME_SERVICE_SIGNIN, true);
		serviceDb.put(uuidString, serviceList.toString());
	}

	public static JSONArray getUserProposals(String username) throws JSONException, IOException {
		JSONArray proposals = new JSONArray();
		String proposalString = TardisService.getTardisService().listProposalsForUser(username);
		if (proposalString != null) {
			JSONObject json = new JSONObject(proposalString);
			Object jstring = json.get(TardisService.NAME_USERS_PROPOSAL);
			if (jstring instanceof JSONArray) {
				proposals = (JSONArray) jstring;
			}
//			String defaultProp = System.getProperty(PROP_DEFAULT_PROPOSAL);
//			if (String.valueOf(jstring).contains("\"" + defaultProp + "\"")){
//				flag = true;
//			}
		}
		return proposals;
	}
	
	public static GroupLevel checkUsername(String username) {
		GroupLevel level = GroupLevel.INVALID;
		try {
			level = LDAPService.getService().checkUsername(username);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return level;
	}

	public static GroupLevel checkLogin(String username, String password) {
		GroupLevel level = GroupLevel.INVALID;
		try {
			level = LDAPService.getService().validateUser(username, password);
		} catch (Exception e) {
		}
		return level;
	}
	
	public static CookieSetting createUUIDCookie(String username, String uuidString, 
			MapDatabase sessionDb, MapDatabase timestampDb) throws IOException, RecordsFileException {
		CookieSetting cookie = new CookieSetting(0, UserSessionService.COOKIE_NAME_UUID + "." 
					+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID), username + ":" + uuidString, 
					"/", null, UserSessionService.COOKIE_COMMENT_UUID, UserSessionService.COOKIE_EXP_SECONDS, false);
		sessionDb.put(username, uuidString);
		timestampDb.put(uuidString, String.valueOf(System.currentTimeMillis()));
		return cookie;
	}
	
	public static void updateSessionTimestamp(String uuidString) throws IOException, RecordsFileException {
		timestampDb.put(uuidString, String.valueOf(System.currentTimeMillis()));
	}
	
	public static String getUserName(Request request) {
		String userCookie = null;
		Cookie cookie = request.getCookies().getFirst(UserSessionService.COOKIE_NAME_UUID + "." 
				+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID));
		if (cookie != null) {
			userCookie = cookie.getValue();
			String[] pair = userCookie.split(":");
			if (pair.length == 2) {
				String username = pair[0];
				if (username.contains("@")){
					username = username.substring(0, username.indexOf("@"));
				}
				return username.trim();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String getSessionId(Request request) {
		String userCookie = null;
		Cookie cookie = request.getCookies().getFirst(UserSessionService.COOKIE_NAME_UUID + "." 
				+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID));
		if (cookie != null) {
			userCookie = cookie.getValue();
			String[] pair = userCookie.split(":");
			if (pair.length == 2) {
				String sessionId = pair[1];
				return sessionId;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private static UserSessionObject checkDavSession(Request request) {
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
	
	private static UserSessionObject checkIcsSession(Request request) {
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
	
	public static UserSessionObject getUniversalSession(Request request, Response response) 
			throws ClassNotFoundException, RecordsFileException, IOException, JSONException {
		UserSessionObject session = null;
		String userCookie = null;
		Cookie cookie = request.getCookies().getFirst(UserSessionService.COOKIE_NAME_UUID + "." 
				+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID));
		if (cookie != null) {
			userCookie = cookie.getValue();
			session = validateCookie(userCookie);
		}
		if (session == null || !session.isValid()){
			Object header = request.getAttributes().get("org.restlet.http.headers");
			if (header != null) {
				Form qform = (Form) header;
				String remoteUser = qform.getFirstValue(PROP_REMOTE_USER);
				if (remoteUser != null && remoteUser.trim().length() > 0) {
					session = setUpRemoteUserSession(request, response, remoteUser);
				}
			}
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
		if (session == null || !session.isValid()) {
			session = new UserSessionObject();
			session.appendMessage("sign in required");
		}
		return session;
	}
	
	public static UserSessionObject getSession(Request request, Response response) 
			throws ClassNotFoundException, RecordsFileException, IOException, JSONException {
		UserSessionObject session = null;
		String userCookie = null;
		Cookie cookie = request.getCookies().getFirst(UserSessionService.COOKIE_NAME_UUID + "." 
				+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID));
//		Form queryForm = request.getResourceRef().getQueryAsForm();
//		String remoteUser = queryForm.getValues(PROP_REMOTE_USER);
//		if (remoteUser != null) {
//			logger.error("found remote user :" + remoteUser);
//		}else {
////			Map<String, Object> attributes = request.getAttributes();
////			for (String key : attributes.keySet()) {
////				logger.error(key + " : " + attributes.get(key).toString());
////			}
//			logger.error(request.getEntity().getText());
//		}
		if (cookie != null) {
			userCookie = cookie.getValue();
			if (userCookie != null) {
				session = validateCookie(userCookie);
//				if (session.isValid()) {
//					renewCookie(session, response);
//				}
		    }
		} 
		if (session == null || !session.isValid()){
			Object header = request.getAttributes().get("org.restlet.http.headers");
			if (header != null) {
				Form qform = (Form) header;
				String remoteUser = qform.getFirstValue(PROP_REMOTE_USER);
				if (remoteUser != null && remoteUser.trim().length() > 0) {
					session = setUpRemoteUserSession(request, response, remoteUser);
				}
			}
		}
		if (session == null) {
			session = new UserSessionObject();
			session.appendMessage("sign in required");
		}
		return session;
	}
	
	public static void renewCookie(UserSessionObject session, Response response) throws IOException, RecordsFileException {
		CookieSetting cookie = new CookieSetting(0, UserSessionService.COOKIE_NAME_UUID + "." 
					+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID), session.getUserName() + ":" + session.getSessionId(), 
					"/", null, UserSessionService.COOKIE_COMMENT_UUID, UserSessionService.COOKIE_EXP_SECONDS, false);
		timestampDb.put(session.getSessionId(), String.valueOf(System.currentTimeMillis()));
		response.getCookieSettings().add(cookie);
	}
	
//	public static boolean controlSession(Request request, Response response) throws JSONException {
//	    boolean isValid = false;
//		String userCookie = null;
//	    try {
//		    Cookie cookie = request.getCookies().getFirst(UserSessionService.COOKIE_NAME_UUID + "." 
//		    				+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID));
//		    if (cookie != null) {
//		    	userCookie = cookie.getValue();
//		    } else {
//		    	JSONObject json = new JSONObject();
//		    	json.put("status", "please sign in");
//				response.setEntity(json.toString(), MediaType.APPLICATION_JSON);
//		    }
//		} catch (Exception e) {
//		}
//	    if (userCookie == null) {
//	    	JSONObject json = new JSONObject();
//	    	json.put("status", "sign in required");
//			response.setEntity(json.toString(), MediaType.APPLICATION_JSON);
////			JSONObject jsonObject;
////			try {
////	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
////				response.redirectPermanent("/login.html");
////				response.setStatus(Status.SUCCESS_OK);
////			} catch (JSONException e) {
////	    		e.printStackTrace();
////	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
////			}
////	    	return;
//	    } else {
//			try {
//				isValid = validateCookie(userCookie, response);
//			} catch (Exception e) {
//				e.printStackTrace();
//	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//			}
////		    if (isValid) {
////		    	response.setEntity(userCookie, MediaType.TEXT_PLAIN);
////		    }
//	    }
//	    return isValid;
//	}
	
	public static boolean verifyService(UserSessionObject session, String serviceName) 
			throws JSONException, ClassNotFoundException, RecordsFileException, IOException {
		boolean isValid = false;
    	String sessionId = session.getSessionId();
    	if (sessionId != null) {
    		String serviceString = serviceDb.get(sessionId);
    		if (serviceString != null) {
    			JSONObject serviceList = new JSONObject(serviceString);
    			try {
	    			isValid = Boolean.valueOf(String.valueOf(serviceList.get(
	    					UserSessionService.NAME_SERVICE_NOTEBOOKMANAGER)));
				} catch (Exception e) {
				}
    		}
    	}
    	return isValid;
	}
	
	public static JSONArray getProposals(UserSessionObject session) 
			throws ClassNotFoundException, RecordsFileException, IOException, JSONException {
		String sessionId = session.getSessionId();
    	if (sessionId != null) {
    		String serviceString = serviceDb.get(sessionId);
    		if (serviceString != null) {
    			JSONObject serviceList = new JSONObject(serviceString);
    			Object json = serviceList.get(UserSessionService.NAME_SERVICE_NOTEBOOKPROPOSALS);
    			if (json instanceof JSONArray) {
    				return (JSONArray) json;
    			}
    		}
    	}
    	return null;
	}
	
	public static boolean verifyProposalAccess(UserSessionObject session, String proposalId) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		if (verifyService(session, NAME_SERVICE_NOTEBOOKMANAGER)) {
			return true;
		} else if (verifyService(session, NAME_SERVICE_CURRENTPAGE)){
			return true;
		} else {
			JSONArray proposals = getProposals(session);
			if (proposals != null) {
				for (int i = 0; i < proposals.length(); i++) {
					if (proposals.get(i).equals(proposalId)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
