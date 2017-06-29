<?php

$image_list = $_POST['images'];

// Convert JSON string to Array
$image_array = json_decode($image_list, true);

foreach($image_array as $key => $value){
	$mother_id = $value['id'];
	$image_name = $value['name'];
	$image = $value['image'];
	
	$decodedImage = base64_decode($image);

	$upload_path = '/Applications/XAMPP/xamppfiles/htdocs/uploads/tmp/'. $image_name;
	file_put_contents($upload_path, $decodedImage);
	
	//$path = mysql_real_escape_string($upload_path);
	$con = mysqli_connect('localhost','root','','picture_database') or die('error:'.mysqli_error($con));
	$query = "INSERT INTO picture_paths VALUES('$mother_id','$upload_path')";
	$send = mysqli_query($con,$query) or die('Error'.mysqli_error($con));
}

echo 'Success';
?>
