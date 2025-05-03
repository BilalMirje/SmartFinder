<?php
require_once 'cors.php';
require_once 'config.php';
require_once 'Database.php';
require_once 'auth.php';

// Check if user is logged in
$user = checkAuth();

$database = new Database();
$db = $database->getConnection();

// Get the type parameter (lost or found)
$type = isset($_GET['type']) ? sanitizeInput($_GET['type']) : null;

try {
    // Build the query
    $query = "SELECT * FROM items WHERE 1=1";
    $params = array();

    if ($type && in_array($type, ['lost', 'found'])) {
        $query .= " AND type = ?";
        $params[] = $type;
    }

    $query .= " ORDER BY created_at DESC";

    $stmt = $db->prepare($query);
    $stmt->execute($params);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Add full URL to image paths
    foreach ($items as &$item) {
        if (!empty($item['image_path'])) {
            $item['image_path'] = SITE_URL . $item['image_path'];
            logError("Image URL created: " . $item['image_path']);
        }
    }

    http_response_code(200);
    echo json_encode([
        "success" => true,
        "items" => $items
    ]);

} catch (Exception $e) {
    
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error retrieving items: " . $e->getMessage()
    ]);
}
?> 