package org.gumtree.service.httpclient.support;

import org.eclipse.core.net.proxy.IProxyService;
import org.gumtree.service.httpclient.IHttpClient;
import org.gumtree.service.httpclient.IHttpClientFactory;

public class HttpClientFactory implements IHttpClientFactory {

	private IProxyService proxyService;

	public HttpClientFactory() {
		super();
	}

	@Override
	public IHttpClient createHttpClient() {
		return createHttpClient(1);
	}

	@Override
	public IHttpClient createHttpClient(int numberOfThreads) {
		DefaultHttpClient client = new DefaultHttpClient(numberOfThreads);
		client.setProxyService(getProxyService());
		return client;
	}

	public IProxyService getProxyService() {
		return proxyService;
	}

	public void setProxyService(IProxyService proxyService) {
		this.proxyService = proxyService;
	}

}
