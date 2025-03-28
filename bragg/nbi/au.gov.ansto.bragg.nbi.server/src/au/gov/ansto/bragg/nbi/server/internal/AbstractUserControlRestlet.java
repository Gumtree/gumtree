package au.gov.ansto.bragg.nbi.server.internal;

import java.io.IOException;

import org.gumtree.service.db.ProposalDB;
import org.gumtree.service.db.RecordsFileException;
import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.server.login.UserSessionObject;

public abstract class AbstractUserControlRestlet extends Restlet {

	private final static String PROP_INSTRUMENT_ID = "gumtree.instrument.id";
	private static final String PROPERTY_NOTEBOOK_DAVIP = "gumtree.notebook.dav";
	private static final String PROPERTY_NOTEBOOK_ICSIP = "gumtree.notebook.ics";
	private static final String PROPERTY_NOTEBOOK_IPPREFIX = "137.157.";

	private static final Logger logger = LoggerFactory.getLogger(AbstractUserControlRestlet.class);
	
	private String instrumentId;
	private String[] allowedDavIps;
	private String[] allowedIcsIps;

	public AbstractUserControlRestlet(){
		this(null);
	}
	
	public AbstractUserControlRestlet(Context context) {
		super(context);
		instrumentId = System.getProperty(PROP_INSTRUMENT_ID);
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

	protected UserSessionObject checkDavSession(Request request) {
		if (allowedDavIps == null) {
			return null;
		}
		String directIp = request.getClientInfo().getUpstreamAddress();
		if (directIp != null) {
//			if (directIp != null && directIp.equals("127.0.0.1")) {
//				UserSessionObject session = new UserSessionObject();
//				session.setDAV(true);
//				session.setUserName(instrumentId);
//				session.setValid(true);
//				return session;
//			}
			for (int i = 0; i < allowedDavIps.length; i++) {
				if (directIp.equals(allowedDavIps[i])) {
					UserSessionObject session = new UserSessionObject();
					session.setDAV(true);
					session.setUserName(instrumentId);
					session.setValid(true);
					logger.info("direct IP allowed: " + directIp);
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
						session.setUserName(instrumentId);
						session.setValid(true);
						logger.info("forwarded IP allowed: " + forwardedIp);
						return session;
					}
				}
			}
		}
		UserSessionObject session = new UserSessionObject();
		session.appendMessage("sign in required");
		return session;
	}
	
	protected UserSessionObject checkIcsSession(Request request) {
		if (allowedIcsIps == null) {
			return null;
		}
		String directIp = request.getClientInfo().getUpstreamAddress();
		if (directIp != null) {
			for (int i = 0; i < allowedIcsIps.length; i++) {
				if (directIp.equals(allowedIcsIps[i])) {
					UserSessionObject session = new UserSessionObject();
					session.setICS(true);
					session.setUserName(instrumentId);
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
				for (int i = 0; i < allowedIcsIps.length; i++) {
					if (forwardedIp.equals(allowedIcsIps[i])) {
						UserSessionObject session = new UserSessionObject();
						session.setICS(true);
						session.setUserName(instrumentId);
						session.setValid(true);
						return session;
					}
				}
			}
		}
		return null;
	}
	
	protected boolean validateService(UserSessionObject session, String[] services) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		for (int i = 0; i < services.length; i++) {
			if (UserSessionService.verifyService(session, services[i])) {
				return true;
			}
		}
		return false;
	}

	protected boolean isManager(UserSessionObject session) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		return validateService(session, new String[] {
				UserSessionService.NAME_SERVICE_NOTEBOOKADMIN,
				UserSessionService.NAME_SERVICE_NOTEBOOKMANAGER
		});
	}
	
	protected boolean hasMotorConfigurePrivilege(UserSessionObject session) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		return validateService(session, new String[] {
				UserSessionService.NAME_SERVICE_MOTORCONFIGURATION
		});
	}

	protected boolean hasSEConfigurePrivilege(UserSessionObject session) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		return validateService(session, new String[] {
				UserSessionService.NAME_SERVICE_SECONFIGURATION
		});
	}

	protected boolean hasInstrumentConfigurePrivilege(UserSessionObject session, String instrumentId) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		return validateService(session, new String[] {
				UserSessionService.NAME_SERVICE_SECONFIGURATION
		});
	}
	
	protected boolean allowAccessCurrentPage(UserSessionObject session, String pageId, ProposalDB proposalDb) 
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

	protected boolean allowEditHistoryPage(UserSessionObject session, String pageId) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		if (isManager(session)) {
			return true;
		} 
		return false;
	}

	protected boolean allowEditHistoryProposal(UserSessionObject session, String proposalId) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		if (isManager(session)) {
			return true;
		} 
		return false;
	}

	protected boolean allowReadHistoryPage(UserSessionObject session, String pageId, ProposalDB proposalDb) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		if (isManager(session)) {
			return true;
		} else if (verifyPageAccess(session, pageId, proposalDb)) {
			return true;
		}
		return false;
	}

	protected boolean allowReadHistoryProposal(UserSessionObject session, String proposalId, ProposalDB proposalDb) 
			throws ClassNotFoundException, JSONException, RecordsFileException, IOException {
		if (isManager(session)) {
			return true;
		} else if (verifyProposalAccess(session, proposalId, proposalDb)) {
			return true;
		}
		return false;
	}

	protected boolean verifyPageAccess(UserSessionObject session, String pageId, ProposalDB proposalDb) 
			throws ClassNotFoundException, RecordsFileException, IOException, JSONException {
		String proposalId = proposalDb.findProposalId(pageId);
		if (proposalId == null) {
			return false;
		}
		return verifyProposalAccess(session, proposalId, proposalDb);
	}

	protected boolean verifyProposalAccess(UserSessionObject session, String proposalId, ProposalDB proposalDb) 
			throws ClassNotFoundException, RecordsFileException, IOException, JSONException {
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

}
