/**
 * Live histogram of the current collection, from the control server's Kafka
 * collection histogram service (org.gumtree.control.server, HistogramRestlet at
 * histogram/rest). Drawn in the #kafkaHistogramList section of mobile.html,
 * which stays hidden unless the service answers.
 *
 * Frames arrive through Server-Sent Events (histogram/rest/stream), or by
 * long-polling histogram/rest/latest in browsers without EventSource. mobile.js
 * calls onRefresh() on every page refresh, and setLive() when Auto Refresh is
 * switched: while it is off, the stream is closed and "Refresh Now" fetches the
 * latest frame once.
 *
 * The server collects three histograms, X-Y, X-T and Y-T (T in 1000 bins of
 * 20 us over a 20 ms frame), each accumulated since the collection's start. If
 * one is configured to clear after each publish instead, the info line says
 * what time it covers (windowSeconds). The Plot selector
 * (#kafkaHistogramView) shows Total X-Y (X across, Y up), Total X-T (X across,
 * T up) or Total Y-T (T across, Y up) as a 2D image, or Total X, Y or T as a
 * 1D plot. The server computes the plot and sends only the one shown
 * (view=xy, xt, yt, x, y or t); plots not collected are disabled in the
 * selector. An axis with more bins than the image has pixels, such as T in
 * 1000 bins, is shown with neighbouring bins added together.
 *
 * The plots offered, their names and the one shown first come from histmemTypes
 * in the instrument's instrumentsetup.js, in its order: TOTAL_HISTOGRAM_XY,
 * _XT, _YT, _X, _Y and _T are Total X-Y, X-T, Y-T, X, Y and T, and the one with
 * "isDefault" : true is shown first (else the first listed). Other types, such
 * as RAW_TOTAL_HISTOGRAM_XT, are not offered here. Without histmemTypes, or with
 * none of these in it, all six plots are offered and X-Y is shown first.
 *
 * An instrument can turn the section off with, in its instrumentsetup.js:
 *     var kafkaHistogramEnabled = false;
 */
