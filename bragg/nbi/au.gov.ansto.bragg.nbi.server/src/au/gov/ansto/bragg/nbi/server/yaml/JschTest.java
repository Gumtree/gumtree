package au.gov.ansto.bragg.nbi.server.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class JschTest {

	private static final Logger logger = LoggerFactory.getLogger(JschTest.class);
	
	public JschTest() {
	}

	public Session getJs(String ip, String user, String keyPath, String passphrase) throws JSchException {
		JSch.setLogger(new com.jcraft.jsch.Logger() {
			
			@Override
			public void log(int level, String message) {
				logger.debug(message);
			}
			
			@Override
			public boolean isEnabled(int level) {
				return true;
			}
		});
		JSch jsch = new JSch();
		jsch.addIdentity(keyPath, passphrase);
//		System.err.println(EncryptionUtils.encryptBase64(passphrase));
		Session jschSession = jsch.getSession(user, ip, 22);
		jschSession.setConfig("StrictHostKeyChecking", "no"); 
		return jschSession;
	}
}
