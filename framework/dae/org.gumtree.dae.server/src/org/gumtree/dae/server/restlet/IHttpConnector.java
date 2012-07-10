package org.gumtree.dae.server.restlet;

import java.util.Map;

import org.apache.commons.httpclient.HttpClient;

public interface IHttpConnector {

	public static final String KEY_LOGIN = "login";
	
	public static final String KEY_PASSWORD = "password";
	
	public static final String KEY_PROXY_HOST = "proxyHost";
	
	public static final String KEY_PROXY_PORT = "proxyPort";
	
	public Map<String, String> getParameters();
	
	public void setParameters(Map<String, String> parameters);
	
	public HttpClient getClient();
	
}
