<?php
if (!isset($_SESSION)) session_start();
$version="1.2";
$_SESSION['zsuploader_version']=$version;
include_once('version_checker.php');
// And now, magic by zmmaj from ZmajSoft
//This script is free, so uset it as you wish.
$server= $_SERVER['SERVER_NAME'];
//echo $_SERVER["DOCUMENT_ROOT"];
//please change this address if you need some orher place.
//please consider that you have '/' on start and on end off address.
$upload_folder="/lms/admin/vendors/ckeditor/plugins/zsuploader/images/Lessons/";

$full_adr=$server.$upload_folder;
$full_browse_dir=$_SERVER["DOCUMENT_ROOT"].$upload_folder;
$_SESSION['upload_adr']=$full_browse_dir;


//Stuff related to my site
//You can delete this, if you do not need
//******************************************************
if(isset($_SESSION["id"])){ $ses_id=$_SESSION["id"];
if(!file_exists($full_browse_dir."/".$ses_id))
{ mkdir($full_browse_dir."/".$ses_id, 0777, true);
}
$full_adr=$full_adr.$ses_id."/";
$full_browse_dir=$full_browse_dir.$ses_id."/";
$upload_folder=$upload_folder.$ses_id."/";
$_SESSION['upload_adr']=$full_browse_dir;
}
//*******************************************************
function check_jpeg($f, $fix=false ){
# [070203]
# check for jpeg file header and footer - also try to fix it
    if ( false !== (@$fd = fopen($f, 'r+b' )) ){
        if ( fread($fd,2)==chr(255).chr(216) ){
            fseek ( $fd, -2, SEEK_END );
            if ( fread($fd,2)==chr(255).chr(217) ){
                fclose($fd);
                return true;
            }else{
                if ( $fix && fwrite($fd,chr(255).chr(217)) ){return true;}
                fclose($fd);
                return false;
            }
        }else{fclose($fd); return false;}
    }else{
        return false;
    }
}
//*******************************************************
function imageCreateFromAny($filepath) {
    $type = exif_imagetype($filepath); // [] if you don't have exif you could use getImageSize()
    $allowedTypes = array(
        1,  // [] gif
        2,  // [] jpg
        3,  // [] png
        4,  // [] jpeg
        6   // [] bmp
    );
    if (!in_array($type, $allowedTypes)) {
        return false;
    }
    switch ($type) {
        case 1 :
            $im = imageCreateFromGif($filepath);
        break;
        case 2 :
            $im = imageCreateFromJpeg($filepath);
        break;
        case 3 :

            $im = imageCreateFromPng($filepath);
        break;
        case 6 :
            $im = imageCreateFromBmp($filepath);
        break;
    }
    return $im;
}
// create thumbnails from images
function make_thumb($folder,$src,$dest,$thumb_width) {
//if (check_jpeg($folder.'/'.$src)){
	$source_image = imageCreateFromAny($folder.'/'.$src);
//	$source_image = imagecreatefromjpeg($folder.'/'.$src);
	$width = imagesx($source_image);
	$height = imagesy($source_image);

	$thumb_height = floor($height*($thumb_width/$width));

	$virtual_image = imagecreatetruecolor($thumb_width,$thumb_height);

	imagecopyresampled($virtual_image,$source_image,0,0,0,0,$thumb_width,$thumb_height,$width,$height);

	imagejpeg($virtual_image,$dest,100);

}
//}

// display pagination
function print_pagination($numPages,$currentPage) {

   echo 'Page '. $currentPage .' of '. $numPages;

   if ($numPages > 1) {

	   echo '&nbsp;&nbsp;';

       if ($currentPage > 1) {
	       $prevPage = $currentPage - 1;
	       echo '<a href="'. $_SERVER['PHP_SELF'] .'?p='. $prevPage.'">&laquo;&laquo;</a>';
	   }

	   for( $e=0; $e < $numPages; $e++ ) {
           $p = $e + 1;

	       if ($p == $currentPage) {
		       $class = 'current-paginate';
	       } else {
	           $class = 'paginate';
	       }


		       echo '<a class="'. $class .'" href="'. $_SERVER['PHP_SELF'] .'?p='. $p .'">'. $p .'</a>';

	   }

	   if ($currentPage != $numPages) {
           $nextPage = $currentPage + 1;
		   echo '<a href="'. $_SERVER['PHP_SELF'] .'?p='. $nextPage.'">&raquo;&raquo;</a>';
	   }

   }

}
?>

