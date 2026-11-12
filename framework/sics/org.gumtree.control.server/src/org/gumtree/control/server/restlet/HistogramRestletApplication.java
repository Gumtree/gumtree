package org.gumtree.control.server.restlet;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves the histogram of the current collection, from the control server's
 * collection histogram service; see {@link HistogramRestlet}.
 */
public class HistogramRestletApplication extends Application {

	private static final Logger logger = LoggerFactory.getLogger(HistogramRestletApplication.class);

	public HistogramRestletApplication(Context parentContext) {
		super(parentContext);
	}

	@Override
	public synchronized Restlet createRoot() {
		Router router = new Router(getContext());
		if (isKafkaAvailable()) {
			router.attachDefault(new HistogramRestlet());
		} else {
			// org.gumtree.kafka.clients is optional for this bundle; without it
			// HistogramRestlet cannot be loaded.
			logger.warn("Histogram REST service unavailable: the org.gumtree.kafka.clients bundle is not installed");
			router.attachDefault(new Restlet() {
				@Override
				public void handle(Request request, Response response) {
					response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
							"The org.gumtree.kafka.clients bundle is not installed");
				}
			});
		}
		return router;
	}

	private static boolean isKafkaAvailable() {
		try {
			Class.forName("org.gumtree.kafka.clients.da00.Da00DataArray", false,
					HistogramRestletApplication.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (LinkageError e) {
			return false;
		}
	}

}
