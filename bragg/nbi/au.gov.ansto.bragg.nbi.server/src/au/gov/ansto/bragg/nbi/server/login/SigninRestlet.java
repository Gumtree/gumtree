package au.gov.ansto.bragg.nbi.server.login;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.MapDatabase;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.directory.LDAPService;
import org.gumtree.service.directory.LDAPService.GroupLevel;
import org.gumtree.service.directory.TardisService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import au.gov.ansto.bragg.nbi.server.internal.UserSessionService;

public class SigninRestlet extends Restlet implements IDisposable{

//	public final static String COOKIE_NAME_UUID = "nbi.cookie.uuid";
//	public final static String COOKIE_COMMENT_UUID = "ANSTO NBI experiment uuid";
//	public static final String PROPERTY_INSTRUMENT_ID = "gumtree.instrument.id";
//	private static final int COOKIE_EXP_SECONDS = 3600;
	private static final String PROP_DEFAULT_PROPOSAL = "gumtree.tardis.defaultProposal";
	private static final String QUERY_USER_ID = "login_username";
	private static final String QUERY_USER_PASSWORD = "login_password";
	private static final String SEG_NAME_LOGIN = "LOGIN";
	private static final String SEG_NAME_LOGOUT = "LOGOUT";
	private LDAPService service;
	private MapDatabase sessionDb;
	private MapDatabase timestampDb;
	private MapDatabase serviceDb;
	private TardisService tardisService;

	public SigninRestlet() {
		service = new LDAPService();
		sessionDb = MapDatabase.getInstance(UserSessionService.ID_USER_SESSION_DATABASE);
		timestampDb = MapDatabase.getInstance(UserSessionService.ID_SESSION_TIME_DATABASE);
		serviceDb = MapDatabase.getInstance(UserSessionService.ID_SESSION_SERVICE_DATABASE);
		tardisService = TardisService.getTardisService();
	}

	public SigninRestlet(Context context) {
		super(context);
		service = new LDAPService();
		sessionDb = MapDatabase.getInstance(UserSessionService.ID_USER_SESSION_DATABASE);
		timestampDb = MapDatabase.getInstance(UserSessionService.ID_SESSION_TIME_DATABASE);
		serviceDb = MapDatabase.getInstance(UserSessionService.ID_SESSION_SERVICE_DATABASE);
		tardisService = TardisService.getTardisService();
	}

	@Override
	public void handle(final Request request, final Response response) {

		//			Form queryForm = request.getResourceRef().getQueryAsForm();
		//			String seg = request.getResourceRef().getLastSegment();
		//			List<String> segList = request.getResourceRef().getSegments();
		//			String ip = request.getClientInfo().getUpstreamAddress();
		String seg = request.getResourceRef().getLastSegment();
		if (SEG_NAME_LOGIN.equals(seg)) {
			Representation entity = request.getEntity();
			Form form = new Form(entity);
			String username = form.getValues(QUERY_USER_ID);
			if (username != null && !username.contains("@")) {
				username += "@nbi.ansto.gov.au";
			}
			String password = form.getValues(QUERY_USER_PASSWORD);
			final JSONObject result = new JSONObject();
			GroupLevel level = checkLogin(username, password);
			if (level != GroupLevel.INVALID) {
				try {
					String uuidString = UUID.randomUUID().toString();
					CookieSetting cookie = createUUIDCookie(username, uuidString);

					persistServiceList(username, uuidString, level);

					response.getCookieSettings().add(cookie);
					result.put("result", "OK");
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} catch (Exception e) {
					e.printStackTrace();
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				}
			} else {
				try {
					result.put("result", "Login failed");
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
				} catch (JSONException e) {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				}
			}
		} else if (SEG_NAME_LOGOUT.equals(seg)) {
			String userCookie = null;
			final JSONObject result = new JSONObject();
			Cookie existingCookie = request.getCookies().getFirst(UserSessionService.COOKIE_NAME_UUID + "." 
					+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID));
			if (existingCookie != null) {
				userCookie = existingCookie.getValue();
				String[] pair = userCookie.split(":");
				if (pair.length == 2) {
					try {
						CookieSetting clearCookie = clearSession(pair);
						response.getCookieSettings().add(clearCookie);
						result.put("result", "OK");
					} catch (Exception e) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
						return;
					} 
				} 
			}
			response.setStatus(Status.SUCCESS_OK);
			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
		}
	}

	private CookieSetting clearSession(String[] pair) throws RecordsFileException, IOException {
		CookieSetting cookie = new CookieSetting(0, UserSessionService.COOKIE_NAME_UUID + "." 
				+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID), null, 
				"/", null, UserSessionService.COOKIE_COMMENT_UUID, 0, false);
		sessionDb.remove(pair[0]);
		timestampDb.remove(pair[1]);
		serviceDb.remove(pair[1]);
		return cookie;
	}

	@Override
	public void disposeObject() {

	}
	
	private CookieSetting createUUIDCookie(String username, String uuidString) throws IOException, RecordsFileException {
		CookieSetting cookie = new CookieSetting(0, UserSessionService.COOKIE_NAME_UUID + "." 
					+ System.getProperty(UserSessionService.PROPERTY_INSTRUMENT_ID), username + ":" + uuidString, 
					"/", null, UserSessionService.COOKIE_COMMENT_UUID, UserSessionService.COOKIE_EXP_SECONDS, false);
		sessionDb.put(username, uuidString);
		timestampDb.put(uuidString, String.valueOf(System.currentTimeMillis()));
		return cookie;
	}

	private void persistServiceList(String username, String uuidString, GroupLevel level) throws IOException, RecordsFileException, JSONException {
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
	
	private GroupLevel checkLogin(String username, String password) {
		GroupLevel level = GroupLevel.INVALID;
		try {
			level = service.validateUser(username, password);
		} catch (Exception e) {
		}
		return level;
	}

	private boolean verifyInstrumentManager(String username) throws IOException, JSONException {
		boolean flag = false;
		String proposals = tardisService.listProposalsForUser(username);
		if (proposals != null) {
			JSONObject json = new JSONObject(proposals);
			Object jstring = json.get(TardisService.NAME_USERS_PROPOSAL);
			String defaultProp = System.getProperty(PROP_DEFAULT_PROPOSAL);
			if (String.valueOf(jstring).contains("\"" + defaultProp + "\"")){
				flag = true;
			}
		}
		return flag;
	}
	
	private JSONArray getUserProposals(String username) throws JSONException, IOException {
		JSONArray proposals = new JSONArray();
		String proposalString = tardisService.listProposalsForUser(username);
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
}
