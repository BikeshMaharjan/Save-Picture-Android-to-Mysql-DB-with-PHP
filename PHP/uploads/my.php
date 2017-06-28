<?php

$image = $_POST['image'];
$image_name = $_POST['name'].'.jpg';
$mother_id = $_POST['id'];
$decodedImage = base64_decode($image);
$upload_path = '/Applications/XAMPP/xamppfiles/htdocs/uploads/tmp/'. $image_name;
file_put_contents($upload_path, $decodedImage);
//$path = mysql_real_escape_string($upload_path);
$con = mysqli_connect('localhost','root','','picture_database') or die('error:'.mysqli_error($con));
$query = "INSERT INTO picture_paths VALUES(102,'$upload_path')";
$send = mysqli_query($con,$query) or die('Error'.mysqli_error($con));
echo 'Success';
?>
