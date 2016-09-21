package au.gov.ansto.bragg.nbi.server.internal;

import java.io.IOException;

import org.gumtree.service.db.MapDatabase;
import org.gumtree.service.db.RecordsFileException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;

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

	private static MapDatabase sessionDb = MapDatabase.getInstance(ID_USER_SESSION_DATABASE);
	private static MapDatabase serviceDb = MapDatabase.getInstance(ID_SESSION_SERVICE_DATABASE);
	private static MapDatabase timestampDb = MapDatabase.getInstance(ID_SESSION_TIME_DATABASE);
	
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
	
	public static UserSessionObject getSession(Request request, Response response) 
			throws ClassNotFoundException, RecordsFileException, IOException {
		UserSessionObject session = null;
		String userCookie = null;
		Cookie cookie = request.getCookies().getFirst(UserSessionService.COOKIE_NAME_UUID + "." 
				+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID));
		if (cookie != null) {
			userCookie = cookie.getValue();
			if (userCookie != null) {
				session = validateCookie(userCookie);
//				if (session.isValid()) {
//					renewCookie(session, response);
//				}
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
