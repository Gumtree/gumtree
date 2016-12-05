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
	private static final String SEG_NAME_CLEAR = "CLEAR";
	private MapDatabase sessionDb;
	private MapDatabase timestampDb;
	private MapDatabase serviceDb;
	private TardisService tardisService;

	public SigninRestlet() {
		sessionDb = MapDatabase.getInstance(UserSessionService.ID_USER_SESSION_DATABASE);
		timestampDb = MapDatabase.getInstance(UserSessionService.ID_SESSION_TIME_DATABASE);
		serviceDb = MapDatabase.getInstance(UserSessionService.ID_SESSION_SERVICE_DATABASE);
		tardisService = TardisService.getTardisService();
	}

	public SigninRestlet(Context context) {
		super(context);
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
//			if (username != null && !username.contains("@")) {
//				username += "@nbi.ansto.gov.au";
//			}
			String password = form.getValues(QUERY_USER_PASSWORD);
			final JSONObject result = new JSONObject();
			try {
				boolean isValid = UserSessionService.signIn(request, response, username, password);
				if (isValid) {
					result.put("result", "OK");
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} else {
					result.put("result", "Login failed");
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
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
		} else if (SEG_NAME_CLEAR.equals(seg)) {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
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
	

}
