package au.gov.ansto.bragg.nbi.server.notebook;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.gumtree.core.object.IDisposable;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.db.TextDb;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

public class NotebookRestlet extends Restlet implements IDisposable {

	public NotebookRestlet() {
		this(null);
	}

	private final static String SEG_NAME_SAVE = "save";
	private final static String SEG_NAME_LOAD = "load";
	private final static String SEG_NAME_DB = "db";
	private final static String SEG_NAME_NEW = "new";
	private final static String SEG_NAME_ARCHIVE = "archive";
	private final static String STRING_CONTENT_START = "content=";
	private final static String PREFIX_NOTEBOOK_FILES = "Page_";
	private final static String PROP_NOTEBOOK_SAVEPATH = "gumtree.notebook.savePath";
	private final static String PROP_DATABASE_SAVEPATH = "gumtree.loggingDB.savePath";
	private final static String NOTEBOOK_CURRENTFILENAME = "current.xml";
	private final static String NOTEBOOK_DBFILENAME = "loggingDB.rdf";
	private static final String QUERY_ENTRY_START = "start";
	private static final String QUERY_ENTRY_LENGTH = "length";
	private final static String QUERY_FILE_ID = "file";
	
	private String currentFilePath;
	private String currentDBPath;
	
	/**
	 * @param context
	 */
	public NotebookRestlet(Context context) {
		super(context);
		currentFilePath = System.getProperty(PROP_NOTEBOOK_SAVEPATH) + "/" + NOTEBOOK_CURRENTFILENAME;
		currentDBPath = System.getProperty(PROP_DATABASE_SAVEPATH) + "/" + NOTEBOOK_DBFILENAME;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {
	}
	
	@Override
	public void handle(final Request request, final Response response) {
		
//		Form queryForm = request.getResourceRef().getQueryAsForm();
		String seg = request.getResourceRef().getLastSegment();
		if (SEG_NAME_SAVE.equals(seg)) {
//			String content = rForm.getValues("content");
			Representation rep = request.getEntity();
//			String text = request.getEntityAsText();
//			String subText = text.substring(text.indexOf("content=") + 8, text.indexOf("&"));
//			InputStream inputStream = null;
//			OutputStream outputStream = null;
			FileWriter writer = null;
			try {
//				inputStream = rep.getStream();
//				outputStream = new FileOutputStream(new File(currentFilePath));
//				int read = 0;
//				byte[] bytes = new byte[BUFFER_LENGTH];
//				int index = 0;
//				boolean hasContent = loopToStart(inputStream);
//				if (hasContent) {
//					while ((read = inputStream.read()) != -1) {
//						bytes[index] = (byte) read;
//						if (read == KEY_CONTENT_STOP) {
//							if (index > 0) {
//								outputStream.write(bytes, 0, index);
//								index = 0;
//							}
//							break;
//						}
//						index += 1;
//						if (index >= BUFFER_LENGTH) {
//							outputStream.write(bytes, 0, BUFFER_LENGTH);
//							index = 0;
//						}
//					}
//					if (index > 0) {
//						outputStream.write(bytes, 0, index);
//					}
//				}
				String text = rep.getText();
				int start = text.indexOf(STRING_CONTENT_START) + STRING_CONTENT_START.length();
				int stop = text.indexOf("&_method=");
				text = text.substring(start, stop);
				text = URLDecoder.decode(text, "UTF-8");
				writer = new FileWriter(currentFilePath);
				writer.write(text);
				writer.flush();
			} catch (IOException e1) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e1.toString());
				return;
			}finally {
				if (writer != null){
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
//				if (outputStream != null) {
//					try {
//						// outputStream.flush();
//						outputStream.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//
//				}
			}
		    JSONObject jsonObject = new JSONObject();
		    try {
		    	jsonObject.put("status", "OK");
		    	response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
		    } catch (JSONException e) {
				e.printStackTrace();
			}finally{
		    	
		    }
		} else if (SEG_NAME_LOAD.equals(seg)) {
			Form queryForm = request.getResourceRef().getQueryAsForm();
		    String fileId = queryForm.getValues(QUERY_FILE_ID);
		    if (fileId == null || fileId.trim().length() == 0) {
		    	try {
		    		File current = new File(currentFilePath);
		    		if (current.exists()) {
		    			byte[] bytes = Files.readAllBytes(Paths.get(currentFilePath));
		    			response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
		    		}
		    	} catch (IOException e) {
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		return;
		    	}
		    } else {
		    	try {
		    		File current = new File(currentFilePath);
		    		if (current.exists()) {
		    			String filename = current.getParent() + "/" + fileId + ".xml"; 
		    			byte[] bytes = Files.readAllBytes(Paths.get(filename));
		    			response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
		    		}
		    	} catch (IOException e) {
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		    		return;
		    	}
		    }
		} else if (SEG_NAME_DB.equals(seg)) {
//			try {
//				File current = new File(currentDBPath);
//				if (current.exists()) {
//					byte[] bytes = Files.readAllBytes(Paths.get(currentDBPath));
//					response.setEntity(new String(bytes), MediaType.TEXT_PLAIN);
//				}
//			} catch (IOException e) {
//				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//				return;
//			}
	    	Form form = request.getResourceRef().getQueryAsForm();
	    	String startValue = form.getValues(QUERY_ENTRY_START);
	    	int start = 0;
	    	boolean isBeginning = false;
	    	if (startValue != null) {
	    		try {
			    	start = Integer.valueOf(startValue);
				} catch (Exception e) {
					start = 0;
				}
	    	} else {
	    		isBeginning = true;
	    	}
	    	final int length = Integer.valueOf(form.getValues(QUERY_ENTRY_LENGTH));
	    	TextDb db = null;
			try {
				db = new TextDb(currentDBPath, "r");
				String html = "";
				if (isBeginning) {
					html = db.getEntries(length);
				} else {
					html = db.getEntries(start, length);
				}
				response.setEntity(html, MediaType.TEXT_PLAIN);
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			} finally {
				if (db != null) {
					try {
						db.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (RecordsFileException e) {
						e.printStackTrace();
					}
				}
			}
		} else if (SEG_NAME_NEW.equals(seg)) {
			try {
				File current = new File(currentFilePath);
				if (current.exists()) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
					String newName = PREFIX_NOTEBOOK_FILES + format.format(new Date());
					File newFile = new File(current.getParent() + "/" + newName + ".xml");
					current.renameTo(newFile);
					if (!current.createNewFile()) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL, "failed to create new file");
						return;
					}
					response.setEntity(newName, MediaType.TEXT_PLAIN);
				}
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			}
		} else if (SEG_NAME_ARCHIVE.equals(seg)) {
			try {
				File current = new File(currentFilePath);
				if (current.exists()) {
					File parent = current.getParentFile();
					String[] fileList = parent.list(new FilenameFilter() {
						
						@Override
						public boolean accept(File dir, String name) {
							if (name.startsWith(PREFIX_NOTEBOOK_FILES)){
								return true;
							}
							return false;
						}
					});
					Arrays.sort(fileList);
					String responseText = "";
					for (int i = fileList.length - 1; i >= 0; i--) {
						responseText += fileList[i].substring(0, fileList[i].length() - 4);
						if (i > 0){
							responseText += ":";
						}
					}
					response.setEntity(responseText, MediaType.TEXT_PLAIN);
				}
			} catch (Exception e) {
				response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				return;
			}
		} 
		response.setStatus(Status.SUCCESS_OK);
//	    String typeString = queryForm.getValues(QUERY_TYPE);
//	    JSONObject jsonObject = new JSONObject();
//	    try {
//	    	jsonObject.put("status", 1);
//	    	response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
//	    	response.setStatus(Status.SUCCESS_OK);
//	    } catch (JSONException e) {
//	    	e.printStackTrace();
//	    	response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//	    }
	    return;
	}
	

}
