<?php
header('Content-Type: application/json');

$connection = mysqli_connect("localhost", "root", "", "jewerly");

$response = array(
    "IsSuccess" => false,
    "Message" => "",
    "Products" => array()
);

if (!$connection) {
    $response["Message"] = "Database connection failed: " . mysqli_connect_error();
    echo json_encode($response);
    die();
}

$sql_select = "SELECT * FROM `product`";

$result = mysqli_query($connection, $sql_select);

if ($result) {
    if (mysqli_num_rows($result) > 0) {
        while ($row = mysqli_fetch_assoc($result)) {
            $response["Products"][] = $row;
        }
        $response["IsSuccess"] = true;
        $response["Message"] = "Products loaded successfully";
    } else {
        $response["Message"] = "No products found";
    }
} else {
    $response["Message"] = "Query error: " . mysqli_error($connection);
}

echo json_encode($response);
mysqli_close($connection);
?>