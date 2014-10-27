var historyIdx = -1;
var commandHistory = [];
var selectedFiles = [];
var isGuiView = false;

function setUpdateInterval(){
    var interval_id = setInterval(function(){
            updateStatus(interval_id);
        }, 1000);
//    if (timerObject.interval_id === undefined) {
//        timerObject.interval_id = setInterval(function(){
//            updateStatus();
//        }, 1000);
//    }
}

function getUserInfo(){
    var userUrl = "jython/user?type=INFO";
    $.get(userUrl,function(data,status){
		if (status == "success") {
            if (data['result'] == "OK") {
                $('#div_user_header').html('<a title="Sign out ' + data['email'] + '" id="a_sign_out" href="login.html?action=signout">Sign Out</a>');
            }
		}
    });
}

var jythonUrl = "jython/runner/res";

function getBool(value){
    if (value){
        return 'True';
    } else {
        return 'False';
    }
}

function run_image_hover(item) {
    item.setAttribute("src", "images/go_button.png");
    item.style.border = "#0a00ff 1px solid";
}

function run_image_unhover(item) {
    item.setAttribute("src", "images/go_button_grey.png");
    item.style.border = "";
}

//function htmlEntities(text) {
//    var escaped = text.replace(/\]\]>/g, ']]' + '>]]&gt;<' + '![CDATA[');
//    return '<' + '![CDATA[' + escaped + ']]' + '>';
//}
function htmlEntities(s) { 
    return s.replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
}
function updateConsole(data){
    if (data['text'] != null && data['text'].trim().length > 0){
        $('#console').append('<pre class="consoleText">' + htmlEntities(data['text']) + '</pre>');
    }
    if (data['error'] != null && data['error'].trim().length > 0){
        $('#console').append('<pre class="consoleError">' + htmlEntities(data['error']) + '</pre>');
    }
}
//function updateConsole(data){
//    if (data.text != null && data.text.trim().length > 0){
//        $('#console').append(document.createTextNode(data.text));
//    }
//    alert(htmlentities(data.text));
//    if (data.error != null && data.error.trim().length > 0){
//        $('#console').append(document.createTextNode(data.error));
//    }
//}

function listProp(obj){
    var text = "";
    $.each( obj, function(i, n){
        text += i + ":" + n + "\n";
    });
    return text;
}

function updateRunScriptText() {
    if (isGuiView) {
        $("#run_script").val("Process Data");
    } else {
        $("#run_script").val("Run Script");
    }
}

function enableWidgets(){
    $("#runner_status").css("background-color", "green");
//    $("#run_script").removeAttr("disabled");
    updateRunScriptText();
    $('#lineFeed').removeAttr("disabled");
}

function disableWidgets(){
    $("#runner_status").css("background-color", "orange");
//    $("#run_script").attr("disabled", true);
    $("#run_script").val("Interrupt");
    $("#lineFeed").attr("disabled", true);
}

function setErrorWidgets(){
    $("#runner_status").css("background-color", "red");
//    $("#run_script").removeAttr("disabled");
    updateRunScriptText();
    $('#lineFeed').removeAttr("disabled");
}

function updateStatus(interval_id){
    var getUrl = jythonUrl + "?type=STATUS&timestamp=" + new Date().getTime();
    $.get(getUrl,function(data,status){
		if (status == "success") {
            processStatus(data, interval_id);
		}
    });
}

function updatePlot(id) {
	var imgUrl = jythonUrl + "?type=PLOT&id=" + id + "&timestamp=" + new Date().getTime();
	$("#plot_image" + id).attr("src", imgUrl);
}

function processStatus(data, interval_id) {
    var rStatus = data['status'];
    $('#runner_status').html(rStatus);
    updateConsole(data);
    $('#console').scrollTop($('#console')[0].scrollHeight);
    if (data['status'] == "IDLE"){
        enableWidgets();
        clearInterval(interval_id);
    } else if (data['status'] == "BUSY"){
        disableWidgets();
    } else if (data['status'] == "ERROR"){
        setErrorWidgets();
        clearInterval(interval_id);
    }
    if (data['plot1']) {
    	updatePlot(1);
    }
    if (data['plot2']) {
    	updatePlot(2);
    }
    if (data['plot3']) {
    	updatePlot(3);
    }
    if (data['js']){
        try{
            eval(data['js']);
        }catch (e) {
            alert(data['js']);
        }
    }
    if (data['files']){
        var files = data['files'].split(';');
        $.each( files, function( idx, file ) {
            if (file != null && file.trim().length > 0){
                downloadFile(file);
            }
        });
    }
}

