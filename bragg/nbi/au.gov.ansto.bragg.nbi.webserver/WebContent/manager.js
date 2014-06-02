var evEnabled = false;

function toPtg(val, numDigit) {
	return String((Math.round(Number(val) * 1000) / 10).toFixed(numDigit)) + "%";
}
function labelFormatter(label, series) {
	return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;'>" + label + "<br/>" + (Math.round(series.percent * 10) / 10).toFixed(1) + "%</div>";
}
function padStr(i) {
    return (i < 10) ? "0" + i : "" + i;
}
var refresh = function(){
	var dateValues = $("#div-slider").dateRangeSlider("values");
	var start = dateValues.min;
	var end = dateValues.max;
	if (end < start) {
		end = start;
	}
	var startString = String(start.getFullYear()) + "-" + padStr(1+start.getMonth()) + "-" + padStr(start.getDate());
	var endString = String(end.getFullYear()) + "-" + padStr(1+end.getMonth()) + "-" + padStr(end.getDate());
	var url = "st/rest/status?from=" + startString + "&to=" + endString;
	$.get(url,function(data,status){
		if (status == "success") {
			var obj = jQuery.parseJSON(data);
			if (!evEnabled) {
				var data = [];
				jQuery.each(obj, function(name, val) {
					if (name.indexOf("STATUS") == 0) {
						name = name.substring(7);
						var text = toPtg(val, 1);
						$("#stList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">' + name + ': </div> <div class="div-inlist" id="' + name + '">' + text + '</div></li>');
						data.push({label:name, data:val});
					} 
				});
				if (obj.TOTAL != null) {
					$("#stList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">TOTAL TIME: </div> <div class="div-inlist" id="TOTAL">' + obj.TOTAL + '</div></li>');
				}
				$("#stList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-piechart" id="statusChart"></div></li>');
				try {
					$.plot('#statusChart', data, {
					    series: {
					        pie: {
					            show: true,
					            radius: 1,
					            label: {
					                show: true,
					                radius: 3/4,
					                formatter: labelFormatter,
					                background: {
					                    opacity: 0.5
					                }
					            }
					        }
					    },
					    legend: {
					        show: false
					    }
					});
				} catch (e) {
				}
				if (obj.SECONDARY != null || obj.TERTIARY != null) {
					$("#stList").append('<li class="ui-li ui-li-divider ui-bar-d ui-first-child" role="heading" data-role="list-divider">SHUTTER OPEN PERCENTAGE</li>');
					if (obj.SECONDARY != null) {
						$("#stList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">SECONDARY: </div> <div class="div-inlist" id="SECONDARY">' + toPtg(obj.SECONDARY, 1) + ' of ' + obj.TOTAL + '</div></li>');
					}
					if (obj.TERTIARY != null) {
						$("#stList").append('<li class="ui-li ui-li-static ui-btn-up-c"><div class="div-inlist-left">TERTIARY: </div> <div class="div-inlist" id="TERTIARY">' + toPtg(obj.TERTIARY, 1) + ' of ' + obj.TOTAL + '</div></li>');
					}
					$("#stList").append('<li class="ui-li ui-li-static ui-btn-up-c"><table id="shutterChartTable"><tbody><tr><td><div class="div-piechart" ' +
							'id="secondaryChart"></div></td><td><div class="div-piechart" id="tertiaryChart"></div></td></tr>' + 
							'<tr><td align="center">SECONDARY SHUTTER</td><td align="center">TERTIARY SHUTTER</td></tr></tbody></table></li>');
					if (obj.SECONDARY != null) {
						var open = parseFloat(obj.SECONDARY);
						data = [{label:"OPEN", data:open}, {label:"CLOSE", data:1-open}];
						try {
							$.plot('#secondaryChart', data, {
								series: {
									pie: {
										show: true,
										radius: 1,
										label: {
											show: true,
											radius: 3/4,
											formatter: labelFormatter,
											threshold: 0.1,
											background: {
												opacity: 0.5
											}
										}
									}
								},
								legend: {
									show: false
								}
							});
						} catch (e) {
							alert(e);
						}
					}
					if (obj.TERTIARY != null) {
						var open = parseFloat(obj.TERTIARY);
						data = [{label:"OPEN", data:open}, {label:"CLOSE", data:1-open}];
						try {
							$.plot('#tertiaryChart', data, {
								series: {
									pie: {
										show: true,
										radius: 1,
										label: {
											show: true,
											radius: 3/4,
											formatter: labelFormatter,
											threshold: 0.1,
											background: {
												opacity: 0.5
											}
										}
									}
								},
								legend: {
									show: false
								}
							});
						} catch (e) {
							alert(e);
						}
					}
				}
				evEnabled = true;
			} else {
				var data = [];
				jQuery.each(obj, function(name, val) {
					if (name.indexOf("STATUS") == 0) {
						name = name.substring(7);
						var text = toPtg(val, 1);
						$("#" + name).text(text);
						data.push({label:name, data:val});
					} 
				});
				if (obj.TOTAL != null) {
					$("#TOTAL").text(obj.TOTAL);
				}
				try {
					$("#statusChart").unbind();
					$.plot("#statusChart", data, {
					    series: {
					        pie: {
					            show: true,
					            radius: 1,
					            label: {
					                show: true,
					                radius: 3/4,
					                formatter: labelFormatter,
					                background: {
					                    opacity: 0.5
					                }
					            }
					        }
					    },
					    legend: {
					        show: false
					    }
					});
				} catch (e) {
					alert(e);
				}
				if (obj.SECONDARY != null) {
					$("#SECONDARY").text(toPtg(obj.SECONDARY, 1) + " of " + obj.TOTAL);
					var open = parseFloat(obj.SECONDARY);
					data = [{label:"OPEN", data:open}, {label:"CLOSE", data:1-open}];
					$("#SECONDARY").unbind();
					try {
						$.plot('#secondaryChart', data, {
							series: {
								pie: {
									show: true,
									radius: 1,
									label: {
										show: true,
										radius: 3/4,
										formatter: labelFormatter,
										threshold: 0.1,
										background: {
											opacity: 0.5
										}
									}
								}
							},
							legend: {
								show: false
							}
						});
					} catch (e) {
						alert(e);
					}
				}
				if (obj.TERTIARY != null) {
					$("#TERTIARY").text(toPtg(obj.TERTIARY, 1) + " of " + obj.TOTAL);
					var open = parseFloat(obj.TERTIARY);
					data = [{label:"OPEN", data:open}, {label:"CLOSE", data:1-open}];
					$("#TERTIARY").unbind();
					try {
						$.plot('#tertiaryChart', data, {
							series: {
								pie: {
									show: true,
									radius: 1,
									label: {
										show: true,
										radius: 3/4,
										formatter: labelFormatter,
										threshold: 0.1,
										background: {
											opacity: 0.5
										}
									}
								}
							},
							legend: {
								show: false
							}
						});
					} catch (e) {
						alert(e);
					}
				}
			}
		}
	});
};

jQuery(document).ready(function(){
	$(document).attr("title", title + " - Statistical Report");
	$('#titleString').text(title + " - Statistical Report");

	$("#getDom").click(function() {
		refresh();
	});
	var startUrl = "st/rest/getStart";
	$.get(startUrl, function(data,status){
		if (status == "success") {
			try {
				var ds = jQuery.parseJSON(data).start;
				var startDate = new Date(ds.substring(0,4), ds.substring(5,7) - 1, ds.substring(8,10));
				var endDate = new Date();
				$("#div-slider").dateRangeSlider({
					bounds:{
						min: startDate,
						max: endDate
					},
					defaultValues:{
						min: startDate,
						max: endDate
					}
				});
				refresh();
			} catch (e) {
			}
		}
	});
});

