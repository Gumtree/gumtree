var LAST_MODIFIED_PROPOSALS;
var SORTED_PROPOSALS;
var HIGHLIGHTED_PROPOSAL_CLASS = "class_proposal_highlighted";
var URL_PAGE_NAME = "catalogAdmin.html";

function startCheckNewFile() {
}

function loadProposal(pid) {
	var titleText = "Data Catalog - " + title;
	var getUrl = "catalog/read?proposal=" + pid + "&" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			var re = data["status"];
			if (re == "OK") {
	        	$("#id_table_catalog > thead").html(makeTableHeader(COLUMN_NAMES));
	        	$("#id_table_catalog > tbody").empty();
	        	$("#id_table_catalog > tbody").html(makeTableBody(COLUMN_NAMES, data["body"]));
	        	$('#titleString').text(titleText + ' Proposal ' + data["proposal"]);
	        	TABLE_SIZE = data["size"];
	        	LOADED_PROPOSALID = pid;
	        	$('.class_column_Comments').mousedown(function(e) {
					$(this).children('input:first').show();
					$(this).children('span:first').hide();
					$(this).children('input:first').focus();
				});
				$('.class_input_comments').keypress(function(e) {
					if(e.which === 13) {
						var span = $(this).parent().children('span:first');
						if (span.text().trim() != $(this).val().trim()) {
							updateEntry(LOADED_PROPOSALID, $(this).closest('tr').children('th:first').text(), 'Comments', $(this).val(), $(this));
						} else {
							$(this).hide();
							span.show();
						}
					}
					if(e.keyCode === 27) {
						$(this).hide();
						var span = $(this).parent().children('span:first');
						$(this).val(span.text());
						span.show();
					}
				});
				$('.class_input_comments').blur(function(e) {
					var span = $(this).parent().children('span:first');
					if (span.text().trim() != $(this).val().trim()) {
						updateEntry(LOADED_PROPOSALID, $(this).closest('tr').children('th:first').text(), 'Comments', $(this).val(), $(this));
					} else {
						$(this).hide();
						span.show();
					}
				});
			}
		}
	}).fail(function(e) {
		if (e.status == 401) {
			alert("Your session has timed out. Please sign in again.");
		}
	}).always(function() {
	});

	$('.class_a_proposals').removeClass(HIGHLIGHTED_PROPOSAL_CLASS);
	$("#id_a_proposal" + pid + "> span").addClass(HIGHLIGHTED_PROPOSAL_CLASS);
}

jQuery(document).ready(function(){
	var titleText = "Manage Data Catalog - " + title;
	$(document).attr("title", titleText);
	$('#titleString').text(titleText);
	
	var getUrl = "catalog/list?" + (new Date()).getTime();
	$.get(getUrl, function(data, status) {
		if (status == "success") {
			var re = data["status"];
			if (re == "OK") {

				LAST_MODIFIED_PROPOSALS = data["lastModified"];
	        	for ( var i = 0; i < LAST_MODIFIED_PROPOSALS.length; i++) {
		        	$("#id_ul_sortTimestamp").append('<li><a id="id_a_proposal' + LAST_MODIFIED_PROPOSALS[i] 
		        			+ '" onclick="loadProposal(\'' + LAST_MODIFIED_PROPOSALS[i] 
		        			+ '\')"' + '>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="class_a_proposals">Proposal - ' 
		        			+ LAST_MODIFIED_PROPOSALS[i] + '</span></a></li>');
				}

	        	SORTED_PROPOSALS = data["valueOrdered"];
	        	for ( var i = 0; i < SORTED_PROPOSALS.length; i++) {
		        	$("#id_ul_sortProposal").append('<li><a id="id_a_proposal' + SORTED_PROPOSALS[i] 
		        			+ '" onclick="loadProposal(\'' + SORTED_PROPOSALS[i] 
		        			+ '\')"' + '>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="class_a_proposals">Proposal - ' 
		        			+ SORTED_PROPOSALS[i] + '</span></a></li>');
				}
	        	
	        	$("#id_a_current").text("Load Current Proposal - " + data["currentProposal"]);
			}
		}
	}).fail(function(e) {
	}).always(function() {
	});
	
	$('#id_a_current').on('click', function(e) {
		loadProposal(CURRENT_PROPOSALID);
	});
});