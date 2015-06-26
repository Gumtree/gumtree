package org.gumtree.service.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.gumtree.service.db.RecordsFileException;

public class RemoteTextDbService {

	private static final String PROP_LOGGINGDB_URL = "gumtree.loggingDB.url";
	private static RemoteTextDbService instance;
	
	private String loggingDbURL; 
			
	public synchronized static RemoteTextDbService getInstance() {
		synchronized (RemoteTextDbService.class) {
			if (instance == null) {
				instance = new RemoteTextDbService();
			} 
			return instance;
		}
	}
	
	public RemoteTextDbService() {
		loggingDbURL = System.getProperty(PROP_LOGGINGDB_URL);
	}

	public void appendImageEntry(String name, BufferedImage image, String footer) 
			throws IOException, RecordsFileException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", out);
		byte[] bytes = out.toByteArray();
		byte[] base64bytes = Base64.encodeBase64(bytes);
		String html = "<img src=\"data:image/png;base64," + new String(base64bytes) + "\" alt=\"" + name + "\">";
		if (footer != null) {
			html += "<span class=\"class_span_tablefoot\">" + footer + "</span><br>";			
		}
		String className = "class_db_image";
		appendClassEntry(name, className, html);
	}

	public void appendImageEntry(String name, String imageText, String footer) 
			throws IOException, RecordsFileException {
		String html = "<img src=\"data:image/png;base64," + imageText + "\" alt=\"" + name + "\">";
		if (footer != null) {
			html += "<span class=\"class_span_tablefoot\">" + footer + "</span><br>";			
		}
		String className = "class_db_image";
		appendClassEntry(name, className, html);
	}

	public void appendTextEntry(String name, String text) 
			throws IOException, RecordsFileException {
		String className = "class_db_text";
		appendClassEntry(name, className, text);
	}

	public void appendTableEntry(String name, String tableText) 
			throws IOException, RecordsFileException {
		String className = "class_db_table";
		appendClassEntry(name, className, tableText);
	}
	
	public void appendClassEntry(String name, String className, String text) throws IOException, RecordsFileException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		String key = name + "-" + dateFormat.format(new Date());
		appendHtmlEntry(key, (wrapToHtml(key, className, text)));		
	}

	private String wrapToHtml(String key, String className, String html) {
		String output = "<div class=\"" + className + " class_db_object\" id=\"" + key + "\">";
		output += html;
		output += "</div>";
		return output;
	}
	
	public synchronized void appendHtmlEntry(String key, String html) throws HttpException {
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(loggingDbURL + "append");
		postMethod.addParameter("key", key);
		postMethod.addParameter("html", html);
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
