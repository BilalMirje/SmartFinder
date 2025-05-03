<?php
require_once 'cors.php';
require_once 'config.php';
require_once 'Database.php';

function checkAuth() {
    session_start();
    
    // First check for session-based authentication
    if (isset($_SESSION['user_id'])) {
        return [
            'user_id' => $_SESSION['user_id'],
            'username' => $_SESSION['username']
        ];
    }
    
    // If no session, check for token-based authentication
    $headers = apache_request_headers();
    $token = null;
    
    // Check Authorization header
    if (isset($headers['Authorization'])) {
        $token = str_replace('Bearer ', '', $headers['Authorization']);
    }
    // Also check for token in request parameters
    else if (isset($_GET['token'])) {
        $token = $_GET['token'];
    }
    
    if ($token) {
        try {
            $database = new Database();
            $db = $database->getConnection();
            
            $stmt = $db->prepare("SELECT id, username FROM users WHERE token = ?");
            $stmt->execute([$token]);
            
            if ($stmt->rowCount() > 0) {
                $user = $stmt->fetch(PDO::FETCH_ASSOC);
                
                // Store in session for future requests
                $_SESSION['user_id'] = $user['id'];
                $_SESSION['username'] = $user['username'];
                
                return [
                    'user_id' => $user['id'],
                    'username' => $user['username']
                ];
            }
        } catch (Exception $e) {
            logError("Auth error: " . $e->getMessage());
        }
    }
    
    // If no valid session or token found
    http_response_code(401);
    echo json_encode([
        "status" => "error",
        "message" => "Please login to continue"
    ]);
    exit();
}
?>