<head>
	    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title>ZSUploader</title>
    <meta name="author" content="ZmajSOft">

    <link rel="stylesheet" href="styles.css">

<link href="styles.css" rel="stylesheet" type="text/css" />
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script type="text/javascript">
$(document).ready(function (e) {
	$("#uploadForm").on('submit',(function(e) {
		e.preventDefault();
		$.ajax({
        	url: "upload.php",
			type: "POST",
			data:  new FormData(this),
			contentType: false,
    	    cache: false,
			processData:false,
			success: function(data)
		    {
			$("#targetLayer").html(data);
		    },
		  	error: function()
	    	{
	    	}
	   });
	}));
});
</script>
<script>
function useImage(imgSrc,server) {
    function getUrlParam( paramName ) {
        var reParam = new RegExp( '(?:[\?&]|&)' + paramName + '=([^&]+)', 'i' ) ;
        var match = window.location.search.match(reParam) ;

        return ( match && match.length > 1 ) ? match[ 1 ] : null ;
    }
    var funcNum = getUrlParam( 'CKEditorFuncNum' );
    var imgSrc = imgSrc;
    var fileUrl = imgSrc;
    window.opener.CKEDITOR.tools.callFunction(funcNum, 'http://'+server+fileUrl );

    window.close();
}
</script>
<script>
function deleteImage(file_name,no)
{
	if (no==1){
    var r = confirm("Are you sure you want to delete this Image?")
    if(r == true)
    {
        $.ajax({
          url: 'delete.php',
          type: "POST",
          data: {'file' : file_name },
          success: function (response) {
			  alert(response);
             // do something
          },
          error: function () {
             // do something
             alert("something wrong");
          }
        });
    }
}
if (no==2) location = location;

}
</script>
<script type='text/javascript'>
function preview_image(event)
{
 var reader = new FileReader();
 reader.onload = function()
 {
  var output = document.getElementById('output_image');
  output.src = reader.result;
 }
 reader.readAsDataURL(event.target.files[0]);
}
</script>
<style type="text/css">
body {
background:#d9d9d9;
margin:0;
padding:0;
font:12px arial, Helvetica, sans-serif;
color:#222;
}

.gallery {
position:relative;
overflow:hidden;
width:device-width;
margin:15px auto;
padding:50px;
background:#eee;
}

.gallery a:link,
.gallery a:active,
.gallery a:visited {
color:#555;
outline:0;
text-decoration:none;
}

