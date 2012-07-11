package au.gov.ansto.bragg.taipan.webserver.restlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.gumtree.vis.awt.PlotFactory;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.dataset.XYErrorSeries;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
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

		XYErrorSeries series = new XYErrorSeries("data");
		series.add(1.0, 1.0, 0.0);
		series.add(2.0, 4.0, 0.0);
		series.add(3.0, 3.0, 0.0);
		series.add(5.0, 8.0, 0.0);
		dataset.addSeries(series);

		JFreeChart chart = PlotFactory.createXYErrorChart(dataset);
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
