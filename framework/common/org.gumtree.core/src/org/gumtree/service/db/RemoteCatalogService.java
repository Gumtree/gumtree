package org.gumtree.service.db;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

public class RemoteCatalogService {

	private static final String PROP_CATALOGDB_URL = "gumtree.catalogDB.url";
	private static RemoteCatalogService instance;
	
	private String catalogDbURL; 
			
	public synchronized static RemoteCatalogService getInstance() {
		synchronized (RemoteCatalogService.class) {
			if (instance == null) {
				instance = new RemoteCatalogService();
			} 
			return instance;
		}
	}
	
	public RemoteCatalogService() {
		catalogDbURL = System.getProperty(PROP_CATALOGDB_URL);
	}

	public void updateEntry(String key, Map<String, String> columns) throws IOException, RecordsFileException {
		JSONObject json = new JSONObject(columns);
		updateHtmlEntry(key, json.toString());		
	}
	
	public synchronized void updateHtmlEntry(String key, String html) throws HttpException {
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(catalogDbURL + "update");
		System.err.println(key);
		postMethod.addParameter("key", key);
		postMethod.addParameter("columns", html);
		try {
			httpClient.executeMethod(postMethod);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
			try {
				String resp = postMethod.getResponseBodyAsString();
				System.err.println(resp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new HttpException("failed to post form.");
		}
	}
	
}
