<?php
header('Content-Type: application/json');
$connection = mysqli_connect("localhost", "root", "", "jewerly");

$response = array("IsSuccess" => false, "Message" => "");

if (!$connection) {
    $response["Message"] = "Database connection failed: " . mysqli_connect_error();
    echo json_encode($response);
    die();
}
$json_data = file_get_contents('php://input');
$data = json_decode($json_data);

$user = $data->user;
$username = $user->username;
$password = $user->password;
$isAdmin = $user->isAdmin;

$sql_insert = "INSERT INTO `user`(`Username`, `Password`, `IsAdmin`) VALUES ('$username','$password','$isAdmin')";

if (mysqli_query($connection, $sql_insert)) {
   $response["IsSuccess"] = true;
   $response["Message"] = "Registration successful!";
} else {
   $response["Message"] = "Invalid input. Please provide username, password, and isAdmin.";
}

echo json_encode($response);

mysqli_close($connection);
?>
