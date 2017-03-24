// File start: /var/deployments/www.raptor-editor.com.3/raptor-gold/raptor-dependencies/plupload.html4.js
(function(d, a, b, c) {
	function e(f) {
		return a.getElementById(f)
	}
	b.runtimes.Html4 = b
			.addRuntime(
					"html4",
					{
						getFeatures : function() {
							return {
								multipart : true,
								triggerDialog : (b.ua.gecko && d.FormData || b.ua.webkit)
							}
						},
						init : function(f, g) {
							f
									.bind(
											"Init",
											function(p) {
												var j = a.body, n, h = "javascript", k, x, q, z = [], r = /MSIE/
														.test(navigator.userAgent), t = [], m = p.settings.filters, o, l, s, w;
												no_type_restriction: for (o = 0; o < m.length; o++) {
													l = m[o].extensions
															.split(/,/);
													for (w = 0; w < l.length; w++) {
														if (l[w] === "*") {
															t = [];
															break no_type_restriction
														}
														s = b.mimeTypes[l[w]];
														if (s
																&& b.inArray(s,
																		t) === -1) {
															t.push(s)
														}
													}
												}
												t = t.join(",");
												function v() {
													var B, y, i, A;
													q = b.guid();
													z.push(q);
													B = a.createElement("form");
													B.setAttribute("id",
															"form_" + q);
													B.setAttribute("method",
															"post");
													B
															.setAttribute(
																	"enctype",
																	"multipart/form-data");
													B
															.setAttribute(
																	"encoding",
																	"multipart/form-data");
													B.setAttribute("target",
															p.id + "_iframe");
													B.style.position = "absolute";
													y = a
															.createElement("input");
													y.setAttribute("id",
															"input_" + q);
													y.setAttribute("type",
															"file");
													y.setAttribute("accept", t);
													y.setAttribute("size", 1);
													A = e(p.settings.browse_button);
													if (p.features.triggerDialog
															&& A) {
														b
																.addEvent(
																		e(p.settings.browse_button),
																		"click",
																		function(
																				C) {
																			if (!y.disabled) {
																				y
																						.click()
																			}
																			C
																					.preventDefault()
																		}, p.id)
													}
													b.extend(y.style, {
														width : "100%",
														height : "100%",
														opacity : 0,
														fontSize : "99px",
														cursor : "pointer"
													});
													b.extend(B.style, {
														overflow : "hidden"
													});
													i = p.settings.shim_bgcolor;
													if (i) {
														B.style.background = i
													}
													if (r) {
														b
																.extend(
																		y.style,
																		{
																			filter : "alpha(opacity=0)"
																		})
													}
													b
															.addEvent(
																	y,
																	"change",
																	function(F) {
																		var D = F.target, C, E = [], G;
																		if (D.value) {
																			e("form_"
																					+ q).style.top = -1048575
																					+ "px";
																			C = D.value
																					.replace(
																							/\\/g,
																							"/");
																			C = C
																					.substring(
																							C.length,
																							C
																									.lastIndexOf("/") + 1);
																			E
																					.push(new b.File(
																							q,
																							C));
																			if (!p.features.triggerDialog) {
																				b
																						.removeAllEvents(
																								B,
																								p.id)
																			} else {
																				b
																						.removeEvent(
																								A,
																								"click",
																								p.id)
																			}
																			b
																					.removeEvent(
																							y,
																							"change",
																							p.id);
																			v();
																			if (E.length) {
																				f
																						.trigger(
																								"FilesAdded",
																								E)
																			}
																		}
																	}, p.id);
													B.appendChild(y);
													j.appendChild(B);
													p.refresh()
												}
												function u() {
													var i = a
															.createElement("div");
													i.innerHTML = '<iframe id="'
															+ p.id
															+ '_iframe" name="'
															+ p.id
															+ '_iframe" src="'
															+ h
															+ ':&quot;&quot;" style="display:none"></iframe>';
													n = i.firstChild;
													j.appendChild(n);
													b
															.addEvent(
																	n,
																	"load",
																	function(C) {
																		var D = C.target, B, y;
																		if (!k) {
																			return
																		}
																		try {
																			B = D.contentWindow.document
																					|| D.contentDocument
																					|| d.frames[D.id].document
																		} catch (A) {
																			p
																					.trigger(
																							"Error",
																							{
																								code : b.SECURITY_ERROR,
																								message : b
																										.translate("Security error."),
																								file : k
																							});
																			return
																		}
																		y = B.documentElement.innerText
																				|| B.documentElement.textContent;
																		if (y) {
																			k.status = b.DONE;
																			k.loaded = 1025;
																			k.percent = 100;
																			p
																					.trigger(
																							"UploadProgress",
																							k);
																			p
																					.trigger(
																							"FileUploaded",
																							k,
																							{
																								response : y
																							})
																		}
																	}, p.id)
												}
												if (p.settings.container) {
													j = e(p.settings.container);
													if (b.getStyle(j,
															"position") === "static") {
														j.style.position = "relative"
													}
												}
												p
														.bind(
																"UploadFile",
																function(i, A) {
																	var B, y;
																	if (A.status == b.DONE
																			|| A.status == b.FAILED
																			|| i.state == b.STOPPED) {
																		return
																	}
																	B = e("form_"
																			+ A.id);
																	y = e("input_"
																			+ A.id);
																	y
																			.setAttribute(
																					"name",
																					i.settings.file_data_name);
																	B
																			.setAttribute(
																					"action",
																					i.settings.url);
																	b
																			.each(
																					b
																							.extend(
																									{
																										name : A.target_name
																												|| A.name
																									},
																									i.settings.multipart_params),
																					function(
																							E,
																							C) {
																						var D = a
																								.createElement("input");
																						b
																								.extend(
																										D,
																										{
																											type : "hidden",
																											name : C,
																											value : E
																										});
																						B
																								.insertBefore(
																										D,
																										B.firstChild)
																					});
																	k = A;
																	e("form_"
																			+ q).style.top = -1048575
																			+ "px";
																	B.submit()
																});
												p.bind("FileUploaded",
														function(i) {
															i.refresh()
														});
												p
														.bind(
																"StateChanged",
																function(i) {
																	if (i.state == b.STARTED) {
																		u()
																	} else {
																		if (i.state == b.STOPPED) {
																			d
																					.setTimeout(
																							function() {
																								b
																										.removeEvent(
																												n,
																												"load",
																												i.id);
																								if (n.parentNode) {
																									n.parentNode
																											.removeChild(n)
																								}
																							},
																							0)
																		}
																	}
																	b
																			.each(
																					i.files,
																					function(
																							A,
																							y) {
																						if (A.status === b.DONE
																								|| A.status === b.FAILED) {
																							var B = e("form_"
																									+ A.id);
																							if (B) {
																								B.parentNode
																										.removeChild(B)
																							}
																						}
																					})
																});
												p
														.bind(
																"Refresh",
																function(y) {
																	var F, A, B, C, i, G, H, E, D;
																	F = e(y.settings.browse_button);
																	if (F) {
																		i = b
																				.getPos(
																						F,
																						e(y.settings.container));
																		G = b
																				.getSize(F);
																		H = e("form_"
																				+ q);
																		E = e("input_"
																				+ q);
																		b
																				.extend(
																						H.style,
																						{
																							top : i.y
																									+ "px",
																							left : i.x
																									+ "px",
																							width : G.w
																									+ "px",
																							height : G.h
																									+ "px"
																						});
																		if (y.features.triggerDialog) {
																			if (b
																					.getStyle(
																							F,
																							"position") === "static") {
																				b
																						.extend(
																								F.style,
																								{
																									position : "relative"
																								})
																			}
																			D = parseInt(
																					F.style.zIndex,
																					10);
																			if (isNaN(D)) {
																				D = 0
																			}
																			b
																					.extend(
																							F.style,
																							{
																								zIndex : D
																							});
																			b
																					.extend(
																							H.style,
																							{
																								zIndex : D - 1
																							})
																		}
																		B = y.settings.browse_button_hover;
																		C = y.settings.browse_button_active;
																		A = y.features.triggerDialog ? F
																				: H;
																		if (B) {
																			b
																					.addEvent(
																							A,
																							"mouseover",
																							function() {
																								b
																										.addClass(
																												F,
																												B)
																							},
																							y.id);
																			b
																					.addEvent(
																							A,
																							"mouseout",
																							function() {
																								b
																										.removeClass(
																												F,
																												B)
																							},
																							y.id)
																		}
																		if (C) {
																			b
																					.addEvent(
																							A,
																							"mousedown",
																							function() {
																								b
																										.addClass(
																												F,
																												C)
																							},
																							y.id);
																			b
																					.addEvent(
																							a.body,
																							"mouseup",
																							function() {
																								b
																										.removeClass(
																												F,
																												C)
																							},
																							y.id)
																		}
																	}
																});
												f
														.bind(
																"FilesRemoved",
																function(y, B) {
																	var A, C;
																	for (A = 0; A < B.length; A++) {
																		C = e("form_"
																				+ B[A].id);
																		if (C) {
																			C.parentNode
																					.removeChild(C)
																		}
																	}
																});
												f
														.bind(
																"DisableBrowse",
																function(i, A) {
																	var y = a
																			.getElementById("input_"
																					+ q);
																	if (y) {
																		y.disabled = A
																	}
																});
												f
														.bind(
																"Destroy",
																function(i) {
																	var y, A, B, C = {
																		inputContainer : "form_"
																				+ q,
																		inputFile : "input_"
																				+ q,
																		browseButton : i.settings.browse_button
																	};
																	for (y in C) {
																		A = e(C[y]);
																		if (A) {
																			b
																					.removeAllEvents(
																							A,
																							i.id)
																		}
																	}
																	b
																			.removeAllEvents(
																					a.body,
																					i.id);
																	b
																			.each(
																					z,
																					function(
																							E,
																							D) {
																						B = e("form_"
																								+ E);
																						if (B) {
																							B.parentNode
																									.removeChild(B)
																						}
																					})
																});
												v()
											});
							g({
								success : true
							})
						}
					})
})(window, document, plupload);
;
// File end:
// /var/deployments/www.raptor-editor.com.3/raptor-gold/raptor-dependencies/plupload.html4.js
;
// File start:
// /var/deployments/www.raptor-editor.com.3/raptor-gold/raptor-dependencies/plupload.html5.js
(function(k, m, l, g) {
	var d = {}, j;
	function c(s) {
		var r = s.naturalWidth, u = s.naturalHeight;
		if (r * u > 1024 * 1024) {
			var t = m.createElement("canvas");
			t.width = t.height = 1;
			var q = t.getContext("2d");
			q.drawImage(s, -r + 1, 0);
			return q.getImageData(0, 0, 1, 1).data[3] === 0
		} else {
			return false
		}
	}
	function f(u, r, z) {
		var q = m.createElement("canvas");
		q.width = 1;
		q.height = z;
		var A = q.getContext("2d");
		A.drawImage(u, 0, 0);
		var t = A.getImageData(0, 0, 1, z).data;
		var x = 0;
		var v = z;
		var y = z;
		while (y > x) {
			var s = t[(y - 1) * 4 + 3];
			if (s === 0) {
				v = y
			} else {
				x = y
			}
			y = (v + x) >> 1
		}
		var w = (y / z);
		return (w === 0) ? 1 : w
	}
	function o(K, s, t) {
		var v = K.naturalWidth, z = K.naturalHeight;
		var E = t.width, B = t.height;
		var F = s.getContext("2d");
		F.save();
		var r = c(K);
		if (r) {
			v /= 2;
			z /= 2
		}
		var I = 1024;
		var q = m.createElement("canvas");
		q.width = q.height = I;
		var u = q.getContext("2d");
		var G = f(K, v, z);
		var A = 0;
		while (A < z) {
			var J = A + I > z ? z - A : I;
			var C = 0;
			while (C < v) {
				var D = C + I > v ? v - C : I;
				u.clearRect(0, 0, I, I);
				u.drawImage(K, -C, -A);
				var x = (C * E / v) << 0;
				var y = Math.ceil(D * E / v);
				var w = (A * B / z / G) << 0;
				var H = Math.ceil(J * B / z / G);
				F.drawImage(q, 0, 0, D, J, x, w, y, H);
				C += I
			}
			A += I
		}
		F.restore();
		q = u = null
	}
	function p(r, s) {
		var q;
		if ("FileReader" in k) {
			q = new FileReader();
			q.readAsDataURL(r);
			q.onload = function() {
				s(q.result)
			}
		} else {
			return s(r.getAsDataURL())
		}
	}
	function n(r, s) {
		var q;
		if ("FileReader" in k) {
			q = new FileReader();
			q.readAsBinaryString(r);
			q.onload = function() {
				s(q.result)
			}
		} else {
			return s(r.getAsBinary())
		}
	}
	function e(u, s, q, y) {
		var t, r, x, v, w = this;
		p(d[u.id], function(z) {
			t = m.createElement("canvas");
			t.style.display = "none";
			m.body.appendChild(t);
			x = new Image();
			x.onerror = x.onabort = function() {
				y({
					success : false
				})
			};
			x.onload = function() {
				var F, A, C, B, E;
				if (!s.width) {
					s.width = x.width
				}
				if (!s.height) {
					s.height = x.height
				}
				v = Math.min(s.width / x.width, s.height / x.height);
				if (v < 1) {
					F = Math.round(x.width * v);
					A = Math.round(x.height * v)
				} else {
					if (s.quality && q === "image/jpeg") {
						F = x.width;
						A = x.height
					} else {
						y({
							success : false
						});
						return
					}
				}
				t.width = F;
				t.height = A;
				o(x, t, {
					width : F,
					height : A
				});
				if (q === "image/jpeg") {
					B = new h(atob(z.substring(z.indexOf("base64,") + 7)));
					if (B.headers && B.headers.length) {
						E = new a();
						if (E.init(B.get("exif")[0])) {
							E.setExif("PixelXDimension", F);
							E.setExif("PixelYDimension", A);
							B.set("exif", E.getBinary());
							if (w.hasEventListener("ExifData")) {
								w.trigger("ExifData", u, E.EXIF())
							}
							if (w.hasEventListener("GpsData")) {
								w.trigger("GpsData", u, E.GPS())
							}
						}
					}
				}
				if (s.quality && q === "image/jpeg") {
					try {
						z = t.toDataURL(q, s.quality / 100)
					} catch (D) {
						z = t.toDataURL(q)
					}
				} else {
					z = t.toDataURL(q)
				}
				z = z.substring(z.indexOf("base64,") + 7);
				z = atob(z);
				if (B && B.headers && B.headers.length) {
					z = B.restore(z);
					B.purge()
				}
				t.parentNode.removeChild(t);
				y({
					success : true,
					data : z
				})
			};
			x.src = z
		})
	}
	l.runtimes.Html5 = l
			.addRuntime(
					"html5",
					{
						getFeatures : function() {
							var v, r, u, t, s, q;
							r = u = s = q = false;
							if (k.XMLHttpRequest) {
								v = new XMLHttpRequest();
								u = !!v.upload;
								r = !!(v.sendAsBinary || v.upload)
							}
							if (r) {
								t = !!(v.sendAsBinary || (k.Uint8Array && k.ArrayBuffer));
								s = !!(File
										&& (File.prototype.getAsDataURL || k.FileReader) && t);
								q = !!(File && (File.prototype.mozSlice
										|| File.prototype.webkitSlice || File.prototype.slice))
							}
							j = l.ua.safari && l.ua.windows;
							return {
								html5 : r,
								dragdrop : (function() {
									var w = m.createElement("div");
									return ("draggable" in w)
											|| ("ondragstart" in w && "ondrop" in w)
								}()),
								jpgresize : s,
								pngresize : s,
								multipart : s || !!k.FileReader || !!k.FormData,
								canSendBinary : t,
								cantSendBlobInFormData : !!(l.ua.gecko
										&& k.FormData && k.FileReader && !FileReader.prototype.readAsArrayBuffer)
										|| l.ua.android,
								progress : u,
								chunks : q,
								multi_selection : !(l.ua.safari && l.ua.windows),
								triggerDialog : (l.ua.gecko && k.FormData || l.ua.webkit)
							}
						},
						init : function(s, u) {
							var q, t;
							function r(z) {
								var x, w, y = [], A, v = {};
								for (w = 0; w < z.length; w++) {
									x = z[w];
									if (v[x.name] && l.ua.safari
											&& l.ua.windows) {
										continue
									}
									v[x.name] = true;
									A = l.guid();
									d[A] = x;
									y.push(new l.File(A, x.fileName || x.name,
											x.fileSize || x.size))
								}
								if (y.length) {
									s.trigger("FilesAdded", y)
								}
							}
							q = this.getFeatures();
							if (!q.html5) {
								u({
									success : false
								});
								return
							}
							s
									.bind(
											"Init",
											function(A) {
												var J, I, F = [], z, G, w = A.settings.filters, x, E, v = m.body, H;
												J = m.createElement("div");
												J.id = A.id
														+ "_html5_container";
												l
														.extend(
																J.style,
																{
																	position : "absolute",
																	background : s.settings.shim_bgcolor
																			|| "transparent",
																	width : "100px",
																	height : "100px",
																	overflow : "hidden",
																	zIndex : 99999,
																	opacity : s.settings.shim_bgcolor ? ""
																			: 0
																});
												J.className = "plupload html5";
												if (s.settings.container) {
													v = m
															.getElementById(s.settings.container);
													if (l.getStyle(v,
															"position") === "static") {
														v.style.position = "relative"
													}
												}
												v.appendChild(J);
												no_type_restriction: for (z = 0; z < w.length; z++) {
													x = w[z].extensions
															.split(/,/);
													for (G = 0; G < x.length; G++) {
														if (x[G] === "*") {
															F = [];
															break no_type_restriction
														}
														E = l.mimeTypes[x[G]];
														if (E
																&& l.inArray(E,
																		F) === -1) {
															F.push(E)
														}
													}
												}
												J.innerHTML = '<input id="'
														+ s.id
														+ '_html5"  style="font-size:999px" type="file" accept="'
														+ F.join(",")
														+ '" '
														+ (s.settings.multi_selection
																&& s.features.multi_selection ? 'multiple="multiple"'
																: "") + " />";
												J.scrollTop = 100;
												H = m.getElementById(s.id
														+ "_html5");
												if (A.features.triggerDialog) {
													l.extend(H.style, {
														position : "absolute",
														width : "100%",
														height : "100%"
													})
												} else {
													l.extend(H.style, {
														cssFloat : "right",
														styleFloat : "right"
													})
												}
												H.onchange = function() {
													r(this.files);
													this.value = ""
												};
												I = m
														.getElementById(A.settings.browse_button);
												if (I) {
													var C = A.settings.browse_button_hover, D = A.settings.browse_button_active, B = A.features.triggerDialog ? I
															: J;
													if (C) {
														l.addEvent(B,
																"mouseover",
																function() {
																	l.addClass(
																			I,
																			C)
																}, A.id);
														l
																.addEvent(
																		B,
																		"mouseout",
																		function() {
																			l
																					.removeClass(
																							I,
																							C)
																		}, A.id)
													}
													if (D) {
														l.addEvent(B,
																"mousedown",
																function() {
																	l.addClass(
																			I,
																			D)
																}, A.id);
														l
																.addEvent(
																		m.body,
																		"mouseup",
																		function() {
																			l
																					.removeClass(
																							I,
																							D)
																		}, A.id)
													}
													if (A.features.triggerDialog) {
														l
																.addEvent(
																		I,
																		"click",
																		function(
																				K) {
																			var y = m
																					.getElementById(A.id
																							+ "_html5");
																			if (y
																					&& !y.disabled) {
																				y
																						.click()
																			}
																			K
																					.preventDefault()
																		}, A.id)
													}
												}
											});
							s
									.bind(
											"PostInit",
											function() {
												var v = m
														.getElementById(s.settings.drop_element);
												if (v) {
													if (j) {
														l
																.addEvent(
																		v,
																		"dragenter",
																		function(
																				z) {
																			var y, w, x;
																			y = m
																					.getElementById(s.id
																							+ "_drop");
																			if (!y) {
																				y = m
																						.createElement("input");
																				y
																						.setAttribute(
																								"type",
																								"file");
																				y
																						.setAttribute(
																								"id",
																								s.id
																										+ "_drop");
																				y
																						.setAttribute(
																								"multiple",
																								"multiple");
																				l
																						.addEvent(
																								y,
																								"change",
																								function() {
																									r(this.files);
																									l
																											.removeEvent(
																													y,
																													"change",
																													s.id);
																									y.parentNode
																											.removeChild(y)
																								},
																								s.id);
																				l
																						.addEvent(
																								y,
																								"dragover",
																								function(
																										A) {
																									A
																											.stopPropagation()
																								},
																								s.id);
																				v
																						.appendChild(y)
																			}
																			w = l
																					.getPos(
																							v,
																							m
																									.getElementById(s.settings.container));
																			x = l
																					.getSize(v);
																			if (l
																					.getStyle(
																							v,
																							"position") === "static") {
																				l
																						.extend(
																								v.style,
																								{
																									position : "relative"
																								})
																			}
																			l
																					.extend(
																							y.style,
																							{
																								position : "absolute",
																								display : "block",
																								top : 0,
																								left : 0,
																								width : x.w
																										+ "px",
																								height : x.h
																										+ "px",
																								opacity : 0
																							})
																		}, s.id);
														return
													}
													l
															.addEvent(
																	v,
																	"dragover",
																	function(w) {
																		w
																				.preventDefault()
																	}, s.id);
													l
															.addEvent(
																	v,
																	"drop",
																	function(x) {
																		var w = x.dataTransfer;
																		if (w
																				&& w.files) {
																			r(w.files)
																		}
																		x
																				.preventDefault()
																	}, s.id)
												}
											});
							s
									.bind(
											"Refresh",
											function(v) {
												var w, x, y, A, z;
												w = m
														.getElementById(s.settings.browse_button);
												if (w) {
													x = l
															.getPos(
																	w,
																	m
																			.getElementById(v.settings.container));
													y = l.getSize(w);
													A = m
															.getElementById(s.id
																	+ "_html5_container");
													l.extend(A.style, {
														top : x.y + "px",
														left : x.x + "px",
														width : y.w + "px",
														height : y.h + "px"
													});
													if (s.features.triggerDialog) {
														if (l.getStyle(w,
																"position") === "static") {
															l
																	.extend(
																			w.style,
																			{
																				position : "relative"
																			})
														}
														z = parseInt(
																l
																		.getStyle(
																				w,
																				"zIndex"),
																10);
														if (isNaN(z)) {
															z = 0
														}
														l.extend(w.style, {
															zIndex : z
														});
														l.extend(A.style, {
															zIndex : z - 1
														})
													}
												}
											});
							s.bind("DisableBrowse", function(v, x) {
								var w = m.getElementById(v.id + "_html5");
								if (w) {
									w.disabled = x
								}
							});
							s.bind("CancelUpload", function() {
								if (t && t.abort) {
									t.abort()
								}
							});
							s
									.bind(
											"UploadFile",
											function(v, x) {
												var y = v.settings, B, w;
												function A(D, G, C) {
													var E;
													if (File.prototype.slice) {
														try {
															D.slice();
															return D
																	.slice(G, C)
														} catch (F) {
															return D.slice(G, C
																	- G)
														}
													} else {
														if (E = File.prototype.webkitSlice
																|| File.prototype.mozSlice) {
															return E.call(D, G,
																	C)
														} else {
															return null
														}
													}
												}
												function z(C) {
													var F = 0, E = 0;
													function D() {
														var L, P, N, O, K, M, H, G = v.settings.url;
														function J(S) {
															if (t.sendAsBinary) {
																t
																		.sendAsBinary(S)
															} else {
																if (v.features.canSendBinary) {
																	var Q = new Uint8Array(
																			S.length);
																	for ( var R = 0; R < S.length; R++) {
																		Q[R] = (S
																				.charCodeAt(R) & 255)
																	}
																	t
																			.send(Q.buffer)
																}
															}
														}
														function I(R) {
															var V = 0, W = "----pluploadboundary"
																	+ l.guid(), T, S = "--", U = "\r\n", Q = "";
															t = new XMLHttpRequest;
															if (t.upload) {
																t.upload.onprogress = function(
																		X) {
																	x.loaded = Math
																			.min(
																					x.size,
																					E
																							+ X.loaded
																							- V);
																	v
																			.trigger(
																					"UploadProgress",
																					x)
																}
															}
															t.onreadystatechange = function() {
																var X, Z;
																if (t.readyState == 4
																		&& v.state !== l.STOPPED) {
																	try {
																		X = t.status
																	} catch (Y) {
																		X = 0
																	}
																	if (X >= 400) {
																		v
																				.trigger(
																						"Error",
																						{
																							code : l.HTTP_ERROR,
																							message : l
																									.translate("HTTP Error."),
																							file : x,
																							status : X
																						})
																	} else {
																		if (N) {
																			Z = {
																				chunk : F,
																				chunks : N,
																				response : t.responseText,
																				status : X
																			};
																			v
																					.trigger(
																							"ChunkUploaded",
																							x,
																							Z);
																			E += M;
																			if (Z.cancelled) {
																				x.status = l.FAILED;
																				return
																			}
																			x.loaded = Math
																					.min(
																							x.size,
																							(F + 1)
																									* K)
																		} else {
																			x.loaded = x.size
																		}
																		v
																				.trigger(
																						"UploadProgress",
																						x);
																		R = L = T = Q = null;
																		if (!N
																				|| ++F >= N) {
																			x.status = l.DONE;
																			v
																					.trigger(
																							"FileUploaded",
																							x,
																							{
																								response : t.responseText,
																								status : X
																							})
																		} else {
																			D()
																		}
																	}
																}
															};
															if (v.settings.multipart
																	&& q.multipart) {
																O.name = x.target_name
																		|| x.name;
																t
																		.open(
																				"post",
																				G,
																				true);
																l
																		.each(
																				v.settings.headers,
																				function(
																						Y,
																						X) {
																					t
																							.setRequestHeader(
																									X,
																									Y)
																				});
																if (typeof (R) !== "string"
																		&& !!k.FormData) {
																	T = new FormData();
																	l
																			.each(
																					l
																							.extend(
																									O,
																									v.settings.multipart_params),
																					function(
																							Y,
																							X) {
																						T
																								.append(
																										X,
																										Y)
																					});
																	T
																			.append(
																					v.settings.file_data_name,
																					R);
																	t.send(T);
																	return
																}
																if (typeof (R) === "string") {
																	t
																			.setRequestHeader(
																					"Content-Type",
																					"multipart/form-data; boundary="
																							+ W);
																	l
																			.each(
																					l
																							.extend(
																									O,
																									v.settings.multipart_params),
																					function(
																							Y,
																							X) {
																						Q += S
																								+ W
																								+ U
																								+ 'Content-Disposition: form-data; name="'
																								+ X
																								+ '"'
																								+ U
																								+ U;
																						Q += unescape(encodeURIComponent(Y))
																								+ U
																					});
																	H = l.mimeTypes[x.name
																			.replace(
																					/^.+\.([^.]+)/,
																					"$1")
																			.toLowerCase()]
																			|| "application/octet-stream";
																	Q += S
																			+ W
																			+ U
																			+ 'Content-Disposition: form-data; name="'
																			+ v.settings.file_data_name
																			+ '"; filename="'
																			+ unescape(encodeURIComponent(x.name))
																			+ '"'
																			+ U
																			+ "Content-Type: "
																			+ H
																			+ U
																			+ U
																			+ R
																			+ U
																			+ S
																			+ W
																			+ S
																			+ U;
																	V = Q.length
																			- R.length;
																	R = Q;
																	J(R);
																	return
																}
															}
															G = l
																	.buildUrl(
																			v.settings.url,
																			l
																					.extend(
																							O,
																							v.settings.multipart_params));
															t.open("post", G,
																	true);
															t
																	.setRequestHeader(
																			"Content-Type",
																			"application/octet-stream");
															l
																	.each(
																			v.settings.headers,
																			function(
																					Y,
																					X) {
																				t
																						.setRequestHeader(
																								X,
																								Y)
																			});
															if (typeof (R) === "string") {
																J(R)
															} else {
																t.send(R)
															}
														}
														if (x.status == l.DONE
																|| x.status == l.FAILED
																|| v.state == l.STOPPED) {
															return
														}
														O = {
															name : x.target_name
																	|| x.name
														};
														if (y.chunk_size
																&& x.size > y.chunk_size
																&& (q.chunks || typeof (C) == "string")) {
															K = y.chunk_size;
															N = Math
																	.ceil(x.size
																			/ K);
															M = Math
																	.min(
																			K,
																			x.size
																					- (F * K));
															if (typeof (C) == "string") {
																L = C
																		.substring(
																				F
																						* K,
																				F
																						* K
																						+ M)
															} else {
																L = A(
																		C,
																		F * K,
																		F
																				* K
																				+ M)
															}
															O.chunk = F;
															O.chunks = N
														} else {
															M = x.size;
															L = C
														}
														if (v.settings.multipart
																&& q.multipart
																&& typeof (L) !== "string"
																&& k.FileReader
																&& q.cantSendBlobInFormData
																&& q.chunks
																&& v.settings.chunk_size) {
															(function() {
																var Q = new FileReader();
																Q.onload = function() {
																	I(Q.result);
																	Q = null
																};
																Q
																		.readAsBinaryString(L)
															}())
														} else {
															I(L)
														}
													}
													D()
												}
												B = d[x.id];
												if (q.jpgresize
														&& v.settings.resize
														&& /\.(png|jpg|jpeg)$/i
																.test(x.name)) {
													e
															.call(
																	v,
																	x,
																	v.settings.resize,
																	/\.png$/i
																			.test(x.name) ? "image/png"
																			: "image/jpeg",
																	function(C) {
																		if (C.success) {
																			x.size = C.data.length;
																			z(C.data)
																		} else {
																			if (q.chunks) {
																				z(B)
																			} else {
																				n(
																						B,
																						z)
																			}
																		}
																	})
												} else {
													if (!q.chunks
															&& q.jpgresize) {
														n(B, z)
													} else {
														z(B)
													}
												}
											});
							s.bind("Destroy", function(v) {
								var x, y, w = m.body, z = {
									inputContainer : v.id + "_html5_container",
									inputFile : v.id + "_html5",
									browseButton : v.settings.browse_button,
									dropElm : v.settings.drop_element
								};
								for (x in z) {
									y = m.getElementById(z[x]);
									if (y) {
										l.removeAllEvents(y, v.id)
									}
								}
								l.removeAllEvents(m.body, v.id);
								if (v.settings.container) {
									w = m.getElementById(v.settings.container)
								}
								w.removeChild(m
										.getElementById(z.inputContainer))
							});
							u({
								success : true
							})
						}
					});
	function b() {
		var t = false, r;
		function u(w, y) {
			var v = t ? 0 : -8 * (y - 1), z = 0, x;
			for (x = 0; x < y; x++) {
				z |= (r.charCodeAt(w + x) << Math.abs(v + x * 8))
			}
			return z
		}
		function q(x, v, w) {
			var w = arguments.length === 3 ? w : r.length - v - 1;
			r = r.substr(0, v) + x + r.substr(w + v)
		}
		function s(w, x, z) {
			var A = "", v = t ? 0 : -8 * (z - 1), y;
			for (y = 0; y < z; y++) {
				A += String.fromCharCode((x >> Math.abs(v + y * 8)) & 255)
			}
			q(A, w, z)
		}
		return {
			II : function(v) {
				if (v === g) {
					return t
				} else {
					t = v
				}
			},
			init : function(v) {
				t = false;
				r = v
			},
			SEGMENT : function(v, x, w) {
				switch (arguments.length) {
				case 1:
					return r.substr(v, r.length - v - 1);
				case 2:
					return r.substr(v, x);
				case 3:
					q(w, v, x);
					break;
				default:
					return r
				}
			},
			BYTE : function(v) {
				return u(v, 1)
			},
			SHORT : function(v) {
				return u(v, 2)
			},
			LONG : function(v, w) {
				if (w === g) {
					return u(v, 4)
				} else {
					s(v, w, 4)
				}
			},
			SLONG : function(v) {
				var w = u(v, 4);
				return (w > 2147483647 ? w - 4294967296 : w)
			},
			STRING : function(v, w) {
				var x = "";
				for (w += v; v < w; v++) {
					x += String.fromCharCode(u(v, 1))
				}
				return x
			}
		}
	}
	function h(v) {
		var x = {
			65505 : {
				app : "EXIF",
				name : "APP1",
				signature : "Exif\0"
			},
			65506 : {
				app : "ICC",
				name : "APP2",
				signature : "ICC_PROFILE\0"
			},
			65517 : {
				app : "IPTC",
				name : "APP13",
				signature : "Photoshop 3.0\0"
			}
		}, w = [], u, q, s = g, t = 0, r;
		u = new b();
		u.init(v);
		if (u.SHORT(0) !== 65496) {
			return
		}
		q = 2;
		r = Math.min(1048576, v.length);
		while (q <= r) {
			s = u.SHORT(q);
			if (s >= 65488 && s <= 65495) {
				q += 2;
				continue
			}
			if (s === 65498 || s === 65497) {
				break
			}
			t = u.SHORT(q + 2) + 2;
			if (x[s]
					&& u.STRING(q + 4, x[s].signature.length) === x[s].signature) {
				w.push({
					hex : s,
					app : x[s].app.toUpperCase(),
					name : x[s].name.toUpperCase(),
					start : q,
					length : t,
					segment : u.SEGMENT(q, t)
				})
			}
			q += t
		}
		u.init(null);
		return {
			headers : w,
			restore : function(B) {
				u.init(B);
				var z = new h(B);
				if (!z.headers) {
					return false
				}
				for ( var A = z.headers.length; A > 0; A--) {
					var C = z.headers[A - 1];
					u.SEGMENT(C.start, C.length, "")
				}
				z.purge();
				q = u.SHORT(2) == 65504 ? 4 + u.SHORT(4) : 2;
				for ( var A = 0, y = w.length; A < y; A++) {
					u.SEGMENT(q, 0, w[A].segment);
					q += w[A].length
				}
				return u.SEGMENT()
			},
			get : function(A) {
				var B = [];
				for ( var z = 0, y = w.length; z < y; z++) {
					if (w[z].app === A.toUpperCase()) {
						B.push(w[z].segment)
					}
				}
				return B
			},
			set : function(B, A) {
				var C = [];
				if (typeof (A) === "string") {
					C.push(A)
				} else {
					C = A
				}
				for ( var z = ii = 0, y = w.length; z < y; z++) {
					if (w[z].app === B.toUpperCase()) {
						w[z].segment = C[ii];
						w[z].length = C[ii].length;
						ii++
					}
					if (ii >= C.length) {
						break
					}
				}
			},
			purge : function() {
				w = [];
				u.init(null)
			}
		}
	}
	function a() {
		var t, q, r = {}, w;
		t = new b();
		q = {
			tiff : {
				274 : "Orientation",
				34665 : "ExifIFDPointer",
				34853 : "GPSInfoIFDPointer"
			},
			exif : {
				36864 : "ExifVersion",
				40961 : "ColorSpace",
				40962 : "PixelXDimension",
				40963 : "PixelYDimension",
				36867 : "DateTimeOriginal",
				33434 : "ExposureTime",
				33437 : "FNumber",
				34855 : "ISOSpeedRatings",
				37377 : "ShutterSpeedValue",
				37378 : "ApertureValue",
				37383 : "MeteringMode",
				37384 : "LightSource",
				37385 : "Flash",
				41986 : "ExposureMode",
				41987 : "WhiteBalance",
				41990 : "SceneCaptureType",
				41988 : "DigitalZoomRatio",
				41992 : "Contrast",
				41993 : "Saturation",
				41994 : "Sharpness"
			},
			gps : {
				0 : "GPSVersionID",
				1 : "GPSLatitudeRef",
				2 : "GPSLatitude",
				3 : "GPSLongitudeRef",
				4 : "GPSLongitude"
			}
		};
		w = {
			ColorSpace : {
				1 : "sRGB",
				0 : "Uncalibrated"
			},
			MeteringMode : {
				0 : "Unknown",
				1 : "Average",
				2 : "CenterWeightedAverage",
				3 : "Spot",
				4 : "MultiSpot",
				5 : "Pattern",
				6 : "Partial",
				255 : "Other"
			},
			LightSource : {
				1 : "Daylight",
				2 : "Fliorescent",
				3 : "Tungsten",
				4 : "Flash",
				9 : "Fine weather",
				10 : "Cloudy weather",
				11 : "Shade",
				12 : "Daylight fluorescent (D 5700 - 7100K)",
				13 : "Day white fluorescent (N 4600 -5400K)",
				14 : "Cool white fluorescent (W 3900 - 4500K)",
				15 : "White fluorescent (WW 3200 - 3700K)",
				17 : "Standard light A",
				18 : "Standard light B",
				19 : "Standard light C",
				20 : "D55",
				21 : "D65",
				22 : "D75",
				23 : "D50",
				24 : "ISO studio tungsten",
				255 : "Other"
			},
			Flash : {
				0 : "Flash did not fire.",
				1 : "Flash fired.",
				5 : "Strobe return light not detected.",
				7 : "Strobe return light detected.",
				9 : "Flash fired, compulsory flash mode",
				13 : "Flash fired, compulsory flash mode, return light not detected",
				15 : "Flash fired, compulsory flash mode, return light detected",
				16 : "Flash did not fire, compulsory flash mode",
				24 : "Flash did not fire, auto mode",
				25 : "Flash fired, auto mode",
				29 : "Flash fired, auto mode, return light not detected",
				31 : "Flash fired, auto mode, return light detected",
				32 : "No flash function",
				65 : "Flash fired, red-eye reduction mode",
				69 : "Flash fired, red-eye reduction mode, return light not detected",
				71 : "Flash fired, red-eye reduction mode, return light detected",
				73 : "Flash fired, compulsory flash mode, red-eye reduction mode",
				77 : "Flash fired, compulsory flash mode, red-eye reduction mode, return light not detected",
				79 : "Flash fired, compulsory flash mode, red-eye reduction mode, return light detected",
				89 : "Flash fired, auto mode, red-eye reduction mode",
				93 : "Flash fired, auto mode, return light not detected, red-eye reduction mode",
				95 : "Flash fired, auto mode, return light detected, red-eye reduction mode"
			},
			ExposureMode : {
				0 : "Auto exposure",
				1 : "Manual exposure",
				2 : "Auto bracket"
			},
			WhiteBalance : {
				0 : "Auto white balance",
				1 : "Manual white balance"
			},
			SceneCaptureType : {
				0 : "Standard",
				1 : "Landscape",
				2 : "Portrait",
				3 : "Night scene"
			},
			Contrast : {
				0 : "Normal",
				1 : "Soft",
				2 : "Hard"
			},
			Saturation : {
				0 : "Normal",
				1 : "Low saturation",
				2 : "High saturation"
			},
			Sharpness : {
				0 : "Normal",
				1 : "Soft",
				2 : "Hard"
			},
			GPSLatitudeRef : {
				N : "North latitude",
				S : "South latitude"
			},
			GPSLongitudeRef : {
				E : "East longitude",
				W : "West longitude"
			}
		};
		function s(x, F) {
			var z = t.SHORT(x), C, I, J, E, D, y, A, G, H = [], B = {};
			for (C = 0; C < z; C++) {
				A = y = x + 12 * C + 2;
				J = F[t.SHORT(A)];
				if (J === g) {
					continue
				}
				E = t.SHORT(A += 2);
				D = t.LONG(A += 2);
				A += 4;
				H = [];
				switch (E) {
				case 1:
				case 7:
					if (D > 4) {
						A = t.LONG(A) + r.tiffHeader
					}
					for (I = 0; I < D; I++) {
						H[I] = t.BYTE(A + I)
					}
					break;
				case 2:
					if (D > 4) {
						A = t.LONG(A) + r.tiffHeader
					}
					B[J] = t.STRING(A, D - 1);
					continue;
				case 3:
					if (D > 2) {
						A = t.LONG(A) + r.tiffHeader
					}
					for (I = 0; I < D; I++) {
						H[I] = t.SHORT(A + I * 2)
					}
					break;
				case 4:
					if (D > 1) {
						A = t.LONG(A) + r.tiffHeader
					}
					for (I = 0; I < D; I++) {
						H[I] = t.LONG(A + I * 4)
					}
					break;
				case 5:
					A = t.LONG(A) + r.tiffHeader;
					for (I = 0; I < D; I++) {
						H[I] = t.LONG(A + I * 4) / t.LONG(A + I * 4 + 4)
					}
					break;
				case 9:
					A = t.LONG(A) + r.tiffHeader;
					for (I = 0; I < D; I++) {
						H[I] = t.SLONG(A + I * 4)
					}
					break;
				case 10:
					A = t.LONG(A) + r.tiffHeader;
					for (I = 0; I < D; I++) {
						H[I] = t.SLONG(A + I * 4) / t.SLONG(A + I * 4 + 4)
					}
					break;
				default:
					continue
				}
				G = (D == 1 ? H[0] : H);
				if (w.hasOwnProperty(J) && typeof G != "object") {
					B[J] = w[J][G]
				} else {
					B[J] = G
				}
			}
			return B
		}
		function v() {
			var y = g, x = r.tiffHeader;
			t.II(t.SHORT(x) == 18761);
			if (t.SHORT(x += 2) !== 42) {
				return false
			}
			r.IFD0 = r.tiffHeader + t.LONG(x += 2);
			y = s(r.IFD0, q.tiff);
			r.exifIFD = ("ExifIFDPointer" in y ? r.tiffHeader
					+ y.ExifIFDPointer : g);
			r.gpsIFD = ("GPSInfoIFDPointer" in y ? r.tiffHeader
					+ y.GPSInfoIFDPointer : g);
			return true
		}
		function u(z, x, C) {
			var E, B, A, D = 0;
			if (typeof (x) === "string") {
				var y = q[z.toLowerCase()];
				for (hex in y) {
					if (y[hex] === x) {
						x = hex;
						break
					}
				}
			}
			E = r[z.toLowerCase() + "IFD"];
			B = t.SHORT(E);
			for (i = 0; i < B; i++) {
				A = E + 12 * i + 2;
				if (t.SHORT(A) == x) {
					D = A + 8;
					break
				}
			}
			if (!D) {
				return false
			}
			t.LONG(D, C);
			return true
		}
		return {
			init : function(x) {
				r = {
					tiffHeader : 10
				};
				if (x === g || !x.length) {
					return false
				}
				t.init(x);
				if (t.SHORT(0) === 65505
						&& t.STRING(4, 5).toUpperCase() === "EXIF\0") {
					return v()
				}
				return false
			},
			EXIF : function() {
				var y;
				y = s(r.exifIFD, q.exif);
				if (y.ExifVersion && l.typeOf(y.ExifVersion) === "array") {
					for ( var z = 0, x = ""; z < y.ExifVersion.length; z++) {
						x += String.fromCharCode(y.ExifVersion[z])
					}
					y.ExifVersion = x
				}
				return y
			},
			GPS : function() {
				var x;
				x = s(r.gpsIFD, q.gps);
				if (x.GPSVersionID) {
					x.GPSVersionID = x.GPSVersionID.join(".")
				}
				return x
			},
			setExif : function(x, y) {
				if (x !== "PixelXDimension" && x !== "PixelYDimension") {
					return false
				}
				return u("exif", x, y)
			},
			getBinary : function() {
				return t.SEGMENT()
			}
		}
	}
})(window, document, plupload);
;
// File end:
// /var/deployments/www.raptor-editor.com.3/raptor-gold/raptor-dependencies/plupload.html5.js
;
// File start:
// /var/deployments/www.raptor-editor.com.3/raptor-gold/raptor-dependencies/jquery.ui.plupload.js
(function(f, a, c, g, e) {
	var h = {};
	function b(i) {
		return c.translate(i) || i
	}
	function d(i) {
		i
				.html('<div class="plupload_wrapper"><div class="ui-widget-content plupload_container"><div class="plupload"><div class="ui-state-default ui-widget-header plupload_header"><div class="plupload_header_content"><div class="plupload_header_title">'
						+ b("Select files")
						+ '</div><div class="plupload_header_text">'
						+ b("Add files to the upload queue and click the start button.")
						+ '</div></div></div><div class="plupload_content"><table class="plupload_filelist"><tr class="ui-widget-header plupload_filelist_header"><td class="plupload_cell plupload_file_name">'
						+ b("Filename")
						+ '</td><td class="plupload_cell plupload_file_status">'
						+ b("Status")
						+ '</td><td class="plupload_cell plupload_file_size">'
						+ b("Size")
						+ '</td><td class="plupload_cell plupload_file_action">&nbsp;</td></tr></table><div class="plupload_scroll"><table class="plupload_filelist_content"></table></div><table class="plupload_filelist"><tr class="ui-widget-header ui-widget-content plupload_filelist_footer"><td class="plupload_cell plupload_file_name"><div class="plupload_buttons"><!-- Visible --><a class="plupload_button plupload_add">'
						+ b("Add Files")
						+ '</a>&nbsp;<a class="plupload_button plupload_start">'
						+ b("Start Upload")
						+ '</a>&nbsp;<a class="plupload_button plupload_stop plupload_hidden">'
						+ b("Stop Upload")
						+ '</a>&nbsp;</div><div class="plupload_started plupload_hidden"><!-- Hidden --><div class="plupload_progress plupload_right"><div class="plupload_progress_container"></div></div><div class="plupload_cell plupload_upload_status"></div><div class="plupload_clearer">&nbsp;</div></div></td><td class="plupload_file_status"><span class="plupload_total_status">0%</span></td><td class="plupload_file_size"><span class="plupload_total_file_size">0 kb</span></td><td class="plupload_file_action"></td></tr></table></div></div></div><input class="plupload_count" value="0" type="hidden"></div>')
	}
	g
			.widget(
					"ui.plupload",
					{
						contents_bak : "",
						runtime : null,
						options : {
							browse_button_hover : "ui-state-hover",
							browse_button_active : "ui-state-active",
							dragdrop : true,
							multiple_queues : true,
							buttons : {
								browse : true,
								start : true,
								stop : true
							},
							autostart : false,
							sortable : false,
							rename : false,
							max_file_count : 0
						},
						FILE_COUNT_ERROR : -9001,
						_create : function() {
							var i = this, k, j;
							k = this.element.attr("id");
							if (!k) {
								k = c.guid();
								this.element.attr("id", k)
							}
							this.id = k;
							this.contents_bak = this.element.html();
							d(this.element);
							this.container = g(".plupload_container",
									this.element).attr("id", k + "_container");
							this.filelist = g(".plupload_filelist_content",
									this.container).attr({
								id : k + "_filelist",
								unselectable : "on"
							});
							this.browse_button = g(".plupload_add",
									this.container).attr("id", k + "_browse");
							this.start_button = g(".plupload_start",
									this.container).attr("id", k + "_start");
							this.stop_button = g(".plupload_stop",
									this.container).attr("id", k + "_stop");
							if (g.ui.button) {
								this.browse_button.button({
									icons : {
										primary : "ui-icon-circle-plus"
									}
								});
								this.start_button.button({
									icons : {
										primary : "ui-icon-circle-arrow-e"
									},
									disabled : true
								});
								this.stop_button.button({
									icons : {
										primary : "ui-icon-circle-close"
									}
								})
							}
							this.progressbar = g(
									".plupload_progress_container",
									this.container);
							if (g.ui.progressbar) {
								this.progressbar.progressbar()
							}
							this.counter = g(".plupload_count", this.element)
									.attr({
										id : k + "_count",
										name : k + "_count"
									});
							j = this.uploader = h[k] = new c.Uploader(g.extend(
									{
										container : k,
										browse_button : k + "_browse"
									}, this.options));
							j.bind("Error", function(l, m) {
								if (m.code === c.INIT_ERROR) {
									i.destroy()
								}
							});
							j
									.bind(
											"Init",
											function(l, m) {
												if (!i.options.buttons.browse) {
													i.browse_button.button(
															"disable").hide();
													l.disableBrowse(true)
												}
												if (!i.options.buttons.start) {
													i.start_button.button(
															"disable").hide()
												}
												if (!i.options.buttons.stop) {
													i.stop_button.button(
															"disable").hide()
												}
												if (!i.options.unique_names
														&& i.options.rename) {
													i._enableRenaming()
												}
												if (j.features.dragdrop
														&& i.options.dragdrop) {
													i._enableDragAndDrop()
												}
												i.container
														.attr(
																"title",
																b("Using runtime: ")
																		+ (i.runtime = m.runtime));
												i.start_button
														.click(function(n) {
															if (!g(this)
																	.button(
																			"option",
																			"disabled")) {
																i.start()
															}
															n.preventDefault()
														});
												i.stop_button
														.click(function(n) {
															i.stop();
															n.preventDefault()
														})
											});
							if (i.options.max_file_count) {
								j.bind("FilesAdded", function(l, n) {
									var o = [], m = n.length;
									var p = l.files.length + m
											- i.options.max_file_count;
									if (p > 0) {
										o = n.splice(m - p, p);
										l.trigger("Error", {
											code : i.FILE_COUNT_ERROR,
											message : b("File count error."),
											file : o
										})
									}
								})
							}
							j.init();
							j.bind("FilesAdded", function(l, m) {
								i._trigger("selected", null, {
									up : l,
									files : m
								});
								if (i.options.autostart) {
									setTimeout(function() {
										i.start()
									}, 10)
								}
							});
							j.bind("FilesRemoved", function(l, m) {
								i._trigger("removed", null, {
									up : l,
									files : m
								})
							});
							j.bind("QueueChanged", function() {
								i._updateFileList()
							});
							j.bind("StateChanged", function() {
								i._handleState()
							});
							j.bind("UploadFile", function(l, m) {
								i._handleFileStatus(m)
							});
							j.bind("FileUploaded", function(l, m) {
								i._handleFileStatus(m);
								i._trigger("uploaded", null, {
									up : l,
									file : m
								})
							});
							j.bind("UploadProgress", function(l, m) {
								g("#" + m.id).find(".plupload_file_status")
										.html(m.percent + "%").end().find(
												".plupload_file_size").html(
												c.formatSize(m.size));
								i._handleFileStatus(m);
								i._updateTotalProgress();
								i._trigger("progress", null, {
									up : l,
									file : m
								})
							});
							j.bind("UploadComplete", function(l, m) {
								i._trigger("complete", null, {
									up : l,
									files : m
								})
							});
							j
									.bind(
											"Error",
											function(l, p) {
												var n = p.file, o, m;
												if (n) {
													o = "<strong>" + p.message
															+ "</strong>";
													m = p.details;
													if (m) {
														o += " <br /><i>"
																+ p.details
																+ "</i>"
													} else {
														switch (p.code) {
														case c.FILE_EXTENSION_ERROR:
															m = b("File: %s")
																	.replace(
																			"%s",
																			n.name);
															break;
														case c.FILE_SIZE_ERROR:
															m = b(
																	"File: %f, size: %s, max file size: %m")
																	.replace(
																			/%([fsm])/g,
																			function(
																					r,
																					q) {
																				switch (q) {
																				case "f":
																					return n.name;
																				case "s":
																					return n.size;
																				case "m":
																					return c
																							.parseSize(i.options.max_file_size)
																				}
																			});
															break;
														case i.FILE_COUNT_ERROR:
															m = b(
																	"Upload element accepts only %d file(s) at a time. Extra files were stripped.")
																	.replace(
																			"%d",
																			i.options.max_file_count);
															break;
														case c.IMAGE_FORMAT_ERROR:
															m = c
																	.translate("Image format either wrong or not supported.");
															break;
														case c.IMAGE_MEMORY_ERROR:
															m = c
																	.translate("Runtime ran out of available memory.");
															break;
														case c.IMAGE_DIMENSIONS_ERROR:
															m = c
																	.translate(
																			"Resoultion out of boundaries! <b>%s</b> runtime supports images only up to %wx%hpx.")
																	.replace(
																			/%([swh])/g,
																			function(
																					r,
																					q) {
																				switch (q) {
																				case "s":
																					return l.runtime;
																				case "w":
																					return l.features.maxWidth;
																				case "h":
																					return l.features.maxHeight
																				}
																			});
															break;
														case c.HTTP_ERROR:
															m = b("Upload URL might be wrong or doesn't exist");
															break
														}
														o += " <br /><i>" + m
																+ "</i>"
													}
													i.notify("error", o);
													i._trigger("error", null, {
														up : l,
														file : n,
														error : o
													})
												}
											})
						},
						_setOption : function(j, k) {
							var i = this;
							if (j == "buttons" && typeof (k) == "object") {
								k = g.extend(i.options.buttons, k);
								if (!k.browse) {
									i.browse_button.button("disable").hide();
									up.disableBrowse(true)
								} else {
									i.browse_button.button("enable").show();
									up.disableBrowse(false)
								}
								if (!k.start) {
									i.start_button.button("disable").hide()
								} else {
									i.start_button.button("enable").show()
								}
								if (!k.stop) {
									i.stop_button.button("disable").hide()
								} else {
									i.start_button.button("enable").show()
								}
							}
							i.uploader.settings[j] = k
						},
						start : function() {
							this.uploader.start();
							this._trigger("start", null)
						},
						stop : function() {
							this.uploader.stop();
							this._trigger("stop", null)
						},
						getFile : function(j) {
							var i;
							if (typeof j === "number") {
								i = this.uploader.files[j]
							} else {
								i = this.uploader.getFile(j)
							}
							return i
						},
						removeFile : function(j) {
							var i = this.getFile(j);
							if (i) {
								this.uploader.removeFile(i)
							}
						},
						clearQueue : function() {
							this.uploader.splice()
						},
						getUploader : function() {
							return this.uploader
						},
						refresh : function() {
							this.uploader.refresh()
						},
						_handleState : function() {
							var j = this, i = this.uploader;
							if (i.state === c.STARTED) {
								g(j.start_button).button("disable");
								g([]).add(j.stop_button).add(
										".plupload_started").removeClass(
										"plupload_hidden");
								g(".plupload_upload_status", j.element).html(
										b("Uploaded %d/%d files").replace(
												"%d/%d",
												i.total.uploaded + "/"
														+ i.files.length));
								g(".plupload_header_content", j.element)
										.addClass("plupload_header_content_bw")
							} else {
								g([]).add(j.stop_button).add(
										".plupload_started").addClass(
										"plupload_hidden");
								if (j.options.multiple_queues) {
									g(j.start_button).button("enable");
									g(".plupload_header_content", j.element)
											.removeClass(
													"plupload_header_content_bw")
								}
								j._updateFileList()
							}
						},
						_handleFileStatus : function(l) {
							var n, j;
							if (!g("#" + l.id).length) {
								return
							}
							switch (l.status) {
							case c.DONE:
								n = "plupload_done";
								j = "ui-icon ui-icon-circle-check";
								break;
							case c.FAILED:
								n = "ui-state-error plupload_failed";
								j = "ui-icon ui-icon-alert";
								break;
							case c.QUEUED:
								n = "plupload_delete";
								j = "ui-icon ui-icon-circle-minus";
								break;
							case c.UPLOADING:
								n = "ui-state-highlight plupload_uploading";
								j = "ui-icon ui-icon-circle-arrow-w";
								var i = g(".plupload_scroll", this.container), m = i
										.scrollTop(), o = i.height(), k = g(
										"#" + l.id).position().top
										+ g("#" + l.id).height();
								if (o < k) {
									i.scrollTop(m + k - o)
								}
								break
							}
							n += " ui-state-default plupload_file";
							g("#" + l.id).attr("class", n).find(".ui-icon")
									.attr("class", j)
						},
						_updateTotalProgress : function() {
							var i = this.uploader;
							this.progressbar.progressbar("value",
									i.total.percent);
							this.element.find(".plupload_total_status").html(
									i.total.percent + "%").end().find(
									".plupload_total_file_size").html(
									c.formatSize(i.total.size)).end().find(
									".plupload_upload_status").html(
									b("Uploaded %d/%d files").replace(
											"%d/%d",
											i.total.uploaded + "/"
													+ i.files.length))
						},
						_updateFileList : function() {
							var k = this, j = this.uploader, m = this.filelist, l = 0, o, n = this.id
									+ "_", i;
							if (g.ui.sortable && this.options.sortable) {
								g("tbody.ui-sortable", m).sortable("destroy")
							}
							m.empty();
							g
									.each(
											j.files,
											function(q, p) {
												i = "";
												o = n + l;
												if (p.status === c.DONE) {
													if (p.target_name) {
														i += '<input type="hidden" name="'
																+ o
																+ '_tmpname" value="'
																+ c
																		.xmlEncode(p.target_name)
																+ '" />'
													}
													i += '<input type="hidden" name="'
															+ o
															+ '_name" value="'
															+ c
																	.xmlEncode(p.name)
															+ '" />';
													i += '<input type="hidden" name="'
															+ o
															+ '_status" value="'
															+ (p.status === c.DONE ? "done"
																	: "failed")
															+ '" />';
													l++;
													k.counter.val(l)
												}
												m
														.append('<tr class="ui-state-default plupload_file" id="'
																+ p.id
																+ '"><td class="plupload_cell plupload_file_name"><span>'
																+ p.name
																+ '</span></td><td class="plupload_cell plupload_file_status">'
																+ p.percent
																+ '%</td><td class="plupload_cell plupload_file_size">'
																+ c
																		.formatSize(p.size)
																+ '</td><td class="plupload_cell plupload_file_action"><div class="ui-icon"></div>'
																+ i
																+ "</td></tr>");
												k._handleFileStatus(p);
												g(
														"#"
																+ p.id
																+ ".plupload_delete .ui-icon, #"
																+ p.id
																+ ".plupload_done .ui-icon")
														.click(
																function(r) {
																	g(
																			"#"
																					+ p.id)
																			.remove();
																	j
																			.removeFile(p);
																	r
																			.preventDefault()
																});
												k._trigger("updatelist", null,
														m)
											});
							if (j.total.queued === 0) {
								g(".ui-button-text", k.browse_button).html(
										b("Add Files"))
							} else {
								g(".ui-button-text", k.browse_button).html(
										b("%d files queued").replace("%d",
												j.total.queued))
							}
							if (j.files.length === (j.total.uploaded + j.total.failed)) {
								k.start_button.button("disable")
							} else {
								k.start_button.button("enable")
							}
							m[0].scrollTop = m[0].scrollHeight;
							k._updateTotalProgress();
							if (!j.files.length && j.features.dragdrop
									&& j.settings.dragdrop) {
								g("#" + o + "_filelist").append(
										'<tr><td class="plupload_droptext">'
												+ b("Drag files here.")
												+ "</td></tr>")
							} else {
								if (k.options.sortable && g.ui.sortable) {
									k._enableSortingList()
								}
							}
						},
						_enableRenaming : function() {
							var i = this;
							this.filelist
									.on(
											"click",
											".plupload_delete .plupload_file_name span",
											function(o) {
												var m = g(o.target), k, n, j, l = "";
												k = i.uploader.getFile(m
														.parents("tr")[0].id);
												j = k.name;
												n = /^(.+)(\.[^.]+)$/.exec(j);
												if (n) {
													j = n[1];
													l = n[2]
												}
												m
														.hide()
														.after(
																'<input class="plupload_file_rename" type="text" />');
												m
														.next()
														.val(j)
														.focus()
														.blur(
																function() {
																	m
																			.show()
																			.next()
																			.remove()
																})
														.keydown(
																function(q) {
																	var p = g(this);
																	if (g
																			.inArray(
																					q.keyCode,
																					[
																							13,
																							27 ]) !== -1) {
																		q
																				.preventDefault();
																		if (q.keyCode === 13) {
																			k.name = p
																					.val()
																					+ l;
																			m
																					.html(k.name)
																		}
																		p
																				.blur()
																	}
																})
											})
						},
						_enableDragAndDrop : function() {
							this.filelist
									.append('<tr><td class="plupload_droptext">'
											+ b("Drag files here.")
											+ "</td></tr>");
							this.filelist.parent().attr("id",
									this.id + "_dropbox");
							this.uploader.settings.drop_element = this.options.drop_element = this.id
									+ "_dropbox"
						},
						_enableSortingList : function() {
							var j, i = this;
							if (g("tbody tr", this.filelist).length < 2) {
								return
							}
							g("tbody", this.filelist)
									.sortable(
											{
												containment : "parent",
												items : ".plupload_delete",
												helper : function(l, k) {
													return k
															.clone(true)
															.find(
																	"td:not(.plupload_file_name)")
															.remove().end()
															.css("width",
																	"100%")
												},
												stop : function(p, o) {
													var l, n, k, m = [];
													g
															.each(
																	g(this)
																			.sortable(
																					"toArray"),
																	function(q,
																			r) {
																		m[m.length] = i.uploader
																				.getFile(r)
																	});
													m.unshift(m.length);
													m.unshift(0);
													Array.prototype.splice
															.apply(
																	i.uploader.files,
																	m)
												}
											})
						},
						notify : function(j, k) {
							var i = g('<div class="plupload_message"><span class="plupload_message_close ui-icon ui-icon-circle-close" title="'
									+ b("Close")
									+ '"></span><p><span class="ui-icon"></span>'
									+ k + "</p></div>");
							i.addClass(
									"ui-state-"
											+ (j === "error" ? "error"
													: "highlight")).find(
									"p .ui-icon")
									.addClass(
											"ui-icon-"
													+ (j === "error" ? "alert"
															: "info")).end()
									.find(".plupload_message_close").click(
											function() {
												i.remove()
											}).end();
							g(".plupload_header_content", this.container)
									.append(i)
						},
						destroy : function() {
							g(".plupload_button", this.element).unbind();
							if (g.ui.button) {
								g(
										".plupload_add, .plupload_start, .plupload_stop",
										this.container).button("destroy")
							}
							if (g.ui.progressbar) {
								this.progressbar.progressbar("destroy")
							}
							if (g.ui.sortable && this.options.sortable) {
								g("tbody", this.filelist).sortable("destroy")
							}
							this.uploader.destroy();
							this.element.empty().html(this.contents_bak);
							this.contents_bak = "";
							g.Widget.prototype.destroy.apply(this)
						}
					})
}(window, document, plupload, jQuery));
;
// File end:
// /var/deployments/www.raptor-editor.com.3/raptor-gold/raptor-dependencies/jquery.ui.plupload.js
