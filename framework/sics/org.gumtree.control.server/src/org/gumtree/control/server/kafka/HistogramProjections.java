package org.gumtree.control.server.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.gumtree.kafka.clients.da00.Da00DataArray;
import org.gumtree.kafka.clients.da00.Da00Variable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The views of one histogram frame over its X, Y and T axes: the 2D views X-Y,
 * X-T and Y-T, each summed over any remaining axis, and the 1D views X, Y and
 * T, each summed over the others. An X-T frame, for example, has the views
 * X-T, X and T. Computed once per frame, so that clients receive only the view
 * they show.
 * <p>
 * Axes are found by name (the da00 variable's axes, case-insensitive), whatever
 * their order in the frame. The 2D views are drawn X across and Y up (X-Y), X
 * across and T up (X-T), and T across and Y up (Y-T). Immutable.
 */
public final class HistogramProjections {

	/** All views, in the order offered to clients */
	public static final List<String> ALL_VIEWS = Collections.unmodifiableList(Arrays.asList("xy", "xt",
			"yt", "x", "y", "t"));

	private static final String[] AXIS_NAMES = { "X", "Y", "T" };

	/**
	 * The bins of one axis as requested from the histogram service. minimum and
	 * step are NaN when unknown.
	 */
	public static final class Bins {

		public final double minimum;

		public final double step;

		/** For example posn or usec; null if not given */
		public final String units;

		public Bins(double minimum, double step, String units) {
			this.minimum = minimum;
			this.step = step;
			this.units = units;
		}

	}

	/**
	 * One axis of a view.
	 */
	public static final class Axis {

		/** X, Y or T */
		public final String name;

		/** Number of bins */
		public final int size;

		/** From the histogram request; null if unknown */
		public final Bins bins;

		Axis(String name, int size, Bins bins) {
			this.name = name;
			this.size = size;
			this.bins = bins;
		}

	}

	/**
	 * One view: counts over one axis (1D) or two (2D), summed over the others.
	 */
	public static final class View {

		/** xy, xt, yt, x, y or t */
		public final String name;

		/** The axis across the plot */
		public final Axis horizontal;

		/** The axis up the plot; null for a 1D view */
		public final Axis vertical;

		/** The axes summed over, possibly none */
		public final List<String> summedOver;

		/**
		 * The counts. 2D: data[v * horizontal.size + h], with v = 0 at the bottom.
		 * 1D: data[h].
		 */
		public final double[] data;

		View(String name, Axis horizontal, Axis vertical, List<String> summedOver, double[] data) {
			this.name = name;
			this.horizontal = horizontal;
			this.vertical = vertical;
			this.summedOver = summedOver;
			this.data = data;
		}

	}

	private final Map<String, View> views;

	private final double total;

	private final String unit;

	private HistogramProjections(Map<String, View> views, double total, String unit) {
		this.views = views;
		this.total = total;
		this.unit = unit;
	}

	/**
	 * @param bins
	 *            the requested bins by axis name (X, Y, T), for axis labels; may
	 *            be empty
	 * @return the frame's views, or null if the frame holds no numeric counts
	 *         with X, Y or T axes
	 */
	public static HistogramProjections of(Da00DataArray frame, Map<String, Bins> bins) {
		Da00Variable counts = findCounts(frame);
		if (counts == null) {
			return null;
		}
		int[] shape = counts.getShapeAsInt();
		String[] names = counts.getAxes();
		int rank = shape.length;
		// Dimension of X, Y and T in the variable; -1 if absent
		int[] dimensionOf = { -1, -1, -1 };
		for (int d = 0; d < rank; d++) {
			if (names == null || d >= names.length || names[d] == null) {
				continue;
			}
			String name = names[d].trim().toUpperCase(Locale.ENGLISH);
			for (int a = 0; a < AXIS_NAMES.length; a++) {
				if (AXIS_NAMES[a].equals(name) && dimensionOf[a] < 0) {
					dimensionOf[a] = d;
				}
			}
		}
		Axis[] axes = new Axis[AXIS_NAMES.length];
		boolean any = false;
		for (int a = 0; a < AXIS_NAMES.length; a++) {
			if (dimensionOf[a] >= 0) {
				axes[a] = new Axis(AXIS_NAMES[a], shape[dimensionOf[a]], bins == null ? null : bins.get(AXIS_NAMES[a]));
				any = true;
			}
		}
		if (!any) {
			return null;
		}
		double[] values = counts.getDataAsDouble();

		// The 1D and 2D sums of the axes present, filled in one pass.
		double[][] sums1 = new double[AXIS_NAMES.length][];
		double[][][] sums2 = new double[AXIS_NAMES.length][AXIS_NAMES.length][];
		for (int a = 0; a < AXIS_NAMES.length; a++) {
			if (axes[a] == null) {
				continue;
			}
			sums1[a] = new double[axes[a].size];
			for (int b = a + 1; b < AXIS_NAMES.length; b++) {
				if (axes[b] != null) {
					sums2[a][b] = new double[axes[a].size * axes[b].size];
				}
			}
		}
		// Row-major strides: element (i0, i1, ...) is values[i0 * stride[0] + ...].
		int[] stride = new int[rank];
		int size = 1;
		for (int d = rank - 1; d >= 0; d--) {
			stride[d] = size;
			size *= shape[d];
		}
		int[] index = new int[AXIS_NAMES.length];
		double total = 0;
		int count = Math.min(size, values.length);
		for (int i = 0; i < count; i++) {
			double value = values[i];
			if (Double.isNaN(value) || Double.isInfinite(value)) {
				continue;
			}
			total += value;
			for (int a = 0; a < AXIS_NAMES.length; a++) {
				if (axes[a] != null) {
					int d = dimensionOf[a];
					index[a] = (i / stride[d]) % shape[d];
					sums1[a][index[a]] += value;
				}
			}
			for (int a = 0; a < AXIS_NAMES.length; a++) {
				for (int b = a + 1; b < AXIS_NAMES.length; b++) {
					if (sums2[a][b] != null) {
						// a across, b up: [v][h] = [index b][index a]
						sums2[a][b][index[b] * axes[a].size + index[a]] += value;
					}
				}
			}
		}

		Map<String, View> views = new LinkedHashMap<String, View>();
		for (int a = 0; a < AXIS_NAMES.length; a++) {
			for (int b = a + 1; b < AXIS_NAMES.length; b++) {
				if (sums2[a][b] != null) {
					String name = (AXIS_NAMES[a] + AXIS_NAMES[b]).toLowerCase(Locale.ENGLISH);
					if (name.equals("yt")) {
						// T across, Y up
						views.put(name, new View(name, axes[b], axes[a], others(axes, a, b),
								transpose(sums2[a][b], axes[a].size, axes[b].size)));
					} else {
						views.put(name, new View(name, axes[a], axes[b], others(axes, a, b), sums2[a][b]));
					}
				}
			}
		}
		for (int a = 0; a < AXIS_NAMES.length; a++) {
			if (sums1[a] != null) {
				String name = AXIS_NAMES[a].toLowerCase(Locale.ENGLISH);
				views.put(name, new View(name, axes[a], null, others(axes, a, a), sums1[a]));
			}
		}
		return new HistogramProjections(Collections.unmodifiableMap(views), total, counts.getUnit());
	}

	/**
	 * @param data
	 *            [v][h] with width columns and height rows
	 * @return [h][v]: the same counts with the axes swapped
	 */
	private static double[] transpose(double[] data, int width, int height) {
		double[] swapped = new double[data.length];
		for (int v = 0; v < height; v++) {
			for (int h = 0; h < width; h++) {
				swapped[h * height + v] = data[v * width + h];
			}
		}
		return swapped;
	}

	private static List<String> others(Axis[] axes, int a, int b) {
		List<String> others = new ArrayList<String>();
		for (int i = 0; i < axes.length; i++) {
			if (axes[i] != null && i != a && i != b) {
				others.add(axes[i].name);
			}
		}
		return Collections.unmodifiableList(others);
	}

	/**
	 * The counts: a numeric variable named "data" or "signal" if there is one,
	 * else the numeric variable with the most dimensions.
	 */
	private static Da00Variable findCounts(Da00DataArray frame) {
		Da00Variable best = null;
		for (Da00Variable variable : frame.getVariables()) {
			if (variable.isString() || variable.getDataType() == null || variable.getShape().length == 0) {
				continue;
			}
			if ("data".equals(variable.getName()) || "signal".equals(variable.getName())) {
				return variable;
			}
			if (best == null || variable.getShape().length > best.getShape().length) {
				best = variable;
			}
		}
		return best;
	}

	/**
	 * Reads the bins of each axis from a histogram request (hz_axis/hz_bins,
	 * vt_axis/vt_bins, dp_axis/dp_bins).
	 *
	 * @return the bins by axis name; empty if the request cannot be read
	 */
	public static Map<String, Bins> binsFromRequest(String histogramJson) {
		Map<String, Bins> bins = new HashMap<String, Bins>();
		if (histogramJson == null) {
			return bins;
		}
		JSONObject histogram;
		try {
			histogram = new JSONObject(histogramJson);
		} catch (JSONException e) {
			return bins;
		}
		for (String prefix : new String[] { "hz", "vt", "dp" }) {
			String axis = histogram.optString(prefix + "_axis", null);
			JSONObject spec = histogram.optJSONObject(prefix + "_bins");
			if (axis == null || spec == null || "null".equals(axis)) {
				continue;
			}
			String units = spec.optString("units", null);
			bins.put(axis.trim().toUpperCase(Locale.ENGLISH), new Bins(spec.optDouble("minimum", Double.NaN),
					spec.optDouble("step", Double.NaN), units));
		}
		return bins;
	}

	/**
	 * @return the views this frame has, in the order of {@link #ALL_VIEWS}
	 */
	public List<String> getViewNames() {
		List<String> names = new ArrayList<String>();
		for (String name : ALL_VIEWS) {
			if (views.containsKey(name)) {
				names.add(name);
			}
		}
		return names;
	}

	/**
	 * @return the view, or null if this frame does not have it
	 */
	public View getView(String name) {
		return views.get(name);
	}

	/**
	 * @return the sum of all counts
	 */
	public double getTotal() {
		return total;
	}

	/**
	 * @return the counts' unit, or null if not given
	 */
	public String getUnit() {
		return unit;
	}

}
