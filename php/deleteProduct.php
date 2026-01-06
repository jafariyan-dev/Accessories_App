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

if (isset($_POST['Id'])) {
    $productId = $_POST['Id'];

    $sql = "DELETE FROM `product` WHERE `Id` = ?";
    $stmt = mysqli_prepare($connection, $sql);

    if ($stmt) {
        mysqli_stmt_bind_param($stmt, "i", $productId);

        if (mysqli_stmt_execute($stmt)) {
            if (mysqli_stmt_affected_rows($stmt) > 0) {
                $response["IsSuccess"] = true;
                $response["Message"] = "Product deleted successfully.";
            } else {
                $response["IsSuccess"] = false;
                $response["Message"] = "No product found with the given ID.";
            }
        } else {
            $response["Message"] = "Failed to delete product. DB Error: " . mysqli_error($connection);
        }
        mysqli_stmt_close($stmt);
    } else {
        $response["Message"] = "SQL statement preparation failed.";
    }
} else {
    $response["Message"] = "Product ID is required.";
}

echo json_encode($response);
mysqli_close($connection);
?>