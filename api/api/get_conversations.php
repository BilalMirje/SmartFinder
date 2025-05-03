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

try {
    // Get all conversations where user is either sender or receiver
    $query = "SELECT 
        i.id as item_id,
        i.title as item_title,
        i.type as item_type,
        m.message as last_message,
        m.created_at as last_message_time,
        CASE 
            WHEN m.sender_id = ? THEN receiver.username
            ELSE sender.username
        END as other_user_name,
        COUNT(CASE WHEN m.is_read = 0 AND m.receiver_id = ? THEN 1 END) as unread_count
        FROM chat_messages m
        JOIN items i ON m.item_id = i.id
        JOIN users sender ON m.sender_id = sender.id
        JOIN users receiver ON m.receiver_id = receiver.id
        WHERE m.sender_id = ? OR m.receiver_id = ?
        GROUP BY i.id
        ORDER BY m.created_at DESC";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$user_id, $user_id, $user_id, $user_id]);
    
    $conversations = $stmt->fetchAll(PDO::FETCH_ASSOC);

    http_response_code(200);
    echo json_encode([
        "success" => true,
        "conversations" => $conversations
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "An error occurred while retrieving conversations"
    ]);
}
?>