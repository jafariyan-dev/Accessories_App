<?php
header('Content-Type: application/json');

$connection = mysqli_connect("localhost", "root", "", "jewerly");

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

if ($data === null || !isset($data->Title) || !isset($data->Price) || !isset($data->CategoryId)) {
    $response["Message"] = "Invalid or incomplete product data. 'Title', 'Price', and 'CategoryId' are required.";
    echo json_encode($response);
    exit(); 
}
        $Title = $data->Title;
        $Description = $data->Description;
        $PhotoUrl = $data->PhotoUrl;
        $VideoUrl = $data->VideoUrl;
        $Price = $data->Price;
        $CategoryId = $data->CategoryId;

$sql = "INSERT INTO `product`(`Title`, `Description`,  `PhotoUrl`,`VideoUrl`, `Price`, `CategoryId`) VALUES
 ('$Title','$Description','$PhotoUrl','$VideoUrl', '$Price', '$CategoryId')";

    $result = mysqli_query($connection, $sql);

    if (mysqli_affected_rows($connection) >= 0) {
        $response["IsSuccess"] = true;
        $response["Message"] = "Product added successfully!";
    } else {
        $response["Message"] = "Failed to add product. DB Error: " . mysqli_error($connection);
}  

echo json_encode($response);
mysqli_close($connection);
?>