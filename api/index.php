<?php
// Set headers for API
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// Include database configuration
require_once 'config.php';

// Check database connection
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(array(
        "status" => "error",
        "message" => "Database connection failed."
    ));
} else {
    http_response_code(200);
    echo json_encode(array(
        "status" => "ok",
        "message" => "SmartFinder API is running.",
        "version" => "1.0.0"
    ));
}

$conn->close();
?>