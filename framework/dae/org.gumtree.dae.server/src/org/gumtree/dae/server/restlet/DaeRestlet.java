package org.gumtree.dae.server.restlet;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.gumtree.core.management.IManageableBean;
import org.gumtree.core.management.IManageableBeanProvider;
import org.gumtree.dae.server.internal.SystemProperties;
import org.gumtree.security.EncryptionUtils;
import org.gumtree.util.SystemProperty;
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

import com.google.common.base.Strings;

public class DaeRestlet extends Restlet implements IManageableBeanProvider {
	
	private static final String PART_IMAGE = "image";
	
	private static final SystemProperty HISTOGRAM_LOG_SCALING = new SystemProperty("gumtree.hm.imageLogScaling", "2");
	
	private static final String DEFAULT_QUERY = "open_format=DISLIN_PNG&open_colour_table=RAIN&open_plot_zero_pixels=AUTO&open_annotations=ENABLE&scaling_type=LOG&log_scaling_range=" 
			+ HISTOGRAM_LOG_SCALING.getValue();
	
	private static Logger logger = LoggerFactory.getLogger(DaeRestlet.class);
	
	private volatile IHttpConnector connector;
	
	private Map<String, ImageCache> imagedataCache;
	
	private Lock fetchLock;
	
	private DaeRestletMBean mbean;
	
	public DaeRestlet() {
		imagedataCache = new HashMap<String, ImageCache>();
		fetchLock = new ReentrantLock();
		mbean = new DaeRestletMBean();
	}
	
	public IHttpConnector getConnector() {
		if (connector == null) {
			synchronized (this) {
				if (connector == null) {
					connector = new HttpConnector();
					Map<String, String> parameters = new HashMap<String, String>();
					// Login
					parameters.put(IHttpConnector.KEY_LOGIN, SystemProperties.DAE_LOGIN.getValue());
					if (SystemProperties.DAE_PASSWORD_ENCRYPTED.getBoolean()) {
						try {
							parameters.put(IHttpConnector.KEY_PASSWORD, EncryptionUtils.decryptBase64(SystemProperties.DAE_PASSWORD.getValue()));
						} catch (Exception e) {
							logger.error("Cannot read encrypted password for DAE.");
						}	
					} else {
						parameters.put(IHttpConnector.KEY_PASSWORD, SystemProperties.DAE_PASSWORD.getValue());
					}
					// Proxy
//					if (!StringUtils.isEmpty(SystemProperties.HTTP_PROXY_HOST.getValue())) {
//						parameters.put(IHttpConnector.KEY_PROXY_HOST, SystemProperties.HTTP_PROXY_HOST.getValue());
//						parameters.put(IHttpConnector.KEY_PROXY_PORT, SystemProperties.HTTP_PROXY_PORT.getValue());
//					}
					connector.setParameters(parameters);
				}
			}
		}
		return connector;
	}
	
	public void handle(Request request, Response response) {
		fetchLock.lock();
		RequestContext context = createContext(request);
		if (context.isGetImage) {
			handleGetImage(context, response);
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
		fetchLock.unlock();
    }
	
	private void handleGetImage(RequestContext context, Response response) {
		try {
			byte[] imageData = fetchImage(context);
			Representation result = new InputRepresentation(new ByteArrayInputStream(imageData), MediaType.IMAGE_PNG);
			response.setEntity(result);
		} catch (Exception e) {
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e.toString());
		}
	}
	
	private byte[] fetchImage(RequestContext context) throws Exception {
		// Clean up cache
		Iterator<Entry<String, ImageCache>> iterator = imagedataCache.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, ImageCache> cacheEntry = iterator.next();
			if (cacheEntry.getValue().isExpired()) {
				imagedataCache.remove(cacheEntry.getKey());
			}
		}
		// Check cache
		String uri = "http://" + SystemProperties.DAE_HOST.getValue().trim()
				+ ":" + SystemProperties.DAE_PORT.getValue()
				+ SystemProperties.DAE_IMAGE_URL_PATH.getValue().trim() + "?"
				+ DEFAULT_QUERY;
		StringBuilder query = new StringBuilder();
		if (!Strings.isNullOrEmpty(context.query)) {
			query.append("&");
			query.append(context.query);
		}
		if (query.length() > 0) {
			uri += query.toString();
		}
		if (imagedataCache.containsKey(uri)) {
			return imagedataCache.get(uri).imagedata;
		}
		// Otherwise fetch data
		GetMethod getMethod = new GetMethod(uri);
		getMethod.setDoAuthentication(true);
		int statusCode = getConnector().getClient().executeMethod(getMethod);
		if (statusCode != HttpStatus.SC_OK) {
			logger.error("Method GET failed: " + getMethod.getStatusLine());
			getMethod.releaseConnection();
		}
		byte[] imagedata = getMethod.getResponseBody();
		// Cache if buffer is not full
		if (imagedataCache.size() < mbean.getCacheSize()) {
			imagedataCache.put(uri, new ImageCache(System.currentTimeMillis(), imagedata, uri));
		}
		return imagedata;
	}
	
	private RequestContext createContext(Request request) {
		RequestContext context = new RequestContext();
		// Get path + query (everything after http://.../anstohm)
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
        	context.query = queryForm.getQueryString();
        }
        // Get path tokens
        String[] pathTokens = path.split("/");
        // Set is get image
        context.isGetImage = pathTokens.length > 0 && pathTokens[0].equals(PART_IMAGE);
        return context;
	}
	
	class RequestContext {
		String query = ""; 
		boolean isGetImage;
	}
	
	class ImageCache {
		long timestamp;
		byte[] imagedata;
		String uri;
		ImageCache(long timestamp, byte[] imagedata, String uri) {
			this.timestamp = timestamp;
			this.imagedata = imagedata;
			this.uri = uri;
		}
		boolean isExpired() {
			return System.currentTimeMillis() > (timestamp + mbean.getCacheExpiry());  
		}
	}

	@Override
	public IManageableBean[] getManageableBeans() {
		return new IManageableBean[] { mbean };
	}

}
