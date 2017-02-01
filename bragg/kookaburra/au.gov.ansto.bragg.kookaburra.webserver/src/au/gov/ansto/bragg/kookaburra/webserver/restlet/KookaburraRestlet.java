package au.gov.ansto.bragg.kookaburra.webserver.restlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import au.gov.ansto.bragg.nbi.server.jython.JythonRunner;
import au.gov.ansto.bragg.nbi.server.jython.JythonRunnerManager;
import au.gov.ansto.bragg.nbi.server.restlet.JythonExecutor.ExecutorStatus;
//import org.gumtree.data.interfaces.IArray;
//import org.gumtree.data.nexus.IAxis;
//import org.gumtree.data.nexus.INXDataset;
//import org.gumtree.data.nexus.INXdata;
//import org.gumtree.data.nexus.utils.NexusUtils;
//import org.gumtree.vis.awt.PlotFactory;
//import org.gumtree.vis.dataset.XYErrorDataset;
//import org.gumtree.vis.nexus.dataset.NXDatasetSeries;
//import org.jfree.chart.ChartUtilities;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.ValueAxis;
//import org.jfree.chart.renderer.xy.XYItemRenderer;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class KookaburraRestlet extends Restlet {

	private static final String ONLINE_SCRIPTING_INIT_SCRIPT = "gumtree.scripting.initscript";
	
	private static final String SICS_DATA_PATH = "sics.data.path";
	
	private static final String QUERY_HEIGHT = "height";

	private static final String QUERY_WIDTH = "width";

