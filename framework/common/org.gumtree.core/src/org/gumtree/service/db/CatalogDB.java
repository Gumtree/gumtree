package org.gumtree.service.db;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class CatalogDB {
	
	private static final String PROP_CATALOG_TITLES = "gumtree.catalog.columnTitles";
	private static final String NAME_DB_CATALOGINDEX = "catalogIndex";
	private static final String NAME_COLUMN_TIMESTAMP = "_update_timestamp_";
	private static final String SPAN_FREFIX = "<span class=\"class_span_search_highlight\">";
	private static final String SPAN_END = "</span>";
	private static final String PROP_CATALOG_SAVEPATH = "gumtree.catalog.savePath";

	private ObjectDBService db;
	private ObjectDBService indexDb;
	private boolean inSearch = false;
	private CatalogProvider provider;
	
	public CatalogDB(String dbName) {
		db = ObjectDBService.getDb(System.getProperty(PROP_CATALOG_SAVEPATH), dbName);
		indexDb = ObjectDBService.getDb(System.getProperty(PROP_CATALOG_SAVEPATH), NAME_DB_CATALOGINDEX);
		String[] columns = System.getProperty(PROP_CATALOG_TITLES).split(",");
		for (int i = 0; i < columns.length; i++) {
			columns[i] = columns[i].split(":")[1].trim();
		}
		setProvider(new CatalogProvider(Arrays.asList(columns)));
	}

	public void setProvider(CatalogProvider provider) {
		this.provider = provider;
	}
	
	public synchronized static CatalogDB getInstance(String dbName){
		return new CatalogDB(dbName);
	}

	public static List<String> listDbNames() {
		String path = System.getProperty(PROP_CATALOG_SAVEPATH);
		File folder = new File(path);
		List<String> list = new ArrayList<String>();
		if (!folder.exists() || !folder.isDirectory()) {
			return list;
		} else {
			File[] files = folder.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (name.toLowerCase().endsWith(".rdf") 
							&& name.substring(0, name.indexOf(".rdf")).matches("\\d+")) {
						return true;
					} else {
						return false;						
					}
				}
			});
			
			Arrays.sort(files, new Comparator<File>() {
			    public int compare(File f1, File f2) {
			        return Long.compare(f2.lastModified(), f1.lastModified());
			    }
			});
			
			for (File file : files) {
				list.add(file.getName().replace(".rdf", ""));
			}
			return list;
		}
		
	}
	
	public boolean isEntryAvailable(String key) {
		return db.keyExists(key);
	}
	
	public void updateEntry(String key, List<String> valueList) 
			throws IOException, RecordsFileException, JSONException{
		if (valueList == null || valueList.size() != provider.getColumnNames().size()) {
			throw new RecordsFileException("columns don't match");
		}
		JSONObject json = new JSONObject();
		int i = 0;
		for (String columnName : provider.getColumnNames()){
			json.put(columnName, valueList.get(i));
		}
		json.put(NAME_COLUMN_TIMESTAMP, System.currentTimeMillis());
		updateHtmlEntry(key, json.toString());

//		if (valueList == null || valueList.size() != provider.getColumnNames().size()) {
//			throw new RecordsFileException("columns don't match");
//		}
//		JSONObject json = new JSONObject();
//		json.put(key, value);
//		StringBuilder html = new StringBuilder("<tr>");
//		html.append("<th>" + key + "</th>");
//		for (String value : valueList) {
//			html.append("<td>" + (value != null ? value : "") + "</td>");
//		}
//		html.append("</tr>");
//		updateHtmlEntry(key, html.toString());
	}

	public void updateEntry(String key, String[] values) 
			throws JSONException, RecordsFileException, IOException{
		if (values == null || values.length != provider.getColumnNames().size()) {
			throw new RecordsFileException("columns don't match");
		}
		JSONObject json = new JSONObject();
		int i = 0;
		for (String columnName : provider.getColumnNames()){
			json.put(columnName, values[i++]);
		}
		json.put(NAME_COLUMN_TIMESTAMP, System.currentTimeMillis());
		updateHtmlEntry(key, json.toString());
//		StringBuilder html = new StringBuilder("<tr>");
//		html.append("<th>" + key + "</th>");
//		for (String value : values) {
//			html.append("<td>" + (value != null ? value : "") + "</td>");
//		}
//		html.append("</tr>");
//		updateHtmlEntry(key, html.toString());
	}

	public void updateColumnForEntry(String key, String columnName, String value) 
			throws ClassNotFoundException, IOException, RecordsFileException, JSONException 
			{
//		int index = provider.getIndex(columnName);
//		if (index < 0) {
//			throw new RecordsFileException("column does not exist");
//		}
//		String entry = null;
//		try {
//			entry = String.valueOf(db.getEntry(key));
//		} catch (Exception e) {
//		}
//		if (entry != null) {
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder builder = factory.newDocumentBuilder();
//			InputSource is = new InputSource(new StringReader(entry));
//			Document root = builder.parse(is);
//			NodeList nodes = root.getChildNodes();
//			nodes.item(index).setNodeValue(value);
//			updateEntry(key, nodes);
//		} else {
//			Map<String, String> values = new HashMap<String, String>();
//			values.put(key, value);
//			updateEntry(key, values);
//		}
		Map<String, String> values = new HashMap<String, String>();
		values.put(columnName, value);
		updateEntry(key, values);
	}
	
