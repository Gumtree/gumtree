package au.gov.ansto.bragg.taipan.webserver.restlet;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.utils.NexusUtils;
import org.gumtree.vis.awt.PlotFactory;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.dataset.XYErrorSeries;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

public class TaipanRestlet extends Restlet {

	private static final String QUERY_HEIGHT = "height";

	private static final String QUERY_WIDTH = "width";

	private ImageCache imageCache;

	public TaipanRestlet() {

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
				handlePlotRequest(request, response, queryForm);
			}
		}
	}

	private void handlePlotRequest(Request request, Response response,
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
			imageCache = new ImageCache(createPlot(height, width), key);
		}
		byte[] imageData = imageCache.imagedata;
		Representation result = new InputRepresentation(
				new ByteArrayInputStream(imageData), MediaType.IMAGE_PNG);
		response.setEntity(result);
	}

	private byte[] createPlot(int height, int width) {
		
		XYErrorDataset dataset = new XYErrorDataset();
		dataset.setTitle("Bean Monitor VS Scan Variable");
		dataset.setXTitle("Scan Variable");
		dataset.setYTitle("Bean Monitor");

		XYErrorSeries series1 = new XYErrorSeries("data");
		series1.add(1.0, 1.0, 0.0);
		series1.add(2.0, 4.0, 0.0);
		series1.add(3.0, 3.0, 0.0);
		series1.add(5.0, 8.0, 0.0);
		dataset.addSeries(series1);

		try {
			String filepath = "/experiments/taipan/data/current";
			File dir = new File(filepath);

			File[] files = dir.listFiles();
			if (files.length > 0) {
				File lastModifiedFile = files[0];
				for (int i = 1; i < files.length; i++) {
					if (lastModifiedFile.lastModified() < files[i].lastModified()) {
						lastModifiedFile = files[i];
					}
				}
				INXDataset ds = NexusUtils.readNexusDataset(lastModifiedFile.toURI());
				IArray dataArray = ds.getNXroot().getFirstEntry().getGroup("monitor").getDataItem("bm2_counts").getData();
				INXdata data = ds.getNXroot().getFirstEntry().getData();
				IArray axisArray = data.getAxisList().get(0).getData();
				NXDatasetSeries series = new NXDatasetSeries(lastModifiedFile.getName());
				series.setData(dataArray, axisArray, dataArray);
//				dataset.addSeries(series);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

}
