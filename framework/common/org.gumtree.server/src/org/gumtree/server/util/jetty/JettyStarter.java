package org.gumtree.server.util.jetty;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyStarter {

	private static final String ID_WEB_APP = "restlet";
	
	private static final int DEFAULT_PORT = 9876;
	
	private static Logger logger = LoggerFactory.getLogger(JettyStarter.class);
	
	private int port = DEFAULT_PORT;
	
	private boolean isStarted = false;
	
	private boolean enable = true;
	
	public JettyStarter() {
		super();
	}
	
	public void init() {
		if (isEnable()) {
			Dictionary<String, Object> d = new Hashtable<String, Object>();
			// configure the port
			d.put("http.port", new Integer(getPort())); //$NON-NLS-1$

			// set the base URL
			d.put("context.path", "/"); //$NON-NLS-1$ //$NON-NLS-2$

			try {
				JettyConfigurator.startServer(ID_WEB_APP, d);
				isStarted = true;
				logger.info("Server " + ID_WEB_APP + " started on port " + getPort());
			} catch (Exception e) {
				logger.error("Cannot start server " + ID_WEB_APP + " on port " + getPort(), e);
			}
		} else {
			logger.info("Restlet Jetty server is disabled by current configuration.");
		}
	}
	
	public void cleanup() {
		if (isStarted) {
			try {
				JettyConfigurator.stopServer(ID_WEB_APP);
				logger.info("Stopped server " + ID_WEB_APP);
			} catch (Exception e) {
				logger.error("Error in stopping server " + ID_WEB_APP);
			}
		}
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
}