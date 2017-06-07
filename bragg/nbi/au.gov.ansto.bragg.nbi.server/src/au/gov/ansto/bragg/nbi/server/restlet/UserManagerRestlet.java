package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.gumtree.core.object.IDisposable;
import org.gumtree.security.EncryptionUtils;
import org.gumtree.service.persistence.PersistentEntry;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.server.db.INbiPersistenceManager;
import au.gov.ansto.bragg.nbi.server.db.NbiPersistenceManager;
import au.gov.ansto.bragg.nbi.server.jython.JythonRunner;
import au.gov.ansto.bragg.nbi.server.jython.JythonRunnerManager;

import com.db4o.ObjectSet;

public class UserManagerRestlet extends Restlet implements IDisposable {

	public UserManagerRestlet() {
		this(null);
	}

	public final static String COOKIE_NAME_UUID = "nbi.cookie.uuid";
	private final static String QUERY_TYPE = "type";
	private final static String QUERY_EMAIL = "login_email";
	private final static String QUERY_PASSWORD = "login_password";
	private final static String QUERY_CODE = "code";
	private final static String QUERY_REGISTER_CODE = "register_code";
	private final static String QUERY_UUID = "uuid";
	private final static String RESPOND_RESULT = "result";
	private final static String COOKIE_PATH_UUID = "/jython";
	private final static String COOKIE_DOMAIN_UUID = "www.nbi.ansto.gov.au";
	public final static String COOKIE_COMMENT_UUID = "Jython runner uuid";
	private static final String NAME_EMAIL_HOST = "greenhouse.nbi.ansto.gov.au";
	private static final String NAME_EMAIL_SERVICE = "mail.smtp.host";
	private static final String NAME_EMAIL_SENDER = "noreply@ansto.gov.au";
	public static final String PROPERTY_INSTRUMENT_ID = "gumtree.instrument.id";
	
	private static final String TEXT_EMAIL_FIRST_PART = "Hi ,\n\nYou're receiving this email " +
			"because you requested a password reset for your ANSTO Python Scripting Account. " +
			"If you did not request this change, you can safely ignore this email. \nTo choose " +
			"a new password and complete your request, please follow the link below: \n\n";
	private static final String TEXT_EMAIL_SECOND_PART = "\n\nPlease do not reply to this message.\n" +
			"\n" + "Thanks,\nThe Gumtree Team at ANSTO";
	private static final long TIME_EXP = 3600000;
	private static final String HTML_PASSWORDFORM_TEMP = "<form id=\"password_form\" method=\"post\" " +
			"action=\"jython/user?type=PASSWORD\" name=\"password_form\" accept-charset=\"utf-8\">" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"scrollTable\" id=\"table_password\">" +
                    "<tbody class=\"scrollContent table_jython_content\" id=\"password_body\">" +
                        "<tr><td colspan=\"2\"><div class=\"table_header\">Set New Password</div></td></tr>" +
                        "<tr><td>Password: </td><td><input type=\"password\" id=\"login_password\" name=\"login_password\" autocomplete=\"off\"></td></tr>" + 
                        "<tr><td>Re-enter Password: </td><td><input type=\"password\" id=\"login_repassword\" name=\"login_repassword\"></td></tr>" +
                        "<tr><td></td><td><input type=\"button\" class=\"buttonStyle\" data-role=\"button\" id=\"password_submit\" value=\"Submit\"></td></tr>" +
                    "</tbody></table>";
	private static final String HTML_FORMEND = "</form>";
	private static final int COOKIE_EXP_SECONDS = 86400;
			
	private JythonRunnerManager runnerManager;
	private INbiPersistenceManager persistence;
	private static Logger logger = LoggerFactory.getLogger(UserManagerRestlet.class);
	
	enum QueryType {
		INFO,
		LOGIN,
		REGISTER,
		RESET,
		PASSWORD,
		CHANGEPASSWORD,
		LOGOUT,
		LISTACCOUNTS,
		ADDACCOUNT
	}

