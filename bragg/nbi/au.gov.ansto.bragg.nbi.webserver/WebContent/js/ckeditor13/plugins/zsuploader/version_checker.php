<?php
if (!isset($_SESSION)) session_start();
// check for new version if exists
if (isset($_SESSION['zsuploader_version'])) $version=$_SESSION['zsuploader_version'];
//To do CHECK.. pls wait a while for this :(
