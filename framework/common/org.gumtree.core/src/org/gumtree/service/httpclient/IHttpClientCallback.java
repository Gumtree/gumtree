package org.gumtree.service.httpclient;

import java.io.InputStream;

public interface IHttpClientCallback {

	public void handleResponse(InputStream in);
	
	public void handleError();
	
}
