<?php
header('Content-Type: application/json');

$db_host = "localhost";
$db_user = "root";
$db_pass = "";
$db_name = "jewerly";

$connection = mysqli_connect($db_host, $db_user, $db_pass, $db_name);

$response = array(
    "IsSuccess" => false,
    "Message" => ""
);

if (!$connection) {
    $response["Message"] = "Database connection failed: " . mysqli_connect_error();
    echo json_encode($response);
    exit();
}

$json_input = file_get_contents('php://input');
$data = json_decode($json_input);

if ($data === null || !isset($data->Id)) {
    $response["Message"] = "Invalid data. 'Id' is required for editing.";
    echo json_encode($response);
    exit();
}
        $Title = $data->Title;
        $Description = $data->Description;
        $PhotoUrl = $data->PhotoUrl;
        $VideoUrl = $data->VideoUrl;
        $Price = $data->Price;
        $CategoryId = $data->CategoryId;
        $Id = $data->Id;
$sql = "UPDATE `product` SET
             `Title` = '$Title', 
            `Description` = '$Description', 
            `Price` = '$Price', 
            `CategoryId` = '$CategoryId', 
            `PhotoUrl` = '$PhotoUrl', 
            `VideoUrl` = '$VideoUrl'
        WHERE `Id` = '$Id'";

        $result = mysqli_query($connection, $sql);
  
        if (mysqli_affected_rows($connection) >= 0) {
            $response["IsSuccess"] = true;
            $response["Message"] = "Product updated successfully.";
        } 
     else {
        $response["Message"] = "Failed to update product. DB Error: " . mysqli_error($connection);
    }

echo json_encode($response);
mysqli_close($connection);
?>