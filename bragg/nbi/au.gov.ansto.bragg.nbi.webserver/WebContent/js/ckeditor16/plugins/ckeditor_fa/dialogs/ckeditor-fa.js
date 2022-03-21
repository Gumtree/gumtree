function ckeditor_fa_click(el) {
  var className = el.getAttribute('title');
  document.getElementsByClassName('fontawesomeClass')[0].getElementsByTagName('input')[0].value = el.getAttribute('title');
  var icons = document.getElementById('ckeditor-fa-icons-select');
  var icon = icons.getElementsByTagName('i');
  for (var i = 0; i < icon.length; i++) {
    icon[i].className = icon[i].className.replace('active', '');
  }
  el.className += ' active'
}
(function ($) {

  CKEDITOR.dialog.add('ckeditorFaDialog', function (editor) {
    function ckeditorFaGetIcons() {
      $.ajaxSetup({async: false});
      var icons = $.getJSON('/ajax/ckedtor-fa/get-icons');
      $.ajaxSetup({async: true});
      if (icons.status == 200) {
        return icons.responseJSON.html;
      } else {
        return  'Icons not allowed';
      }
    }
    var icons = ckeditorFaGetIcons();
    var ckeditorFaIcons = icons;
    return {
      title: 'FontAwesome Icons',
      minWidth: 400,
      minHeight: 200,
      contents: [
        {
          id: 'font-awesome',
          label: 'Add icon',
          elements: [
            {
              type: 'text',
              id: 'faicon',
              className: 'fontawesomeClass',
              style: 'display:none',
              validate: CKEDITOR.dialog.validate.notEmpty("Select fontAwesome icon")
            },
            {
              type: 'html',
              html: '<div id="ckeditor-fa-icons">' + ckeditorFaIcons + '</div>'
            }
          ]
        },
      ],
      onOk: function () {
        var icons = document.getElementById('ckeditor-fa-icons-select');
        var activeIcon = icons.getElementsByClassName('active');
        for (var i = 0; i < activeIcon.length; i++) {
          activeIcon[i].className = activeIcon[i].className.replace('active', '');
        }
        var dialog = this;
        var icon = editor.document.createElement('i');
        icon.setAttribute('class', 'fa fa-' + dialog.getValueOf('font-awesome', 'faicon'));
        editor.insertElement(icon);
      }
    };
  });
})(jQuery);
