<?php
require_once 'cors.php';
require_once 'config.php';
require_once 'Database.php';

// Start session at the beginning
session_start();

// Prevent any output before JSON response
ob_start();

try {
    $data = json_decode(file_get_contents("php://input"), true);
    $response = array();

    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("Invalid JSON data: " . json_last_error_msg());
    }

    if (!isset($data['email']) || !isset($data['password'])) {
        http_response_code(400);
        $response["status"] = "error";
        $response["message"] = "Email and password are required";
        echo json_encode($response);
        return;
    }

    $email = sanitizeInput($data['email']);
    $password = $data['password'];

    $database = new Database();
    $db = $database->getConnection();

    // Get user data including id, username, email, and password
    $stmt = $db->prepare("SELECT id, username, email, password FROM users WHERE email = ?");
    $stmt->execute([$email]);

    if ($stmt->rowCount() > 0) {
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if (password_verify($password, $user['password'])) {
            // Generate a new token
            $token = bin2hex(random_bytes(32));
            
            // Store session data
            $_SESSION['user_id'] = $user['id'];
            $_SESSION['username'] = $user['username'];
            $_SESSION['email'] = $user['email'];
            
            // Update the token in the database
            $updateStmt = $db->prepare("UPDATE users SET token = ? WHERE id = ?");
            $updateStmt->execute([$token, $user['id']]);
            
            http_response_code(200);
            $response["status"] = "success";
            $response["message"] = "Login successful";
            $response["data"] = array(
                "user_id" => $user['id'],
                "username" => $user['username'],
                "email" => $user['email'],
                "token" => $token
            );
        } else {
            http_response_code(401);
            $response["status"] = "error";
            $response["message"] = "Invalid email or password";
        }
    } else {
        http_response_code(401);
        $response["status"] = "error";
        $response["message"] = "Invalid email or password";
    }
} catch (Exception $e) {
    http_response_code(500);
    $response["status"] = "error";
    $response["message"] = "Server error: " . $e->getMessage();
} catch (PDOException $e) {
    http_response_code(500);
    $response["status"] = "error";
    $response["message"] = "Database error occurred";
}

// Clear any output buffers
while (ob_get_level()) {
    ob_end_clean();
}

// Send JSON response
echo json_encode($response);
?>