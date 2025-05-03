<?php
require_once 'cors.php';
require_once 'config.php';
require_once 'Database.php';
require_once 'auth.php';

// Check if user is logged in
$user = checkAuth();
$sender_id = $user['user_id'];

// Get database connection
$database = new Database();
$db = $database->getConnection();

// Get POST data
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->item_id) || !isset($data->message)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Missing required fields"
    ]);
    exit();
}

try {
    // First get the item and owner details
    $query = "SELECT i.*, u.id as owner_id 
              FROM items i 
              JOIN users u ON i.user_id = u.id 
              WHERE i.id = ?";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$data->item_id]);
    
    if ($stmt->rowCount() > 0) {
        $item = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // Check if there's an existing conversation or if the user is not the owner
        $check_conversation = "SELECT COUNT(*) as count FROM chat_messages WHERE item_id = ? AND (sender_id = ? OR receiver_id = ?)";
        $stmt = $db->prepare($check_conversation);
        $stmt->execute([$data->item_id, $sender_id, $sender_id]);
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($result['count'] == 0 && $item['user_id'] == $sender_id) {
            http_response_code(400);
            echo json_encode([
                "success" => false,
                "message" => "You cannot start a conversation with yourself"
            ]);
            exit();
        }

        // Determine the receiver_id based on the sender
        if ($sender_id == $item['user_id']) {
            // If sender is owner, get the other party from the last message
            $last_message_query = "SELECT IF(sender_id = ?, receiver_id, sender_id) as other_user 
                                 FROM chat_messages 
                                 WHERE item_id = ? 
                                 ORDER BY created_at DESC LIMIT 1";
            $stmt = $db->prepare($last_message_query);
            $stmt->execute([$sender_id, $data->item_id]);
            $last_message = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($last_message && $last_message['other_user']) {
                $receiver_id = $last_message['other_user'];
            } else {
                http_response_code(400);
                echo json_encode([
                    "success" => false,
                    "message" => "Could not determine message recipient. No previous conversation found."
                ]);
                exit();
            }
        } else {
            // If sender is not owner, send to owner
            $receiver_id = $item['owner_id'];
        }

        // Store the message
        $message_query = "INSERT INTO chat_messages (item_id, sender_id, receiver_id, message, is_read) 
                         VALUES (?, ?, ?, ?, FALSE)";
        $stmt = $db->prepare($message_query);
        $stmt->execute([
            $data->item_id,
            $sender_id,
            $receiver_id,
            sanitizeInput($data->message)
        ]);

        $message_id = $db->lastInsertId();

        // Get the inserted message details
        $get_message = "SELECT 
            m.id,
            m.item_id,
            m.sender_id,
            m.receiver_id,
            m.message,
            CAST(m.is_read AS UNSIGNED) as is_read,
            m.created_at,
            sender.username as sender_name,
            receiver.username as receiver_name,
            i.title as item_title,
            i.type as item_type
            FROM chat_messages m
            JOIN users sender ON m.sender_id = sender.id
            JOIN users receiver ON m.receiver_id = receiver.id
            JOIN items i ON m.item_id = i.id
            WHERE m.id = ?";
        
        $stmt = $db->prepare($get_message);
        $stmt->execute([$message_id]);
        $message_details = $stmt->fetch(PDO::FETCH_ASSOC);

        http_response_code(200);
        echo json_encode([
            "success" => true,
            "message" => "Message sent successfully",
            "data" => $message_details
        ]);
    } else {
        http_response_code(404);
        echo json_encode([
            "success" => false,
            "message" => "Item not found"
        ]);
    }
} catch (Exception $e) {
    
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error sending message: " . $e->getMessage()
    ]);
} 