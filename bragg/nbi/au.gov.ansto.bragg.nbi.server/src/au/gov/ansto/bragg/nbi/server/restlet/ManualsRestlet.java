/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.HtmlSearchHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;

import au.gov.ansto.bragg.nbi.server.internal.AbstractUserControlRestlet;

/**
 * @author nxi
 *
 */
public class ManualsRestlet extends AbstractUserControlRestlet implements IDisposable {

	private static final String PROP_MANUALS_SAVEPATH = "gumtree.manuals.path";
	private static final String PROP_MANUALS_ITEMS = "gumtree.manuals.items";
	private static final String FILE_FREFIX = "<div class=\"class_div_search_file\" title=\"$title\" path=\"$path\">";
	private static final String SPAN_SEARCH_RESULT_HEADER = "<h4>";
	private static final String DIV_END = "</div>";
	private static final String SPAN_END = "</h4>";
	private static final String SEG_NAME_LIST = "list";
	private static final String SEG_NAME_SEARCH = "search";
	private static final String QUERY_PATH_NAME = "path";
	private static final String QUERY_PATTERN = "pattern";
	private static final String SEG_NAME_LOAD = "load";
	private static final String NAME_JSON_TITLE = "title";
	private static final String NAME_JSON_RESOURCE = "resource";
	private static final String NAME_JSON_PATH = "path";
	private static final String NAME_JSON_FOUND = "found";
	private static final String NAME_JSON_LENGTH = "length";

	private static String manualItems;
	private static String manualFolder;
	
	static {
		manualItems = System.getProperty(PROP_MANUALS_ITEMS);
		manualFolder = System.getProperty(PROP_MANUALS_SAVEPATH);
	}
	
	public ManualsRestlet(){
		this(null);
	}
	/**
	 * @param context
	 */
	public ManualsRestlet(Context context) {
		super(context);
	}

	@Override
	public void handle(Request request, Response response) {
		String seg = request.getResourceRef().getLastSegment();
		List<String> segList = request.getResourceRef().getSegments();

		if (SEG_NAME_LIST.equalsIgnoreCase(seg)){
			try {
				if (manualItems != null) {
					response.setEntity(manualItems, MediaType.APPLICATION_JSON);
					response.setStatus(Status.SUCCESS_OK);
				} else {
					response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setEntity("<span style=\"color:red\">Error appending to catalogue database: " 
						+ e.getMessage() + ".</span>", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
				return;
			}
		} else if (SEG_NAME_LOAD.equalsIgnoreCase(seg)){
			try {
				Form queryForm = request.getResourceRef().getQueryAsForm();
				String targetFilename = queryForm.getValues(QUERY_PATH_NAME);
				String targetPath = manualFolder + "/" + targetFilename;
				String baseFilename = targetFilename;
				if (baseFilename.contains("/")) {
					baseFilename = baseFilename.substring(baseFilename.lastIndexOf("/") + 1);
				}
				File current = new File(targetPath);
				if (current.exists()) {
					if (baseFilename.toLowerCase().endsWith(".pdf")) {
						try {
							FileRepresentation representation = new FileRepresentation(targetPath, MediaType.APPLICATION_PDF);
							Disposition disposition = new Disposition();
							disposition.setFilename(baseFilename);
							disposition.setType(Disposition.TYPE_INLINE);
							representation.setDisposition(disposition);
							response.setEntity(representation);
						} catch (Exception e) {
							response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
							return;
						}
					} else if (baseFilename.toLowerCase().endsWith(".html") || baseFilename.toLowerCase().endsWith(".htm")) {
						try {
							byte[] bytes = Files.readAllBytes(Paths.get(targetPath));
							response.setEntity(new String(bytes), MediaType.TEXT_HTML);
						} catch (IOException e) {
							response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
							return;
						}
					} else {
						try {
							FileRepresentation representation = new FileRepresentation(targetPath, MediaType.APPLICATION_OCTET_STREAM);
							Disposition disposition = new Disposition();
							disposition.setFilename(baseFilename);
							disposition.setType(Disposition.TYPE_ATTACHMENT);
							representation.setDisposition(disposition);
							response.setEntity(representation);
						} catch (Exception e) {
							response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
							return;
						}
					}
				}else {
					response.setStatus(Status.SERVER_ERROR_INTERNAL, "<span style=\"color:red\">File not found.</span>");
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setEntity("<span style=\"color:red\">Error appending to catalogue database: " 
						+ e.getMessage() + ".</span>", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
				return;
			}
		} else if (SEG_NAME_SEARCH.equalsIgnoreCase(seg)) {
			Form queryForm = request.getResourceRef().getQueryAsForm();
			String pattern = queryForm.getValues(QUERY_PATTERN);
			if (pattern.trim().length() == 0) {
				response.setEntity("Please input a valid pattern", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SUCCESS_OK);
				return;
			}
			File current = new File(manualFolder);
			if (current.exists()) {
				try {
					JSONObject result = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					JSONObject json = new JSONObject(manualItems);
					Iterator<String> keyIter = json.keys();
					while (keyIter.hasNext()) {
						String catagory = keyIter.next();
						JSONObject collection = json.getJSONObject(catagory);
						Iterator<String> manualIter = collection.keys();
						while (manualIter.hasNext()) {
							String manualName = manualIter.next();
							JSONObject manualDetail = collection.getJSONObject(manualName);
							String title = manualDetail.getString("full_title");
							JSONObject resource = manualDetail.getJSONObject(NAME_JSON_RESOURCE);
							Iterator<String> resourceIter = resource.keys();
							while (resourceIter.hasNext()) {
								String type = resourceIter.next();
								if (type.toUpperCase().equals("HTML") || type.toLowerCase().equals("HTM")){
									String path = resource.getString(type);
									String found = searchForPattern(path, pattern);
									if (found != null) {
										JSONObject item = new JSONObject();
										item.put(NAME_JSON_TITLE, title);
										item.put(NAME_JSON_PATH, path);
										found = FILE_FREFIX.replace("$title", title).replace("$path", path) 
												+ SPAN_SEARCH_RESULT_HEADER + title + SPAN_END + found + DIV_END;
										item.put(NAME_JSON_FOUND, found);
										jsonArray.put(item);
									}
								}
							}
						}
					}
					result.put(NAME_JSON_LENGTH, jsonArray.length());
					result.put("list", jsonArray);
					response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
				} catch (Exception e) {
					response.setEntity("<span style=\"color:red\">Error in searching the manuals: " 
							+ e.getMessage() + ".</span>", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			} else {
				response.setEntity("<span style=\"color:red\">Error in searching the manuals: no manual found.</span>", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		} else {
			response.setEntity("<span style=\"color:red\">Service not available: " 
					+ seg + ".</span>", MediaType.TEXT_PLAIN);
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
	}

	private String searchForPattern(String path, String pattern) {
		File targetFile = new File(manualFolder + "/" + path);
		if (targetFile.exists()) {
			HtmlSearchHelper helper = new HtmlSearchHelper(targetFile);
			try {
				String searchRes = helper.search(pattern);
				if (searchRes.length() > 0){
					return searchRes;
				}
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {

	}

}
