<?php
	header('Content-Type: application/json');

$connection = mysqli_connect("localhost", "root", "", "jewerly");

$response = array("IsSuccess" => false, "Message" => "", "IsAdmin"=> false);

if (!$connection) {
    $response["Message"] = "Database connection failed: " . mysqli_connect_error();
    echo json_encode($response);
    die(); 
}
	$json_data = file_get_contents('php://input');
	$data = json_decode($json_data);

    $username = mysqli_real_escape_string($connection, $data->username);
    $password = mysqli_real_escape_string($connection, $data->password);
   
	$sql_select = "SELECT `Username`, `Password`, `IsAdmin` FROM `user` WHERE `Username` ='$username'  AND `Password`='$password'";
	$result = mysqli_query($connection, $sql_select);
	if (mysqli_num_rows($result) === 1){
        $user = mysqli_fetch_assoc($result);
        $response["IsSuccess"] = true;
        $response["Message"] = "Login successful!";
        $response["IsAdmin"] = (bool)$user['IsAdmin'];
    }
    else {
        $response["Message"] = "Failed to execute query: " . mysqli_error($connection);
    }
	
echo json_encode($response);

mysqli_close($connection);
?>