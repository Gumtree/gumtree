package org.gumtree.control.server.kafka;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.control.server.kafka.HistogramProjections.Axis;
import org.gumtree.control.server.kafka.HistogramProjections.Bins;
import org.gumtree.control.server.kafka.HistogramProjections.View;
import org.gumtree.kafka.clients.da00.Da00DataArray;
import org.gumtree.kafka.clients.da00.Da00Variable;
import org.json.JSONObject;

/**
 * Keeps the collection state and the newest histogram frame of each kind (X-Y,
 * X-T, Y-T) of a {@link CollectionHistogramService} for clients that poll or
 * stream them, and lets them wait for the next change. Frames are converted to
 * JSON once, on first request, and shared by all clients.
 * <p>
 * Every change, a frame or a collection start/stop, increases
 * {@link Snapshot#sequence}; clients remember the sequence they have seen and
 * wait for a larger one. A client slower than the frames simply gets the newest
 * frame next, skipping those in between, which is right for accumulated
 * histograms: each frame contains all the counts so far.
 * <p>
 * Clients normally ask for one view (see {@link #toViewJson}): Total X-Y, X-T
 * or Y-T from the frame of that kind, Total X and Y from the X-Y frame, and
 * Total T from the X-T frame (each falling back to another frame that has the
 * axis), computed once per frame by {@link HistogramProjections}.
 */
public class HistogramBroadcaster implements CollectionHistogramService.Listener {

	/** For each view, the kinds of frame it can be computed from, best first */
	private static final Map<String, List<String>> VIEW_SOURCES = new HashMap<String, List<String>>();

	static {
		VIEW_SOURCES.put("xy", Collections.singletonList(CollectionHistogramService.XY));
		VIEW_SOURCES.put("xt", Collections.singletonList(CollectionHistogramService.XT));
		VIEW_SOURCES.put("yt", Collections.singletonList(CollectionHistogramService.YT));
		// X and Y from X-Y, which holds every event; X-T and Y-T leave out
		// events outside their T range.
		VIEW_SOURCES.put("x", list(CollectionHistogramService.XY, CollectionHistogramService.XT));
		VIEW_SOURCES.put("y", list(CollectionHistogramService.XY, CollectionHistogramService.YT));
		VIEW_SOURCES.put("t", list(CollectionHistogramService.XT, CollectionHistogramService.YT));
	}

	private static List<String> list(String first, String second) {
		List<String> list = new ArrayList<String>();
		list.add(first);
		list.add(second);
		return Collections.unmodifiableList(list);
	}

	/**
	 * The state at one moment. Immutable.
	 */
	public static final class Snapshot {

		/** Increases with every change */
		public final long sequence;

		/** The sequence of the last collection start or stop */
		public final long statusSequence;

		/** The sequence of the newest frame, of any kind; 0 if there is none */
		public final long frameSequence;

		/** The current (or last) collection, or null before the first */
		public final String collectionId;

		public final boolean collectionRunning;

		/** ns since the Unix epoch; 0 if unknown */
		public final long startTimeNs;

		/** ns since the Unix epoch; 0 while running */
		public final long endTimeNs;

		/** The newest frame of each kind of this collection */
		public final Map<String, Da00DataArray> frames;

		/** The sequence of each frame in {@link #frames} */
		public final Map<String, Long> frameSequences;

		Snapshot(long sequence, long statusSequence, long frameSequence, String collectionId,
				boolean collectionRunning, long startTimeNs, long endTimeNs, Map<String, Da00DataArray> frames,
				Map<String, Long> frameSequences) {
			this.sequence = sequence;
			this.statusSequence = statusSequence;
			this.frameSequence = frameSequence;
			this.collectionId = collectionId;
			this.collectionRunning = collectionRunning;
			this.startTimeNs = startTimeNs;
			this.endTimeNs = endTimeNs;
			this.frames = frames;
			this.frameSequences = frameSequences;
		}

		/**
		 * @return the kind of frame a view is computed from, or null if no frame
		 *         here has its axes
		 */
		public String sourceOf(String view) {
			List<String> kinds = VIEW_SOURCES.get(view);
			if (kinds != null) {
				for (String kind : kinds) {
					if (frames.containsKey(kind)) {
						return kind;
					}
				}
			}
			return null;
		}

