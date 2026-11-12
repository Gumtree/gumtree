package org.gumtree.control.server.kafka;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.gumtree.kafka.clients.da00.Da00DataArray;
import org.gumtree.kafka.clients.da00.Da00Variable;
import org.json.JSONObject;

/**
 * Keeps the collection state and newest histogram frame of a
 * {@link CollectionHistogramService} for clients that poll or stream them, and
 * lets them wait for the next change. Each frame is converted to JSON once, on
 * first request, and shared by all clients.
 * <p>
 * Every change, a frame or a collection start/stop, increases
 * {@link Snapshot#sequence}; clients remember the sequence they have seen and
 * wait for a larger one. A client slower than the frames simply gets the newest
 * frame next, skipping those in between, which is right for accumulated
 * histograms: each frame contains all the counts so far.
 */
public class HistogramBroadcaster implements CollectionHistogramService.Listener {

	/**
	 * The state at one moment. Immutable.
	 */
	public static final class Snapshot {

		/** Increases with every change */
		public final long sequence;

		/** The sequence of the last collection start or stop */
		public final long statusSequence;

		/** The sequence of the frame below; 0 if there is none */
		public final long frameSequence;

		/** The current (or last) collection's task, or null before the first */
		public final String taskId;

		public final boolean collectionRunning;

		/** ns since the Unix epoch; 0 if unknown */
		public final long startTimeNs;

		/** ns since the Unix epoch; 0 while running */
		public final long endTimeNs;

		/** The newest frame of this collection, or null */
		public final Da00DataArray frame;

		Snapshot(long sequence, long statusSequence, long frameSequence, String taskId, boolean collectionRunning,
				long startTimeNs, long endTimeNs, Da00DataArray frame) {
			this.sequence = sequence;
			this.statusSequence = statusSequence;
			this.frameSequence = frameSequence;
			this.taskId = taskId;
			this.collectionRunning = collectionRunning;
			this.startTimeNs = startTimeNs;
			this.endTimeNs = endTimeNs;
			this.frame = frame;
		}

	}

	private final Object lock = new Object();

	// Guarded by lock
	private long sequence;

	private long statusSequence;

	private long frameSequence;

	private String taskId;

	private boolean collectionRunning;

	private long startTimeNs;

	private long endTimeNs;

	private Da00DataArray frame;

	private boolean closed;

	// Guarded by cacheLock: the JSON of one frame, by its sequence
	private final Object cacheLock = new Object();

	private long cachedFrameSequence;

	private String cachedFrameJson;

	// ---- Listener: called on the service's worker thread --------------------

	public void collectionStarted(String newTaskId, long newStartTimeNs) {
		synchronized (lock) {
			taskId = newTaskId;
			collectionRunning = true;
			startTimeNs = newStartTimeNs;
			endTimeNs = 0;
			frame = null;
			frameSequence = 0;
			statusSequence = ++sequence;
			lock.notifyAll();
		}
	}

	public void histogramReceived(String frameTaskId, Da00DataArray histogram, boolean running) {
		synchronized (lock) {
			if (!frameTaskId.equals(taskId)) {
				return;
			}
			frame = histogram;
			frameSequence = ++sequence;
			lock.notifyAll();
		}
	}

	public void collectionStopped(String stoppedTaskId, long stoppedEndTimeNs) {
		synchronized (lock) {
			if (!stoppedTaskId.equals(taskId)) {
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
		return new Snapshot(sequence, statusSequence, frameSequence, taskId, collectionRunning, startTimeNs,
				endTimeNs, frame);
	}

	// ---- JSON -----------------------------------------------------------------

	/**
	 * @return the collection state as a JSON object
	 */
	public static String toStatusJson(Snapshot snapshot) {
		StringBuilder json = new StringBuilder(256);
		json.append('{');
		json.append("\"sequence\":").append(snapshot.sequence);
		json.append(",\"taskId\":").append(quote(snapshot.taskId));
		json.append(",\"collectionRunning\":").append(snapshot.collectionRunning);
		appendTime(json, "startTime", snapshot.startTimeNs);
		appendTime(json, "endTime", snapshot.endTimeNs);
		json.append(",\"frameSequence\":").append(snapshot.frameSequence);
		json.append(",\"timestamp\":").append(
				quote(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
		json.append('}');
		return json.toString();
	}

	/**
	 * @return the snapshot's frame as a JSON object, or null if it has none.
	 *         Converted once per frame.
	 */
	public String toFrameJson(Snapshot snapshot) {
		if (snapshot.frame == null) {
			return null;
		}
		synchronized (cacheLock) {
			if (cachedFrameSequence != snapshot.frameSequence) {
				cachedFrameJson = frameJson(snapshot);
				cachedFrameSequence = snapshot.frameSequence;
			}
			return cachedFrameJson;
		}
	}

	/**
	 * Frame layout: sequence, taskId, source, collectionRunning, time (ns, plus
	 * ms for JavaScript, whose numbers cannot hold ns exactly), and variables:
	 * each with name, unit, label, source (when given), dataType, axes, shape
	 * and data. Numeric data is a flat array in row-major order: for shape
	 * [n0, n1], element [i][j] is data[i * n1 + j], and axes[0] names the
	 * first dimension. String data is a string.
	 */
	private static String frameJson(Snapshot snapshot) {
		Da00DataArray array = snapshot.frame;
		int size = 256;
		for (Da00Variable variable : array.getVariables()) {
			size += 32 + variable.getSize() * 4;
		}
		StringBuilder json = new StringBuilder(size);
		json.append('{');
		json.append("\"sequence\":").append(snapshot.frameSequence);
		json.append(",\"taskId\":").append(quote(snapshot.taskId));
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
