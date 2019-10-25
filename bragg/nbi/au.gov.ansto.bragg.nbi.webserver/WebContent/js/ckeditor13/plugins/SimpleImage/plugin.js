CKEDITOR.plugins.add( 'SimpleImage', {
    icons: 'simpleimage',
    init: function( editor ) {
        editor.addCommand( 'simpleimage', new CKEDITOR.dialogCommand( 'simpleimageDialog' ) );
        editor.ui.addButton( 'SimpleImage', {
            label: 'Add a image',
            icons: 'simpleimage',
            command: 'simpleimage'
        });

        CKEDITOR.dialog.add( 'simpleimageDialog', this.path + 'dialogs/simpleimage.js' );
    }
});
