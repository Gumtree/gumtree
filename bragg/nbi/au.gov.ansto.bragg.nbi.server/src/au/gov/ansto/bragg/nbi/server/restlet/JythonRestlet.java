/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.ByteArrayInputStream;

import org.gumtree.core.object.IDisposable;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.ScriptBlock;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import au.gov.ansto.bragg.nbi.server.image.ChartImage;
import au.gov.ansto.bragg.nbi.server.restlet.JythonExecutor.ExecutorStatus;

/**
 * @author nxi
 *
 */
public class JythonRestlet extends Restlet implements IDisposable {

	private final static String QUERY_SCRIPT_TEXT = "script_text";
	private final static String QUERY_SCRIPT_INPUT_MODE = "script_input";
	private final static String QUERY_TYPE = "type";
	private final static String QUERY_PLOT_ID = "id";
	private final static String SCRIPT_START_FLAG = "Content-Type:";
	private final static int IMAGE_WIDTH = 480;
	private final static int IMAGE_HEIGHT = 240;
	
	private static ChartImage plot1Cache;
	private static ChartImage plot2Cache;
	private static ChartImage plot3Cache;
	
	enum QueryType {
		START,
		STATUS,
		INTERRUPT,
		READSCRIPT,
		PLOT
	}
	/**
	 * 
	 */
	public JythonRestlet() {
		this(null);
	}

	/**
	 * @param context
	 */
	public JythonRestlet(Context context) {
		super(context);
		plot1Cache = new ChartImage(IMAGE_WIDTH, IMAGE_HEIGHT);
		plot2Cache = new ChartImage(IMAGE_WIDTH, IMAGE_HEIGHT);
		plot3Cache = new ChartImage(IMAGE_WIDTH, IMAGE_HEIGHT);
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
	    			JythonExecutor.runScriptBlock(block);
	    		} else if ("textInput".equals(inputMode)){
		    		JythonExecutor.runScriptLine(value);
	    		}
	    		Thread.sleep(200);
	    		JSONObject jsonObject = getExecutorStatus();
	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
	    	}
			break;
		case STATUS:
//			ExecutorStatus status = JythonExecutor.getStatus();
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
				jsonObject = getExecutorStatus();
	    		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
			} catch (JSONException e) {
	    		e.printStackTrace();
	    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
			}
			break;
		case INTERRUPT:
			JythonExecutor.interrupt();
			try {
				jsonObject = getExecutorStatus();
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
						new ByteArrayInputStream(getPlot1().getImageCache()), MediaType.IMAGE_PNG);
				response.setEntity(result);
			} else if (plotId == 2) {
				Representation result = new InputRepresentation(
						new ByteArrayInputStream(getPlot2().getImageCache()), MediaType.IMAGE_PNG);
				response.setEntity(result);
			} else if (plotId == 3) {
				Representation result = new InputRepresentation(
						new ByteArrayInputStream(getPlot3().getImageCache()), MediaType.IMAGE_PNG);
				response.setEntity(result);
			} 
			break;
		default:
			break;
		}
	}
	
	private JSONObject getExecutorStatus() throws JSONException{
		JSONObject jsonObject = new JSONObject();
		ExecutorStatus status = JythonExecutor.getStatus();
		jsonObject.put("status", status);
		jsonObject.put("text", JythonExecutor.getRecentText(true));
		jsonObject.put("error", JythonExecutor.getRecentError(true));
		jsonObject.put("plot1", getPlot1().isUpdated(true));
		jsonObject.put("plot2", getPlot2().isUpdated(true));
		jsonObject.put("plot3", getPlot3().isUpdated(true));
//		if (status == ExecutorStatus.ERROR) {
//			JythonExecutor.resetErrorStatus();
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

	public static ChartImage getPlot1(){
		return plot1Cache;
	}
	
	public static ChartImage getPlot2(){
		return plot2Cache;
	}
	
	public static ChartImage getPlot3(){
		return plot3Cache;
	}
}
