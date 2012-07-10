package org.gumtree.service.httpclient;

import java.io.InputStream;

public abstract class HttpClientAdapter implements IHttpClientCallback {

	@Override
	public void handleResponse(InputStream in) {
	}

	@Override
	public void handleError() {
	}

}
