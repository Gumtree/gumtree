<?php
if (!isset($_SESSION)) session_start();
if(isset($_SESSION['upload_adr'])) $full_adr=$_SESSION['upload_adr'];
else $full_adr="./images/Lessons/";
if(is_array($_FILES)) {
if(is_uploaded_file($_FILES['userImage']['tmp_name'])) {
$sourcePath = $_FILES['userImage']['tmp_name'];
//Change THIS target path if you need to
$targetPath = $full_adr.$_FILES['userImage']['name'];


$filename = $_FILES['userImage']['name'];
$loc = $targetPath;
if(file_exists($loc)){
    $increment = 0;
    list($name, $ext) = explode('.', $loc);
    while(file_exists($loc)) {
        $increment++;
        // $loc is now "userpics/example1.jpg"
        $loc = $name. $increment . '.' . $ext;
        $filename = $name.'-'. $increment . '.' . $ext;
        $targetPath = $filename;
    }
}

if(move_uploaded_file($sourcePath,$targetPath )) {
	chmod($targetPath,0777);
echo '<img class="image-preview" src="<?php echo $targetPath; ?>" class="upload-preview" />';
?>
<script>
    location.reload();
</script>
<?php
}
}
}
?>
