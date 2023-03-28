package au.gov.ansto.bragg.koala.webserver.restlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.imageio.ImageIO;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KoalaRestlet extends Restlet {

	private static final Logger logger = LoggerFactory.getLogger(KoalaRestlet.class);
	private static final String QUERY_HEIGHT = "height";

	private static final String QUERY_WIDTH = "width";
	

	private static final String GUMTREE_IMAGE_FILE = "gumtree.server.imageFile";
	private static final String JAVA_IMAGEIO_CACHE = "java.imageio.cache";
	private static final String DEFAULT_QUERY = "open_format=DISLIN_PNG&open_colour_table=RAIN&open_plot_zero_pixels=AUTO&open_annotations=ENABLE";

	private ImageCache imageCache;
	
	private Map<String, HMMCache> imagedataCache;
	
	private Lock fetchLock;
	

	public KoalaRestlet() {
		String cacheDir = System.getProperty(JAVA_IMAGEIO_CACHE);
		if (cacheDir != null && cacheDir != "null") {
			try {
				ImageIO.setCacheDirectory(new File(cacheDir));
			} catch (Exception e) {
				logger.error("failed to set ImageIO cache directory");
			}
		}
		imagedataCache = new HashMap<String, HMMCache>();
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
//		String key = request.getResourceRef().getRemainingPart();
//		if (imageCache == null || imageCache.isExpired()
//				|| !imageCache.key.equals(key)) {
//			int height = 300;
//			int width = 300;
//			try {
//				height = Integer.parseInt(queryForm.getValues(QUERY_HEIGHT));
//				width = Integer.parseInt(queryForm.getValues(QUERY_WIDTH));
//			} catch (Exception e) {
//			}
//			try {
//				imageCache = new ImageCache(createImage(height, width), key);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		byte[] imageData = imageCache.imagedata;
//		Representation result = new InputRepresentation(
//				new ByteArrayInputStream(imageData), MediaType.IMAGE_PNG);
//		response.setEntity(result);
		FileRepresentation representation = new FileRepresentation(System.getProperty(GUMTREE_IMAGE_FILE), 
				MediaType.IMAGE_ALL);
		Disposition disposition = new Disposition();
		disposition.setFilename("koala.png");
//		disposition.setType(Disposition.TYPE_ATTACHMENT);
		representation.setDisposition(disposition);
		response.setEntity(representation);
	}

//	private byte[] createImage(int height, int width) throws IOException {
//		File f = new File(System.getProperty(GUMTREE_IMAGE_FILE));
//		if (f.exists()) {
//			BufferedImage image = ImageIO.read(f);
////			ByteArrayOutputStream out = new ByteArrayOutputStream();
////			EncoderUtil.writeBufferedImage(image, ImageFormat.PNG, out);
////			ImageIO.write(image, "png", out);
////			byte[] outByte = out.toByteArray();
//			return image.
////			return outByte;
//		} else {
//			return null;
//		}
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

}
