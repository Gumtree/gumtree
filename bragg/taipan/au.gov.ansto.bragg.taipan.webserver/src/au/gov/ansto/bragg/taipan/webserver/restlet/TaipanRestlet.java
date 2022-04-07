package au.gov.ansto.bragg.taipan.webserver.restlet;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.netcdf.NXConstants;
import org.gumtree.data.nexus.utils.NexusFactory;
import org.gumtree.data.nexus.utils.NexusUtils;
import org.gumtree.security.EncryptionUtils;
import org.gumtree.vis.awt.PlotFactory;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TaipanRestlet extends Restlet {

	private static final Logger logger = LoggerFactory.getLogger(TaipanRestlet.class);
	private static final String QUERY_HEIGHT = "height";

	private static final String QUERY_WIDTH = "width";
	
	private static final String DATA_DICT_PATH = "gumtree.data.dictpath";

	private static final String SICS_DATA_PATH = "gumtree.sics.dataPath";
	private static final String JAVA_IMAGEIO_CACHE = "java.imageio.cache";
	private static final String DEFAULT_QUERY = "open_format=DISLIN_PNG&open_colour_table=RAIN&open_plot_zero_pixels=AUTO&open_annotations=ENABLE";

	private ImageCache imageCache;
	
	private Map<String, HMMCache> imagedataCache;
	
	private Lock fetchLock;
	
//	private volatile IHttpConnector connector;
	private HttpClient client;
	
	private NexusFactory nxFactory;
	private IDictionary dict;


	public TaipanRestlet() {
		String cacheDir = System.getProperty(JAVA_IMAGEIO_CACHE);
		if (cacheDir != null && cacheDir != "null") {
			try {
				ImageIO.setCacheDirectory(new File(cacheDir));
			} catch (Exception e) {
				logger.error("failed to set ImageIO cache directory");
			}
		}
		imagedataCache = new HashMap<String, HMMCache>();
		fetchLock = new ReentrantLock();
		nxFactory = new NexusFactory();
		String dictPath = System.getProperty(DATA_DICT_PATH);
		if (dictPath != null) {
			try {
				dict = nxFactory.openDictionary(dictPath);
			} catch (Exception e) {
			}
//            self.__iNXroot__.setDictionary(self.__iDictionary__)
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
			if (pathTokens[0].equals("plot1")) {
				handlePlot1Request(request, response, queryForm);
			} else if (pathTokens[0].equals("plot2")) {
				handlePlot2Request(request, response, queryForm);
			}
		}
	}

	private void handlePlot1Request(Request request, Response response,
			Form queryForm) {
		String key = request.getResourceRef().getRemainingPart();
		if (imageCache == null || imageCache.isExpired()
				|| !imageCache.key.equals(key)) {
			int height = 300;
			int width = 300;
			try {
				height = Integer.parseInt(queryForm.getValues(QUERY_HEIGHT));
				width = Integer.parseInt(queryForm.getValues(QUERY_WIDTH));
			} catch (Exception e) {
			}
			imageCache = new ImageCache(createPlot(height, width), key);
		}
		byte[] imageData = imageCache.imagedata;
		Representation result = new InputRepresentation(
				new ByteArrayInputStream(imageData), MediaType.IMAGE_PNG);
		response.setEntity(result);
	}

	private void handlePlot2Request(Request request, Response response,
			Form queryForm) {
		String key = request.getResourceRef().getRemainingPart();
		if (imageCache == null || imageCache.isExpired()
				|| !imageCache.key.equals(key)) {
			int height = 300;
			int width = 300;
			try {
				height = Integer.parseInt(queryForm.getValues(QUERY_HEIGHT));
				width = Integer.parseInt(queryForm.getValues(QUERY_WIDTH));
			} catch (Exception e) {
				// TODO log error
			}
			byte[] plot2 = createPlot2(height, width);
			if (plot2 != null) {
				imageCache = new ImageCache(plot2, key);
			} else {
//				dae.handle(request, response);
				handleDAE(request, response);
				return;
			}
		}
		byte[] imageData = imageCache.imagedata;
		Representation result = new InputRepresentation(
				new ByteArrayInputStream(imageData), MediaType.IMAGE_PNG);
		response.setEntity(result);
	}

	private byte[] createPlot(int height, int width) {
		
		boolean isHmm = false;
		XYErrorDataset dataset = new XYErrorDataset();
		XYErrorDataset dataset2 = new XYErrorDataset();
		dataset.setTitle("Data plot not available");
		dataset.setXTitle("Scan Variable");
		dataset.setYTitle("Detector Counts");
		String yTitle = "Detector Counts";

//		XYErrorSeries series1 = new XYErrorSeries("data");
//		series1.add(1.0, 1.0, 0.0);
//		series1.add(2.0, 4.0, 0.0);
//		series1.add(3.0, 3.0, 0.0);
//		series1.add(5.0, 8.0, 0.0);
//		dataset.addSeries(series1);

		try {
			String filepath = System.getProperty(SICS_DATA_PATH);
			if (filepath == null) {
				filepath = "/experiments/taipan/cycle/current/data/sics";
			}
			File dir = new File(filepath);

			File[] files = dir.listFiles();
			if (files.length > 0) {
				File lastModifiedFile = null;
				for (int i = 1; i < files.length; i++) {
					if (files[i].getName().toLowerCase().endsWith(".nx.hdf") 
							&& (lastModifiedFile == null || lastModifiedFile.lastModified() < files[i].lastModified()) 
							&& files[i].isFile()) {
						lastModifiedFile = files[i];
					}
				}
				if (lastModifiedFile == null) {
					throw new Exception("No data is available");
				}
				INXDataset ds = null;
				try {
					IArray dataArray;
					IArray monitorArray;
					ds = NexusUtils.readNexusDataset(lastModifiedFile.toURI());
					if (dict != null) {
						ds.getNXroot().setDictionary(dict);
					}
					if (ds.getNXroot().getFirstEntry().getGroup("data").getDataItem("total_counts") != null) {
						isHmm = true;
						dataArray = ds.getNXroot().getFirstEntry().getGroup("data").getDataItem("total_counts").getData();
						if (ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm1_counts") != null) {
							IArray bm1_counts = ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm1_counts").getData();
							if (bm1_counts.getSize() > 0) {
								double avg = bm1_counts.getArrayMath().sum() * 1.0 / bm1_counts.getSize();
								dataArray = dataArray.getArrayMath().toScale(avg).eltDivide(bm1_counts).getArray();
								yTitle = "Detector Counts (Normalised)";
							}
						}
					} else {
						dataArray = ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm2_counts").getData();
					}
					monitorArray = ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm1_counts").getData();
					INXdata data = ds.getNXroot().getFirstEntry().getData();
					IDataItem hAxis = null;
					List<IAxis> axes = data.getAxisList();

					if (axes.size() > 1) {
						hAxis = data.getAxisList().get(0);
						for (IAxis axis : axes) {
							if (axis.getSize() <= 1) {
								continue;
							}
							IArray array = axis.getData();
							double begin = array.getDouble(array.getIndex().set(0));
							double end = array.getDouble(array.getIndex().set((int) array.getSize() - 1));
							double diff = begin - end;
							if (Math.abs(diff) < 1E-3) {
								continue;
							}
							if (Math.abs(begin) > Math.abs(end)){
				                if (Math.abs(diff / begin) < 1E-3){
				                    continue;
				                }
							} else {
				                if (Math.abs(diff / end) < 1E-3){
				                    continue;
				                }
				            }
				            hAxis= axis;

				            break;
						}
					} else if (axes.size() == 1) {
						hAxis = axes.get(0);
					} else {
						ISignal signal = data.getSignal();
						IAttribute axesAttribute = signal.getAttribute(NXConstants.SIGNAL_AXES_LABEL);
						if (axesAttribute != null) {
							String value = axesAttribute.getStringValue();
							String[] axisNames = value.split(":");
							if (axisNames.length > 0) {
								String haxisName = axisNames[0];

								hAxis = data.getRootGroup().findDataItem(haxisName);
							}
						}

					}
//					if (hAxis.getShortName().equals("ei")) {
//						try {
//							IDataItem vei = ds.getNXroot().getFirstEntry().getGroup("sample").getDataItem("vei_1");
//							if (vei != null) {
//								hAxis = vei;
//							}
//						} catch (Exception e) {
//						}
//					}
					IArray axisArray;
					if (hAxis != null) {
						axisArray = hAxis.getData();
					} else {
						int size = ((Long) dataArray.getSize()).intValue();
						int[] shape = new int[] {size};
						int[] storage = new int[size];
						for (int i = 0; i < size; i++) {
							storage[i] = i;
						}
						axisArray = nxFactory.createArray(int.class, shape, storage);
					}

					NXDatasetSeries series = new NXDatasetSeries("Detector Counts");
					series.setData(axisArray, dataArray, dataArray.getArrayMath().toSqrt().getArray());
					dataset.addSeries(series);
					dataset.setTitle(lastModifiedFile.getName());
					if (hAxis != null) {
						dataset.setXTitle(hAxis.getShortName());
					} else {
						dataset.setXTitle("run_number");
					}
					dataset.setYTitle(yTitle);
					
					if (isHmm) {
						dataset2.setTitle("Data plot not available");
						dataset2.setXTitle("Scan Variable");
						dataset2.setYTitle("Monitor Counts");
						NXDatasetSeries series2 = new NXDatasetSeries("Monitor Counts");
						series2.setData(axisArray, monitorArray, monitorArray.getArrayMath().toSqrt().getArray());
						dataset2.addSeries(series2);
					}
					
				} catch (Exception e) {
					logger.error("failed to create plots", e);
					throw e;
				} finally {
					if (ds != null) {
						ds.close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			dataset.setTitle(e.getMessage());
		} 

		
		
		JFreeChart chart = PlotFactory.createXYErrorChart(dataset);
		chart.getLegend().setVisible(isHmm);
		chart.setBackgroundPaint(Color.BLACK);
		chart.getXYPlot().setBackgroundPaint(Color.DARK_GRAY);
		chart.getTitle().setPaint(Color.WHITE);
		ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
		domainAxis.setLabelPaint(Color.WHITE);
		domainAxis.setTickLabelPaint(Color.LIGHT_GRAY);
		domainAxis.setTickMarkPaint(Color.LIGHT_GRAY);
		ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
		rangeAxis.setLabelPaint(Color.WHITE);
		rangeAxis.setTickLabelPaint(Color.LIGHT_GRAY);
		rangeAxis.setTickMarkPaint(Color.LIGHT_GRAY);
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		if (renderer instanceof XYLineAndShapeRenderer) {
			renderer.setSeriesPaint(0, Color.CYAN);
		}
		
		if (isHmm) {
	        chart.getXYPlot().setDataset(1, dataset2);
			final NumberAxis rangeAxis2 = new NumberAxis("Monitor Counts");
	        rangeAxis2.setAutoRangeIncludesZero(false);
	        rangeAxis2.setLabelPaint(Color.WHITE);
	        rangeAxis2.setTickLabelPaint(Color.LIGHT_GRAY);
	        rangeAxis2.setTickMarkPaint(Color.LIGHT_GRAY);
	        DefaultXYItemRenderer newRenderer = new DefaultXYItemRenderer();
	        newRenderer.setBaseShapesVisible(false);
	        chart.getXYPlot().setRenderer(1, newRenderer);
	        chart.getXYPlot().setRangeAxis(1, rangeAxis2);
	        chart.getXYPlot().mapDatasetToRangeAxis(1, 1);
	        chart.getXYPlot().setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
	        rangeAxis2.setVisible(true);
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ChartUtilities.writeChartAsPNG(out, chart, width, height);
		} catch (IOException e) {
			logger.error("failed to create chart", e);
		}
		byte[] outByte = out.toByteArray();
		return outByte;
	}

	private byte[] createPlot2(int height, int width) {
		
		XYErrorDataset dataset = new XYErrorDataset();
		dataset.setTitle("Data plot not available");
		dataset.setXTitle("Scan Variable");
		dataset.setYTitle("Monitor Counts");
		String yTitle = "Monitor Counts";

//		XYErrorSeries series1 = new XYErrorSeries("data");
//		series1.add(1.0, 1.0, 0.0);
//		series1.add(2.0, 4.0, 0.0);
//		series1.add(3.0, 3.0, 0.0);
//		series1.add(5.0, 8.0, 0.0);
//		dataset.addSeries(series1);

		try {
			String filepath = System.getProperty(SICS_DATA_PATH);
			if (filepath == null) {
				filepath = "/experiments/taipan/cycle/current/data/sics";
			}
			File dir = new File(filepath);

			File[] files = dir.listFiles();
			if (files.length > 0) {
				File lastModifiedFile = null;
				for (int i = 1; i < files.length; i++) {
					if (files[i].getName().toLowerCase().endsWith(".nx.hdf") 
							&& (lastModifiedFile == null || lastModifiedFile.lastModified() < files[i].lastModified()) 
							&& files[i].isFile()) {
						lastModifiedFile = files[i];
					}
				}
				if (lastModifiedFile == null) {
					throw new Exception("No data is available");
				}
				INXDataset ds = null;
				try {
					IArray dataArray;
					ds = NexusUtils.readNexusDataset(lastModifiedFile.toURI());
					if (dict != null) {
						ds.getNXroot().setDictionary(dict);
					}

					if (ds.getNXroot().getFirstEntry().getGroup("data").getDataItem("total_counts") != null) {
						return null;
					} else {
						dataArray = ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm1_counts").getData();
					}
					INXdata data = ds.getNXroot().getFirstEntry().getData();
					IDataItem hAxis = null;
					List<IAxis> axes = data.getAxisList();
					if (axes.size() > 1) {
						hAxis = axes.get(0);
						for (IAxis axis : axes) {
							if (axis.getSize() <= 1) {
								continue;
							}
							IArray array = axis.getData();
							double begin = array.getDouble(array.getIndex().set(0));
							double end = array.getDouble(array.getIndex().set((int) array.getSize() - 1));
							double diff = begin - end;
							if (Math.abs(diff) < 1E-3) {
								continue;
							}
							if (Math.abs(begin) > Math.abs(end)){
				                if (Math.abs(diff / begin) < 1E-3){
				                    continue;
				                }
							} else {
				                if (Math.abs(diff / end) < 1E-3){
				                    continue;
				                }
				            }
				            hAxis= axis;
				            break;
						}
					} else if (axes.size() == 1) {
						hAxis = axes.get(0);
					} else {
						ISignal signal = data.getSignal();
						IAttribute axesAttribute = signal.getAttribute(NXConstants.SIGNAL_AXES_LABEL);
						if (axesAttribute != null) {
							String value = axesAttribute.getStringValue();
							String[] axisNames = value.split(":");
							if (axisNames.length > 0) {
								String haxisName = axisNames[0];
								hAxis = data.getRootGroup().findDataItem(haxisName);
							}
						}


					}
//					if (hAxis.getShortName().equals("ei")) {
//						try {
//							IDataItem vei = ds.getNXroot().getFirstEntry().getGroup("sample").getDataItem("vei_1");
//							if (vei != null) {
//								hAxis = vei;
//							}
//						} catch (Exception e) {
//						}
//					}
					IArray axisArray;
					if (hAxis != null) {
						axisArray = hAxis.getData();
					} else {
						int size = ((Long) dataArray.getSize()).intValue();
						int[] shape = new int[] {size};
						int[] storage = new int[size];
						for (int i = 0; i < size; i++) {
							storage[i] = i;
						}
						axisArray = nxFactory.createArray(int.class, shape, storage);
					}
					NXDatasetSeries series = new NXDatasetSeries("Monitor Counts");
					series.setData(axisArray, dataArray, dataArray.getArrayMath().toSqrt().getArray());
					dataset.addSeries(series);
					dataset.setTitle(lastModifiedFile.getName());
					if (hAxis != null) {
						dataset.setXTitle(hAxis.getShortName());
					} else {
						dataset.setXTitle("run_number");
					}
					dataset.setYTitle(yTitle);
					
				} catch (Exception e) {
					throw e;
				} finally {
					if (ds != null) {
						ds.close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			dataset.setTitle(e.getMessage());
		} 

		
		
		JFreeChart chart = PlotFactory.createXYErrorChart(dataset);
		chart.getLegend().setVisible(false);
		chart.setBackgroundPaint(Color.BLACK);
		chart.getXYPlot().setBackgroundPaint(Color.DARK_GRAY);
		chart.getTitle().setPaint(Color.WHITE);
		ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
		domainAxis.setLabelPaint(Color.WHITE);
		domainAxis.setTickLabelPaint(Color.LIGHT_GRAY);
		domainAxis.setTickMarkPaint(Color.LIGHT_GRAY);
		ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
		rangeAxis.setLabelPaint(Color.WHITE);
		rangeAxis.setTickLabelPaint(Color.LIGHT_GRAY);
		rangeAxis.setTickMarkPaint(Color.LIGHT_GRAY);
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		if (renderer instanceof XYLineAndShapeRenderer) {
			renderer.setSeriesPaint(0, Color.CYAN);
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ChartUtilities.writeChartAsPNG(out, chart, width, height);
		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
		return out.toByteArray();
	}
	
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

	class HMMCache {
		long timestamp;
		byte[] imagedata;
		String uri;
		HMMCache(long timestamp, byte[] imagedata, String uri) {
			this.timestamp = timestamp;
			this.imagedata = imagedata;
			this.uri = uri;
		}
		boolean isExpired() {
			return System.currentTimeMillis() > (timestamp + 5000);  
		}
	}

	public void handleDAE(Request request, Response response) {
		fetchLock.lock();
		try {
			handleGetImage(response);
		} catch (Exception e) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
		fetchLock.unlock();
    }
	
	
	private void handleGetImage(Response response) {
		try {
			byte[] imageData = fetchImage();
			Representation result = new InputRepresentation(new ByteArrayInputStream(imageData), MediaType.IMAGE_PNG);
			response.setEntity(result);
		} catch (Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
	}
	
	private byte[] fetchImage() throws Exception {
		// Clean up cache
		Iterator<Entry<String, HMMCache>> iterator = imagedataCache.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, HMMCache> cacheEntry = iterator.next();
			if (cacheEntry.getValue().isExpired()) {
				imagedataCache.remove(cacheEntry.getKey());
			}
		}
		// Check cache
		String uri = "http://" + SystemProperties.DAE_HOST.getValue().trim()
				+ ":" + SystemProperties.DAE_PORT.getValue()
				+ SystemProperties.DAE_IMAGE_URL_PATH.getValue().trim() + "?"
				+ DEFAULT_QUERY + "&type=TOTAL_HISTOGRAM_XY&screen_size_x=640&screen_size_y=600";
		if (imagedataCache.containsKey(uri)) {
			return imagedataCache.get(uri).imagedata;
		}
		// Otherwise fetch data
		GetMethod getMethod = new GetMethod(uri);
		getMethod.setDoAuthentication(true);
		int statusCode = getClient().executeMethod(getMethod);
		if (statusCode != HttpStatus.SC_OK) {
			getMethod.releaseConnection();
		}
		byte[] imagedata = getMethod.getResponseBody();
		// Cache if buffer is not full
		imagedataCache.put(uri, new HMMCache(System.currentTimeMillis(), imagedata, uri));
		return imagedata;
	}
	
	public HttpClient getClient() {
		if (client == null) {
			synchronized (this) {
				client = new HttpClient();

				// Set credentials if login information supplied
				client.getParams().setAuthenticationPreemptive(true);
				String user = SystemProperties.DAE_LOGIN.getValue();
				String password = null;
				try {
					password = EncryptionUtils.decryptBase64(SystemProperties.DAE_PASSWORD.getValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (user != null && password != null) {
					Credentials defaultcreds = new UsernamePasswordCredentials(
							user, password);
					client.getState().setCredentials(AuthScope.ANY, defaultcreds);
				}
			}
		}
		return client;
	}
}
