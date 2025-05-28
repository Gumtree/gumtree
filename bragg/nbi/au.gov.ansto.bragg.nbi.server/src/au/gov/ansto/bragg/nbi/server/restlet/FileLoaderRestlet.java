/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.gumtree.core.object.IDisposable;
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
public abstract class FileLoaderRestlet extends AbstractUserControlRestlet implements IDisposable {

	private Map<String, String> nameToFileDict = new HashMap<String, String>();
	
	private static final String QUERY_FILENAME = "name";
	private static final String QUERY_TYPE = "type";
	private static final String QUERY_TIMESTAMP = "ts";
	
	public FileLoaderRestlet(){
		this(null);
		init();
	}
	/**
	 * @param context
	 */
	public FileLoaderRestlet(Context context) {
		super(context);
		init();
	}

	protected void addFilename(String name, String path) {
		nameToFileDict.put(name, path);
	}
	
	protected abstract void init();
		
	@Override
	public void handle(Request request, Response response) {

		try {
			Form queryForm = request.getResourceRef().getQueryAsForm();
			String targetFilename = queryForm.getValues(QUERY_FILENAME);
			String queryType = queryForm.getValues(QUERY_TYPE);
			String queryTime = queryForm.getValues(QUERY_TIMESTAMP);
			long ts = 0L;
			if (queryTime != null) {
				ts = Long.valueOf(queryTime);
			}
			String targetPath = nameToFileDict.get(targetFilename);
			String baseFilename = targetFilename;
			if (baseFilename.contains("/")) {
				baseFilename = baseFilename.substring(baseFilename.lastIndexOf("/") + 1);
			}
			File current = new File(targetPath);
			if (current.exists()) {
				if (queryType != null && "json".equalsIgnoreCase(queryType)) {
					long lastModified = current.lastModified();
					if (lastModified > ts) {
						try {
							byte[] bytes = Files.readAllBytes(Paths.get(targetPath));
							String content = new String(bytes);
							final JSONObject result = new JSONObject();
							result.put("text", content);
							result.put("ts", String.valueOf(lastModified));
							result.put("status", "OK");
							response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
						} catch (Exception e) {
							final JSONObject result = new JSONObject();
							result.put("reason", "failed to load status file: " + e.getMessage());
							result.put("status", "ERROR");
							response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
						}
					} else {
						final JSONObject result = new JSONObject();
						result.put("status", "unchanged");
						response.setEntity(result.toString(), MediaType.APPLICATION_JSON);
					}
				} else {
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
							FileRepresentation representation = new FileRepresentation(targetPath, MediaType.ALL);
							response.setEntity(representation);
						} catch (Exception e) {
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
	} 

	
	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {

	}

}
