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

// Get item_id from query string
$item_id = isset($_GET['item_id']) ? $_GET['item_id'] : null;

if (!$item_id) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Item ID is required"
    ]);
    exit();
}

try {
    // Get messages for the item
    $query = "SELECT m.*, 
                    u_sender.username as sender_name,
                    u_receiver.username as receiver_name,
                    i.title as item_title,
                    i.type as item_type
             FROM chat_messages m
             JOIN users u_sender ON m.sender_id = u_sender.id
             JOIN users u_receiver ON m.receiver_id = u_receiver.id
             JOIN items i ON m.item_id = i.id
             WHERE m.item_id = ? 
             AND (m.sender_id = ? OR m.receiver_id = ?)
             ORDER BY m.created_at ASC";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$item_id, $user_id, $user_id]);
    $messages = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Mark messages as read if user is the receiver
    $update_query = "UPDATE chat_messages 
                    SET is_read = 1 
                    WHERE item_id = ? 
                    AND receiver_id = ? 
                    AND is_read = 0";
    $stmt = $db->prepare($update_query);
    $stmt->execute([$item_id, $user_id]);

    // Return messages
    echo json_encode([
        "success" => true,
        "messages" => $messages
    ]);
} catch (Exception $e) {
    
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error retrieving messages: " . $e->getMessage()
    ]);
} 