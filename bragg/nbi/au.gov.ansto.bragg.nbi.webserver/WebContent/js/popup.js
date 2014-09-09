$(function() {
  var images = $('.div-embedded-plot');
  var image = $('.div-embedded-plot>img');
  var current = -1;
  var focus = $('.focus');
  var container = $('.focus .container');
  var close = $('.close');
  var next = $('.next');
  var prev = $('.prev');
  
  image.on('click', function(e) {
    current = image.index($(this));
    container.empty();
//    container.append('<img class="img_popup" src=\"' + image.eq(current).attr('src') + '\" />');
    container.append('<img class="img_popup" src=\"' + $(this).attr('src') + '\" />');
    focus.fadeIn().addClass('enabled');
    $("#bd").addClass('darken');
    $("#bd").append('<div class="overlay"></div>');
    $('html, body').animate({
      scrollTop: focus.offset().top - 50
    }, 500);      

    e.stopPropagation();
    
    $("body").click(function(){
        focus.css('display', 'none').removeClass('enabled');
        $("#bd").removeClass('darken');
        $('.overlay').remove();
    });
  
  });

  container.click(function(e){
      e.stopPropagation();
  });
    
//  close.on('click', function() {
//    focus.css('display', 'none').removeClass('enabled');
//    images.removeClass('darken');
//    $('.overlay').remove();
//  });
  
  prev.on('click', function(e) {
    current = (current - 1 < 0) ? image.length - 1 : current - 1;
    container.empty();
    container.append('<img src=\"' + image.eq(current).attr('src') + '\" />');
      e.stopPropagation();
  });
  
  next.on('click', function(e) {
    current = (current + 1 > image.length - 1) ? 0 : current + 1;
    container.empty();
    container.append('<img src=\"' + image.eq(current).attr('src') + '\" />');
    e.stopPropagation();
  });
});