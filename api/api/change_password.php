<?php
require_once 'cors.php';
require_once 'config.php';
require_once 'Database.php';
require_once 'auth.php';

// Check if user is logged in
$user = checkAuth();
$user_id = $user['user_id'];

// Get database connection
$database = new Database();
$db = $database->getConnection();

// Get POST data
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->current_password) || !isset($data->new_password)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Missing required fields"
    ]);
    exit();
}

try {
    // First verify current password
    $query = "SELECT password FROM users WHERE id = ?";
    $stmt = $db->prepare($query);
    $stmt->execute([$user_id]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user || !password_verify($data->current_password, $user['password'])) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "message" => "Current password is incorrect"
        ]);
        exit();
    }

    // Update password
    $new_password_hash = password_hash($data->new_password, PASSWORD_DEFAULT);
    $update_query = "UPDATE users SET password = ? WHERE id = ?";
    $stmt = $db->prepare($update_query);
    $stmt->execute([$new_password_hash, $user_id]);

    echo json_encode([
        "success" => true,
        "message" => "Password updated successfully"
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "An error occurred while updating the password"
    ]);
}
?>