		/**
		 * @param view
		 *            a view, or null for the whole frame ({@link #wholeFrameKind()})
		 * @return the sequence of the frame the view is computed from; 0 if there
		 *         is none
		 */
		public long sequenceOf(String view) {
			String kind = view == null ? wholeFrameKind() : sourceOf(view);
			Long frameSequence = kind == null ? null : frameSequences.get(kind);
			return frameSequence == null ? 0 : frameSequence.longValue();
		}

		/**
		 * @return the kind of frame served whole, without a view: X-Y if there is
		 *         one, else the first kind there is; null if there is no frame
		 */
		public String wholeFrameKind() {
			for (String kind : CollectionHistogramService.KINDS) {
				if (frames.containsKey(kind)) {
					return kind;
				}
			}
			return null;
		}

	}

	private final Object lock = new Object();

	// Guarded by lock
	private long sequence;

	private long statusSequence;

	private long frameSequence;

	private String collectionId;

	private boolean collectionRunning;

	private long startTimeNs;

	private long endTimeNs;

	private Map<String, Da00DataArray> frames = Collections.emptyMap();

	private Map<String, Long> frameSequences = Collections.emptyMap();

	private boolean closed;

	// Guarded by cacheLock: JSON by view (or "" for the whole frame), with the
	// sequence of the frame it was made from; views of each kind of frame.
	private final Object cacheLock = new Object();

	private final Map<String, CachedJson> cachedJson = new HashMap<String, CachedJson>();

	private final Map<String, CachedProjections> cachedProjections = new HashMap<String, CachedProjections>();

	/** The requested bins by axis name, for the views' axis labels */
	private final Map<String, Bins> bins;

	/** The views the kinds of frame collected give */
	private final List<String> views;

	private static final class CachedJson {

		final long frameSequence;

		final String json;

		CachedJson(long frameSequence, String json) {
			this.frameSequence = frameSequence;
			this.json = json;
		}

	}

	private static final class CachedProjections {

		final long frameSequence;

		final HistogramProjections projections;

		CachedProjections(long frameSequence, HistogramProjections projections) {
			this.frameSequence = frameSequence;
			this.projections = projections;
		}

	}

	/**
	 * For all kinds of frame, without the histogram requests: views label their
	 * axes by bin number only.
	 */
	public HistogramBroadcaster() {
		this(null);
	}

	/**
	 * @param histogramRequests
	 *            the histograms collected, by kind, as JSON requests to the
	 *            histogram service (see
	 *            {@link CollectionHistogramService.Settings#histograms()}): which
	 *            views clients can expect, and the bins of each axis; null for
	 *            all kinds without bins
	 */
	public HistogramBroadcaster(Map<String, String> histogramRequests) {
		Map<String, Bins> allBins = new HashMap<String, Bins>();
		List<String> kinds = CollectionHistogramService.KINDS;
		if (histogramRequests != null) {
			kinds = new ArrayList<String>(histogramRequests.keySet());
			for (String request : histogramRequests.values()) {
				allBins.putAll(HistogramProjections.binsFromRequest(request));
			}
		}
		bins = Collections.unmodifiableMap(allBins);
		List<String> collected = new ArrayList<String>();
		for (String view : HistogramProjections.ALL_VIEWS) {
			for (String kind : VIEW_SOURCES.get(view)) {
				if (kinds.contains(kind) && !collected.contains(view)) {
					collected.add(view);
				}
			}
		}
		views = Collections.unmodifiableList(collected);
	}

	/**
	 * @return the views the kinds of frame collected give, in the order of
	 *         {@link HistogramProjections#ALL_VIEWS}
	 */
	public List<String> getViews() {
		return views;
	}

	// ---- Listener: called on the service's worker thread --------------------

	public void collectionStarted(String newCollectionId, long newStartTimeNs) {
		synchronized (lock) {
			collectionId = newCollectionId;
			collectionRunning = true;
			startTimeNs = newStartTimeNs;
			endTimeNs = 0;
			frames = Collections.emptyMap();
			frameSequences = Collections.emptyMap();
			frameSequence = 0;
			statusSequence = ++sequence;
			lock.notifyAll();
		}
	}