function downloadFile(file){
    var pair = file.split(':');
    var getUrl = "jython/rest/" + pair[1] + "?type=FILE&folder=" + pair[0];
    window.location.href = getUrl;
}

function initUpdateStatus(){
    var getUrl = jythonUrl + "?type=STATUS";
    $.get(getUrl,function(data,status){
		if (status == "success") {
            processStatus(data);
            if (data['status'] == "BUSY"){
                setUpdateInterval();
            }
		}
    });
}

function runScript(){
    if (isGuiView) {
        sendJython("__run_script__(__selected_files__)");
    } else {
        var postUrl = jythonUrl + "?type=START";
        $.post( postUrl, $("form#script_form").serialize(), function(data, status) {
            if (status == "success") {
                processStatus(data);
                if (data['status'] == "BUSY"){
                    setUpdateInterval();
                }
            }
        })
        .fail(function(e) {
            alert( "error submitting the script");
        });
    }
}

function interruptScript(){
    var getUrl = jythonUrl + "?type=INTERRUPT";
    $.get(getUrl, function(data, status) {
        if (status == "success") {
            processStatus(data);
        }
    })
    .fail(function(e) {
            alert( "error interrupting the script");
    });
}

function sendJython(cmd){
    $('#console').append('<div class="consoleText"> >> ' + cmd + '</div>');
    $('#console').scrollTop($('#console')[0].scrollHeight);
    var postUrl = jythonUrl + "?type=START";
    $.post( postUrl, { script_text: cmd, script_input: "textInput" }, function(data, status) {
                if (status == "success") {
                    processStatus(data);
                    if (data['status'] == "BUSY"){
                        setUpdateInterval();
                    }
                }
            })
            .fail(function(e) {
                alert( "error submitting the command");
            });
}

function selectScript(){
    var name = $("#script_select").val();
    if (name) {
        var getUrl = jythonUrl + "?type=SCRIPT&name=" + name;
        $.get(getUrl, function(data, status) {
            if (status == "success") {
                $("#jython_file").val("");
                $("#script_text").val(data);
                createGui();
            }
        })
        .fail(function(e) {
                alert( "error loading the script");
        });
    } else {
        $("#script_text").val("");
    }
}

function changeOptions(obj, options){
    obj.empty();
    $.each(options, function(idx, value) {
      obj.append($("<option></option>")
         .attr("value", value).text(value));
    });
}

$.fn.selectRange = function(start, end) {
    return this.each(function() {
        if (this.setSelectionRange) {
            this.focus();
            this.setSelectionRange(start, end);
        } else if (this.createTextRange) {
            var range = this.createTextRange();
            range.collapse(true);
            range.moveEnd('character', end);
            range.moveStart('character', start);
            range.select();
        }
    });
};

$.fn.setSelection = function(selectionStart, selectionEnd) {
    if(this.length == 0) return this;
    input = this[0];

    if (input.createTextRange) {
        var range = input.createTextRange();
        range.collapse(true);
        range.moveEnd('character', selectionEnd);
        range.moveStart('character', selectionStart);
        range.select();
    } else if (input.setSelectionRange) {
        input.focus();
        input.setSelectionRange(selectionStart, selectionEnd);
    }

    return this;
};

$.fn.setCursorPosition = function(position){
    if(this.length == 0) return this;
    return $(this).setSelection(position, position);
};

function removeClassName (elem, className) {
	elem.className = elem.className.replace(className, "").trim();
}

function addCSSClass (elem, className) {
	removeClassName (elem, className);
	elem.className = (elem.className + " " + className).trim();
}

String.prototype.trim = function() {
	return this.replace( /^\s+|\s+$/, "" );
}

function stripedTable() {
	if (document.getElementById && document.getElementsByTagName) {  
		var allTables = document.getElementsByTagName('table');
		if (!allTables) { return; }

		for (var i = 0; i < allTables.length; i++) {
			if (allTables[i].className.match(/[\w\s ]*scrollTable[\w\s ]*/)) {
				var trs = allTables[i].getElementsByTagName("tr");
				for (var j = 0; j < trs.length; j++) {
					removeClassName(trs[j], 'alternateRow');
					addCSSClass(trs[j], 'normalRow');
				}
				for (var k = 0; k < trs.length; k += 2) {
					removeClassName(trs[k], 'normalRow');
					addCSSClass(trs[k], 'alternateRow');
				}
			}
		}
	}
}

function getFileList(){
    var getUrl = jythonUrl + "?type=FILENAMES";
    $.get(getUrl, function(data, status) {
        if (status == "success") {
            $("#table_datafiles > tbody").html(data);
        }
    })
    .fail(function(e) {
            alert( "error loading data files");
    });
}

