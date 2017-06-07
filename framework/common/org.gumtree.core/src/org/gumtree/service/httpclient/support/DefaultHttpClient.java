package org.gumtree.service.httpclient.support;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.gumtree.service.httpclient.IHttpClient;
import org.gumtree.service.httpclient.IHttpClientCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHttpClient implements IHttpClient {

	private static final URI DEFAULT_URI = URI.create("http://www.eclipse.org");

	private static Logger logger = LoggerFactory
			.getLogger(DefaultHttpClient.class);

	private IProxyService proxyService;

	private ExecutorService executor;

	private volatile HttpClient httpClient;

	public DefaultHttpClient() {
		this(1);
	}

	public DefaultHttpClient(int numberOfThreads) {
		executor = Executors.newFixedThreadPool(numberOfThreads);
	}

	public void performGet(final URI uri, final IHttpClientCallback callback) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				HttpClient client = getHttpClient();
				GetMethod getMethod = new GetMethod(uri.toString());
				try {
					int statusCode = client.executeMethod(getMethod);
					if (statusCode != HttpStatus.SC_OK) {
						logger.error("HTTP GET failed: "
								+ getMethod.getStatusLine());
						getMethod.releaseConnection();
					}
					if (callback != null) {
						callback.handleResponse(getMethod
								.getResponseBodyAsStream());
					}
				} catch (Exception e) {
					logger.error("HTTP GET failed: " + uri.toString(), e);
				}
			}
		});
	}

	public void performGet(final URI uri, final IHttpClientCallback callback, final String username, final String passwd) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				HttpClient client = getHttpClient();
				GetMethod getMethod = new GetMethod(uri.toString());
			    client.getParams().setAuthenticationPreemptive(true);
			    Credentials defaultcreds = new UsernamePasswordCredentials(username, passwd);
			    client.getState().setCredentials(AuthScope.ANY, defaultcreds);
				try {
					int statusCode = client.executeMethod(getMethod);
					if (statusCode != HttpStatus.SC_OK) {
						logger.error("HTTP GET failed: "
								+ getMethod.getStatusLine());
						getMethod.releaseConnection();
					}
					if (callback != null) {
						callback.handleResponse(getMethod
								.getResponseBodyAsStream());
					}
				} catch (Exception e) {
					logger.error("HTTP GET failed: " + uri.toString(), e);
				}
			}
		});
	}

	public HttpClient getHttpClient() {
		if (httpClient == null) {
			synchronized (this) {
				if (httpClient == null) {
					httpClient = new HttpClient();
					if (getProxyService() != null
							&& getProxyService().isProxiesEnabled()) {
						// TODO: support proxy data with different URI
						IProxyData[] proxyData = getProxyService().select(
								DEFAULT_URI);
						if (proxyData.length > 0) {
							httpClient.getHostConfiguration().setProxy(
									proxyData[0].getHost(),
									proxyData[0].getPort());
						}
					}
				}
			}
		}
		return httpClient;
	}

	@Override
	public void disposeObject() {
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
		proxyService = null;
		httpClient = null;
	}

	public IProxyService getProxyService() {
		return proxyService;
	}

	public void setProxyService(IProxyService proxyService) {
		this.proxyService = proxyService;
	}

	@Override
	public void setProxy(String hostName, int port) {
		getHttpClient().getHostConfiguration().setProxy(hostName, port);
	}

}
