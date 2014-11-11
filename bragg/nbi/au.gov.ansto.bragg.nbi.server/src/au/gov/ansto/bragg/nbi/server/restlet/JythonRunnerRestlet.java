package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.gumtree.core.object.IDisposable;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.ScriptBlock;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import au.gov.ansto.bragg.nbi.server.jython.JythonRunner;
import au.gov.ansto.bragg.nbi.server.jython.JythonRunnerManager;
import au.gov.ansto.bragg.nbi.server.restlet.JythonExecutor.ExecutorStatus;

public class JythonRunnerRestlet extends Restlet implements IDisposable {

	public JythonRunnerRestlet() {
		this(null);
	}

	private final static String QUERY_SCRIPT_TEXT = "script_text";
	private final static String QUERY_SCRIPT_INPUT_MODE = "script_input";
	private final static String QUERY_TYPE = "type";
	private final static String QUERY_PLOT_ID = "id";
	private final static String QUERY_FILE_NAME = "name";
	private final static String QUERY_UUID = "uuid";
	private final static String QUERY_FILE_FOLDER = "folder";
	private final static String SCRIPT_START_FLAG = "Content-Type:";
	
	private JythonRunnerManager runnerManager;
	
	enum QueryType {
		START,
		STATUS,
		INTERRUPT,
		READSCRIPT,
		PLOT,
		GUI,
		FILENAMES,
		FILE,
		LISTSCRIPTS,
		SCRIPT, 
		USERFILES,
		UPLOADFILES
	}

	/**
	 * @param context
	 */
	public JythonRunnerRestlet(Context context) {
		super(context);
		runnerManager = new JythonRunnerManager();
	}

	/* (non-Javadoc)
	 * @see org.gumtree.core.object.IDisposable#disposeObject()
	 */
	@Override
	public void disposeObject() {
	}
	
