package org.gumtree.server.util.jetty;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.gumtree.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyStarter implements IJettyStarter {

	private static final String ID_WEB_APP = "restlet";

	private static Logger logger = LoggerFactory.getLogger(JettyStarter.class);

	private boolean isStarted = false;

	public JettyStarter() {
		super();
	}

	@Override
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
				logger.info("Server " + ID_WEB_APP + " started on port "
						+ getPort());
			} catch (Exception e) {
				logger.error("Cannot start server " + ID_WEB_APP + " on port "
						+ getPort(), e);
			}
		} else {
			logger.info("Restlet Jetty server is disabled by current configuration.");
		}
	}

	@Override
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

	@Override
	public int getPort() {
		return ServerProperties.SERVER_PORT.getInt();
	}

	@Override
	public void setPort(int port) {
		ServerProperties.SERVER_PORT.setInt(port).save();
	}

	@Override
	public boolean isEnable() {
		return ServerProperties.SERVER_ENABLE.getBoolean();
	}

	@Override
	public void setEnable(boolean enable) {
		ServerProperties.SERVER_ENABLE.setBoolean(enable).save();
	}

}