function getScriptList(){
    var getUrl = jythonUrl + "?type=LISTSCRIPTS";
    $.get(getUrl, function(data, status) {
        if (status == "success") {
            var files = data.split(";");
            $.each(files, function(idx, file) {
                if (file){
                    var o = new Option(file, file);
                    $(o).html(file);
                    $("#script_select").append(o);
                }
            });
        }
    })
    .fail(function(e) {
            alert( "error loading available scripts");
    });
}

$(window).on('hashchange',function(){ 
    window.scrollTo(0,0);
});

var prev = -1;
    
$(function() {
 
    function updateSelectedList( $selectees ) {

        selected = $.makeArray( $selectees.filter( ".ui-selected" ) );

        selectedFiles = [];
        $.each(selected, function( index, value ) {
//            var cmd = [];
//            for(var key in value.childNodes[0]) {
//                cmd.push(key + ":" + value[key] + "\n");
//            }
//            $('#script_text').text(cmd);
//            $('#script_text').text(value.firstChild.innerHTML);
            selectedFiles.push("'" + value.firstChild.firstChild.innerHTML + "'");
        });
    }

    $( "table > tbody" ).selectable({

        // Don't allow individual table cell selection.
        filter: ":not(td, div)",

        // Update the initial total to 0, since nothing is selected yet.
        create: function( e, ui ) {
            updateSelectedList( $() );
        },

        // When a row is selected, add the highlight class to the row and
        // update the total.
        selected: function( e, ui ) {
            var curr = $(ui.selected.tagName, e.target).index(ui.selected); // get selecting item index
            if(e.shiftKey && prev > -1) { // if shift key was pressed and there is previous - select them all
                $(ui.selected.tagName, e.target).slice(Math.min(prev, curr), 1 + Math.max(prev, curr)).addClass('ui-selected ui-state-highlight-customised');
                prev = -1; // and reset prev
            } else {
                prev = curr; // othervise just save prev
                $(ui.selected).addClass( "ui-state-highlight-customised" );
            }
            var widget = $( this ).data( "uiSelectable" );
            updateSelectedList( widget.selectees );
        },

        stop: function( e, ui ) {
            sendJython('__set_selected_files__([' + selectedFiles + '])');
        },
        
        distance: 0,
        
        // When a row is unselected, remove the highlight class from the row.
        unselected: function( e, ui ) {
            $(ui.unselected).removeClass( "ui-state-highlight-customised" );
            var widget = $( this ).data( "uiSelectable" );
            updateSelectedList( widget.selectees );
        }
    });

    $("#table_datafiles").dblclick(function(){
        sendJython("__run_script__(__selected_files__)");
    });
});

function createGui(){
    var postUrl = jythonUrl + "?type=GUI";
    $.post( postUrl, $("form#script_form").serialize(), function(data, status) {
        if (status == "success") {
            $("#div_script_gui").html(data['html']);
            processStatus(data);
            if (data['status'] == "BUSY"){
                    setUpdateInterval();
            }
            if (data['error'] == null || data['error'].trim().length == 0){
                $("#tab2").click();
                window.location = $('#tab2').attr('href');
            }
        }
    })
    .fail(function(e) {
        alert( "error submitting the script");
    });
}

