package org.gumtree.dae.core.util;

import java.net.URI;

public interface ILiveDataRetriever {

	public String getUser();

	public void setUser(String user);

	public String getPassword();

	public void setPassword(String password);

	public String getProxyHost();

	public void setProxyHost(String proxyHost);

	public Integer getProxyPort();

	public void setProxyPort(Integer proxyPort);
	
	public URI getBaseLocation();

	public void setBaseLocation(URI baseLocation);

	public URI getHDFFileHandle(String host, int port, HistogramType type) throws Exception;

	/**
	 * Clear all .hdf files from the buffer directory. 
	 */
	public void clearBuffer();
	
}