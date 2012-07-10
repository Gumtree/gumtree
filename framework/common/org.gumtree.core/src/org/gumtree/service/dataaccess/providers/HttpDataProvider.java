/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.service.dataaccess.providers;

import java.net.URI;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.InvalidResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDataProvider extends AbstractDataProvider<GetMethod>{

	private static final Logger logger = LoggerFactory.getLogger(HttpDataProvider.class);

	private static final String PROP_PROXY_HOST = "http.proxyHost";
	
	private static final String PROP_PROXY_PORT = "http.proxyPort";
	
	private static final String PROP_LOGIN = "login";
	
	private static final String PROP_PASSWORD = "password";
	
	private HttpClient clientWithNoProxy;
	
	private HttpClient clientWithProxy;
	
	public HttpDataProvider() {
		super();
		
		// For localhost or 127.0.0.1
		clientWithNoProxy = new HttpClient();
		
		// For external website
		clientWithProxy = new HttpClient();
		// Set proxy if available
		String proxyHost = System.getProperty(PROP_PROXY_HOST);
		String proxyPort = System.getProperty(PROP_PROXY_PORT);
		if (proxyHost != null && proxyPort != null) {
			clientWithProxy.getHostConfiguration().setProxy(proxyHost, Integer.parseInt(proxyPort));
		}
	}
	
	public <T> T get(URI uri, Class<T> representation, Map<String, Object> properties) throws DataAccessException {
		GetMethod getMethod = new GetMethod(uri.toString());
		
		HttpClient client = clientWithProxy;
		if (uri.getHost().equals("localhost") || uri.getHost().equals("127.0.0.1")) {
			client = clientWithNoProxy;
		}
		
		// Set credentials if login information supplied
		client.getParams().setAuthenticationPreemptive(true);
		String user = (String) properties.get(PROP_LOGIN);
		String password = (String) properties.get(PROP_PASSWORD);
		if (user != null && password != null) {
			Credentials defaultcreds = new UsernamePasswordCredentials(
					user, password);
			client.getState().setCredentials(AuthScope.ANY, defaultcreds);
		}
		
		getMethod.setDoAuthentication(true);
		int statusCode;
		try {
			statusCode = client.executeMethod(getMethod);
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
		if (statusCode != HttpStatus.SC_OK) {
			logger.error("Method GET failed: " + getMethod.getStatusLine());
			getMethod.releaseConnection();
			throw new InvalidResourceException(uri.toString());
		}
		return convert(getMethod, representation, null);
	}
	
}
