(function ($) {
  CKEDITOR.plugins.add('ckeditor_fa', {
    icons: 'ckeditor-fa',
    init: function (editor) {
      editor.addCommand('ckeditor_fa', new CKEDITOR.dialogCommand('ckeditorFaDialog', {
        allowedContent: 'i(!fa)',
      }));
      editor.ui.addButton('ckeditor_fa', {
        label: 'Insert FontAwesome icon',
        command: 'ckeditor_fa',
        toolbar: 'insert',
        icon: this.path + 'icons/ckeditor-fa.png',
      });
      CKEDITOR.dialog.add('ckeditorFaDialog', this.path + 'dialogs/ckeditor-fa.js');
      CKEDITOR.document.appendStyleSheet(this.path + 'css/ckeditor-fa.css');
    }
  });
})(jQuery);