var kafkaHistogram = (function() {

	var BASE = "histogram/rest/";
	var VIEWS = ["xy", "xt", "yt", "x", "y", "t"];
	var MAX_HEIGHT_FRACTION = 0.7;	// of the window height
	var MAX_SQUARE_ASPECT = 4;		// 2D plots more elongated than this are stretched
	var VIEW_TITLES = {xy : "X-Y", xt : "X-T", yt : "Y-T", x : "X", y : "Y", t : "T"};
	var HISTMEM_TYPE_VIEWS = {TOTAL_HISTOGRAM_XY : "xy", TOTAL_HISTOGRAM_XT : "xt", TOTAL_HISTOGRAM_YT : "yt",
			TOTAL_HISTOGRAM_X : "x", TOTAL_HISTOGRAM_Y : "y", TOTAL_HISTOGRAM_T : "t"};
	var RETRY_MS = 5000;

	var enabled = typeof kafkaHistogramEnabled === 'undefined' || kafkaHistogramEnabled;
	var available = false;		// the service answered
	var checking = false;
	var live = true;			// follow new frames as they come
	var source = null;			// EventSource
	var polling = false;		// long-poll loop running (no EventSource)
	var pollGeneration = 0;		// ends an old poll loop
	var logScale = true;		// log colour and 1D scale by default
	var offered = VIEWS.slice();	// the plots in the selector, from histmemTypes
	var view = "xy";			// the plot chosen, one of offered
	var profile = null;			// the 1D plot drawn, for the readout
	var PLOT_MARGIN = {left : 56, right : 10, top : 10, bottom : 32};	// CSS pixels
	var lastFrameSequence = 0;
	var lastStatus = null;
	var plot = null;			// what is drawn: {view, horizontal, vertical, values, taskId, ...}
	var grid = null;			// the 2D image as drawn, bins added in groups: {nx, ny, gx, gy, values}
	var shownMax = 0;			// max of the plot shown, for the info line

	// ---- Colour map: the histogram server's rainbow ----------------------------

	/**
	 * The colour scale of the histogram server's images (Total x-y histogram
	 * PNGs): dark purple at the bottom, then blue, cyan, green, yellow, and red
	 * at the top. Stops sampled from the colour bar of such an image, at their
	 * fraction of its height. Bins with no counts are drawn black.
	 */
	var COLOUR_STOPS = [
		[0, [34, 0, 68]],
		[0.047, [51, 0, 170]],
		[0.081, [34, 0, 238]],
		[0.115, [0, 0, 255]],
		[0.375, [0, 255, 255]],
		[0.541, [0, 255, 0]],
		[0.753, [255, 255, 0]],
		[1, [255, 0, 0]]
	];

	/** 256 colours along COLOUR_STOPS, linear between stops. */
	function buildColourMap() {
		var table = new Array(256);
		var k = 0;
		for (var i = 0; i < 256; i++) {
			var t = i / 255;
			while (k < COLOUR_STOPS.length - 2 && t > COLOUR_STOPS[k + 1][0]) {
				k++;
			}
			var a = COLOUR_STOPS[k], b = COLOUR_STOPS[k + 1];
			var f = (t - a[0]) / (b[0] - a[0]);
			table[i] = [Math.round(a[1][0] + (b[1][0] - a[1][0]) * f),
					Math.round(a[1][1] + (b[1][1] - a[1][1]) * f),
					Math.round(a[1][2] + (b[1][2] - a[1][2]) * f)];
		}
		return table;
	}

	var lut = buildColourMap();	// after COLOUR_STOPS is set

	// ---- Showing the section ------------------------------------------------

	function show() {
		$("#kafkaHistogramList").show();
		try {
			$("#kafkaHistogramList").listview("refresh");
		} catch (e) {
		}
	}

	function hide() {
		$("#kafkaHistogramList").hide();
	}

	function setConnection(text, colour) {
		$("#kafkaHistogramConnection").text(text).css("color", colour);
	}

	/** Looks for the service; shows the section and starts if it answers. */
	function checkAvailable() {
		if (!enabled || available || checking) {
			return;
		}
		checking = true;
		$.ajax({
			url : BASE + "status",
			data : {"format" : "json", "timestamp" : new Date().getTime()},
			dataType : "json",
			timeout : 10000,
			success : function(status) {
				checking = false;
				available = true;
				show();
				updateStatus(status);
				if (live) {
					connect();
				} else {
					fetchLatest();
				}
			},
			error : function() {
				// Not enabled (503), not installed (404) or unreachable: stay
				// hidden; the next page refresh asks again.
				checking = false;
			}
		});
	}

	function unavailable() {
		disconnect();
		available = false;
		hide();
	}

	// ---- Receiving -------------------------------------------------------------

	function connect() {
		if (source !== null || polling) {
			return;
		}
		if (typeof window.EventSource !== 'undefined') {
			source = new EventSource(BASE + "stream?view=" + view);
			source.addEventListener("collection", function(event) {
				updateStatus(JSON.parse(event.data));
			}, false);
			source.addEventListener("histogram", function(event) {
				drawFrame(JSON.parse(event.data));
			}, false);
			source.onopen = function() {
				setConnection("LIVE", "green");
			};
			source.onerror = function() {
				if (source !== null && source.readyState === 2) {
					// CLOSED: the server refused (for example 503 when the
					// service stopped), so EventSource will not retry.
					unavailable();
				} else {
					setConnection("RECONNECTING", "#FFA500");
				}
			};
		} else {
			polling = true;
			setConnection("LIVE (polling)", "green");
			poll(++pollGeneration);
		}
	}

	function disconnect() {
		if (source !== null) {
			source.close();
			source = null;
		}
		polling = false;
		pollGeneration++;
	}

	/** Long-poll loop for browsers without EventSource. */
	function poll(generation) {
		if (generation !== pollGeneration) {
			return;
		}
		$.ajax({
			url : BASE + "latest",
			data : {"after" : lastFrameSequence, "wait" : 25000, "view" : view, "format" : "json",
				"timestamp" : new Date().getTime()},
			dataType : "json",
			timeout : 40000,
			success : function(frame, textStatus, xhr) {
				if (generation !== pollGeneration) {
					return;
				}
				if (xhr.status === 200 && frame) {
					drawFrame(frame);
				}
				refreshStatus();
				poll(generation);
			},
			error : function(xhr) {
				if (generation !== pollGeneration) {
					return;
				}
				if (xhr.status === 503 || xhr.status === 404) {
					unavailable();
					return;
				}
				setConnection("RECONNECTING", "#FFA500");
				setTimeout(function() {
					poll(generation);
				}, RETRY_MS);
			}
		});
	}

	function refreshStatus() {
		$.ajax({
			url : BASE + "status",
			data : {"format" : "json", "timestamp" : new Date().getTime()},
			dataType : "json",
			success : updateStatus
		});
	}

	/** One request for the newest frame, when not live. */
	function fetchLatest() {
		$.ajax({
			url : BASE + "latest",
			data : {"view" : view, "format" : "json", "timestamp" : new Date().getTime()},
			dataType : "json",
			success : function(frame, textStatus, xhr) {
				if (xhr.status === 200 && frame) {
					drawFrame(frame);
				}
			},
			error : function(xhr) {
				if (xhr.status === 503 || xhr.status === 404) {
					unavailable();
				}
			}
		});
		refreshStatus();
		setConnection("PAUSED", "#0000c4");
	}

	/**
	 * Fills the Plot selector from the instrument's histmemTypes: the plots it
	 * lists, in its order, with its names, and its default chosen.
	 */
	function setUpViewOptions() {
		if (typeof histmemTypes === 'undefined' || !histmemTypes) {
			return;
		}
		var views = [];
		var titles = {};
		var defaultView = null;
		for (var i = 0; i < histmemTypes.length; i++) {
			var v = HISTMEM_TYPE_VIEWS[histmemTypes[i].id];
			if (v === undefined || views.indexOf(v) >= 0) {
				continue;
			}
			views.push(v);
			titles[v] = histmemTypes[i].text || "Total " + VIEW_TITLES[v];
			if (histmemTypes[i].isDefault && defaultView === null) {
				defaultView = v;
			}
		}
		if (views.length === 0) {
			return;	// none of the Total plots listed: offer all six
		}
		var select = $("#kafkaHistogramView");
		select.empty();
		for (var j = 0; j < views.length; j++) {
			select.append($("<option></option>").val(views[j]).text(titles[views[j]]));
		}
		offered = views;
		view = defaultView !== null ? defaultView : views[0];
		select.val(view);
		try {
			select.selectmenu("refresh");
		} catch (e) {
		}
	}

	/** Shows another plot: asks the server for that view of the frame. */
	function setView(newView) {
		if (offered.indexOf(newView) < 0 || newView === view) {
			return;
		}
		view = newView;
		$("#kafkaHistogramView").val(view);
		try {
			$("#kafkaHistogramView").selectmenu("refresh");
		} catch (e) {
		}
		$("#kafkaHistogramReadout").text("");
		lastFrameSequence = 0;
		if (plot !== null) {
			plot = null;
			clearImage();
			$("#kafkaHistogramInfo").text("Loading the plot");
		}
		if (!available) {
			return;
		}
		if (live) {
			// The stream and the poll loop carry one view; start again with the
			// new one. The server sends the current frame first.
			disconnect();
			connect();
		} else {
			fetchLatest();
		}
	}

	/** Disables the plots this histogram does not have. */
	function updateViewOptions(views) {
		var changed = false;
		$("#kafkaHistogramView option").each(function() {
			var disabled = views.indexOf($(this).val()) < 0;
			if ($(this).prop("disabled") !== disabled) {
				$(this).prop("disabled", disabled);
				changed = true;
			}
		});
		if (changed) {
			try {
				$("#kafkaHistogramView").selectmenu("refresh");
			} catch (e) {
			}
		}
	}

	// ---- Collection state ----------------------------------------------------

	function formatTime(ms) {
		if (!ms) {
			return "--";
		}
		var d = new Date(ms);
		function two(n) {
			return n < 10 ? "0" + n : "" + n;
		}
		return two(d.getHours()) + ":" + two(d.getMinutes()) + ":" + two(d.getSeconds());
	}

	function updateStatus(status) {
		lastStatus = status;
		var text;
		var colour;
		if (!status.taskId) {
			text = "WAITING FOR A COLLECTION";
			colour = "black";
		} else if (status.collectionRunning) {
			text = "RUNNING since " + formatTime(status.startTimeMs);
			colour = "#FFA500";
		} else {
			text = "STOPPED at " + formatTime(status.endTimeMs) + " (started " + formatTime(status.startTimeMs) + ")";
			colour = "#00c400";
		}
		$("#kafkaHistogramCollection").text(text).css("color", colour);
		if (plot === null) {
			return;
		}
		if (status.taskId && status.taskId !== plot.taskId && status.collectionRunning && status.frameSequence === 0) {
			// A new collection is running: the old plot goes, its first frame
			// is to come. Otherwise (no collection, or the server restarted)
			// the plot of the last collection stays.
			plot = null;
			clearImage();
			return;
		}
		showInfo(shownMax);	// running or last collection
	}

	/** Whether the plot is of a collection that is still running. */
	function plotRunning() {
		if (plot === null || !plot.running) {
			return false;
		}
		// The final frame may not come (for example, the histogram service
		// gave up), so the status of the same collection decides too.
		return !(lastStatus !== null && (!lastStatus.taskId || (lastStatus.taskId === plot.taskId
				&& !lastStatus.collectionRunning)));
	}

	// ---- Axes ----------------------------------------------------------------

	/** Whether the axis has bins worth showing in its units, rather than bin numbers. */
	function hasUnits(axis) {
		return typeof axis.minimum === "number" && typeof axis.step === "number" && axis.units
				&& axis.units !== "posn";
	}

	/** The axis name with its units, for example "T (usec)". */
	function axisTitle(axis) {
		return axis.name + (hasUnits(axis) ? " (" + axis.units + ")" : "");
	}

	/** One bin of an axis, for the readout: "12", or "4000–6000 usec". */
	function binText(axis, bin) {
		if (!hasUnits(axis)) {
			return String(bin);
		}
		var low = axis.minimum + bin * axis.step;
		return formatNumber(low) + "–" + formatNumber(low + axis.step) + " " + axis.units;
	}

	/** Bins first to last of an axis, for the readout: "12", "12–14", or "4000–4060 usec". */
	function binsText(axis, first, last) {
		if (first === last) {
			return binText(axis, first);
		}
		if (!hasUnits(axis)) {
			return first + "–" + last;
		}
		return formatNumber(axis.minimum + first * axis.step) + "–" + formatNumber(axis.minimum + (last + 1) * axis.step)
				+ " " + axis.units;
	}

	/** A tick label at the centre of a bin: its number, or its value in units. */
	function tickText(axis, bin) {
		if (!hasUnits(axis)) {
			return String(bin);
		}
		return formatTick(axis.minimum + (bin + 0.5) * axis.step);
	}

	function summedText() {
		return plot.summedOver.length > 0 ? "summed over " + plot.summedOver.join(", ") : "";
	}

	// ---- Drawing -------------------------------------------------------------

	function drawFrame(frame) {
		if (frame.view !== view) {
			return;	// from the stream of a plot no longer shown
		}
		if (plot !== null && frame.taskId === plot.taskId && frame.sequence <= lastFrameSequence) {
			return;	// already drawn (sequences start again when the server restarts)
		}
		var views = frame.views || [];
		updateViewOptions(views);
		if (frame.data === null || frame.data === undefined) {
			if (views.indexOf(view) < 0) {
				// Not collected: show an offered plot that is.
				var next = null;
				for (var i = 0; i < offered.length && next === null; i++) {
					if (views.indexOf(offered[i]) >= 0) {
						next = offered[i];
					}
				}
				if (next !== null) {
					setView(next);
				} else {
					$("#kafkaHistogramInfo").text("None of the plots offered is collected");
				}
			} else if (plot === null) {
				// Collected, but its histogram has not arrived yet.
				$("#kafkaHistogramInfo").text("Waiting for the " + VIEW_TITLES[view] + " histogram");
			}
			return;
		}
		lastFrameSequence = frame.sequence;
		var values = frame.data;
		var max = -Infinity;
		var min = Infinity;
		for (var i = 0; i < values.length; i++) {
			var value = values[i];
			if (value === null || value === undefined) {
				values[i] = NaN;
			} else {
				if (value > max) {
					max = value;
				}
				if (value < min) {
					min = value;
				}
			}
		}
		plot = {
			view : frame.view,
			horizontal : frame.horizontal,
			vertical : frame.vertical || null,	// null for a 1D plot
			summedOver : frame.summedOver || [],
			values : values,	// 2D: values[v * horizontal.size + h], v = 0 at the bottom
			min : min === Infinity ? 0 : min,
			max : max === -Infinity ? 0 : max,
			total : frame.total,
			unit : frame.unit || "counts",
			timeMs : frame.timeMs,
			running : frame.collectionRunning,
			taskId : frame.taskId,
			windowSeconds : frame.windowSeconds || 0	// 0: accumulated since the start
		};
		render();
	}

	/** Where value lies between lo and hi, 0 to 1, on the linear or log scale. */
	function fraction(value, lo, hi) {
		if (logScale) {
			value = Math.log(1 + Math.max(0, value - lo));
			hi = Math.log(1 + Math.max(0, hi - lo));
			lo = 0;
		}
		if (hi <= lo) {
			return 0;
		}
		return Math.max(0, Math.min(1, (value - lo) / (hi - lo)));
	}

	/** The value at fraction t between lo and hi: the inverse of fraction(). */
	function valueAt(t, lo, hi) {
		if (logScale) {
			return lo + Math.exp(t * Math.log(1 + Math.max(0, hi - lo))) - 1;
		}
		return lo + t * (hi - lo);
	}

	function scaled(value, lo, hi) {
		return Math.round(fraction(value, lo, hi) * 255);
	}

	function render() {
		if (plot === null) {
			return;
		}
		var canvas = document.getElementById("kafkaHistogramCanvas");
		if (!canvas || !canvas.getContext) {
			$("#kafkaHistogramInfo").text("This browser cannot draw the histogram");
			return;
		}
		if (plot.vertical === null) {
			renderProfile(canvas);
		} else {
			renderImage(canvas);
		}
	}

	/**
	 * The size of the 2D image on the page, in CSS pixels. Square bins when the
	 * image is not too elongated (X-Y); otherwise (X-T, Y-T) it fills the width,
	 * and is about square when the long axis runs up.
	 */
	function imageSize(nx, ny) {
		var width = $("#kafkaHistogramImage").width() || 300;
		var maxHeight = Math.max(150, $(window).height() * MAX_HEIGHT_FRACTION);
		var aspect = nx / ny;
		if (aspect <= MAX_SQUARE_ASPECT && aspect >= 1 / MAX_SQUARE_ASPECT) {
			var scale = Math.min(width / nx, maxHeight / ny);
			if (scale >= 1) {
				scale = Math.floor(scale);	// whole pixels look sharper
			}
			return {width : Math.round(nx * scale), height : Math.round(ny * scale)};
		}
		var cssWidth = Math.floor(width - 2);
		var cssHeight = ny > nx ? Math.min(maxHeight, cssWidth) : Math.min(maxHeight, Math.max(150, cssWidth * 0.5));
		return {width : cssWidth, height : Math.round(cssHeight)};
	}

	/**
	 * Adds bins together in groups of gx across and gy up, so that the image
	 * has no more cells than the screen has pixels. Scaled down instead, it
	 * would leave bins out.
	 */
	function groupBins(gx, gy) {
		var nx = plot.horizontal.size;
		var ny = plot.vertical.size;
		var cx = Math.ceil(nx / gx);
		var cy = Math.ceil(ny / gy);
		var values = new Array(cx * cy);
		var i;
		for (i = 0; i < values.length; i++) {
			values[i] = NaN;
		}
		for (var py = 0; py < ny; py++) {
			for (var px = 0; px < nx; px++) {
				var value = plot.values[py * nx + px];
				if (!isNaN(value)) {
					var k = Math.floor(py / gy) * cx + Math.floor(px / gx);
					values[k] = isNaN(values[k]) ? value : values[k] + value;
				}
			}
		}
		var min = Infinity;
		var max = -Infinity;
		for (i = 0; i < values.length; i++) {
			if (!isNaN(values[i])) {
				min = Math.min(min, values[i]);
				max = Math.max(max, values[i]);
			}
		}
		return {nx : cx, ny : cy, gx : gx, gy : gy, values : values, min : min === Infinity ? 0 : min,
			max : max === -Infinity ? 0 : max};
	}

	/** Total X-Y, X-T or Y-T: a 2D image, the horizontal axis across, the vertical up. */
	function renderImage(canvas) {
		profile = null;
		var nx = plot.horizontal.size;
		var ny = plot.vertical.size;
		var size = imageSize(nx, ny);
		var ratio = window.devicePixelRatio || 1;
		grid = groupBins(Math.max(1, Math.ceil(nx / (size.width * ratio))),
				Math.max(1, Math.ceil(ny / (size.height * ratio))));
		canvas.width = grid.nx;
		canvas.height = grid.ny;
		var context = canvas.getContext("2d");
		var pixels = context.createImageData(grid.nx, grid.ny);
		var p = pixels.data;
		for (var row = 0; row < grid.ny; row++) {
			var py = grid.ny - 1 - row;	// up
			for (var px = 0; px < grid.nx; px++) {
				var value = grid.values[py * grid.nx + px];
				var o = (row * grid.nx + px) * 4;
				if (isNaN(value)) {
					p[o] = p[o + 1] = p[o + 2] = 200;
				} else if (value === 0) {
					p[o] = p[o + 1] = p[o + 2] = 0;	// no counts: black, as in the server's images
				} else {
					var c = lut[scaled(value, grid.min, grid.max)];
					p[o] = c[0];
					p[o + 1] = c[1];
					p[o + 2] = c[2];
				}
				p[o + 3] = 255;
			}
		}
		context.putImageData(pixels, 0, 0);
		$("#kafkaHistogramCanvas").css({"width" : size.width + "px", "height" : size.height + "px"});
		renderColourBar();
		var notes = [];
		var summed = summedText();
		if (summed) {
			notes.push(summed);
		}
		if (grid.gx > 1) {
			notes.push(plot.horizontal.name + " in groups of " + grid.gx + " bins");
		}
		if (grid.gy > 1) {
			notes.push(plot.vertical.name + " in groups of " + grid.gy + " bins");
		}
		$("#kafkaHistogramTitle").text("Total " + VIEW_TITLES[plot.view] + ": " + axisTitle(plot.horizontal) + " →, "
				+ axisTitle(plot.vertical) + " ↑  (" + nx + " × " + ny + (notes.length > 0 ? ", " + notes.join(", ") : "")
				+ ")");
		showInfo(grid.max);
		$("#kafkaHistogramCanvas").show();
		$("#kafkaHistogramScale").show();
	}

	function showInfo(max) {
		shownMax = max;
		$("#kafkaHistogramInfo").text((plotRunning() ? "Frame " : "Last collection, frame ") + formatTime(plot.timeMs)
				+ ", " + coverageText() + ", total " + formatNumber(plot.total) + " " + plot.unit + ", max "
				+ formatNumber(max));
	}

	/**
	 * What the plot counts: "since the start" (X-Y), or for a window histogram
	 * (X-T, Y-T) "last 2 s", with the number of T frames when the T axis is
	 * known: "last 2 s (100 frames of 20 ms)".
	 */
	function coverageText() {
		if (!plot.windowSeconds) {
			return "since the start";
		}
		var text = "last " + formatNumber(plot.windowSeconds) + " s";
		var axes = [plot.horizontal, plot.vertical];
		for (var i = 0; i < axes.length; i++) {
			var axis = axes[i];
			if (axis && axis.name === "T" && typeof axis.step === "number" && axis.units === "usec") {
				var frameUs = axis.size * axis.step;
				text += " (" + formatNumber(Math.round(plot.windowSeconds * 1e6 / frameUs)) + " frames of "
						+ formatNumber(frameUs / 1000) + " ms)";
				break;
			}
		}
		return text;
	}

	/** Total X, Y or T: a step plot of the summed counts per bin. */
	function renderProfile(canvas) {
		var axis = plot.horizontal;
		var sums = plot.values;
		var n = sums.length;
		var lo = 0;
		var hi = 0;
		for (var i = 0; i < n; i++) {
			if (!isNaN(sums[i])) {
				lo = Math.min(lo, sums[i]);
				hi = Math.max(hi, sums[i]);
			}
		}
		var max = hi;
		if (hi <= lo) {
			hi = lo + 1;	// all zero: still draw the axes
		}
		var summed = summedText();

		// Drawn at the screen's resolution, so lines and text stay sharp.
		var cssWidth = Math.max(200, Math.floor(($("#kafkaHistogramImage").width() || 300) - 2));
		var cssHeight = Math.round(Math.min(cssWidth * 0.6, Math.max(150, $(window).height() * MAX_HEIGHT_FRACTION)));
		var ratio = window.devicePixelRatio || 1;
		canvas.width = Math.round(cssWidth * ratio);
		canvas.height = Math.round(cssHeight * ratio);
		$(canvas).css({"width" : cssWidth + "px", "height" : cssHeight + "px"});
		var context = canvas.getContext("2d");
		context.setTransform(ratio, 0, 0, ratio, 0, 0);
		context.fillStyle = "#ffffff";
		context.fillRect(0, 0, cssWidth, cssHeight);

		var m = PLOT_MARGIN;
		var width = cssWidth - m.left - m.right;
		var height = cssHeight - m.top - m.bottom;
		function xOf(bin) {
			return m.left + width * bin / n;
		}
		function yOf(value) {
			return m.top + height * (1 - fraction(value, lo, hi));
		}

		// Grid and count axis
		context.font = "11px sans-serif";
		context.lineWidth = 1;
		context.textAlign = "right";
		context.textBaseline = "middle";
		var t;
		for (t = 0; t <= 4; t++) {
			var y = Math.round(m.top + height * (1 - t / 4)) + 0.5;
			context.strokeStyle = t === 0 ? "#021a40" : "#e0e0e0";
			context.beginPath();
			context.moveTo(m.left, y);
			context.lineTo(m.left + width, y);
			context.stroke();
			context.fillStyle = "#333333";
			context.fillText(formatTick(valueAt(t / 4, lo, hi)), m.left - 4, y);
		}

		// Bin axis: ticks at the centre of the first, quarter, ..., last bins
		context.textAlign = "center";
		context.textBaseline = "top";
		var ticks = [0, Math.round((n - 1) / 4), Math.round((n - 1) / 2), Math.round(3 * (n - 1) / 4), n - 1];
		var last = -1;
		for (t = 0; t < ticks.length; t++) {
			if (ticks[t] === last) {
				continue;
			}
			last = ticks[t];
			var x = Math.round(xOf(ticks[t] + 0.5)) + 0.5;
			context.strokeStyle = "#021a40";
			context.beginPath();
			context.moveTo(x, m.top + height);
			context.lineTo(x, m.top + height + 4);
			context.stroke();
			context.fillText(tickText(axis, ticks[t]), x, m.top + height + 6);
		}
		context.fillText(axisTitle(axis) + (summed ? " (" + summed + ")" : ""), m.left + width / 2, m.top + height + 19);
		context.strokeStyle = "#021a40";
		context.beginPath();
		context.moveTo(m.left + 0.5, m.top);
		context.lineTo(m.left + 0.5, m.top + height);
		context.stroke();

		// Step plot
		var base = yOf(lo);
		context.beginPath();
		context.moveTo(xOf(0), base);
		for (var b = 0; b < n; b++) {
			var level = yOf(isNaN(sums[b]) ? lo : sums[b]);
			context.lineTo(xOf(b), level);
			context.lineTo(xOf(b + 1), level);
		}
		context.lineTo(xOf(n), base);
		context.closePath();
		var fill = lut[110];
		var line = lut[50];
		context.fillStyle = "rgba(" + fill[0] + "," + fill[1] + "," + fill[2] + ",0.35)";
		context.fill();
		context.strokeStyle = "rgb(" + line[0] + "," + line[1] + "," + line[2] + ")";
		context.lineWidth = 1.5;
		context.stroke();

		profile = {axis : axis, sums : sums, n : n, left : m.left, width : width};
		grid = null;
		$("#kafkaHistogramTitle").text("Total " + VIEW_TITLES[plot.view] + ": " + plot.unit + " per " + axis.name + " bin"
				+ (summed ? ", " + summed : "") + " (" + n + " bins" + (logScale ? ", log scale" : "") + ")");
		showInfo(max);
		$("#kafkaHistogramCanvas").show();
		$("#kafkaHistogramScale").hide();
	}

	function formatTick(value) {
		if (Math.abs(value) >= 10) {
			return formatNumber(Math.round(value));
		}
		return formatNumber(Math.round(value * 10) / 10);
	}

	/** Window resized: redrawn to fit (an image's bin groups depend on its size). */
	function onResize() {
		render();
	}

	function renderColourBar() {
		var bar = document.getElementById("kafkaHistogramBar");
		if (!bar || !bar.getContext) {
			return;
		}
		bar.width = 256;
		bar.height = 1;
		var context = bar.getContext("2d");
		var pixels = context.createImageData(256, 1);
		for (var i = 0; i < 256; i++) {
			pixels.data[i * 4] = lut[i][0];
			pixels.data[i * 4 + 1] = lut[i][1];
			pixels.data[i * 4 + 2] = lut[i][2];
			pixels.data[i * 4 + 3] = 255;
		}
		context.putImageData(pixels, 0, 0);
		$("#kafkaHistogramMin").text(formatNumber(grid.min));
		$("#kafkaHistogramMax").text(formatNumber(grid.max) + (logScale ? " (log)" : ""));
	}

	function clearImage() {
		$("#kafkaHistogramCanvas").hide();
		$("#kafkaHistogramScale").hide();
		$("#kafkaHistogramTitle").text("");
		$("#kafkaHistogramInfo").text("Waiting for the first histogram");
		$("#kafkaHistogramReadout").text("");
	}

	function formatNumber(value) {
		if (!isFinite(value)) {
			return "--";
		}
		if (Math.abs(value) >= 1e6) {
			return value.toExponential(3);
		}
		return Math.round(value) === value ? String(value) : value.toFixed(2);
	}

	/** Shows the counts under the pointer or finger. */
	function readout(event) {
		if (plot === null) {
			return;
		}
		var canvas = $("#kafkaHistogramCanvas");
		var offset = canvas.offset();
		var point = event;
		var original = event.originalEvent;
		if (original && original.touches && original.touches.length > 0) {
			point = original.touches[0];
		}
		var summed = summedText();
		if (profile !== null) {
			var bin = Math.floor((point.pageX - offset.left - profile.left) / profile.width * profile.n);
			if (bin < 0 || bin >= profile.n) {
				return;
			}
			$("#kafkaHistogramReadout").text(profile.axis.name + " = " + binText(profile.axis, bin) + ": "
					+ formatNumber(profile.sums[bin]) + " " + plot.unit + (summed ? " (" + summed + ")" : ""));
			return;
		}
		if (grid === null) {
			return;
		}
		var x = (point.pageX - offset.left) / canvas.width();
		var y = (point.pageY - offset.top) / canvas.height();
		if (x < 0 || x >= 1 || y < 0 || y >= 1) {
			return;
		}
		var cx = Math.floor(x * grid.nx);
		var cy = grid.ny - 1 - Math.floor(y * grid.ny);
		var value = grid.values[cy * grid.nx + cx];
		var lastX = Math.min(plot.horizontal.size, (cx + 1) * grid.gx) - 1;
		var lastY = Math.min(plot.vertical.size, (cy + 1) * grid.gy) - 1;
		$("#kafkaHistogramReadout").text(plot.horizontal.name + " = " + binsText(plot.horizontal, cx * grid.gx, lastX)
				+ ", " + plot.vertical.name + " = " + binsText(plot.vertical, cy * grid.gy, lastY) + ": "
				+ (isNaN(value) ? "--" : formatNumber(value)) + " " + plot.unit + (summed ? " (" + summed + ")" : ""));
	}

	// ---- Public ----------------------------------------------------------------

	return {
		/** Called once the page is ready. */
		init : function() {
			hide();
			if (!enabled) {
				return;
			}
			clearImage();
			if ($("#kafkaHistogramLog").length > 0) {
				// Follow the selector, which a browser may restore on reload.
				logScale = $("#kafkaHistogramLog").val() !== "linear";
			}
			$("#kafkaHistogramLog").on("change", function() {
				logScale = $(this).val() === "log";
				render();
			});
			$("#kafkaHistogramView").on("change", function() {
				setView($(this).val());
			});
			if (VIEWS.indexOf($("#kafkaHistogramView").val()) >= 0) {
				// A browser may restore the last choice on reload.
				view = $("#kafkaHistogramView").val();
			}
			setUpViewOptions();	// the instrument's list and default, if it has one
			$("#kafkaHistogramCanvas").on("mousemove touchstart touchmove", readout);
			$(window).on("resize orientationchange", onResize);
			checkAvailable();
		},

		/** Called on every page refresh (timer or Refresh Now). */
		onRefresh : function() {
			if (!enabled) {
				return;
			}
			if (!available) {
				checkAvailable();
			} else if (!live) {
				fetchLatest();
			}
		},

		/** Auto Refresh switched: follow frames live, or pause. */
		setLive : function(isLive) {
			live = isLive;
			if (!available) {
				return;
			}
			if (live) {
				connect();
			} else {
				disconnect();
				setConnection("PAUSED", "#0000c4");
			}
		}
	};
})();