	@Override
	public void handle(final Request request, final Response response) {
		
        Form queryForm = request.getResourceRef().getQueryAsForm();
	    String typeString = queryForm.getValues(QUERY_TYPE);
	    
	    String uuidString = null;
	    try {
		    Cookie cookie = request.getCookies().getFirst(UserManagerRestlet.COOKIE_NAME_UUID + "." + System.getProperty(UserManagerRestlet.PROPERTY_INSTRUMENT_ID));
		    if (cookie != null) {
		    	uuidString = cookie.getValue();
		    }			
		} catch (Exception e) {
		}
    	UUID uuid = null;
	    JythonRunner runner = null;
	    if (uuidString != null) {
	    	uuid = UUID.fromString(uuidString);
	    	runner = runnerManager.getJythonRunner(uuid);
	    } 
	    if (runner == null) {
			JSONObject jsonObject;
			try {
		    	jsonObject = jumpToLogin();
	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
//				response.redirectPermanent("/login.html");
//				response.setStatus(Status.SUCCESS_OK);
			} catch (JSONException e) {
	    		e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
	    	return;
	    }
	    CookieSetting cookieSetting = new CookieSetting(0, UserManagerRestlet.COOKIE_NAME_UUID, uuidString, 
				"/", null, UserManagerRestlet.COOKIE_COMMENT_UUID, 1800, false);
	    response.getCookieSettings().add(cookieSetting);
	    
	    QueryType type = QueryType.valueOf(typeString);
	    
	    switch (type) {
		case START:
	    	//		Form form = request.getResourceRef().getQueryAsForm();
	    	Representation entity = request.getEntity();
	    	Form form = new Form(entity);
	    	final String value = form.getValues(QUERY_SCRIPT_TEXT);
	    	final String inputMode = form.getValues(QUERY_SCRIPT_INPUT_MODE);
	    	try {
	    		final JSONObject result = new JSONObject();
	    		result.put("script", value);
	    		//			response.setEntity(result.toString(), MediaType.TEXT_PLAIN);
	    		if ("textArea".equals(inputMode)) {
	    			IScriptBlock block = new ScriptBlock(){
	    				public String getScript() {
	    					return value;
	    				}
	    			};
	    			runner.runScriptBlock(block);
	    		} else if ("textInput".equals(inputMode)){
	    			runner.runScriptLine(value);
	    		}
	    		Thread.sleep(200);
	    		JSONObject jsonObject = getExecutorStatus(runner);
	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
	    	}
			break;
		case STATUS:
//			ExecutorStatus status = runner.getStatus();
//			if (status == ExecutorStatus.BUSY) {
//				response.setEntity("Status:" + status.name() + ",Text:" + JythonExecutor.getRecentText(true), MediaType.TEXT_PLAIN);
//			} else if (status == ExecutorStatus.ERROR){
//				response.setEntity("Status:" + status.name() + ",Text:" + JythonExecutor.getRecentError(true), MediaType.TEXT_PLAIN);
//			} else if (status == ExecutorStatus.IDLE) {
//				response.setEntity("Status:" + status.name() + ",Text:" + JythonExecutor.getRecentText(true), MediaType.TEXT_PLAIN);
//			} else {
//				response.setEntity("Status:" + status.name(), MediaType.TEXT_PLAIN);
//			}
			JSONObject jsonObject;
			try {
				jsonObject = getExecutorStatus(runner);
	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
			} catch (JSONException e) {
	    		e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			break;
		case INTERRUPT:
			runner.interrupt();
			try {
				jsonObject = getExecutorStatus(runner);
	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
			} catch (JSONException e) {
	    		e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			break;
		case READSCRIPT:
			try {
		    	Representation formEntity = request.getEntity();
		    	
//		    	RestletFileUpload upload = new RestletFileUpload(factory);
//                List<FileItem> items;
//
//                // 3/ Request is parsed by the handler which generates a
//                // list of FileItems
//                items = upload.parseRequest(getRequest());
		    	String text = formEntity.getText();
		    	int start = text.indexOf(SCRIPT_START_FLAG);
		    	start = text.indexOf("\n", start) + 3;
		    	int end = text.lastIndexOf("\n", text.length() - 2) - 1;
		    	text = text.substring(start, end);
				response.setEntity(text, MediaType.TEXT_PLAIN);
			} catch (Exception e) {
				e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			break;
		case PLOT:
			int plotId = Integer.valueOf(queryForm.getValues(QUERY_PLOT_ID));
			if (plotId == 1) {
				Representation result = new InputRepresentation(
						new ByteArrayInputStream(runner.getPlot1().getImageCache()), MediaType.IMAGE_PNG);
				response.setEntity(result);
			} else if (plotId == 2) {
				Representation result = new InputRepresentation(
						new ByteArrayInputStream(runner.getPlot2().getImageCache()), MediaType.IMAGE_PNG);
				response.setEntity(result);
			} else if (plotId == 3) {
				Representation result = new InputRepresentation(
						new ByteArrayInputStream(runner.getPlot3().getImageCache()), MediaType.IMAGE_PNG);
				response.setEntity(result);
			} 
			break;
		case GUI:
	    	entity = request.getEntity();
	    	form = new Form(entity);
	    	String script = form.getValues(QUERY_SCRIPT_TEXT);
	    	try {
				String html = runner.getScriptGUI(script);
				jsonObject = getExecutorStatus(runner);
				jsonObject.put("html", html);
				response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case FILENAMES:
			response.setEntity(runner.getAllDataHtml(), MediaType.TEXT_PLAIN);
			break;
		case USERFILES:
			response.setEntity(runner.getUserDataHtml(), MediaType.TEXT_PLAIN);
			break;
		case FILE:
			String folderString = queryForm.getValues(QUERY_FILE_FOLDER);
			String filename = request.getResourceRef().getLastSegment();
			if ("save".equals(folderString)){
				folderString = runner.getDataHandler().getSavePath();
			} else if ("data".equals(folderString)){
				folderString = runner.getDataHandler().getDataPath();
			}
//			Path path = Paths.get(folderString + "/" + filename);
//			byte[] data = null;
//			try {
//				data = Files.readAllBytes(path);
//			} catch (IOException e) {
//				e.printStackTrace();
//	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
//			}
//			Representation representation = new InputRepresentation(new ByteArrayInputStream(data), MediaType.APPLICATION_OCTET_STREAM);
			FileRepresentation representation = new FileRepresentation(folderString + "/" + filename, MediaType.APPLICATION_ZIP);
			Disposition disposition = new Disposition();
			disposition.setFilename(filename);
			representation.setDisposition(disposition);
			response.setEntity(representation);
			break;
		case LISTSCRIPTS:
			jsonObject = new JSONObject();
			try {
				jsonObject.put("scripts", runner.getUIHandler().getAvailableScripts());
				String initScript = runner.getDefaultScript();
				if (initScript != null) {
					if (runner.getUIHandler().isScriptAvailable(initScript)) {
						jsonObject.put("default", initScript);
					}
				} else {
					String propName = "";
					if (uuid != null) {
						propName = uuid.toString() + "_";
					}
					String lastScriptName = JythonModelRegister.getPreference(propName + JythonUIHandler.NAME_SCRIPT_LASTRUN);
					if (lastScriptName != null && lastScriptName.trim().length() > 0) {
						if (runner.getUIHandler().isScriptAvailable(lastScriptName)) {
							jsonObject.put("default", lastScriptName);
						}
					}
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}			
			response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
			break;
		case SCRIPT:
			String nameString = queryForm.getValues(QUERY_FILE_NAME);
			try {
				String text = runner.getUIHandler().getScriptFileContent(nameString);
				response.setEntity(text, MediaType.TEXT_PLAIN);
			} catch (Exception e) {
				e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			try {
				String propName = "";
				if (uuid != null) {
					propName = uuid.toString() + "_";
				}
				JythonModelRegister.setPreference(propName + JythonUIHandler.NAME_SCRIPT_LASTRUN, nameString);
				JythonModelRegister.savePreferenceStore();
			} catch (Exception e) {
			}
			break;
		case UPLOADFILES:
			try {

				// 1/ Create a factory for disk-based file items
		    	DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1000240);

                // 2/ Create a new file upload handler based on the Restlet
                // FileUpload extension that will parse Restlet requests and
                // generates FileItems.
                RestletFileUpload upload = new RestletFileUpload(factory);
                List<FileItem> items;

                // 3/ Request is parsed by the handler which generates a
                // list of FileItems
                items = upload.parseRequest(request);
                
                List<File> dataFiles = new ArrayList<File>();
                for (final Iterator<FileItem> it = items.iterator(); it.hasNext();) {
                    FileItem fi = it.next();
                    if (fi.getFieldName().equals("input_upload_file")) {
                        File file = new File(runner.getUserPath() + "/" + fi.getName());
                        fi.write(file);
                        dataFiles.add(file);
                    }
                }
                jsonObject = new JSONObject();
                jsonObject.put("result", "OK");
                if (dataFiles.size() > 0) {
                	jsonObject.put("html", runner.getDataHandler().appendUserFiles(dataFiles));
                }
				response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
				response.setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			break;
		default:
			break;
		}
	}
	
	private JSONObject jumpToLogin() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("js", "window.location = 'login.html';");
		return jsonObject;
	}

	private JSONObject getExecutorStatus(JythonRunner runner) throws JSONException{
		JSONObject jsonObject = new JSONObject();
		ExecutorStatus status = runner.getStatus();
		jsonObject.put("status", status);
		jsonObject.put("text", runner.getRecentText(true));
		jsonObject.put("error", runner.getRecentError(true));
		jsonObject.put("js", runner.getEventJs(true));
		jsonObject.put("files", runner.getFilesForDownload(true));
		jsonObject.put("plot1", runner.getPlot1().isUpdated(true));
		jsonObject.put("plot2", runner.getPlot2().isUpdated(true));
		jsonObject.put("plot3", runner.getPlot3().isUpdated(true));
//		if (status == ExecutorStatus.ERROR) {
//			runner.resetErrorStatus();
//		}
		return jsonObject;
	}
	

//	private void handlePlotRequest(Request request, Response response,
//			Form queryForm) {
//		String key = request.getResourceRef().getRemainingPart();
//		if (imageCache == null || imageCache.isExpired()
//				|| !imageCache.key.equals(key)) {
//			int height = 300;
//			int width = 300;
//			try {
//				height = Integer.parseInt(queryForm.getValues(QUERY_HEIGHT));
//				width = Integer.parseInt(queryForm.getValues(QUERY_WIDTH));
//			} catch (Exception e) {
//				// TODO log error
//			}
//			imageCache = new ImageCache(createPlot(height, width), key);
//		}
//		byte[] imageData = imageCache.imagedata;
//		Representation result = new InputRepresentation(
//				new ByteArrayInputStream(imageData), MediaType.IMAGE_PNG);
//		response.setEntity(result);
//	}
//
//	private byte[] createPlot(int height, int width) {
//		
//		XYErrorDataset dataset = new XYErrorDataset();
//		dataset.setTitle("Data plot not availalbe");
//		dataset.setXTitle("Scan Variable");
//		dataset.setYTitle("Bean Monitor");
//
////		XYErrorSeries series1 = new XYErrorSeries("data");
////		series1.add(1.0, 1.0, 0.0);
////		series1.add(2.0, 4.0, 0.0);
////		series1.add(3.0, 3.0, 0.0);
////		series1.add(5.0, 8.0, 0.0);
////		dataset.addSeries(series1);
//
//		try {
//			String filepath = System.getProperty(SICS_DATA_PATH);
//			if (filepath == null) {
//				filepath = "/experiments/taipan/data/current";
//			}
//			File dir = new File(filepath);
//
//			File[] files = dir.listFiles();
//			if (files.length > 0) {
//				File lastModifiedFile = files[0];
//				for (int i = 1; i < files.length; i++) {
//					if (lastModifiedFile.lastModified() < files[i].lastModified() && files[i].isFile()) {
//						lastModifiedFile = files[i];
//					}
//				}
//				INXDataset ds = NexusUtils.readNexusDataset(lastModifiedFile.toURI());
//				IArray dataArray = ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm2_counts").getData();
//				INXdata data = ds.getNXroot().getFirstEntry().getData();
//				IAxis axis = data.getAxisList().get(0);
//				IArray axisArray = axis.getData();
//				NXDatasetSeries series = new NXDatasetSeries(lastModifiedFile.getName());
//				series.setData(axisArray, dataArray, dataArray.getArrayMath().toSqrt().getArray());
//				dataset.addSeries(series);
//				dataset.setTitle(lastModifiedFile.getName());
//				dataset.setXTitle(axis.getShortName());
//				dataset.setYTitle("Detector Counts");
//				ds.close();
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			dataset.setTitle(e.getMessage());
//		} 
//
//		
//		
//		JFreeChart chart = PlotFactory.createXYErrorChart(dataset);
//		chart.getLegend().setVisible(false);
//		chart.setBackgroundPaint(Color.BLACK);
//		chart.getXYPlot().setBackgroundPaint(Color.DARK_GRAY);
//		chart.getTitle().setPaint(Color.WHITE);
//		ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
//		domainAxis.setLabelPaint(Color.WHITE);
//		domainAxis.setTickLabelPaint(Color.LIGHT_GRAY);
//		domainAxis.setTickMarkPaint(Color.LIGHT_GRAY);
//		ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
//		rangeAxis.setLabelPaint(Color.WHITE);
//		rangeAxis.setTickLabelPaint(Color.LIGHT_GRAY);
//		rangeAxis.setTickMarkPaint(Color.LIGHT_GRAY);
//		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
//		if (renderer instanceof XYLineAndShapeRenderer) {
//			renderer.setSeriesPaint(0, Color.CYAN);
//		}
//		
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		try {
//			ChartUtilities.writeChartAsPNG(out, chart, width, height);
//		} catch (IOException e) {
//			// TODO error handling
//			e.printStackTrace();
//		}
//		return out.toByteArray();
//	}

}
