var historyIdx = -1;
var commandHistory = [];

var timerObject = {
		interval_id : null
};

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
    if (data.text != null && data.text.trim().length > 0){
        $('#console').append('<div class="consoleText">' + htmlEntities(data.text) + '</div>');
    }
    if (data.error != null && data.error.trim().length > 0){
        $('#console').append('<div class="consoleError">' + htmlEntities(data.error) + '</div>');
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

function updateStatus(){
    var getUrl = "jython/rest/res?type=STATUS";
    $.get(getUrl,function(data,status){
		if (status == "success") {
			var rStatus = data.status;
            $('#runner_status').html(rStatus);
            updateConsole(data);
            $('#console').scrollTop($('#console')[0].scrollHeight);
            if (data.status == "IDLE"){
                $("#runner_status").css("background-color", "green");
                $("#run_script").removeAttr("disabled");
                clearInterval(timerObject.interval_id);
            } else if (data.status == "BUSY"){
                $("#runner_status").css("background-color", "orange");
                $("#run_script").attr("disabled", true);
            } else if (data.status == "ERROR"){
                $("#runner_status").css("background-color", "red");
                $("#run_script").removeAttr("disabled");
            }
		}
    });
}

function runScript(){
    var postUrl = "jython/rest/res?type=START";
    $.post( postUrl, $("form#script_form").serialize(), function(data, status) {
            var rStatus = data.status;
            $('#runner_status').html(rStatus);
            updateConsole(data);
            $('#console').scrollTop($('#console')[0].scrollHeight);
            
            if (data.status == "BUSY"){
                $("#runner_status").css("background-color", "orange");
                $("#run_script").attr("disabled", true);
                timerObject.interval_id = setInterval(function(){
		          updateStatus();
	           }, 1000);
            } else if (data.status == "IDLE"){
                $("#runner_status").css("background-color", "green");
                clearInterval(timerObject.interval_id);
                $("#run_script").removeAttr("disabled");
            } else if (data.status == "ERROR"){
                $("#runner_status").css("background-color", "red");
                clearInterval(timerObject.interval_id);
                $("#run_script").removeAttr("disabled");
            }
        })
        .fail(function(e) {
            alert( "error submitting the script");
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

jQuery(document).ready(function(){
	$(document).attr("title", title + " - Jython Runner");
	$('#titleString').text(title + " - Jython Runner");

	$("#run_script").click(function() {
		runScript();
	});
    
    $("#jython_file").on("change", function(event) {
        var postUrl = "jython/rest/res?type=READSCRIPT";
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
    });
    
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
            var postUrl = "jython/rest/res?type=START";
            $.post( postUrl, { script_text: $('#lineFeed').val(), script_input: "textInput" }, function(data, status) {
                var rStatus = data.status;
                $('#runner_status').html(rStatus);
                updateConsole(data);
                $('#console').scrollTop($('#console')[0].scrollHeight);

                if (data.status == "BUSY"){
                    $("#runner_status").css("background-color", "orange");
                    $("#run_script").attr("disabled", true);
                    timerObject.interval_id = setInterval(function(){
                      updateStatus();
                   }, 1000);
                } else if (data.status == "IDLE"){
                    $("#runner_status").css("background-color", "green");
                    clearInterval(timerObject.interval_id);
                    $("#run_script").removeAttr("disabled");
                } else if (data.status == "ERROR"){
                    $("#runner_status").css("background-color", "red");
                    clearInterval(timerObject.interval_id);
                    $("#run_script").removeAttr("disabled");
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
});

