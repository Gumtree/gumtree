$(function(){

	$(window).resize(function() {
	    var bodyheight = $(window).height();
		$(".slide-out-div").height(bodyheight - 80);
		$(".div_sidebar_inner").height(bodyheight - 80);
	});
	
    $('.slide-out-div').tabSlideOut({
        tabHandle: '.id_sidebar_handle',                     //class of the element that will become your tab
//        pathToTabImage: 'images/contact_tab.gif', //path to the image for the tab //Optionally can be set using css
        imageHeight: '122px',                     //height of tab image           //Optionally can be set using css
        imageWidth: '40px',                       //width of tab image            //Optionally can be set using css
        tabLocation: 'right',                      //side of screen where tab lives, top, right, bottom, or left
        speed: 300,                               //speed of animation
        action: 'click',                          //options: 'click' or 'hover', action to trigger animation
        topPos: '80px',                          //position from the top/ use if tabLocation is left or right
        leftPos: '20px',                          //position from left/ use if tabLocation is bottom or top
        fixedPosition: true,                      //options: true makes it stick(fixed position) on scroll
        onSlideOut: function() {
			$('.div_shiftable').css({ marginLeft: "20px" });
			$('.class_editable_page').css({ marginLeft: "20px" });
		},
        onSlideIn: function() {
        	$('.div_shiftable').css({ marginLeft: "auto" });
        	$('.class_editable_page').css({ margin: "0px auto" });
		}
    });
    
//            if (self.location.href == top.location.href){
//                $("body").css({font:"normal 13px/16px 'trebuchet MS', verdana, sans-serif"});
//                var logo=$("<a href='http://pupunzi.com'><img id='logo' border='0' src='http://pupunzi.com/images/logo.png' alt='mb.ideas.repository' style='display:none;'></a>").css({position:"absolute"});
//                $("body").prepend(logo);
//                $("#logo").fadeIn();
//            }
//	try {
//        $("#extruderLeft").buildMbExtruder({
//            position:"right",
//            width:600,
//            extruderOpacity:.8,
//            hidePanelsOnClose:true,
//            closeOnExternalClick:false,
//            closeOnClick:false,
//            accordionPanels:true,
//            onExtOpen:function(){},
//            onExtContentLoad:function(){},
//            onExtClose:function(){}
//        });

		
//        $("#extruderLeft1").buildMbExtruder({
//            position:"left",
//            width:300,
//            extruderOpacity:.8,
//            onExtOpen:function(){},
//            onExtContentLoad:function(){},
//            onExtClose:function(){}
//        });

//	} catch (e) {
//		alert(e.getMessage());
//		// TODO: handle exception
//	}

            /*
             $("#extruderLeft").buildMbExtruder({
             position:"left",
             width:300,
             extruderOpacity:.8,
             hidePanelsOnClose:false,
             accordionPanels:false,
             onExtOpen:function(){},
             onExtContentLoad:function(){$("#extruderLeft").openPanel();},
             onExtClose:function(){}
             });
             */

//            $("#extruderLeft2").buildMbExtruder({
//                position:"top",
//                width:300,
//                positionFixed:false,
//                top:0,
//                extruderOpacity:.8,
//                onExtOpen:function(){},
//                onExtContentLoad:function(){},
//                onExtClose:function(){}
//            });
});
            
jQuery(document).ready(function(){

	var getUrl = "notebook/load";
	$.get(getUrl, function(data, status) {
		if (status == "success") {
//			$('#id_editable_page').html(decodeURIComponent(data.replace(/\+/g, ' ')));
			$('#id_editable_page').html(data);
//			alert(decodeURIComponent(data));
			jQuery(function($) {
				$('.class_editable_page').raptor({
					"plugins": {
						"classMenu": {
							"classes": {
								"Blue background": "cms-blue-bg",
								"Round corners": "cms-round-corners",
								"Indent and center": "cms-indent-center"
							}
						},
						"dockToScreen": false,
						"dockToElement": false,
						"dock": {
							"docked": true
						},
						"languageMenu": false,
						"logo": false,
						// The save UI plugin/button
						"save": {
							// Specifies the UI to call the saveRest plugin to do the actual saving
							plugin: 'saveRest'
						},
						"saveRest": {
							// The URI to send the content to
							url: 'notebook/save',
							// Returns an object containing the data to send to the server
							data: function(html) {
								return {
									id: this.raptor.getElement().data('id'),
									content: html
								};
							},
							retain: true
						},
						"snippetMenu": {
							"snippets": {
								"Grey Box": "<div class=\"grey-box\"><h1>Grey Box<\/h1><ul><li>This is a list<\/li><\/ul><\/div>"
							}
						},
						"statistics": false
					}
				});
			});

		}
	})
	.fail(function(e) {
		alert( "error loading current notebook file.");
	});

	var bodyheight = $(window).height();
	$(".slide-out-div").height(bodyheight - 80);
	$(".div_sidebar_inner").height(bodyheight - 80);
	
	
//	$('#id_sidebar_inner').mousewheel(function(event) {
//	      event.preventDefault();
//	      event.stopPropagation();
//	});

	$('html').css({
		'overflow':'hidden'
	});
});