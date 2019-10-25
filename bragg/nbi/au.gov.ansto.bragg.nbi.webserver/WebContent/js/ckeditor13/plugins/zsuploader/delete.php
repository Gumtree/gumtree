<?php if(isset($_POST['file']) && !empty($_POST['file'])){
//	$no=$_POST['no'];
    //find the file
    $file = $_POST['file'];
    if(is_file($file)){
        unlink($file);
        echo "deleted";
    }else{
        echo $_POST['file']." has not been found!";
    }
}?>
