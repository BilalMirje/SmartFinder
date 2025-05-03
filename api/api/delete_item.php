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

if (!isset($data->item_id)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Item ID is required"
    ]);
    exit();
}

try {
    // First check if the item exists and belongs to the user
    $query = "SELECT id, image_path FROM items WHERE id = ? AND user_id = ?";
    $stmt = $db->prepare($query);
    $stmt->execute([$data->item_id, $user_id]);
    
    if ($stmt->rowCount() == 0) {
        http_response_code(403);
        echo json_encode([
            "success" => false,
            "message" => "You can only delete your own items"
        ]);
        exit();
    }

    // Get item details for image deletion
    $item = $stmt->fetch(PDO::FETCH_ASSOC);

    // Start transaction
    $db->beginTransaction();

    // Delete related chat messages first (due to foreign key constraint)
    $delete_messages = "DELETE FROM chat_messages WHERE item_id = ?";
    $stmt = $db->prepare($delete_messages);
    $stmt->execute([$data->item_id]);

    // Delete the item
    $delete_item = "DELETE FROM items WHERE id = ?";
    $stmt = $db->prepare($delete_item);
    $stmt->execute([$data->item_id]);

    // Delete the associated image file if it exists
    if (!empty($item['image_path'])) {
        $image_file = $_SERVER['DOCUMENT_ROOT'] . $item['image_path'];
        if (file_exists($image_file)) {
            unlink($image_file);
        }
    }

    // Commit transaction
    $db->commit();

    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Item deleted successfully"
    ]);
} catch (Exception $e) {
    // Rollback transaction on error
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "An error occurred while deleting the item"
    ]);
}
?>