//	public void updateEntry(String key, NodeList values) throws IOException, RecordsFileException {
//		StringBuilder html = new StringBuilder("<tr>");
//		html.append("<th>" + key + "</th>");
//		for (int i = 0; i < values.getLength(); i++) {
//			String value = values.item(i).getNodeValue();
//			html.append("<td>" + (value != null ? value : "") + "</td>");
//		}
//		html.append("</tr>");
//		updateHtmlEntry(key, html.toString());
//	}
	
	public void updateEntry(String key, Map<String, String> values) throws IOException, RecordsFileException, ClassNotFoundException, JSONException {
		JSONObject json = null;
		if (db.keyExists(key)) {
			String currentEntry = db.getEntry(key).toString();
			json = new JSONObject(currentEntry);
			for (String columnkey : values.keySet()) {
				json.put(columnkey, values.get(columnkey));
			}
		} else {
			json = new JSONObject();
			for (String columnName : provider.getColumnNames()){
				String value = values.get(columnName);
				if (value == null) {
					value = "";
				}
				json.put(columnName, value);
			}
		}
		json.put(NAME_COLUMN_TIMESTAMP, System.currentTimeMillis());

//		StringBuilder html = new StringBuilder("<tr>");
//		html.append("<th>" + key + "</th>");
//		for (String name : provider.getColumnNames()) {
//			String value = values.get(name);
//			html.append("<td>" + (value != null ? value : "") + "</td>");
//		}
//		html.append("</tr>");
//		updateHtmlEntry(key, html.toString());
		updateHtmlEntry(key, json.toString());
	}
	
	public void updateEntry(String key, JSONObject values) throws IOException, RecordsFileException, ClassNotFoundException, JSONException {
		JSONObject json = null;
		if (db.keyExists(key)) {
			String currentEntry = db.getEntry(key).toString();
			json = new JSONObject(currentEntry);
			for (Iterator<String> nameIter = values.keys(); nameIter.hasNext();) {
				String name = nameIter.next();
				json.put(name, values.get(name));
			}
		} else {
			json = values;
		}
		json.put(NAME_COLUMN_TIMESTAMP, System.currentTimeMillis());
		updateHtmlEntry(key, json.toString());
	}
	
	public void updateHtmlEntry(String key, String html) throws IOException, RecordsFileException {
		if (db.keyExists(key)) {
			db.updateEntry(key, html);
		} else {
			db.appendEntry(key, html);
		}
		indexDb.appendEntry(String.valueOf(System.currentTimeMillis()), key);
	}

	public String getEntry(String key) throws ClassNotFoundException, RecordsFileException, IOException {
		return String.valueOf(db.getEntry(key));
	}
	
	public LinkedHashMap<String, Object> getNew(int start, String timestamp) throws ClassNotFoundException, RecordsFileException, IOException, JSONException {
		int num = db.getNumRecords();
		LinkedHashMap<String, Object> result;
		if (num > start) {
			result = db.getKeyEntryPairs(num - 1, num - start);
		} else {
			result = new LinkedHashMap<String, Object>();
		}
		if (timestamp != null) {
			int indexLen = indexDb.getNumRecords();
			for (int i = indexLen - 1; i >=0; i--) {
				String indexKey = indexDb.indexToKey(i);
				try {
					if (Long.valueOf(indexKey) > Long.valueOf(timestamp)) {
						String key = indexDb.getEntry(indexKey).toString();
						Object entry = db.getEntry(key);
						result.put(key, entry);
					} else {
						break;
					}
				} catch (Exception e) {
					break;
				}
				
			}
		}
		return result;
	}
	
	public LinkedHashMap<String, Object> getAll() throws ClassNotFoundException, RecordsFileException, IOException {
//		String html = "";
//		int num = db.getNumRecords();
//		List<Object> entries  = db.getEntries(num - 1, num);
//		for (Object entry : entries) {
//			html += entry.toString();
//		}
//		return html;
		int num = db.getNumRecords();
		return db.getKeyEntryPairs(num - 1, num);
	}
	
	public List<String> getColumnNames() {
//		if (provider != null) {
//			List<String> names = provider.getColumnNames();
//			String header = "";
//			if (names.size() > 0) {
//				header = "<tr><th>File Number</th>";
//				for (String name : names) {
//					header += "<th>" + name + "</th>";
//				}
//				header += "</tr>";
//				return header;
//			}
//		}
//		return "";
		return provider.getColumnNames();
	}
	
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
	
	public String search(String pattern) throws ClassNotFoundException, RecordsFileException, IOException {
		String html = "";
		for (int i = 0; i < db.getNumRecords(); i++) {
			String entry = db.getEntry(i).toString();
			String res = searchEntry(entry, pattern);
			if (entry.length() != res.length()) {
				html += res;
			}
		}
		return html;
	}
	
	private String searchEntry(String entry, String pattern) {
//		if (numberOfFound >= DEFAULT_NUMBER_OF_APPERANCE) {
//			return;
//		}
		String html = "";
		if (inSearch) {
			int nextOff = entry.indexOf("<");
			if (nextOff >= 0) {
				String part = entry.substring(0, nextOff);
				html += searchPart(part, pattern);
				inSearch = false;
				html += searchEntry(entry.substring(nextOff), pattern);
			}
		} else {
			int nextOn = entry.indexOf(">");
			if (nextOn >= 0) {
				inSearch = true;
				html += entry.substring(0, nextOn + 1);
				if (nextOn < entry.length()) {
					html += searchEntry(entry.substring(nextOn + 1), pattern);
				}
			}
		}
		return html;
	}

	private String searchPart(String part, String pattern) {
		pattern = pattern.replaceAll("\\s+", " ");
		String[] patterns = pattern.split(" ");
		String newPart = part;
		for (String item : patterns) {
//			newPart = newPart.replaceAll("(?i)" + item, SPAN_FREFIX + item + SPAN_END);
			Matcher matcher = Pattern.compile("(?i)" + item).matcher(newPart);
//			newPart = matcher.(SPAN_FREFIX + item + SPAN_END);
	        StringBuffer stringBuffer = new StringBuffer();
	        while(matcher.find()){
	        	String found = matcher.group();
	            matcher.appendReplacement(stringBuffer, SPAN_FREFIX + found + SPAN_END);
	        }
            matcher.appendTail(stringBuffer);
            newPart = stringBuffer.toString();
		}
		return newPart;
	}
	
	public void close() {
		if (db != null) {
			db.close();
		}
	}
}

