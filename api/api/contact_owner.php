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
    $query = "SELECT i.*, u.email as owner_email, u.username as owner_name 
              FROM items i 
              JOIN users u ON i.user_id = u.id 
              WHERE i.id = ?";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$data->item_id]);
    
    if ($stmt->rowCount() > 0) {
        $item = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // Don't allow contacting your own items
        if ($item['user_id'] == $user_id) {
            http_response_code(400);
            echo json_encode([
                "success" => false,
                "message" => "You cannot contact yourself for your own item"
            ]);
            exit();
        }

        // Get the sender's details
        $sender_query = "SELECT username, email FROM users WHERE id = ?";
        $stmt = $db->prepare($sender_query);
        $stmt->execute([$user_id]);
        $sender = $stmt->fetch(PDO::FETCH_ASSOC);

        // Store the contact request in database
        $contact_query = "INSERT INTO contact_requests (item_id, from_user_id, to_user_id, message) 
                         VALUES (?, ?, ?, ?)";
        $stmt = $db->prepare($contact_query);
        $stmt->execute([
            $data->item_id,
            $user_id,
            $item['user_id'],
            sanitizeInput($data->message)
        ]);

        // Send email to item owner
        $to = $item['owner_email'];
        $subject = "New Contact Request for your " . $item['type'] . " item: " . $item['title'];
        $message = "Hello " . $item['owner_name'] . ",\n\n";
        $message .= "You have received a new contact request regarding your " . $item['type'] . " item '" . $item['title'] . "'.\n\n";
        $message .= "From: " . $sender['username'] . "\n";
        $message .= "Message: " . $data->message . "\n\n";
        $message .= "You can reply directly to this email to contact the sender.\n\n";
        $message .= "Best regards,\nSmartFinder Team";
        
        $headers = "From: " . SMTP_FROM . "\r\n";
        $headers .= "Reply-To: " . $sender['email'] . "\r\n";
        
        if (mail($to, $subject, $message, $headers)) {
            http_response_code(200);
            echo json_encode([
                "success" => true,
                "message" => "Contact request sent successfully"
            ]);
        } else {
            throw new Exception("Failed to send contact request");
        }
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
        "message" => "An error occurred while sending the contact request"
    ]);
}
?>