	public void histogramReceived(String frameCollectionId, String kind, Da00DataArray histogram, boolean running) {
		synchronized (lock) {
			if (!frameCollectionId.equals(collectionId)) {
				return;
			}
			frameSequence = ++sequence;
			Map<String, Da00DataArray> newFrames = new LinkedHashMap<String, Da00DataArray>(frames);
			newFrames.put(kind, histogram);
			frames = Collections.unmodifiableMap(newFrames);
			Map<String, Long> newSequences = new HashMap<String, Long>(frameSequences);
			newSequences.put(kind, Long.valueOf(frameSequence));
			frameSequences = Collections.unmodifiableMap(newSequences);
			lock.notifyAll();
		}
	}

	public void collectionStopped(String stoppedCollectionId, long stoppedEndTimeNs) {
		synchronized (lock) {
			if (!stoppedCollectionId.equals(collectionId)) {
				return;
			}
			collectionRunning = false;
			endTimeNs = stoppedEndTimeNs;
			statusSequence = ++sequence;
			lock.notifyAll();
		}
	}

	// ---- Clients --------------------------------------------------------------

	/**
	 * @return the current state
	 */
	public Snapshot snapshot() {
		synchronized (lock) {
			return currentSnapshot();
		}
	}

	/**
	 * Waits until the state changes after the given sequence.
	 *
	 * @return the state then; its sequence equals afterSequence if the time ran
	 *         out first; null once {@link #close()}d
	 * @throws InterruptedException
	 *             if the waiting thread is interrupted
	 */
	public Snapshot awaitChange(long afterSequence, long timeoutMs) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMs;
		synchronized (lock) {
			while (!closed && sequence <= afterSequence) {
				long remaining = deadline - System.currentTimeMillis();
				if (remaining <= 0) {
					break;
				}
				lock.wait(remaining);
			}
			return closed ? null : currentSnapshot();
		}
	}

	/**
	 * Wakes all waiting clients and ends their waits, for shutdown.
	 */
	public void close() {
		synchronized (lock) {
			closed = true;
			lock.notifyAll();
		}
	}

	public boolean isClosed() {
		synchronized (lock) {
			return closed;
		}
	}

	private Snapshot currentSnapshot() {
		return new Snapshot(sequence, statusSequence, frameSequence, collectionId, collectionRunning, startTimeNs,
				endTimeNs, frames, frameSequences);
	}

	// ---- JSON -----------------------------------------------------------------

	/**
	 * @return the collection state as a JSON object: sequence, taskId (the
	 *         collection's ID), collectionRunning, startTimeNs/Ms, endTimeNs/Ms
	 *         (0 while running), frameSequence, kinds (the kinds of frame
	 *         received) and timestamp
	 */
	public static String toStatusJson(Snapshot snapshot) {
		StringBuilder json = new StringBuilder(256);
		json.append('{');
		json.append("\"sequence\":").append(snapshot.sequence);
		json.append(",\"taskId\":").append(quote(snapshot.collectionId));
		json.append(",\"collectionRunning\":").append(snapshot.collectionRunning);
		appendTime(json, "startTime", snapshot.startTimeNs);
		appendTime(json, "endTime", snapshot.endTimeNs);
		json.append(",\"frameSequence\":").append(snapshot.frameSequence);
		json.append(",\"kinds\":");
		appendNames(json, new ArrayList<String>(snapshot.frames.keySet()));
		json.append(",\"timestamp\":").append(
				quote(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
		json.append('}');
		return json.toString();
	}

	/**
	 * @return the snapshot's whole frame of the kind
	 *         {@link Snapshot#wholeFrameKind()} (the X-Y frame) as a JSON object,
	 *         or null if it has none. Converted once per frame.
	 */
	public String toFrameJson(Snapshot snapshot) {
		String kind = snapshot.wholeFrameKind();
		if (kind == null) {
			return null;
		}
		long source = snapshot.sequenceOf(null);
		synchronized (cacheLock) {
			CachedJson cached = cachedJson.get("");
			if (cached == null || cached.frameSequence != source) {
				cached = new CachedJson(source, frameJson(snapshot, kind, source));
				cachedJson.put("", cached);
			}
			return cached.json;
		}
	}

	/**
	 * One view of the snapshot's frames as a JSON object: sequence (of the frame
	 * it is computed from), taskId (the collection's ID), kind (of that frame),
	 * source, collectionRunning, time (ns, plus ms for JavaScript), unit (null
	 * if not given), total (of all counts in that frame), views (the views
	 * the kinds of frame collected give, of xy, xt, yt, x, y, t: see
	 * {@link #getViews()}), view (the one asked for), and, if available: horizontal (the axis across the plot), vertical
	 * (the axis up it; absent for a 1D view), summedOver (names of the axes
	 * summed over) and data. Each axis has name, size (bins) and, if known from
	 * the histogram requests, minimum, step and units. 2D data is flat, from the
	 * bottom row up: data[v * horizontal.size + h]. If no frame received gives
	 * the view, data is null: it is not collected (not in views), or its frame
	 * has not arrived yet.
	 *
	 * @param view
	 *            one of {@link HistogramProjections#ALL_VIEWS}
	 * @return the JSON, or null if the snapshot has no frame. Converted once per
	 *         frame and view.
	 * @throws IllegalArgumentException
	 *             if the view is not one of {@link HistogramProjections#ALL_VIEWS}
	 */
	public String toViewJson(Snapshot snapshot, String view) {
		if (!HistogramProjections.ALL_VIEWS.contains(view)) {
			throw new IllegalArgumentException("Unknown view " + view + "; use one of " + HistogramProjections.ALL_VIEWS);
		}
		if (snapshot.frames.isEmpty()) {
			return null;
		}
		String kind = snapshot.sourceOf(view);
		if (kind == null) {
			return missingViewJson(snapshot, view, views); // not cached: small
		}
		long source = snapshot.sequenceOf(view);
		synchronized (cacheLock) {
			CachedJson cached = cachedJson.get(view);
			if (cached == null || cached.frameSequence != source) {
				CachedProjections projections = cachedProjections.get(kind);
				if (projections == null || projections.frameSequence != source) {
					projections = new CachedProjections(source, HistogramProjections.of(snapshot.frames.get(kind),
							bins));
					cachedProjections.put(kind, projections);
				}
				cached = new CachedJson(source, viewJson(snapshot, kind, source, projections.projections, view,
						views));
				cachedJson.put(view, cached);
			}
			return cached.json;
		}
	}

	private static String viewJson(Snapshot snapshot, String kind, long source, HistogramProjections projections,
			String viewName, List<String> views) {
		Da00DataArray array = snapshot.frames.get(kind);
		View view = projections == null ? null : projections.getView(viewName);
		StringBuilder json = new StringBuilder(256 + (view == null ? 0 : view.data.length * 6));
		json.append('{');
		json.append("\"sequence\":").append(source);
		json.append(",\"taskId\":").append(quote(snapshot.collectionId));
		json.append(",\"kind\":").append(quote(kind));
		json.append(",\"source\":").append(quote(array.getSourceName()));
		json.append(",\"collectionRunning\":").append(snapshot.collectionRunning);
		appendTime(json, "time", array.getTimestampNs());
		json.append(",\"unit\":").append(quote(projections == null ? null : projections.getUnit()));
		json.append(",\"total\":");
		appendNumber(json, projections == null ? 0 : projections.getTotal());
		json.append(",\"views\":");
		appendNames(json, views);
		json.append(",\"view\":").append(quote(viewName));
		if (view == null) {
			json.append(",\"data\":null}"); // the frame lacks the axes it was expected to have
			return json.toString();
		}
		json.append(",\"horizontal\":");
		appendAxis(json, view.horizontal);
		if (view.vertical != null) {
			json.append(",\"vertical\":");
			appendAxis(json, view.vertical);
		}
		json.append(",\"summedOver\":");
		appendNames(json, view.summedOver);
		json.append(",\"data\":[");
		for (int i = 0; i < view.data.length; i++) {
			if (i > 0) {
				json.append(',');
			}
			appendNumber(json, view.data[i]);
		}
		json.append("]}");
		return json.toString();
	}

	private static String missingViewJson(Snapshot snapshot, String viewName, List<String> views) {
		StringBuilder json = new StringBuilder(256);
		json.append('{');
		json.append("\"sequence\":0");
		json.append(",\"taskId\":").append(quote(snapshot.collectionId));
		json.append(",\"collectionRunning\":").append(snapshot.collectionRunning);
		json.append(",\"views\":");
		appendNames(json, views);
		json.append(",\"view\":").append(quote(viewName));
		json.append(",\"data\":null}");
		return json.toString();
	}

	private static void appendAxis(StringBuilder json, Axis axis) {
		json.append("{\"name\":").append(quote(axis.name));
		json.append(",\"size\":").append(axis.size);
		if (axis.bins != null) {
			if (!Double.isNaN(axis.bins.minimum)) {
				json.append(",\"minimum\":");
				appendNumber(json, axis.bins.minimum);
			}
			if (!Double.isNaN(axis.bins.step)) {
				json.append(",\"step\":");
				appendNumber(json, axis.bins.step);
			}
			appendOptional(json, "units", axis.bins.units);
		}
		json.append('}');
	}

	private static void appendNames(StringBuilder json, List<String> names) {
		json.append('[');
		for (int i = 0; i < names.size(); i++) {
			json.append(i > 0 ? "," : "").append(quote(names.get(i)));
		}
		json.append(']');
	}

	/**
	 * Frame layout: sequence, taskId (the collection's ID), kind, source,
	 * collectionRunning, time (ns, plus ms for JavaScript, whose numbers cannot
	 * hold ns exactly), and variables: each with name, unit, label, source (when
	 * given), dataType, axes, shape and data. Numeric data is a flat array in
	 * row-major order: for shape [n0, n1], element [i][j] is data[i * n1 + j],
	 * and axes[0] names the first dimension. String data is a string.
	 */
	private static String frameJson(Snapshot snapshot, String kind, long source) {
		Da00DataArray array = snapshot.frames.get(kind);
		int size = 256;
		for (Da00Variable variable : array.getVariables()) {
			size += 32 + variable.getSize() * 4;
		}
		StringBuilder json = new StringBuilder(size);
		json.append('{');
		json.append("\"sequence\":").append(source);
		json.append(",\"taskId\":").append(quote(snapshot.collectionId));
		json.append(",\"kind\":").append(quote(kind));
		json.append(",\"source\":").append(quote(array.getSourceName()));
		json.append(",\"collectionRunning\":").append(snapshot.collectionRunning);
		appendTime(json, "time", array.getTimestampNs());
		json.append(",\"variables\":[");
		boolean first = true;
		for (Da00Variable variable : array.getVariables()) {
			if (!first) {
				json.append(',');
			}
			first = false;
			appendVariable(json, variable);
		}
		json.append("]}");
		return json.toString();
	}

	private static void appendVariable(StringBuilder json, Da00Variable variable) {
		json.append("{\"name\":").append(quote(variable.getName()));
		appendOptional(json, "unit", variable.getUnit());
		appendOptional(json, "label", variable.getLabel());
		appendOptional(json, "source", variable.getSource());
		json.append(",\"dataType\":").append(quote(variable.getDataType().name().toLowerCase()));
		json.append(",\"axes\":[");
		String[] axes = variable.getAxes();
		for (int i = 0; i < axes.length; i++) {
			json.append(i > 0 ? "," : "").append(quote(axes[i]));
		}
		json.append("],\"shape\":[");
		long[] shape = variable.getShape();
		for (int i = 0; i < shape.length; i++) {
			json.append(i > 0 ? "," : "").append(shape[i]);
		}
		json.append("],\"data\":");
		if (variable.isString()) {
			json.append(quote(variable.getString()));
		} else {
			try {
				double[] values = variable.getDataAsDouble();
				json.append('[');
				for (int i = 0; i < values.length; i++) {
					if (i > 0) {
						json.append(',');
					}
					appendNumber(json, values[i]);
				}
				json.append(']');
			} catch (IllegalStateException e) {
				json.append("null"); // no data type
			}
		}
		json.append('}');
	}

	private static void appendNumber(StringBuilder json, double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			json.append("null"); // not representable in JSON
		} else if (value == Math.rint(value) && Math.abs(value) < 1e15) {
			json.append((long) value); // counts: no ".0"
		} else {
			json.append(value);
		}
	}

	private static void appendTime(StringBuilder json, String name, long ns) {
		json.append(",\"").append(name).append("Ns\":").append(ns);
		json.append(",\"").append(name).append("Ms\":").append(ns / 1000000L);
	}

	private static void appendOptional(StringBuilder json, String name, String value) {
		if (value != null) {
			json.append(",\"").append(name).append("\":").append(quote(value));
		}
	}

	private static String quote(String value) {
		return value == null ? "null" : JSONObject.quote(value);
	}

}
