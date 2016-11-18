/*
    SlideOutTabs 
    
    By Norman Xiong

    
    example:
    
        $('.slide-out-div').tabSlideOut({
                tabHandle: '.handle',                         //class of the element that will be your tab - does not have to be an anchor
                pathToTabImage: 'images/contact_tab.gif',     //relative path to the image for the tab *required*
                imageHeight: '133px',                         //height of tab image *required*
                imageWidth: '44px',                           //width of tab image *required*    
        });

    
*/


(function($){
    $.fn.tabSlideOut = function(callerSettings) {
        var settings = $.extend({
        	tabHandleClass: '.tabHandle',
        	tabBlockClass: '.tabBlock',
            tabHandles: [],
            tabBlocks: [],
            tabHandleTopPos: 0,
            tabHandleSize: 180,
            speed: 300,
            action: 'click',
            tabLocation: 'right',
            topPos: '180px',
            leftPos: '20px',
            fixedPosition: false,
            positioning: 'absolute',
            pathToTabImage: null,
            imageHeight: null,
            imageWidth: null,
            onLoadSlideOut: false,
            closeOnFocusOut: false,
            onSlideOut:function(){},
			onSlideIn:function(){},
			isSlideOut: false,
			currentTab: null
        }, callerSettings||{});

        var tabBlocks = {};
        for ( var i = 0; i < settings.tabHandles.length; i++) {
			tabBlocks[settings.tabHandles[i].substring(1)] = $(settings.tabBlocks[i]);
		}
        settings.tabBlocks = tabBlocks;
        
        var tabHandles = [];
        $.each(settings.tabHandles, function(idx, val) {
			tabHandles.push($(val));
		});
        settings.tabHandles = tabHandles;
        
        var obj = this;
        if (settings.fixedPosition === true) {
            settings.positioning = 'fixed';
        } else {
            settings.positioning = 'absolute';
        }
        
        //ie6 doesn't do well with the fixed option
        if (document.all && !window.opera && !window.XMLHttpRequest) {
            settings.positioning = 'absolute';
        }
        

        
        
        //set initial tabHandle css
        
        if (settings.pathToTabImage != null) {
        	for ( var i = 0; i < settings.tabHandles.length; i++) {
				settings.tabHandles[i].css({
		            'background' : 'url('+settings.pathToTabImage[i]+') no-repeat',
		            'width' : settings.imageWidth,
		            'height': settings.imageHeight,
	                'display': 'block',
	                'textIndent' : '-99999px',
	                'outline' : 'none',
	                'border': '1px solid #000',
	                'position' : 'absolute'					
				});
			}
//            $(settings.tabHandleClass).css({
//            'background' : 'url('+settings.pathToTabImage+') no-repeat',
//            'width' : settings.imageWidth,
//            'height': settings.imageHeight
//            });
//            $(settings.tabHandleClass).css({ 
//                'display': 'block',
//                'textIndent' : '-99999px',
//                'outline' : 'none',
//                'border': '1px solid #000',
//                'position' : 'absolute'
//            });
        } else {
        	$(settings.tabHandleClass).css({
//                'background' : '#999',
//                'color' : 'white',
                'text-align': 'center',
                
                '-webkit-transform': 'rotate(-90deg)',
                '-moz-transform': 'rotate(-90deg)',
                '-ms-transform': 'rotate(-90deg)',
                '-o-transform': 'rotate(-90deg)',
                'transform': 'rotate(-90deg)',
                /* also accepts left, right, top, bottom coordinates; not required, but a good idea for styling */
                '-webkit-transform-origin': '50% 50%',
                '-moz-transform-origin': '50% 50%',
                '-ms-transform-origin': '50% 50%',
                '-o-transform-origin': '50% 50%',
                'transform-origin': '164px 16px',
                /* Should be unset in IE9+ I think. */
//                'filter': 'progid:DXImageTransform.Microsoft.BasicImage(rotation=3)',
//                'margin': '4px 0 4px 4px',
//                'margin-bottom': '16px',
//                'padding-right': '4px',
//                'border-radius' : '5px',
//                'box-shadow' : '1px 3px 6px #444',
//                '-moz-box-shadow' : '1px 3px 6px #444',
//                '-webkit-box-shadow' : '1px 3px 6px #444',
//                'z-index': '0',
                'margin-top': '1px',
                'border': '1px solid #000',
                'font-family': '"Palatino Linotype", "Book Antiqua", Palatino, serif',
                'font-size': '20px',
                'vertical-align': 'middle',
                'font-weight': 'bold',
            	'font-variant': 'small-caps',
                'text-decoration': 'none',
                
                'width' : '180px',
            	'height' : '32px',
                'line-height': '32px',
            	'display': 'block',
            	'cursor': 'pointer',
            	'position' : 'absolute'
                });
        }
        
        $(settings.tabHandleClass).hover(function(){
        	if ($(this).attr('id') != settings.currentTab) {
//            	$(this).css({
//            		'background-color': '#ddd',
//            		'color': '#000'
//            	});
        		$(this).addClass('div_handle_highlight');
        	}
        }, 
        function(){
        	if ($(this).attr('id') != settings.currentTab) {
//        		$(this).css({
//	        		'background-color': '#999',
//	        		'color': 'white'
//	        	});
        		$(this).removeClass('div_handle_highlight');
        	}
        });
        
        obj.css({
            'line-height' : '1',
            'z-index': '10',
            'position' : settings.positioning
        });

        
        var properties = {
                    containerWidth: parseInt(obj.outerWidth(), 10) + 'px',
                    containerHeight: parseInt(obj.outerHeight(), 10) + 'px',
                    tabWidth: parseInt($(settings.tabHandleClass).outerWidth(), 10) + 'px',
                    tabHeight: parseInt($(settings.tabHandleClass).outerHeight(), 10) + 'px'
                };

        //set calculated css
        if(settings.tabLocation === 'top' || settings.tabLocation === 'bottom') {
            obj.css({'left' : settings.leftPos});
            $(settings.tabHandleClass).css({'right' : 0});
        }
        
        if(settings.tabLocation === 'top') {
            obj.css({'top' : '-' + properties.containerHeight});
            $(settings.tabHandleClass).css({'bottom' : '-' + properties.tabHeight});
        }

        if(settings.tabLocation === 'bottom') {
            obj.css({'bottom' : '-' + properties.containerHeight, 'position' : 'fixed'});
            $(settings.tabHandleClass).css({'top' : '-' + properties.tabHeight});
        }
        
        if(settings.tabLocation === 'left' || settings.tabLocation === 'right') {
            obj.css({
                'height' : properties.containerHeight,
                'top' : settings.topPos
            });
            
            $.each(settings.tabHandles, function(idx, val) {
				$(this).css({'top' : settings.tabHandleTopPos + settings.tabHandleSize * idx});
//				console.log($(this).attr('id') + ':' + (settings.tabHandleTopPos + settings.tabHandleSize * idx));
			});
        }
        
        if(settings.tabLocation === 'left') {
            obj.css({ 'left': '-' + properties.containerWidth});
            $(settings.tabHandleClass).css({'right' : '-' + properties.tabWidth});
        }

        if(settings.tabLocation === 'right') {
            obj.css({ 'right': '-' + properties.containerWidth});
            $(settings.tabHandleClass).css({'left' : '-' + properties.tabWidth});
            
            $('html').css('overflow-x', 'hidden');
        }

        //functions for animation events
        
        $(settings.tabHandleClass).click(function(event){
            event.preventDefault();
        });
        
        var slideIn = function() {
            
            if (settings.tabLocation === 'top') {
                obj.animate({top:'-' + properties.containerHeight}, settings.speed).removeClass('open');
            } else if (settings.tabLocation === 'left') {
                obj.animate({left: '-' + properties.containerWidth}, settings.speed).removeClass('open');
            } else if (settings.tabLocation === 'right') {
                obj.animate({right: '-' + properties.containerWidth}, settings.speed).removeClass('open');
            } else if (settings.tabLocation === 'bottom') {
                obj.animate({bottom: '-' + properties.containerHeight}, settings.speed).removeClass('open');
            }
            if (settings.onSlideIn) {
            	settings.onSlideIn();
            }
            settings.isSlideOut = false;
        };
        
        var slideOut = function() {
            
            if (settings.tabLocation == 'top') {
                obj.animate({top:'-3px'},  settings.speed).addClass('open');
            } else if (settings.tabLocation == 'left') {
                obj.animate({left:'-3px'},  settings.speed).addClass('open');
            } else if (settings.tabLocation == 'right') {
                obj.animate({right:'-3px'},  settings.speed).addClass('open');
            } else if (settings.tabLocation == 'bottom') {
                obj.animate({bottom:'-3px'},  settings.speed).addClass('open');
            }
            if (settings.onSlideOut) {
            	settings.onSlideOut();
            }
            settings.isSlideOut = true;
        };

        var isSlideOut = function() {
			return settings.isSlideOut;
		};
		
        var clickScreenToClose = function() {
            obj.click(function(event){
                event.stopPropagation();
            });
            
            $(document).click(function(){
                slideIn();
            });
        };
        
        var clickAction = function(){
        	
        	for ( var i = 0; i < settings.tabHandles.length; i++) {
				settings.tabHandles[i].click(function(e) {
					if (obj.hasClass('open')) {
						if ($(this).attr('id') == settings.currentTab) {
							slideIn();
							settings.currentTab = null;
							$(settings.tabHandleClass).removeClass('div_handle_highlight');
						} else {
							$(settings.tabBlockClass).hide();
							settings.tabBlocks[$(this).attr('id')].show();
							settings.currentTab = $(this).attr('id');
							$(settings.tabHandleClass).removeClass('div_handle_highlight');
							$(this).addClass('div_handle_highlight');
						}
	                } else {
	                	$(settings.tabBlockClass).hide();
	                	settings.tabBlocks[$(this).attr('id')].show();
						settings.currentTab = $(this).attr('id');
	                    slideOut();
	                    $(this).addClass('div_handle_highlight');
	                }
				});
			}
        	
//            settings.tabHandle.click(function(event){
//                if (obj.hasClass('open')) {
//                    slideIn();
//                } else {
//                    slideOut();
//                }
//            });
            
            if (settings.closeOnFocusOut) {
            	clickScreenToClose();
            }
        };
        
        var hoverAction = function(){
            obj.hover(
                function(){
                    slideOut();
                },
                
                function(){
                    slideIn();
                });
                
                settings.tabHandle.click(function(event){
                    if (obj.hasClass('open')) {
                        slideIn();
                    }
                });
                
                clickScreenToClose();
                
        };
        
        var slideOutOnLoad = function(){
            slideIn();
            setTimeout(slideOut, 500);
        };
        
        //choose which type of action to bind
        if (settings.action === 'click') {
            clickAction();
        }
        
        if (settings.action === 'hover') {
            hoverAction();
        }
        
        if (settings.onLoadSlideOut) {
            slideOutOnLoad();
        };
        
    };
})(jQuery);
