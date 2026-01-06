<?php
header('Content-Type: application/json');

$base_url = "http://10.178.79.48/"; 
$upload_folder = "photo/";

$response = array(
    "IsSuccess" => false,
    "Message" => "",
    "Url" => ""
);

if (isset($_FILES['image'])) {
    $file = $_FILES['image'];
$error = $file['error'];

    if ($error === UPLOAD_ERR_OK) {
        $temp_name = $file['tmp_name'];

        // یک نام منحصر به فرد برای فایل ایجاد می‌کنیم تا با فایل‌های دیگر تداخل نداشته باشد
        // مثال: 1672187921_my_image.jpg
        $file_extension = pathinfo($file['name'], PATHINFO_EXTENSION);
        $new_file_name = time() . "_" . uniqid() . "." . $file_extension;

        // مسیر کامل مقصد برای ذخیره فایل
        // '../' به معنی "یک پوشه به عقب برگرد" است. از پوشه api به روت www می‌رویم.
        $destination = "../" . $upload_folder . $new_file_name;

        // انتقال فایل از مسیر موقت به مقصد نهایی
        if (move_uploaded_file($temp_name, $destination)) {
            $response['IsSuccess'] = true;
            $response['Message'] = "Photo uploaded successfully.";
            $response['Url'] = $base_url . $upload_folder . $new_file_name;
        } else {
            $response['Message'] = "Failed to move uploaded file.";
        }
    } else {
        $response['Message'] = "File upload error: " . $error;
    }
} else {
    $response['Message'] = "No file sent with the key 'image'.";
}

echo json_encode($response);
?>
