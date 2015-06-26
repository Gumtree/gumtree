package org.gumtree.service.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class LoggingDB {
	
	private final static String LOGGING_DB_FILENAME = "loggingDB";
	
	private static LoggingDB instance;
	
	private ObjectDBService db;
	
	public LoggingDB() {
		db = ObjectDBService.getDb(LOGGING_DB_FILENAME);
	}

	public synchronized static LoggingDB getInstance(){
		if (instance == null) {
			instance = new LoggingDB();
		}
		return instance;
	}
	
	public void archive(){
		db.newDBInstance();
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
	
	public synchronized void appendHtmlEntry(String key, String html) throws IOException, RecordsFileException {
		db.appendEntry(key, html);
	}
	
	public synchronized String getHtmlEntries(int start, int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		String html = "";
		List<Object> list = db.getEntries(start, length);
		for (Iterator<Object> iterator = list.iterator(); iterator.hasNext();) {
			String entry = iterator.next().toString();
			html += "\n" + entry;
		}
		return start + ":" + (start - length + 1) + ";" + html;
	}
	
	public synchronized String getEntries(int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		int start = db.getNumRecords() - 1;
		return getHtmlEntries(start, length);
	}
	
	
//	public int keyToIndex(String key) throws IOException {
//		int index = 0;
//		if (key == null || key.trim().length() == 0) {
//			return -1;
//		}
//		String cKey;
//		int numRecords = getNumRecords();
//		while (index < numRecords) {
//			cKey = readKeyFromIndex(index);
//			if (key.equals(cKey)) {
//				return index;
//			}
//			index++;
//		}
//		return -1;
//	}

	public static String ConvertXmlToHtmlTable(String xml) {
	  StringBuilder html = new StringBuilder("<table align='center' " + 
	     "border='1' class='xmlTable'>\r\n");
	  try
	  {
		  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder builder = factory.newDocumentBuilder();
		  InputSource is = new InputSource(new StringReader(xml));
		  Document root = builder.parse(is);
		  NodeList nodes = root.getChildNodes();

	      for (int i = 0; i < nodes.getLength(); i++) {
	    	  Node ele = nodes.item(i);
	          if (!ele.hasChildNodes()) {
	              String elename = "";
	              html.append("<tr>");

	              elename = ele.getNodeName();

	              if (ele.hasAttributes()) {
	                  NamedNodeMap attribs = ele.getAttributes();
	                  for (int j = 0; j < attribs.getLength(); j++) {
	                	  Node attr = attribs.item(j);
	                	  elename += "\n" + attr.getNodeName() + 
	                			  "=" + attr.getNodeValue();
	                  }
	              }

	              html.append("<td>" + elename + "</td>");
	              html.append("<td>" + ele.getNodeValue() + "</td>");
	              html.append("</tr>\r\n");
	          }
	          else
	          {
	              String elename = "";
	              html.append("<tr>");

	              elename = ele.getNodeName();

	              if (ele.hasAttributes()) {
	                  NamedNodeMap attribs = ele.getAttributes();
	                  for (int j = 0; j < attribs.getLength(); j++) {
	                	  Node attr = attribs.item(j);
	                	  elename += "\n" + attr.getNodeName() + 
	                			  "=" + attr.getNodeValue();
	                  }
	              }

	              html.append("<td>" + elename + "</td>");
	              html.append("<td>" + ConvertXmlToHtmlTable(ele.toString()) + "</td>");
	              html.append("</tr>\r\n");
	          }
	      }

	      html.append("</table>\r\n");
	  }
	  catch (Exception e)
	  {
	      return xml;
	      // Returning the original string incase of error.
	  }
	  return html.toString();
	}
	
}

