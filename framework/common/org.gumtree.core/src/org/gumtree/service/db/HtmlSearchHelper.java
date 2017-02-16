/**
 * 
 */
package org.gumtree.service.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nxi
 *
 */
public class HtmlSearchHelper {

	private static final int DEFAULT_NUMBER_OF_APPERANCE = 3;
	private static final int LENGTH_OF_BUFFER = 200;
	
	private static final String SPAN_FREFIX = "<span class=\"class_span_search_highlight\" style=\"color:#ff8;background-color:black;\">";
	private static final String BOOKMARK_HTML = "<a name=\"first_match\"/>";
	private static final String SPAN_END = "</span>";
	private static final String DIV_FREFIX = "<div class=\"class_div_search_line\">";
	private static final String DIV_END = "</div>";
	
	private File file;
	private boolean inSearch = false;
	private int numberOfFound = 0;
	private List<String> foundList;
	
	private String headBuffer;
	private String tailBuffer;
	private boolean toBookmark = false;
	
	/**
	 * 
	 */
	public HtmlSearchHelper(File file) {
		this.file = file;
		foundList = new LinkedList<String>();
		headBuffer = "";
		tailBuffer = "";
	}

//	public static String search(String html, String pattern) {
//		Scanner scanner = new Scanner(html);
//		return scan(scanner, pattern);
//	}
//	
//	public static String search(File file, String pattern) throws FileNotFoundException {
//		Scanner scanner = new Scanner(file);
//		try {
//			return scan(scanner, pattern);			
//		} finally {
//			scanner.close();
//		}
//	}
//	
//	private static String scan(Scanner scanner, String pattern) {
//		scanner.next(pattern);
//	}
	
	public String search(String pattern) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		inSearch = true;
		toBookmark = true;;
		try {
			while (true) {
				try {
					line = reader.readLine();
				} catch (Exception e) {
					break;
				}
				if (line != null) {
					searchLine(line, pattern);
				} else {
					break;
				}
			}			
		} finally {
			reader.close();
		}
		return createSearchResult();
	}

	public String highlightSearch(String pattern) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		String text = "";
		inSearch = true;
		toBookmark = true;
		try {
			while (true) {
				try {
					line = reader.readLine();
				} catch (Exception e) {
					break;
				}
				if (line != null) {
					text += highlightLine(line, pattern) + "\n";
				} else {
					break;
				}
			}			
		} finally {
			reader.close();
		}
		return text;
	}
	
	private String highlightLine(String line, String pattern) {
		String text = "";
		if (inSearch) {
			int nextOff = line.indexOf("<");
			if (nextOff >= 0) {
				String part = line.substring(0, nextOff);
				text += highlightPart(part, pattern);
				inSearch = false;
				text += highlightLine(line.substring(nextOff), pattern);
			} else {
				text += line;
			}
		} else {
			int nextOn = line.indexOf(">");
			if (nextOn >= 0) {
				inSearch = true;
				text += line.substring(0, nextOn + 1);
				if (nextOn < line.length()) {
					text += highlightLine(line.substring(nextOn + 1), pattern);
				}
			} else {
				text += line;
			}
		}
		return text;
	}

	private String highlightPart(String part, String pattern) {
		pattern = pattern.replaceAll("\\s+", " ");
		String[] patterns = pattern.split(" ");
		String newPart = part;
		for (String item : patterns) {
			Matcher matcher = Pattern.compile("(?i)" + item).matcher(newPart);
	        StringBuffer stringBuffer = new StringBuffer();
	        while(matcher.find()){
	        	String found = matcher.group();
	        	if (toBookmark) {
		            matcher.appendReplacement(stringBuffer, BOOKMARK_HTML + SPAN_FREFIX + found + SPAN_END);
		            toBookmark = false;
	        	} else {
		            matcher.appendReplacement(stringBuffer, SPAN_FREFIX + found + SPAN_END);
	        	}
	        }
            matcher.appendTail(stringBuffer);
            newPart = stringBuffer.toString();
		}
		return newPart;
	}
	
	private String createSearchResult() {
		if (numberOfFound > 0){
			String res = "";
			for (String item : foundList) {
				res += DIV_FREFIX + item + DIV_END;
			}
			return res;
		}
		return "";
	}

	private void searchLine(String line, String pattern) {
//		if (numberOfFound >= DEFAULT_NUMBER_OF_APPERANCE) {
//			return;
//		}
		if (inSearch) {
			int nextOff = line.indexOf("<");
			if (nextOff >= 0) {
				String part = line.substring(0, nextOff);
				searchPart(part, pattern);
				inSearch = false;
				searchLine(line.substring(nextOff), pattern);
			}
		} else {
			int nextOn = line.indexOf(">");
			if (nextOn >= 0) {
				inSearch = true;
				if (nextOn < line.length()) {
					searchLine(line.substring(nextOn + 1), pattern);
				}
			}
		}
	}

	private void searchPart(String part, String pattern) {
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
		if (part.length() != newPart.length()) {
			foundList.add(newPart);
			numberOfFound += 1;
		}
	}

}
