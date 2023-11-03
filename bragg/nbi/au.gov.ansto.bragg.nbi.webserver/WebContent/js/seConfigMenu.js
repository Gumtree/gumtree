var INST_LIST = [
	"bilby",
	"dingo",
	"echidna",
	"emu",
	"koala",
	"kookaburra",
	"kowari",
	"pelican",
	"platypus",
	"quokka",
	"spatz",
	"taipan",
	"wombat",
//	"joey"
]

$(document).ready(function() {
	$('#id_a_signout').click(function() {
		signout("seConfigMenu.html");
	});
	
	var mdiv = $('#id_div_main_area');
	$.each(INST_LIST, function(idx, val) {
		mdiv.append('<div class="class_div_instrument_option"><a id="id_button_instrument" ' + 
				'class="btn btn-outline-primary btn-block class_button_instrument" ' + 
				'href="seConfig.html?inst=' + val + '">' + val.toUpperCase() + ' sample environment configuration</a></div>');
	});
	
});