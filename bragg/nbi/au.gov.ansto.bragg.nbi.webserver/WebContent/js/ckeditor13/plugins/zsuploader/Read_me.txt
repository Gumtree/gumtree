This is UPLOADER , but ALSO AND browser
now VERSION 1.2:
dated 13.05.2018

What is new?
well... now you can delete image from a galery
now, you can upload image with same name without replacing old image with same name.
improved thumbs creation.
Preview image before upload...

TO DO?
automatic upgrade.

INSTALLATION
Very easy to install
1: Download and unzip 'ZSUploader' to CKEditor/ plugins...
2: in CKEditor/config.js below a line '// config.uiColor = '#AADC6E';'


CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here. For example:
	 //config.language = 'sr-latn';
	// config.uiColor = '#AADC6E';

};


add this line
   config.extraPlugins = 'zsuploader';

   Or if you allredy have enabled some plugins,just add this new
   Eg


   config.extraPlugins = 'youtybe,zsuploader';

  that is all

 Configuration...

   Find in folder /plugins/zsuploader file 'uploader.php'
   open in some editor, and change PATH to this file on line 9...
   Now, by default ( this is for my site )is:
   $upload_folder="/lms/admin/vendors/ckeditor/plugins/zsuploader/images/Lessons/";
   Your job is to change path to your UPLOAD and BROWSE folder...
   If you haven't jet such folder maker it and change permisions to 0777...

   One more thing..
   You can use $_SESSION["id"] variable...
    For What?
   With this variable uploader and browser will automaticaly change path to user storage with that "id"...
   id, can be number or name...
  If folder 'id' under  '$upload_folder' do not exists. will be created with permision 0777...
  In this way, every user have own image storage on site.. And Can NOT see or USE images from other users,



   tested with lattest CKEditor version:4.9.2


   enjou zmmaj from zmajsoft,
