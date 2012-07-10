package org.gumtree.service.httpclient;

import java.net.URI;

import org.apache.commons.httpclient.HttpClient;
import org.gumtree.core.service.IService;

public interface IHttpClientFactory extends IService {

	public IHttpClient createHttpClient();
	
	public IHttpClient createHttpClient(int numberOfThreads);
	
}
