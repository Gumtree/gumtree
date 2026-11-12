package org.gumtree.control.server.restlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import org.gumtree.control.server.kafka.CollectionHistogramManager;
import org.gumtree.control.server.kafka.HistogramBroadcaster;
import org.gumtree.control.server.kafka.HistogramBroadcaster.Snapshot;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves the histogram accumulated over the current collection, as followed by
 * the control server's collection histogram service
 * ({@link CollectionHistogramManager}; enabled with the system property
 * gumtree.kafka.histogram.enabled=true). Paths, below the servlet alias (for
 * example /histogram/rest):
 * <dl>
 * <dt>status</dt>
 * <dd>The collection state as JSON: sequence, taskId, collectionRunning,
 * startTimeNs/Ms, endTimeNs/Ms (0 while running), frameSequence.</dd>
 * <dt>latest</dt>
 * <dd>The newest histogram frame as JSON (see
 * {@link HistogramBroadcaster#toFrameJson}), or 204 No Content if there is none.
 * With after=&lt;sequence&gt;, it long-polls: it answers as soon as there is a
 * frame with a larger sequence, or after wait=&lt;ms&gt; (default 30000, at most
 * 60000) with 204.</dd>
 * <dt>stream</dt>
 * <dd>Server-Sent Events (text/event-stream, for a browser's EventSource):
 * "collection" events with the state JSON when a collection starts or stops,
 * "histogram" events with the frame JSON as frames arrive, and a keep-alive
 * comment every 15 s. On connecting, the current state and frame are sent
 * first. A client slower than the frames gets the newest frame next, skipping
 * those in between. Each stream occupies a server thread, so at most
 * {@value #MAX_STREAMS} are served at a time.</dd>
 * </dl>
 * As in {@link ControlRestlet}, status and latest return JSONP with
 * callback=&lt;name&gt; (or jsoncallback=), and application/json with
 * format=json, otherwise text/plain. Responses allow any origin (CORS), so pages
 * from other servers can use them.
 */
public class HistogramRestlet extends Restlet {

	private static final Logger logger = LoggerFactory.getLogger(HistogramRestlet.class);

	private static final String PART_STATUS = "status";

	private static final String PART_LATEST = "latest";

	private static final String PART_STREAM = "stream";

	private static final String QUERY_FORMAT = "format";

	private static final String QUERY_CALLBACK = "callback";

	private static final String QUERY_JSON_CALLBACK = "jsoncallback";

	private static final String QUERY_AFTER = "after";

	private static final String QUERY_WAIT = "wait";

	private static final long DEFAULT_WAIT_MS = 30000;

	private static final long MAX_WAIT_MS = 60000;

	private static final long KEEP_ALIVE_MS = 15000;

	private static final int MAX_STREAMS = 20;

	private static final MediaType EVENT_STREAM = new MediaType("text/event-stream");

	private static final Charset UTF8 = Charset.forName("UTF-8");

	private static final String HEADERS_ATTRIBUTE = "org.restlet.http.headers";

	private static final AtomicInteger openStreams = new AtomicInteger();

	@Override
	public void handle(Request request, Response response) {
		allowAnyOrigin(response);
		if (!Method.GET.equals(request.getMethod())) {
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			return;
		}
		// Path below the servlet alias, without query and leading '/'
		String path = request.getResourceRef().getRemainingPart(false, false);
		if (path == null) {
			path = "";
		}
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		Form queryForm = request.getResourceRef().getQueryAsForm();
		HistogramBroadcaster broadcaster = CollectionHistogramManager.getBroadcaster();
		if (broadcaster == null || broadcaster.isClosed()) {
			response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
					"Collection histogram service not enabled (set " + CollectionHistogramManager.PROPERTY_ENABLED
							+ "=true)");
			return;
		}
		String part = path.split("/")[0];
		if (part.length() == 0 || part.equals(PART_STATUS)) {
			writeJson(response, queryForm, HistogramBroadcaster.toStatusJson(broadcaster.snapshot()));
		} else if (part.equals(PART_LATEST)) {
			handleLatest(response, queryForm, broadcaster);
		} else if (part.equals(PART_STREAM)) {
			handleStream(response, broadcaster);
		} else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	private void handleLatest(Response response, Form queryForm, HistogramBroadcaster broadcaster) {
		Snapshot snapshot = broadcaster.snapshot();
		String afterValue = queryForm.getFirstValue(QUERY_AFTER);
		if (afterValue != null) {
			long after;
			long waitMs;
			try {
				after = Long.parseLong(afterValue.trim());
				String waitValue = queryForm.getFirstValue(QUERY_WAIT);
				waitMs = waitValue == null ? DEFAULT_WAIT_MS : Long.parseLong(waitValue.trim());
			} catch (NumberFormatException e) {
				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "after and wait must be integers");
				return;
			}
			waitMs = Math.max(0, Math.min(waitMs, MAX_WAIT_MS));
			long deadline = System.currentTimeMillis() + waitMs;
			try {
				// Wait for a frame newer than after; other changes (such as a
				// collection stop) also wake the wait, so check again.
				while (snapshot != null && snapshot.frameSequence <= after) {
					long remaining = deadline - System.currentTimeMillis();
					if (remaining <= 0) {
						break;
					}
					snapshot = broadcaster.awaitChange(snapshot.sequence, remaining);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (snapshot == null || snapshot.frameSequence <= after) {
				response.setStatus(Status.SUCCESS_NO_CONTENT);
				return;
			}
		}
		String json = broadcaster.toFrameJson(snapshot);
		if (json == null) {
			response.setStatus(Status.SUCCESS_NO_CONTENT);
			return;
		}
		writeJson(response, queryForm, json);
	}

	private void handleStream(Response response, final HistogramBroadcaster broadcaster) {
		if (openStreams.incrementAndGet() > MAX_STREAMS) {
			openStreams.decrementAndGet();
			response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "Too many histogram streams open");
			return;
		}
		boolean handedOver = false;
		try {
			response.getCacheDirectives().add(CacheDirective.noCache());
			addHeader(response, "X-Accel-Buffering", "no"); // no proxy buffering
			response.setEntity(new OutputRepresentation(EVENT_STREAM) {
				@Override
				public void write(OutputStream out) throws IOException {
					try {
						stream(out, broadcaster);
					} finally {
						openStreams.decrementAndGet();
					}
				}
			});
			response.setStatus(Status.SUCCESS_OK);
			handedOver = true;
		} finally {
			if (!handedOver) {
				openStreams.decrementAndGet();
			}
		}
	}

	/**
	 * Writes events until the client goes away (the write fails), the service
	 * stops, or the thread is interrupted.
	 */
	private static void stream(OutputStream out, HistogramBroadcaster broadcaster) throws IOException {
		Snapshot snapshot = broadcaster.snapshot();
		write(out, "retry: 5000\n\n");
		write(out, event("collection", snapshot.statusSequence, HistogramBroadcaster.toStatusJson(snapshot)));
		String frame = broadcaster.toFrameJson(snapshot);
		if (frame != null) {
			write(out, event("histogram", snapshot.frameSequence, frame));
		}
		out.flush();
		long seen = snapshot.sequence;
		long seenStatus = snapshot.statusSequence;
		long seenFrame = snapshot.frameSequence;
		try {
			while (true) {
				snapshot = broadcaster.awaitChange(seen, KEEP_ALIVE_MS);
				if (snapshot == null) {
					return; // service stopped
				}
				if (snapshot.sequence == seen) {
					write(out, ": keep-alive\n\n");
				} else {
					if (snapshot.statusSequence > seenStatus) {
						write(out, event("collection", snapshot.statusSequence,
								HistogramBroadcaster.toStatusJson(snapshot)));
					}
					if (snapshot.frameSequence > seenFrame && snapshot.frame != null) {
						write(out, event("histogram", snapshot.frameSequence, broadcaster.toFrameJson(snapshot)));
					}
					seen = snapshot.sequence;
					seenStatus = snapshot.statusSequence;
					seenFrame = snapshot.frameSequence;
				}
				out.flush();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			logger.debug("Histogram stream closed: " + e.getMessage());
		}
	}

	private static String event(String name, long id, String json) {
		// JSON from HistogramBroadcaster has no line breaks, so one data line.
		return "event: " + name + "\nid: " + id + "\ndata: " + json + "\n\n";
	}

	private static void write(OutputStream out, String text) throws IOException {
		out.write(text.getBytes(UTF8));
	}

	private static void writeJson(Response response, Form queryForm, String json) {
		String format = queryForm.getFirstValue(QUERY_FORMAT);
		String callback = queryForm.getFirstValue(QUERY_CALLBACK);
		if (callback == null) {
			callback = queryForm.getFirstValue(QUERY_JSON_CALLBACK);
		}
		response.getCacheDirectives().add(CacheDirective.noCache());
		if (callback != null) {
			response.setEntity(callback + "(" + json + ")", MediaType.APPLICATION_JAVASCRIPT);
		} else if ("json".equals(format)) {
			response.setEntity(json, MediaType.APPLICATION_JSON);
		} else {
			response.setEntity(json, MediaType.TEXT_PLAIN);
		}
	}

	private static void allowAnyOrigin(Response response) {
		addHeader(response, "Access-Control-Allow-Origin", "*");
	}

	private static void addHeader(Response response, String name, String value) {
		Object headers = response.getAttributes().get(HEADERS_ATTRIBUTE);
		if (!(headers instanceof Form)) {
			headers = new Form();
			response.getAttributes().put(HEADERS_ATTRIBUTE, headers);
		}
		((Form) headers).add(name, value);
	}

}
