<?php
require_once 'cors.php';
require_once 'config.php';
require_once 'Database.php';
require_once 'auth.php';

// Check if user is logged in
$user = checkAuth();

// Get database connection
$database = new Database();
$db = $database->getConnection();

// Get item ID from query parameter and sanitize
$item_id = isset($_GET['id']) ? filter_var($_GET['id'], FILTER_SANITIZE_NUMBER_INT) : null;

if (empty($item_id)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Please provide an item ID."
    ]);
    exit();
}

try {
    // Get item details
    $query = "SELECT 
        i.id,
        i.title,
        i.description,
        i.category,
        i.location,
        i.date,
        i.type,
        i.image_path,
        i.created_at,
        i.user_id,
        u.username AS owner_name
        FROM items i 
        JOIN users u ON i.user_id = u.id 
        WHERE i.id = ?";
    
    $stmt = $db->prepare($query);
    $stmt->execute([$item_id]);
    
    if ($stmt->rowCount() > 0) {
        $item = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // Add full URL to image path if exists
        if (!empty($item['image_path'])) {
            $item['image_path'] = SITE_URL . $item['image_path'];
        }
        
        http_response_code(200);
        echo json_encode([
            "success" => true,
            "item" => $item
        ]);
    } else {
        http_response_code(404);
        echo json_encode([
            "success" => false,
            "message" => "Item not found."
        ]);
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "An error occurred while retrieving item details"
    ]);
}
?>