/**
 * Live histogram of the current collection, from the control server's Kafka
 * collection histogram service (org.gumtree.control.server, HistogramRestlet at
 * histogram/rest). Drawn as a 2D image in the #kafkaHistogramList section of
 * mobile.html, which stays hidden unless the service answers.
 *
 * Frames arrive through Server-Sent Events (histogram/rest/stream), or by
 * long-polling histogram/rest/latest in browsers without EventSource. mobile.js
 * calls onRefresh() on every page refresh, and setLive() when Auto Refresh is
 * switched: while it is off, the stream is closed and "Refresh Now" fetches the
 * latest frame once.
 *
 * The Plot selector (#kafkaHistogramView) shows the frame as Total X-Y (the 2D
 * image), Total X (counts summed over Y, per X bin) or Total Y (summed over X,
 * per Y bin).
 *
 * An instrument can turn the section off with, in its instrumentsetup.js:
 *     var kafkaHistogramEnabled = false;
 */
var kafkaHistogram = (function() {

	var BASE = "histogram/rest/";
	var MAX_HEIGHT_FRACTION = 0.7;	// of the window height
	var RETRY_MS = 5000;

	var enabled = typeof kafkaHistogramEnabled === 'undefined' || kafkaHistogramEnabled;
	var available = false;		// the service answered
	var checking = false;
	var live = true;			// follow new frames as they come
	var source = null;			// EventSource
	var polling = false;		// long-poll loop running (no EventSource)
	var pollGeneration = 0;		// ends an old poll loop
	var logScale = false;
	var view = "xy";			// "xy" image, "x" Total X, "y" Total Y
	var profile = null;			// the 1D plot drawn, for the readout
	var PLOT_MARGIN = {left : 56, right : 10, top : 10, bottom : 32};	// CSS pixels
	var lastFrameSequence = 0;
	var lastStatus = null;
	var image = null;			// what is drawn: {nx, ny, values, xName, yName, ...}
	var lut = buildColourMap();

	// ---- Colour map: viridis, from 9 stops ----------------------------------

	function buildColourMap() {
		var stops = [[68, 1, 84], [71, 44, 122], [59, 81, 139], [44, 113, 142], [33, 144, 141],
				[39, 173, 129], [92, 200, 99], [170, 220, 50], [253, 231, 37]];
		var table = new Array(256);
		for (var i = 0; i < 256; i++) {
			var t = i / 255 * (stops.length - 1);
			var k = Math.min(Math.floor(t), stops.length - 2);
			var f = t - k;
			table[i] = [Math.round(stops[k][0] + (stops[k + 1][0] - stops[k][0]) * f),
					Math.round(stops[k][1] + (stops[k + 1][1] - stops[k][1]) * f),
					Math.round(stops[k][2] + (stops[k + 1][2] - stops[k][2]) * f)];
		}
		return table;
	}

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
			source = new EventSource(BASE + "stream");
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
			data : {"after" : lastFrameSequence, "wait" : 25000, "format" : "json",
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
			data : {"format" : "json", "timestamp" : new Date().getTime()},
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
		if (status.frameSequence === 0 && image !== null) {
			// A new collection: the old image goes, its first frame is to come.
			image = null;
			clearImage();
		}
	}

	// ---- Drawing -------------------------------------------------------------

	/** The variable to draw: "signal" if there is one, else the first 2D numeric one. */
	function pickVariable(variables) {
		var i;
		for (i = 0; i < variables.length; i++) {
			if (variables[i].name === "signal" && variables[i].shape.length === 2) {
				return variables[i];
			}
		}
		for (i = 0; i < variables.length; i++) {
			var v = variables[i];
			if (v.shape.length === 2 && v.dataType !== "c_string" && v.data !== null && typeof v.data === "object") {
				return v;
			}
		}
		return null;
	}

	function drawFrame(frame) {
		if (frame.sequence <= lastFrameSequence && image !== null) {
			return;	// already drawn
		}
		lastFrameSequence = frame.sequence;
		var variable = pickVariable(frame.variables);
		if (variable === null) {
			$("#kafkaHistogramInfo").text("No 2D data in this histogram");
			return;
		}
		var rows = variable.shape[0];
		var cols = variable.shape[1];
		var axes = variable.axes || [];
		// The image draws its first dimension down the screen. Data stored as
		// [X][Y] is swapped so that X runs across and Y up, as in Gumtree.
		var swap = axes.length === 2 && axes[0].toLowerCase() === "x" && axes[1].toLowerCase() === "y";
		var nx = swap ? rows : cols;
		var ny = swap ? cols : rows;
		var data = variable.data;
		var values = new Array(nx * ny);	// values[py * nx + px], py = 0 at the bottom
		var total = 0;
		var max = -Infinity;
		var min = Infinity;
		for (var py = 0; py < ny; py++) {
			for (var px = 0; px < nx; px++) {
				var value = swap ? data[px * cols + py] : data[py * cols + px];
				if (value === null || value === undefined) {
					value = NaN;
				} else {
					total += value;
					if (value > max) {
						max = value;
					}
					if (value < min) {
						min = value;
					}
				}
				values[py * nx + px] = value;
			}
		}
		image = {
			nx : nx,
			ny : ny,
			values : values,
			min : min === Infinity ? 0 : min,
			max : max === -Infinity ? 0 : max,
			total : total,
			xName : axes.length === 2 ? (swap ? axes[0] : axes[1]) : "x",
			yName : axes.length === 2 ? (swap ? axes[1] : axes[0]) : "y",
			unit : variable.unit || "counts",
			timeMs : frame.timeMs,
			running : frame.collectionRunning
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
		if (image === null) {
			return;
		}
		var canvas = document.getElementById("kafkaHistogramCanvas");
		if (!canvas || !canvas.getContext) {
			$("#kafkaHistogramInfo").text("This browser cannot draw the histogram");
			return;
		}
		if (view === "x" || view === "y") {
			renderProfile(canvas, view);
		} else {
			renderImage(canvas);
		}
	}

	/** Total X-Y: the 2D image. */
	function renderImage(canvas) {
		profile = null;
		canvas.width = image.nx;
		canvas.height = image.ny;
		var context = canvas.getContext("2d");
		var pixels = context.createImageData(image.nx, image.ny);
		var p = pixels.data;
		for (var row = 0; row < image.ny; row++) {
			var py = image.ny - 1 - row;	// y up
			for (var px = 0; px < image.nx; px++) {
				var value = image.values[py * image.nx + px];
				var o = (row * image.nx + px) * 4;
				if (isNaN(value)) {
					p[o] = p[o + 1] = p[o + 2] = 200;
				} else {
					var c = lut[scaled(value, image.min, image.max)];
					p[o] = c[0];
					p[o + 1] = c[1];
					p[o + 2] = c[2];
				}
				p[o + 3] = 255;
			}
		}
		context.putImageData(pixels, 0, 0);
		resize();
		renderColourBar();
		$("#kafkaHistogramTitle").text("Total X-Y: " + image.xName + " →, " + image.yName + " ↑  (" + image.nx
				+ " × " + image.ny + ")");
		showInfo(image.max);
		$("#kafkaHistogramCanvas").show();
		$("#kafkaHistogramScale").show();
	}

	function showInfo(max) {
		$("#kafkaHistogramInfo").text("Frame " + formatTime(image.timeMs) + (image.running ? "" : " (final)")
				+ ", total " + formatNumber(image.total) + " " + image.unit + ", max " + formatNumber(max));
	}

	/** Sums the image along one axis: "x" gives one total per X bin, summed over Y. */
	function project(axis) {
		var n = axis === "x" ? image.nx : image.ny;
		var sums = new Array(n);
		var i;
		for (i = 0; i < n; i++) {
			sums[i] = 0;
		}
		for (var py = 0; py < image.ny; py++) {
			for (var px = 0; px < image.nx; px++) {
				var value = image.values[py * image.nx + px];
				if (!isNaN(value)) {
					sums[axis === "x" ? px : py] += value;
				}
			}
		}
		return sums;
	}

	/** Total X or Total Y: a step plot of the summed counts per bin. */
	function renderProfile(canvas, axis) {
		var sums = project(axis);
		var n = sums.length;
		var lo = 0;
		var hi = 0;
		for (var i = 0; i < n; i++) {
			lo = Math.min(lo, sums[i]);
			hi = Math.max(hi, sums[i]);
		}
		var max = hi;
		if (hi <= lo) {
			hi = lo + 1;	// all zero: still draw the axes
		}
		var axisName = axis === "x" ? image.xName : image.yName;
		var otherName = axis === "x" ? image.yName : image.xName;

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
			context.fillText(String(ticks[t]), x, m.top + height + 6);
		}
		context.fillText(axisName + " (summed over " + otherName + ")", m.left + width / 2, m.top + height + 19);
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
			var level = yOf(sums[b]);
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

		profile = {axis : axis, name : axisName, otherName : otherName, sums : sums, n : n,
				left : m.left, width : width};
		$("#kafkaHistogramTitle").text("Total " + axisName + ": " + image.unit + " per " + axisName + " bin, summed over "
				+ otherName + " (" + n + " bins" + (logScale ? ", log scale" : "") + ")");
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

	/** Window resized: the image only rescales, a 1D plot is redrawn to fit. */
	function onResize() {
		if (view === "xy") {
			resize();
		} else {
			render();
		}
	}

	/** Scales the canvas to the page, keeping square pixels. */
	function resize() {
		if (image === null || view !== "xy") {
			return;
		}
		var width = $("#kafkaHistogramImage").width() || 300;
		var maxHeight = Math.max(150, $(window).height() * MAX_HEIGHT_FRACTION);
		var scale = Math.min(width / image.nx, maxHeight / image.ny);
		if (scale >= 1) {
			scale = Math.floor(scale);	// whole pixels look sharper
		}
		$("#kafkaHistogramCanvas").css({
			"width" : Math.round(image.nx * scale) + "px",
			"height" : Math.round(image.ny * scale) + "px"
		});
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
		$("#kafkaHistogramMin").text(formatNumber(image.min));
		$("#kafkaHistogramMax").text(formatNumber(image.max) + (logScale ? " (log)" : ""));
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
		if (image === null) {
			return;
		}
		var canvas = $("#kafkaHistogramCanvas");
		var offset = canvas.offset();
		var point = event;
		var original = event.originalEvent;
		if (original && original.touches && original.touches.length > 0) {
			point = original.touches[0];
		}
		if (profile !== null) {
			var bin = Math.floor((point.pageX - offset.left - profile.left) / profile.width * profile.n);
			if (bin < 0 || bin >= profile.n) {
				return;
			}
			$("#kafkaHistogramReadout").text(profile.name + " = " + bin + ": " + formatNumber(profile.sums[bin]) + " "
					+ image.unit + " (summed over " + profile.otherName + ")");
			return;
		}
		var x = (point.pageX - offset.left) / canvas.width();
		var y = (point.pageY - offset.top) / canvas.height();
		if (x < 0 || x >= 1 || y < 0 || y >= 1) {
			return;
		}
		var px = Math.floor(x * image.nx);
		var py = image.ny - 1 - Math.floor(y * image.ny);
		var value = image.values[py * image.nx + px];
		$("#kafkaHistogramReadout").text(image.xName + " = " + px + ", " + image.yName + " = " + py + ": "
				+ (isNaN(value) ? "--" : formatNumber(value)) + " " + image.unit);
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
			$("#kafkaHistogramLog").on("change", function() {
				logScale = $(this).val() === "log";
				render();
			});
			$("#kafkaHistogramView").on("change", function() {
				var value = $(this).val();
				view = value === "x" || value === "y" ? value : "xy";
				$("#kafkaHistogramReadout").text("");
				render();
			});
			if ($("#kafkaHistogramView").length > 0) {
				// A browser may restore the last choice on reload.
				view = $("#kafkaHistogramView").val() === "x" || $("#kafkaHistogramView").val() === "y"
						? $("#kafkaHistogramView").val() : "xy";
			}
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
