<?php
require_once 'cors.php';
require_once 'config.php';
require_once 'Database.php';
require_once 'auth.php';

// Check if user is logged in
$user = checkAuth();
$user_id = $user['user_id'];

$data = json_decode(file_get_contents("php://input"));
$response = array();

if (!empty($data->title) && !empty($data->type) && in_array($data->type, ['lost', 'found'])) {
    $database = new Database();
    $db = $database->getConnection();

    try {
        // Handle file upload if image is provided
        $image_path = null;
        if (!empty($data->image)) {
            $image_data = base64_decode($data->image);
            if ($image_data !== false) {
                $file_name = uniqid() . '.jpg';
                $target_file = UPLOAD_DIR . $file_name;
                
                // Ensure uploads directory exists
                if (!file_exists(UPLOAD_DIR)) {
                    mkdir(UPLOAD_DIR, 0777, true);
                }
                
                if (file_put_contents($target_file, $image_data)) {
                    $image_path = '/uploads/' . $file_name;  // Store relative path
                }
            }
        }

        $stmt = $db->prepare("
            INSERT INTO items (user_id, title, description, category, location, date, type, image_path) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ");

        $params = [
            $user_id,
            sanitizeInput($data->title),
            sanitizeInput($data->description ?? null),
            sanitizeInput($data->category ?? null),
            sanitizeInput($data->location ?? null),
            sanitizeInput($data->date ?? null),
            $data->type,
            $image_path
        ];

        if ($stmt->execute($params)) {
            $item_id = $db->lastInsertId();
            
            // Get the inserted item
            $stmt = $db->prepare("SELECT * FROM items WHERE id = ?");
            $stmt->execute([$item_id]);
            $item = $stmt->fetch(PDO::FETCH_ASSOC);
            
            // Add the full URL to the image path
            if (!empty($item['image_path'])) {
                $item['image_path'] = SITE_URL . $item['image_path'];
            }
            
            http_response_code(201);
            $response["success"] = true;
            $response["message"] = "Item added successfully";
            $response["item"] = $item;
        } else {
            $error = $stmt->errorInfo();
            http_response_code(500);
            $response["success"] = false;
            $response["message"] = "Unable to add item";
        }
    } catch (Exception $e) {
        http_response_code(500);
        $response["success"] = false;
        $response["message"] = "Server error occurred";
    }
} else {
    http_response_code(400);
    $response["success"] = false;
    $response["message"] = "Missing required fields or invalid type";
}

echo json_encode($response);
?>