jQuery(document).ready(function(){
	$(document).attr("title", title + " - Jython Runner");
	$('#titleString').text(title + " - Jython Runner");

    stripedTable();
	$("#run_script").click(function() {
        if ($('#runner_status').html() == "BUSY"){
            interruptScript();
        } else {
            runScript();
        }
	});
    
    $("#create_gui").click(function() {
        createGui();
	});
    
    $("#button_download").click(function() {
        sendJython('download_selected_files()');
    });
    
    $("#button_reload_data_file").click(function() {
        getFileList();
    });
    
    $("#tab1").click(function(){
        $("#tab1").removeClass("tabUnselected");
        $("#tab1").addClass("tabSelected");
        $("#tab2").removeClass("tabSelected");
        $("#tab2").addClass("tabUnselected");
        $("#create_gui").val("Create GUI");
        isGuiView = false;
        updateRunScriptText();
    });

    $("#tab2").click(function(){
        $("#tab1").removeClass("tabSelected");
        $("#tab1").addClass("tabUnselected");
        $("#tab2").removeClass("tabUnselected");
        $("#tab2").addClass("tabSelected");
        $("#create_gui").val("Reload GUI");
        isGuiView = true;
        updateRunScriptText();
    });

    if (navigator.userAgent.indexOf('MSIE') !== -1 || navigator.appVersion.indexOf('Trident/') > 0) {
        $("#jython_file").change(function() {
            if ($("#jython_file").val()) {
                var postUrl = jythonUrl + "?type=READSCRIPT";
                var formData = new FormData($('form#file_form')[0]);
                $.ajax({
                    url: postUrl,  //Server script to process data
                    type: 'POST',
                    //Ajax events
        //            xhr: function() {  // Custom XMLHttpRequest
        //                var myXhr = $.ajaxSettings.xhr();
        //                if(myXhr.upload){ // Check if upload property exists
        //                    myXhr.upload.addEventListener('progress',progressHandlingFunction, false); // For handling the progress of the upload
        //                }
        //                return myXhr;
        //            },
        //            beforeSend: beforeSendHandler,
                    success: function(data){
                                alert("load");
                                $("#script_select").val("");
                                $('#script_text').val(data);
                            },
                    error:  function(e) {
                                alert( "error uploading script");
                            },
                    // Form data
                    data: formData,
                    //Options to tell jQuery not to process data or worry about content-type.
                    cache: false,
                    contentType: false,
                    processData: false
                });
            }
        });
    } else {
        $("#jython_file").on("change", function(event) {
            if ($("#jython_file").val()) {
                var postUrl = jythonUrl + "?type=READSCRIPT";
                var formData = new FormData($('form#file_form')[0]);
                $.ajax({
                    url: postUrl,  //Server script to process data
                    type: 'POST',
                    //Ajax events
        //            xhr: function() {  // Custom XMLHttpRequest
        //                var myXhr = $.ajaxSettings.xhr();
        //                if(myXhr.upload){ // Check if upload property exists
        //                    myXhr.upload.addEventListener('progress',progressHandlingFunction, false); // For handling the progress of the upload
        //                }
        //                return myXhr;
        //            },
        //            beforeSend: beforeSendHandler,
                    success: function(data){
                                $("#script_select").val("");
                                $('#script_text').val(data);
                            },
                    error:  function(e) {
                                alert( "error uploading script");
                            },
                    // Form data
                    data: formData,
                    //Options to tell jQuery not to process data or worry about content-type.
                    cache: false,
                    contentType: false,
                    processData: false
                });
            }
        });
    }
    
    $(document).delegate('#script_text', 'keydown', function(e) {
        var keyCode = e.keyCode || e.which;

        if (keyCode == 9) {
            e.preventDefault();
            var start = $(this).get(0).selectionStart;
            var end = $(this).get(0).selectionEnd;

            // set textarea value to: text before caret + tab + text after caret
            $(this).val($(this).val().substring(0, start)
                        + "\t"
                        + $(this).val().substring(end));

            // put caret at right position again
            $(this).get(0).selectionStart =
            $(this).get(0).selectionEnd = start + 1;
        }
    });
    
    $(document).delegate('#lineFeed', 'keydown', function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode == 13) {
            e.preventDefault();
            commandHistory.push($('#lineFeed').val());
            historyIdx = commandHistory.length;
            $('#console').append('<div class="consoleText"> >> ' + $('#lineFeed').val() + '</div>');
            $('#console').scrollTop($('#console')[0].scrollHeight);
            var postUrl = jythonUrl + "?type=START";
            $.post( postUrl, { script_text: $('#lineFeed').val(), script_input: "textInput" }, function(data, status) {
                if (status == "success") {
                    processStatus(data);
                    if (data['status'] == "BUSY"){
                        setUpdateInterval();
                    }
                }
            })
            .fail(function(e) {
                alert( "error submitting the command");
            });
            $('#lineFeed').val('');
        } else if (keyCode == 38) {
            if (historyIdx > 0) {
                historyIdx--;
                if (historyIdx >= 0 && historyIdx < commandHistory.length){
                    $('#lineFeed').val(commandHistory[historyIdx]);
                    var strLength = $('#lineFeed').val().length;
                    $('#lineFeed').selectRange(strLength, strLength);
                    $('#lineFeed').setCursorPosition(strLength);
                    $('#lineFeed')[0].setSelectionRange(strLength, strLength);
                }
            }
        } else if (keyCode == 40) {
            if (historyIdx < commandHistory.length - 1){
                historyIdx++;
                if (historyIdx >= 0 && historyIdx < commandHistory.length){
                    $('#lineFeed').val(commandHistory[historyIdx]);
                    var strLength = $('#lineFeed').val().length;
                    $('#lineFeed')[0].setSelectionRange(strLength, strLength);
                }
            } else if (historyIdx < commandHistory.length){
                historyIdx++;
                $('#lineFeed').val('');
            }
        }
    });
    
    initUpdateStatus();
    $("#tab1").click();
    window.location = $('#tab1').attr('href');
    
    getScriptList();
    getFileList();
    
    getUserInfo();
    
});