	/**
	 * @param context
	 */
	public UserManagerRestlet(Context context) {
		super(context);
		runnerManager = new JythonRunnerManager();
		persistence = new NbiPersistenceManager();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {
	}
	
	@Override
	public void handle(final Request request, final Response response) {
		
        Form queryForm = request.getResourceRef().getQueryAsForm();
	    String typeString = queryForm.getValues(QUERY_TYPE);
	    
	    String directIp = request.getClientInfo().getUpstreamAddress();
		logger.error("direct IP = " + directIp);
		Object header = request.getAttributes().get("org.restlet.http.headers");
		if (header != null) {
			Form qform = (Form) header;
			logger.error("Form = " + header.toString());
			String forwardedIp = qform.getFirstValue("X-Forwarded-For");
			if (forwardedIp != null) {
				forwardedIp = forwardedIp.split(" ")[0].trim();
				logger.error("forward ip = " + forwardedIp);
			}
		}
		
//	    String uuidString = queryForm.getValues(QUERY_UUID);
//	    JythonRunner runner = null;
//	    if (uuidString != null) {
//	    	UUID uuid = UUID.fromString(uuidString);
//	    	runner = runnerManager.getJythonRunner(uuid);
//	    } 
//	    if (runner == null) {
//			JSONObject jsonObject;
//			try {
//		    	jsonObject = jumpToLogin();
//	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
//			} catch (JSONException e) {
//	    		e.printStackTrace();
//	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//			}
//	    	return;
//	    }
	    QueryType type = QueryType.valueOf(typeString);
		final JSONObject result = new JSONObject();
	    switch (type) {
	    case INFO:
		    String uuidString = null;
		    try {
			    Cookie cookie = request.getCookies().getFirst(UserManagerRestlet.COOKIE_NAME_UUID + "." + System.getProperty(PROPERTY_INSTRUMENT_ID) );
			    if (cookie != null) {
			    	uuidString = cookie.getValue();
			    }
			} catch (Exception e) {
			}
		    if (uuidString != null) {
		    	String email = persistence.retrieve(NbiPersistenceManager.ID_SESSION_EMAIL_DATABASE, uuidString, String.class);
		    	if (email != null) {
		    		try {
		    			result.put(RESPOND_RESULT, "OK");
		    			result.put("email", email);
		    			response.setStatus(Status.SUCCESS_OK);
		    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
		    		} catch (JSONException e) {
		    			e.printStackTrace();
		    			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		}
		    	} else {
		    		try {
						result.put(RESPOND_RESULT, "can't find session id");
		    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					}
		    	}
		    }
	    	break;
		case LOGIN:
	    	//		Form form = request.getResourceRef().getQueryAsForm();
		    Representation entity = request.getEntity();
	    	Form form = new Form(entity);
	    	String email = form.getValues(QUERY_EMAIL);
	    	String password = form.getValues(QUERY_PASSWORD);
	    	if (checkLogin(email, password)){
	    		try {
	    			uuidString = persistence.retrieve(NbiPersistenceManager.ID_EMAIL_SESSION_DATABASE, email, String.class);
	    			CookieSetting cookie = createUUIDCookie(email, uuidString);
	    	        response.getCookieSettings().add(cookie);
	    			result.put("result", "OK");
	    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
	    	        response.setStatus(Status.SUCCESS_OK);
//	    	        response.redirectPermanent("/pyscript.html");
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
			break;
		case REGISTER:
		    entity = request.getEntity();
	    	form = new Form(entity);
	    	email = form.getValues(QUERY_EMAIL);
	    	password = form.getValues(QUERY_PASSWORD);
	    	String regCode = form.getValues(QUERY_REGISTER_CODE);
	    	
	    	try {
				if (checkRegisterInputs(email, password, regCode, result)){
					register(email, password);
//    				UUID uuid = createJythonRunner();
//    				CookieSetting cS = new CookieSetting(0, COOKIE_NAME_UUID, uuid.toString(), 
//    						COOKIE_PATH_UUID, null, COOKIE_COMMENT_UUID, 1800, false);
    				CookieSetting cookie = createUUIDCookie(email, null);
    				response.getCookieSettings().add(cookie);
    				result.put("result", "OK");
				}
				response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
				response.setStatus(Status.SUCCESS_OK);
			} catch (JSONException e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			break;
		case PASSWORD:
			email = queryForm.getValues(QUERY_EMAIL);
			String rawCode = queryForm.getValues(QUERY_CODE);
			if (rawCode == null) {
				try {
					result.put(RESPOND_RESULT, "Error: empty reset code.");
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					return;
				} catch (JSONException e) {
				}
			}
			String code = null;
			try {
				code = EncryptionUtils.decryptBase64(rawCode);				
			} catch (Exception e) {
				try {
					result.put("result", "Error: the reset code is invalid.");
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					return;
				} catch (JSONException e1) {
				}
			}
			try {
				String savedCode = persistence.retrieve(NbiPersistenceManager.ID_RESET_DATABASE, email, String.class);
				if (savedCode == null){
					result.put("result", "Error: the reset code is invalid.");
				} else if (!savedCode.equals(code)){
					result.put("result", "Error: the reset code is invalid.");
				} else {
					long time = Long.valueOf(code);
					time = System.currentTimeMillis() - time;
					if (time < 0 || time > TIME_EXP){
						result.put(RESPOND_RESULT, "Error: the reset code has expired.");
					} else {
						result.put(RESPOND_RESULT, "OK");
						String htmlResult = HTML_PASSWORDFORM_TEMP;
						htmlResult += "<input type=\"hidden\" name=\"login_email\" value=\"" + email + "\">";
						htmlResult += "<input type=\"hidden\" name=\"code\" value=\"" + rawCode + "\">";
						htmlResult += HTML_FORMEND;
						result.put("html", htmlResult);
						response.setStatus(Status.SUCCESS_OK);
					}
				}
				response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			break;
		case CHANGEPASSWORD:
			email = queryForm.getValues(QUERY_EMAIL);
			rawCode = queryForm.getValues(QUERY_CODE);
			if (rawCode == null) {
				try {
					result.put(RESPOND_RESULT, "Error: empty reset code.");
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					return;
				} catch (JSONException e) {
				}
			}
			code = null;
			try {
				code = EncryptionUtils.decryptBase64(rawCode);				
			} catch (Exception e) {
				try {
					result.put("result", "Error: the reset code is invalid.");
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					return;
				} catch (JSONException e1) {
				}
			}
			try {
				String savedCode = persistence.retrieve(NbiPersistenceManager.ID_RESET_DATABASE, email, String.class);
				if (savedCode == null){
					result.put("result", "Error: the email account doesn't exist in our system.");
				} else if (!savedCode.equals(code)){
					result.put("result", "Error: the reset code is invalid.");
				} else {
					long time = Long.valueOf(code);
					time = System.currentTimeMillis() - time;
					if (time < 0 || time > TIME_EXP){
						result.put(RESPOND_RESULT, "Error: the reset code has expired.");
					} else {
					    entity = request.getEntity();
				    	form = new Form(entity);
				    	password = form.getValues(QUERY_PASSWORD);
				    	if (password.length() < 6 || password.length() > 12){
			    			try {
								result.put("result", "Password length should be between 6 and 12.");
							} catch (JSONException e) {
							}
				    	} else {
				    		try {
				    			changePassword(email, password);
				    			result.put("result", "OK");
				    			response.setStatus(Status.SUCCESS_OK);
				    		} catch (Exception e) {
				    			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				    		}						
				    	}
					}
				}
				response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			break;
		case RESET:
		    entity = request.getEntity();
	    	form = new Form(entity);
	    	email = form.getValues(QUERY_EMAIL);
	    	password = form.getValues(QUERY_PASSWORD);
	    	if (!email.contains("@") || !email.contains(".")){
    			try {
					result.put("result", "Email address is not valid.");
				} catch (JSONException e) {
				}
    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
	    	} else {
	    		String passString = null;
	    		try {
	    			passString = persistence.retrieve(NbiPersistenceManager.ID_USER_DATABASE, email, String.class);
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    		if (passString == null) {
	    			try {
						result.put("result", "Email address doesn't exist in our system. Please try another email address.");
					} catch (JSONException e) {
					}
	    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);						
	    		} else {
	    			String timeString = String.valueOf(System.currentTimeMillis());
	    			try {
						persistence.persist(NbiPersistenceManager.ID_RESET_DATABASE, email, timeString);
						timeString = EncryptionUtils.encryptBase64(timeString);
						System.err.println(timeString);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
	    			try {
	    				String instrument = System.getProperty(PROPERTY_INSTRUMENT_ID);
	    				String resetLink = "http://www.nbi.ansto.gov.au/" + instrument + "/status/password.html?type=PASSWORD&" + QUERY_EMAIL + "=" 
	    					+ URLEncoder.encode(email, "UTF-8") + "&" + QUERY_CODE + "=" + URLEncoder.encode(timeString, "UTF-8");
		    			email(email, resetLink);
	    				result.put("result", "An email has been sent to your email address. Please follow the direction in the email to reset your password.");
	    				response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
	    				response.setStatus(Status.SUCCESS_OK);
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
	    			}						
	    		}
	    	}
			break;
		case LOGOUT:
			uuidString = null;
		    try {
			    Cookie cookie = request.getCookies().getFirst(UserManagerRestlet.COOKIE_NAME_UUID + "." + System.getProperty(PROPERTY_INSTRUMENT_ID));
			    if (cookie != null) {
			    	uuidString = cookie.getValue();
			    }
			} catch (Exception e) {
			}
		    if (uuidString != null) {
		    	email = persistence.retrieve(NbiPersistenceManager.ID_SESSION_EMAIL_DATABASE, uuidString, String.class);
		    	if (email != null) {
		    		try {
		    			clearSession(email, uuidString);
		    			result.put(RESPOND_RESULT, "OK");
		    			response.setStatus(Status.SUCCESS_OK);
		    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
		    		} catch (JSONException e) {
		    			e.printStackTrace();
		    			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		}
		    	} else {
		    		try {
						result.put(RESPOND_RESULT, "can't find session id");
		    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					} catch (JSONException e) {
						e.printStackTrace();
						response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
					}
		    	}
		    }
			break;
		case ADDACCOUNT:
		    entity = request.getEntity();
	    	form = new Form(entity);
	    	email = form.getValues(QUERY_EMAIL);
	    	if (!email.contains("@") || !email.contains(".")){
    			try {
					result.put("result", "Email address is not valid.");
				} catch (JSONException e) {
				}
    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
	    	} else {
	    		regCode = null;
	    		try {
	    			regCode = persistence.retrieve(NbiPersistenceManager.ID_ADMIN_DATABASE, email, String.class);
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    		if (regCode != null) {
	    			try {
						result.put("result", "Email address already exists.");
					} catch (JSONException e) {
					}
	    			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
	    		} else {
	    			try {
	    				regCode = createCode(email);
	    				result.put("result", "OK");
	    				result.put("code", regCode);
	    				String registerLink = getAccountLink(email, regCode);
	    				result.put("link", registerLink);
	    				response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
	    				response.setStatus(Status.SUCCESS_OK);
	    			} catch (Exception e) {
	    				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
	    			}						
	    		}
	    	}
			break;
		case LISTACCOUNTS:
			ObjectSet<String> accountList = persistence.list(NbiPersistenceManager.ID_ADMIN_DATABASE, String.class);
			JSONArray array = new JSONArray();
			for (Object res : accountList) {
				try {
					String key = ((PersistentEntry) res).getKey();
					String passString = null;
					try {
						passString = persistence.retrieve(NbiPersistenceManager.ID_USER_DATABASE, key, String.class);
					} catch (Exception e) {
					}
					JSONObject json = makeJsonResult(key, ((PersistentEntry) res).getData().toString(), passString != null);
					array.put(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				result.put("result", "OK");
				result.put("list", array);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
			response.setStatus(Status.SUCCESS_OK);
			break;
		default:
			break;
		}
	}
	
	private boolean checkRegisterInputs(String email, String password,
			String regCode, JSONObject result) throws JSONException {
		if (!email.contains("@") || !email.contains(".")){
			result.put("result", "Email address is not valid.");
			return false;
		}
		if (password.length() < 6 || password.length() > 12){
			result.put("result", "Password length should be between 6 and 12.");
			return false;
		} 
		String passString = null;
		try {
			passString = persistence.retrieve(NbiPersistenceManager.ID_USER_DATABASE, email, String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (passString != null) {
			result.put("result", "Email address already exists in our system. Please try login instead.");
			return false;
		} 
		String existingCode = null;
		try {
			existingCode = persistence.retrieve(NbiPersistenceManager.ID_ADMIN_DATABASE, email, String.class);						
		} catch (Exception e) {
		}
		if (existingCode == null) {
			result.put("result", "The email has not been issued an invitation code.");
			return false;
		}
		if (!existingCode.equals(regCode)) {
			result.put("result", "The invitation code is not valid. Please contact IT administrator of the Bragg Institute.");
			return false;
		}
		return true;
	}

	private String getAccountLink(String email, String code) throws UnsupportedEncodingException {
		String instrument = System.getProperty(PROPERTY_INSTRUMENT_ID);
		return "http://www.nbi.ansto.gov.au/" + instrument + "/status/register.html?" + QUERY_EMAIL + "=" 
				+ URLEncoder.encode(email, "UTF-8") + "&" + QUERY_CODE + "=" + URLEncoder.encode(code, "UTF-8");
	}
	
	private JSONObject makeJsonResult(String key, String data, boolean isRegistered) throws JSONException, UnsupportedEncodingException {
		JSONObject obj = new JSONObject();
		obj.put("email", key);
		obj.put("code", data);
		obj.put("status", isRegistered);
		obj.put("link", getAccountLink(key, data));
		return obj;
	}

	private String createCode(String email) {
		String uuid = null;
		try {
			uuid = UUID.randomUUID().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		persistence.persist(NbiPersistenceManager.ID_ADMIN_DATABASE, email, uuid);
		return uuid;
	}

	private void clearSession(String email, String uuidString) {
		if (uuidString != null) {
			runnerManager.deleteJythonRunner(UUID.fromString(uuidString));
//			persistence.remove(NbiPersistenceManager.ID_EMAIL_SESSION_DATABASE, email);
//			persistence.remove(NbiPersistenceManager.ID_SESSION_EMAIL_DATABASE, uuidString);
		}
	}

	private CookieSetting createUUIDCookie(String email, String uuidString) {
		UUID uuid = createJythonRunner(uuidString);
		CookieSetting cookie = new CookieSetting(0, COOKIE_NAME_UUID + "." + System.getProperty(PROPERTY_INSTRUMENT_ID), uuid.toString(), 
				"/", null, COOKIE_COMMENT_UUID, COOKIE_EXP_SECONDS, false);
		if (uuidString == null) {
			persistence.persist(NbiPersistenceManager.ID_SESSION_EMAIL_DATABASE, uuid.toString(), email);
			persistence.persist(NbiPersistenceManager.ID_EMAIL_SESSION_DATABASE, email, uuid.toString());
		}
		return cookie;
	}

	private void email(String email, String resetLink) {
		// Recipient's email ID needs to be mentioned.
		String to = email;

		// Sender's email ID needs to be mentioned
		String from = NAME_EMAIL_SENDER;

		// Assuming you are sending email from localhost
		String host = NAME_EMAIL_HOST;

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty(NAME_EMAIL_SERVICE, host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try{
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO,
					new InternetAddress(to));

			// Set Subject: header field
			message.setSubject("ANSTO Python Scripting Account Password Help");

			// Now set the actual message
			message.setText(TEXT_EMAIL_FIRST_PART + resetLink + TEXT_EMAIL_SECOND_PART);

			// Send message
			Transport.send(message);
			System.out.println("Sent message successfully....");
		}catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	private UUID createJythonRunner(String uuidString) {
		JythonRunner runner;
		if (uuidString != null) {
			runner = runnerManager.getJythonRunner(UUID.fromString(uuidString));
			if (runner == null) {
				runner = runnerManager.getNewRunner(uuidString);
			}
		} else {
			runner = runnerManager.getNewRunner();
		}
		String userPath = runner.getUserPath();
		File userFolder = new File(userPath);
		if (!userFolder.exists()) {
			userFolder.mkdirs();
		}
		return runner.getUuid();
	}

	private void register(String email, String password) {
		String passString = null;
		try {
			passString = EncryptionUtils.encryptBase64(password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		persistence.persist(NbiPersistenceManager.ID_USER_DATABASE, email, passString);
	}

	private void changePassword(String email, String password) {
		String passString = null;
		try {
			passString = EncryptionUtils.encryptBase64(password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		persistence.persist(NbiPersistenceManager.ID_USER_DATABASE, email, passString);
		persistence.remove(NbiPersistenceManager.ID_RESET_DATABASE, email);
	}

//	private boolean validateRegister(String email, String password) {
//		if (email.contains("@")){
//			if (password.length() > 5){
//				String passString = null;
//				try {
//					persistence.retrieve(email, String.class);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				if (passString == null) {
//					return true;
//				}
//			}
//			
//		}
//		return false;
//	}

	private boolean checkLogin(String email, String password) {
		String passString = persistence.retrieve(NbiPersistenceManager.ID_USER_DATABASE, email, String.class);
		if (passString != null) {
			String pass = null;
			try {
				pass = EncryptionUtils.decryptBase64(passString);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (password.equals(pass)){
				return true;
			}
		}
		return false;
	}

}
