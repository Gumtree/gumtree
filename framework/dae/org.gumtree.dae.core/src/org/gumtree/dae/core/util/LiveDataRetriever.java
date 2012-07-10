package org.gumtree.dae.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.Platform;
import org.gumtree.dae.core.internal.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveDataRetriever implements ILiveDataRetriever {
	
	private static final String EXT_HDF = ".hdf"; 
	
	private static Logger logger = LoggerFactory.getLogger(LiveDataRetriever.class);
	
	private volatile HttpClient client;

	private String user;
	
	private String password;
	
	private String proxyHost;
	
	private Integer proxyPort;
	
	private URI baseLocation;
	
	public LiveDataRetriever() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#getUser()
	 */
	public String getUser() {
		return user;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#setUser(java.lang.String)
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#getProxyHost()
	 */
	public String getProxyHost() {
		return proxyHost;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#setProxyHost(java.lang.String)
	 */
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#getProxyPort()
	 */
	public Integer getProxyPort() {
		return proxyPort;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#setProxyPort(java.lang.Integer)
	 */
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#getBaseLocation()
	 */
	public URI getBaseLocation() {
		if (baseLocation == null) {
			URL url = Platform.getInstanceLocation().getURL();
			try {
				baseLocation = url.toURI();
//				baseLocation = Platform.getConfigurationLocation().getURL().toURI();
			} catch (URISyntaxException e) {
				// very unlikely for this to happen
				try {
					baseLocation = new URI(url.getProtocol(), url.getUserInfo(), url
							.getHost(), url.getPort(), url.getPath(), url
							.getQuery(), url.getRef());
				} catch (URISyntaxException e2) {
					// The URL is broken beyond automatic repair
					logger.error("Cannot get default base location.", e);
					throw new RuntimeException("Cannot get default base location.", e);
				}
			}
		}
		return baseLocation;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#setBaseLocation(java.net.URI)
	 */
	public void setBaseLocation(URI baseLocation) {
		this.baseLocation = baseLocation;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.hm.utils.ILiveDataRetriever#getDataFileHandle()
	 */
	public synchronized URI getHDFFileHandle(String host, int port, HistogramType type) throws Exception {
		// Get file via HTTP
		GetMethod getMethod = new GetMethod("http://" + host + ":" + port + "/admin/savedataview.egi?type=" + type.toString() + "&data_format=NEXUSHDF5_LZW_6&data_saveopen_action=OPEN_ONLY");
		getMethod.setDoAuthentication(true);
		int statusCode = getClient().executeMethod(getMethod);
		if (statusCode != HttpStatus.SC_OK) {
			logger.error("HTTP GET failed: " + getMethod.getStatusLine());
			getMethod.releaseConnection();
			throw new Exception("Cannot get file");
		}
		
		// Prepare local cache file
		// We rely on our time stamp since the HM server does not always provide file with time stamp
		SimpleDateFormat dateFormat = new SimpleDateFormat("'HSD_'yyyy-MM-dd'T'HH-mm-ss-SSS'.nx" + EXT_HDF + "'");
		// Base location (default to osgi instance location)
		File baseLocation = new File(getBaseLocation());
		// We need this to get JUnit to work
		if (!baseLocation.exists()) {
			baseLocation.mkdir();
		}
		// Plugin folder (hidden)
		File parentLocation = new File(baseLocation, "." + Activator.PLUGIN_ID);
		// Create folder if necessary
		if (!parentLocation.exists()) {
			parentLocation.mkdir();
		}
		File file = new File(parentLocation, dateFormat.format(Calendar.getInstance().getTime()));
		FileOutputStream out = new FileOutputStream(file);
		
		// Save file into disk
		out.write(getMethod.getResponseBody());
		out.close();
		
		// Clean up
		getMethod.releaseConnection();
		
		return file.toURI();
	}
	
	private HttpClient getClient() {
		if (client == null) {
			synchronized (LiveDataRetriever.class) {
				if (client == null) {
					client = new HttpClient();
					
					// Set proxy if available
					if (getProxyHost() != null && getProxyPort() != null) {
						client.getHostConfiguration().setProxy(getProxyHost(), getProxyPort());
					}
					
					// Set credentials if login information supplied
					client.getParams().setAuthenticationPreemptive(true);
					if (getUser() != null && getPassword() != null) {
						Credentials defaultcreds = new UsernamePasswordCredentials(
								getUser(), getPassword());
						client.getState().setCredentials(AuthScope.ANY, defaultcreds);
					}					
				}
			}
		}
		return client;
	}
	
	public void clearBuffer() {
		File baseLocation = new File(getBaseLocation());
		File parentLocation = new File(baseLocation, "." + Activator.PLUGIN_ID);
		if (parentLocation.exists()) {
			for (File file : parentLocation.listFiles()) {
				if (file.isFile() && file.getName().endsWith(EXT_HDF)) {
					try {
						file.delete();
					} catch (Exception e) {
						logger.error("Cannot delete file " + file.getName(), e);
					}
				}
			}
		}
	}
	
}
