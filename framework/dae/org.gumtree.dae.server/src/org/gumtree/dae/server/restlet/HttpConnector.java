package org.gumtree.dae.server.restlet;

import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class HttpConnector implements IHttpConnector {
	
	private HttpClient client;
	
	private Map<String, String> parameters;
	
	public HttpConnector() {
		super();
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public HttpClient getClient() {
		if (client == null) {
			client = new HttpClient();
			
			// Set proxy if available
			String proxyHost = getParameters().get(KEY_PROXY_HOST);
			String proxyPort = getParameters().get(KEY_PROXY_PORT);
			if (proxyHost != null && proxyPort != null) {
				client.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort));
			}
			
			// Set credentials if login information supplied
			client.getParams().setAuthenticationPreemptive(true);
			String user = getParameters().get(KEY_LOGIN);
			String password = getParameters().get(KEY_PASSWORD);
			if (user != null && password != null) {
				Credentials defaultcreds = new UsernamePasswordCredentials(
						user, password);
				client.getState().setCredentials(AuthScope.ANY, defaultcreds);
			}
		}
		return client;
	}
	
}
