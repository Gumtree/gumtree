package org.gumtree.service.directory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.gumtree.security.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TardisService {

	private final static String HOST = "https://tardis-auth.nbi.ansto.gov.au";
	private final static String TARDIS_PASSWORD = "gumtree.tardisSession";
	public final static String NAME_USERS_PROPOSAL = "list_proposals_for_user";
	public final static String NAME_PROPOSAL_USER = "list_users_for_proposal";
	public final static String NAME_PROPOSAL_FILES = "list_files_for_proposal";
	public  final static String NAME_USER_FILES = "list_files_for_user";
	private final static String PATH = "/api/?";
	private final static String KEY = "7zN3u/w=ABED=maq";
    private final static String SHADOW_ENTRY = "AAECAwQFBgcICQoL";
	private HttpClient client;
	private static Logger logger = LoggerFactory.getLogger(TardisService.class);
	private static TardisService instance;

	public TardisService() {
		getClient();
	}

	public static synchronized TardisService getTardisService() {
		if (instance == null) {
			instance = new TardisService();
		}
		return instance;
	}
	
	public String listFilesForUser(String username) throws IOException {
		String function = "function=" + NAME_USER_FILES;
		int atIndex = username.indexOf("@");
		if (atIndex > 0) {
			username = username.substring(0, atIndex);
		}
		String arg = "username=" + username;
		String res = runFunction(function, arg);
		return res;
	}
	
	public String listFilesForProposal(String proposalId) throws IOException {
		String function = "function=" + NAME_PROPOSAL_FILES;
		String arg = "proposal=" + proposalId;
		String res = runFunction(function, arg);
		return res;
	}
	
	public String listUsersForProposal(String proposalId) throws IOException {
		String function = "function=" + NAME_PROPOSAL_USER;
		String arg = "proposal=" + proposalId;
		String res = runFunction(function, arg);
		return res;
	}
	
	public String listProposalsForUser(String username) throws IOException {
		String function = "function=" + NAME_USERS_PROPOSAL;
		int atIndex = username.indexOf("@");
		if (atIndex > 0) {
			username = username.substring(0, atIndex);
		}
		String arg = "username=" + username;
		String res = runFunction(function, arg);
		return res;
	}
	
	private String runFunction(String function, String arg) throws IOException {
		String path = PATH + function + "&" + arg;
		String result = getLink(path);
		return result;
	}
	
	private String getLink(String path) throws IOException {
		GetMethod getMethod = new GetMethod(HOST);
		getMethod.setDoAuthentication(true);
		getMethod.setPath(path);
		try {
			int statusCode = getClient().executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
//				logger.error("HTTP GET failed: " + getMethod.getStatusLine());
//				throw new HttpException("cannot get function result");
				return null;
			} else {
				String resp = getMethod.getResponseBodyAsString();
				return resp;
			}			
		} finally {
			getMethod.releaseConnection();
		}
	}
	
	private synchronized HttpClient getClient()  {
		if (client == null) {
			client = new HttpClient();

			// Set credentials if login information supplied
			client.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = null;
			try {
				defaultcreds = new UsernamePasswordCredentials("gumtree", 
						EncryptionUtils.decryptBase64(System.getProperty(TARDIS_PASSWORD) + "=="));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			client.getState().setCredentials(AuthScope.ANY, defaultcreds);
		}
		return client;
	}
	

}