.gallery a:hover {color:#fc0;}

.gallery img {border:0;}
.gallery .float-left {float:left;}
.gallery .float-right {float:right;}
.gallery .clear {clear:both;}
.gallery .clearb10 {padding-bottom:10px;clear:both;}

.gallery .titlebar {
height:24px;
line-height:24px;
margin:0 5px;
}

.gallery .title {
font-size:18px;
font-weight:400;
}

.gallery .thumb {
overflow:hidden;
float:left;
width:110px;
height:75px;
margin:5px;
border:5px solid #fff;
}

.gallery .thumb:hover {
border:0;
width:120px;
height:85px;
}

/***** pagination style *****/
.gallery .paginate-wrapper {
padding:10px 0;
font-size:11px;
}

.gallery a.paginate {
color:#555;
padding:0;
margin:0 2px;
text-decoration:none;
}

.gallery a.current-paginate,
.gallery a.paginate:hover {
color:#333;
font-weight:700;
padding:0;
margin:0 2px;
text-decoration:none;
}

.gallery a.paginate-arrow {
text-decoration:none;
border:0;
}

/* The Close Button */
.closes {
    color:#e67e12;
    float: right;
    font-size: 18px;
    font-weight: bold;
}

.closes:hover,
.close:focus {
    color: #404040;
    text-decoration: none;
    cursor: pointer;
}
</style>

</head>

<body>

	<?php echo  "<p style='background:green;'><font color='blue'>ZmajSoft Browser Uploader:</font>VERSION:".$version."</p>";?>
<table width="100%"><tr><td>
<div class="bgColor" width="100%">
<form id="uploadForm" action="upload.php" method="post">
<div id="targetLayer"><img id="output_image" alt="No Image" style="max-width:150px;"/></div>
 <img src="images/Logo.png" alt="ZmajSoft Logo" style="width:150px;height:150px;float:right;">
<div id="uploadFormLayer">
<input name="userImage" accept="image/*"  type="file" class="inputFile" onchange="preview_image(event)" /> <br>
<input type="submit" value="Submit" class="btnSubmit" />

</div></div>
</form>
</div>
</td></tr>
<tr><td>
<div class="gallery">
<?php
$itemsPerPage = '16';         // number of images per page
$thumb_width  = '150';        // width of thumbnails
$thumb_height = '130';         // height of thumbnails
$src_folder   =$full_browse_dir;             // current folder
$src_files    = scandir($src_folder); // files in current folder
$extensions   = array(".jpg",".jpeg",".png",".gif",".JPG",".JPEG",".PNG",".GIF"); // allowed extensions in photo gallery
$files = array();
 $Ftype="Images";
$CKEditorFuncNum=0;
  If(isset($_REQUEST['CKEditorFuncNum'])) $CKEditorFuncNum= $_REQUEST['CKEditorFuncNum'];
foreach($src_files as $file) {

	$ext = strrchr($file, '.');
    if(in_array($ext, $extensions)) {

       array_push( $files, $file );


       if (!is_dir($src_folder.'/thumbs')) {
          mkdir($src_folder.'/thumbs');
          chmod($src_folder.'/thumbs', 0777);
       }

	   $thumb = $src_folder.'/thumbs/'.$file;
       if (!file_exists($thumb)) {
          make_thumb($src_folder,$file,$thumb,$thumb_width);
       chmod($thumb, 0777);
	   }

	}

}


if ( count($files) == 0 ) {

    echo 'There are no photos in this album!';

} else {

    $numPages = ceil( count($files) / $itemsPerPage );

    if(isset($_GET['p'])) {

       $currentPage = $_GET['p'];
       if($currentPage > $numPages) {
           $currentPage = $numPages;
       }

    } else {
       $currentPage=1;
    }

    $start = ( $currentPage * $itemsPerPage ) - $itemsPerPage;

    for( $i=$start; $i<$start + $itemsPerPage; $i++ ) {

	   if( isset($files[$i]) && is_file( $src_folder .'/'. $files[$i] ) ) {
	   $slika=$files[$i];
	   $full_slika=$upload_folder.$files[$i];
	   $za_brisanje=$src_folder .'/'. $files[$i];
	   $za_brisanje_thumb=$src_folder .'/thumbs/'. $files[$i];
	   $thumb=$upload_folder."/thumbs/".$files[$i];
	  ?>
	   <div class="thumb">
		    <span  class="closes" title="Delete" onclick="deleteImage('<?php echo $za_brisanje;?>',1);deleteImage('<?php echo $za_brisanje_thumb;?>',2);" >&times;</span>
	            <a href="<?php echo $full_slika?>" class="albumpix" rel="albumpix">
			       <img src='<?php echo $thumb?>' width="<?php echo $thumb_width?>" height="<?php echo $thumb_height?>" alt=""
			     onclick="return false"  ondblclick="useImage('<?php echo $full_slika ?>','<?php echo $server?>');" />

				</a>
			    </div>
	<?php

	    } else {

		  if( isset($files[$i]) ) {
		    echo $files[$i];
		  }

		}

    }


     echo '<div class="clear"></div>';

     echo '<div class="p5-sides">
	         <div class="float-left">'.count($files).' images</div>

	         <div class="float-right" class="paginate-wrapper">';

              print_pagination($numPages,$currentPage);

       echo '</div>

	         <div class="clearb10">
		   </div>';

}
?>

</div>
</td></tr>  </table>
</body>
</html>