//	private static final String SICS_DATA_PATH = "sics.data.path";
	
	private ImageCache imageCache;

	private static JythonRunner jythonRunner;
	
	public KookaburraRestlet() {
		delayedInit();
	}

	public void delayedInit() {
		Thread initThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
				initJythonRunner();
			}
		});
		initThread.start();
	}
	
	public synchronized void initJythonRunner() {
		if (jythonRunner == null) {
			jythonRunner = (new JythonRunnerManager()).getNewRunner();
			init(jythonRunner);
		}
	}
	
	public void init(JythonRunner jythonRunner) {
//		jythonRunner.runScriptLine("from bragg.kookaburra.online import *");
		String initScript = System.getProperty(ONLINE_SCRIPTING_INIT_SCRIPT);
		if (initScript != null) {
			try {
				try {
					URL url = FileLocator.find(Platform.getBundle("au.gov.ansto.bragg.nbi.scripts"), new Path(initScript), null);
					if(url != null){
						initScript = FileLocator.resolve(url).toURI().getPath();
					}
				} catch (Exception e) {
					
				}
				jythonRunner.getUIHandler().getScriptControlHtml(initScript);
				//				String html = jythonRunner.getScriptGUI("from bragg.kookaburra.online import *");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handle(Request request, Response response) {
		// Get path + query (everything after http://<base url>)
		String path = request.getResourceRef().getRemainingPart();
		// Take the first '/' out
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		// Get query
		Form queryForm = request.getResourceRef().getQueryAsForm();
		// Get pure path
		if (queryForm.size() != 0) {
			path = path.substring(0, path.indexOf('?'));
		}
		// Get path tokens
		String[] pathTokens = path.split("/");
		if (pathTokens.length > 0) {
			if (pathTokens[0].equals("plot")) {
				if (jythonRunner == null) {
					response.setEntity("NOT AVAILABLE", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
					return;
				}
				try {
					int height = Integer.parseInt(queryForm.getValues(QUERY_HEIGHT));
					int width = Integer.parseInt(queryForm.getValues(QUERY_WIDTH));
					jythonRunner.getPlot1().setHeight(height);
					jythonRunner.getPlot1().setWidth(width);
				} catch (Exception e) {
					// TODO log error
				}
				try {
					handlePlotRequest(request, response, queryForm);
				} catch (Exception e) {
					e.printStackTrace();
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
				}
			} else if (pathTokens[0].equals("status")) {
				if (jythonRunner == null) {
					response.setEntity("NOT AVAILABLE", MediaType.TEXT_PLAIN);
					response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
					return;
				}
				try {
					handleStatusRequest(request, response, queryForm);
				} catch (Exception e) {
					e.printStackTrace();
		    		response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
				}
			}
		}
	}

	private void handleStatusRequest(Request request, Response response, Form queryForm) throws JSONException {
//		if (jythonRunner.getStatus() == ExecutorStatus.BUSY) {
//			response.setEntity("BUSY", MediaType.TEXT_PLAIN);
//			response.setStatus(Status.SUCCESS_OK);
//		}
//		String filepath = System.getProperty(SICS_DATA_PATH);
//		if (filepath == null) {
//			filepath = "/experiments/taipan/data/current";
//		}
//		File dir = new File(filepath);
//
//		File[] files = dir.listFiles();
//		File lastModifiedFile = null;
//		if (files.length > 0) {
//			lastModifiedFile = files[0];
//			for (int i = 1; i < files.length; i++) {
//				if (files[i].getName().toLowerCase().endsWith(".nx.hdf") && lastModifiedFile.lastModified() < files[i].lastModified() && files[i].isFile()) {
//					lastModifiedFile = files[i];
//				}
//			}
//		}
//		if (lastModifiedFile != null) {
//			jythonRunner.resetConsoleMessage();
//			JSONObject jsonObject = getExecutorStatus(jythonRunner, false);
//			response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
//		} else {
//			response.setEntity("NOT AVAILABLE", MediaType.TEXT_PLAIN);
//		}
//		response.setStatus(Status.SUCCESS_OK);
		JSONObject jsonObject = getExecutorStatus(jythonRunner, true);
		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
		response.setStatus(Status.SUCCESS_OK);
	}
	
	private void handlePlotRequest(Request request, Response response,
			Form queryForm) throws InterruptedException, JSONException {
		// TODO Auto-generated method stub
//		String html = jythonRunner.getScriptGUI("a = Par('string', 'abc')");
//		jythonRunner.runScriptLine("__selected_files__.append('Z:/kookaburra/data/current/KKB0006506.nx.hdf')");
//		jythonRunner.runScriptLine("__run_script__(__selected_files__)");
//		Thread.sleep(8000);
//		JSONObject jsonObject = getExecutorStatus(jythonRunner);
//		response.setEntity(jsonObject.toString(), MediaType.APPLICATION_JSON);
		if (jythonRunner.getStatus() == ExecutorStatus.BUSY) {
			response.setEntity("Online reduction is BUSY. Please Wait.", MediaType.TEXT_PLAIN);
			response.setStatus(Status.SUCCESS_NO_CONTENT);
			return;
		}
		String filepath = System.getProperty(SICS_DATA_PATH);
//		File dir = new File(filepath);
//
//		File[] files = dir.listFiles();
//		File lastModifiedFile = null;
//		if (files.length > 0) {
//			int i;
//			for (i = 0; i < files.length; i++) {
//				if (files[i].isFile()) {
//					lastModifiedFile = files[i];
//					break;
//				}
//			}
//			for (i++; i < files.length; i++) {
//				if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".nx.hdf") && lastModifiedFile.lastModified() < files[i].lastModified()) {
//					lastModifiedFile = files[i];
//				}
//			}
//		}
		File lastModifiedFile = lastFileModified(filepath);
		if (lastModifiedFile != null) {
			long fileTimestamp = lastModifiedFile.lastModified();
			long lastRunTimestamp = 0;
			try {
				lastRunTimestamp = jythonRunner.getUIHandler().getScriptModel().getLastModifiedTimestamp();
			} catch (Exception e) {
			}
			boolean isPlotAvailable = jythonRunner.getPlot1().isUpdated(false);
			if (fileTimestamp > lastRunTimestamp || !isPlotAvailable) {
				jythonRunner.runScriptLine("__selected_files__ = ['" + lastModifiedFile.getAbsolutePath().replace("\\", "/") + "']");
				jythonRunner.runScriptLine("__run_script__(__selected_files__)");
				response.setEntity("PROCESSING", MediaType.TEXT_PLAIN);
				response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
			} else {
				Representation result = new InputRepresentation(
						new ByteArrayInputStream(jythonRunner.getPlot1().getImageCache()), MediaType.IMAGE_PNG);
				response.setEntity(result);
				response.setStatus(Status.SUCCESS_OK);
			}
		} else {
			response.setEntity("NOT AVAILABLE", MediaType.TEXT_PLAIN);
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		}
		
	}

	private JSONObject getExecutorStatus(JythonRunner runner, boolean update) throws JSONException{
		JSONObject jsonObject = new JSONObject();
		ExecutorStatus status = runner.getStatus();
		jsonObject.put("status", status);
		jsonObject.put("text", runner.getRecentText(update));
		jsonObject.put("error", runner.getRecentError(update));
		jsonObject.put("js", runner.getEventJs(update));
		jsonObject.put("files", runner.getFilesForDownload(update));
		jsonObject.put("plot1", runner.getPlot1().isUpdated(update));
		jsonObject.put("plot2", runner.getPlot2().isUpdated(update));
		jsonObject.put("plot3", runner.getPlot3().isUpdated(update));
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

//	private byte[] createPlot(int height, int width) {
//		
//		XYErrorDataset dataset = new XYErrorDataset();
//		dataset.setTitle("Data plot not available");
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
//					if (files[i].getName().toLowerCase().endsWith(".nx.hdf") && lastModifiedFile.lastModified() < files[i].lastModified() && files[i].isFile()) {
//						lastModifiedFile = files[i];
//					}
//				}
//				INXDataset ds = null;
//				try {
//					IArray dataArray;
//					ds = NexusUtils.readNexusDataset(lastModifiedFile.toURI());
//					if (ds.getNXroot().getFirstEntry().getGroup("data").getDataItem("total_counts") != null) {
//						dataArray = ds.getNXroot().getFirstEntry().getGroup("data").getDataItem("total_counts").getData();
//						if (ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm1_counts") != null) {
//							IArray bm1_counts = ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm1_counts").getData();
//							if (bm1_counts.getSize() > 0) {
//								double avg = bm1_counts.getArrayMath().sum() * 1.0 / bm1_counts.getSize();
//								dataArray = dataArray.getArrayMath().toScale(avg).eltDivide(bm1_counts).getArray();
//							}
//						}
//					} else {
//						dataArray = ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm2_counts").getData();
//					}
//					INXdata data = ds.getNXroot().getFirstEntry().getData();
//					IAxis hAxis = data.getAxisList().get(0);
//					List<IAxis> axes = data.getAxisList();
//					if (axes.size() > 1) {
//						for (IAxis axis : axes) {
//							if (axis.getSize() <= 1) {
//								continue;
//							}
//							IArray array = axis.getData();
//							double begin = array.getDouble(array.getIndex().set(0));
//							double end = array.getDouble(array.getIndex().set((int) array.getSize() - 1));
//							double diff = begin - end;
//							if (Math.abs(diff) < 1E-3) {
//								continue;
//							}
//							if (Math.abs(begin) > Math.abs(end)){
//				                if (Math.abs(diff / begin) < 1E-3){
//				                    continue;
//				                }
//							} else {
//				                if (Math.abs(diff / end) < 1E-3){
//				                    continue;
//				                }
//				            }
//				            hAxis= axis;
//				            break;
//						}
//					}
//					IArray axisArray = hAxis.getData();
//					NXDatasetSeries series = new NXDatasetSeries(lastModifiedFile.getName());
//					series.setData(axisArray, dataArray, dataArray.getArrayMath().toSqrt().getArray());
//					dataset.addSeries(series);
//					dataset.setTitle(lastModifiedFile.getName());
//					dataset.setXTitle(hAxis.getShortName());
//					dataset.setYTitle("Detector Counts");
//				} catch (Exception e) {
//					throw e;
//				} finally {
//					if (ds != null) {
//						ds.close();
//					}
//				}
//			}
//		} catch (Exception e) {
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


	class ImageCache {
		long timestamp;
		byte[] imagedata;
		Comparable<?> key;

		ImageCache(byte[] imagedata, Comparable<?> key) {
			this.imagedata = imagedata;
			this.key = key;
			timestamp = System.currentTimeMillis();
		}

		boolean isExpired() {
			return System.currentTimeMillis() > (timestamp + 1000 * 5);
		}
	}

	public static File lastFileModified(String dir) {
	    File fl = new File(dir);
	    File[] files = fl.listFiles();
	    File choice = null;
	    int lastNumber = 0;
	    for (File file : files) {
	        if (file.isFile()) {
		    	String name = file.getName();
		    	if (name.startsWith("KKB") && name.endsWith(".nx.hdf")) {
		    		int number = Integer.valueOf(name.substring(3, 10));
		    		if (number > lastNumber) {
		    			choice = file;
		    			lastNumber = number;
		    		}
		    	}
	        }
	    }
	    return choice;
	}
}
