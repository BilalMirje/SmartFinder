<?php
require_once 'cors.php';
require_once 'config.php';
require_once 'Database.php';

// Prevent any output before JSON response
ob_start();

try {
    $data = json_decode(file_get_contents("php://input"), true);
    $response = array();

    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("Invalid JSON data: " . json_last_error_msg());
    }

    if (
        !empty($data['username']) &&
        !empty($data['email']) &&
        !empty($data['password']) &&
        filter_var($data['email'], FILTER_VALIDATE_EMAIL)
    ) {
        // Sanitize input
        $username = sanitizeInput($data['username']);
        $email = sanitizeInput($data['email']);
        
        $database = new Database();
        $db = $database->getConnection();

        // Check if email already exists
        $check_stmt = $db->prepare("SELECT id FROM users WHERE email = ?");
        $check_stmt->execute([$email]);
        
        if ($check_stmt->rowCount() > 0) {
            http_response_code(400);
            $response["status"] = "error";
            $response["message"] = "Email already exists";
        } else {
            // Check if username already exists
            $check_stmt = $db->prepare("SELECT id FROM users WHERE username = ?");
            $check_stmt->execute([$username]);
            
            if ($check_stmt->rowCount() > 0) {
                http_response_code(400);
                $response["status"] = "error";
                $response["message"] = "Username already exists";
            } else {
                // Hash the password
                $hashed_password = password_hash($data['password'], PASSWORD_DEFAULT);
                
                // Prepare insert statement
                $stmt = $db->prepare("
                    INSERT INTO users (username, email, password, created_at) 
                    VALUES (?, ?, ?, NOW())
                ");
                
                if ($stmt->execute([$username, $email, $hashed_password])) {
                    http_response_code(201);
                    $user_id = $db->lastInsertId();
                    
                    // Generate a token
                    $token = bin2hex(random_bytes(32));
                    
                    // Update the token in the database
                    $updateStmt = $db->prepare("UPDATE users SET token = ? WHERE id = ?");
                    $updateStmt->execute([$token, $user_id]);
                    
                    $response["status"] = "success";
                    $response["message"] = "User registered successfully";
                    $response["data"] = array(
                        "user_id" => $user_id,
                        "username" => $username,
                        "email" => $email,
                        "token" => $token
                    );
                } else {
                    throw new Exception("Unable to register user");
                }
            }
        }
    } else {
        http_response_code(400);
        $response["status"] = "error";
        $response["message"] = "Missing or invalid required fields";
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