<?php
// Database configuration
define('DB_HOST', 'localhost');
define('DB_NAME', 'smartfinder');
define('DB_USER', 'root');
define('DB_PASS', '');

// Application Configuration
define('SITE_URL', 'http://10.0.2.2/smartfinder');
//define('SITE_URL', 'http://192.168.50.4/smartfinder');
define('UPLOAD_DIR', __DIR__ . '/../uploads/');

// Error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);

// Create log file
$log_file = __DIR__ . '/error.log';
ini_set('log_errors', 1);
ini_set('error_log', $log_file);

// Create uploads directory if it doesn't exist
if (!file_exists(UPLOAD_DIR)) {
    mkdir(UPLOAD_DIR, 0777, true);
}

// Function to log errors
function logError($message) {
    global $log_file;
    $timestamp = date('[Y-m-d H:i:s] ');
    error_log($timestamp . $message . "\n", 3, $log_file);
}

// Function to sanitize input
function sanitizeInput($data) {
    if (is_null($data)) return null;
    if (is_array($data)) {
        return array_map('sanitizeInput', $data);
    }
    return htmlspecialchars(strip_tags(trim($data)));
}

// Application Configuration
define('MAX_UPLOAD_SIZE', 5 * 1024 * 1024); // 5MB in bytes

// Allowed MIME types for uploads
define('ALLOWED_MIME_TYPES', [
    'image/jpeg',
    'image/png',
    'image/gif'
]);

// Function to validate file upload
function validateFileUpload($file) {
    if (!isset($file['error']) || is_array($file['error'])) {
        return ["status" => false, "message" => "Invalid file parameters"];
    }

    switch ($file['error']) {
        case UPLOAD_ERR_OK:
            break;
        case UPLOAD_ERR_NO_FILE:
            return ["status" => false, "message" => "No file uploaded"];
        case UPLOAD_ERR_INI_SIZE:
        case UPLOAD_ERR_FORM_SIZE:
            return ["status" => false, "message" => "File size exceeds server limit"];
        case UPLOAD_ERR_PARTIAL:
            return ["status" => false, "message" => "File was only partially uploaded"];
        case UPLOAD_ERR_NO_TMP_DIR:
            return ["status" => false, "message" => "Missing temporary folder"];
        case UPLOAD_ERR_CANT_WRITE:
            return ["status" => false, "message" => "Failed to write file to disk"];
        case UPLOAD_ERR_EXTENSION:
            return ["status" => false, "message" => "File upload stopped by extension"];
        default:
            return ["status" => false, "message" => "Unknown upload error"];
    }

    if ($file['size'] > MAX_UPLOAD_SIZE) {
        return ["status" => false, "message" => "File size exceeds limit (5MB)"];
    }

    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mime_type = $finfo->file($file['tmp_name']);

    if (!in_array($mime_type, ALLOWED_MIME_TYPES)) {
        return ["status" => false, "message" => "Invalid file type. Only JPG, PNG and GIF are allowed."];
    }

    return ["status" => true, "message" => "File is valid"];
}

// Function to get file extension from MIME type
function getExtensionFromMimeType($mime_type) {
    $extensions = [
        'image/jpeg' => 'jpg',
        'image/png' => 'png',
        'image/gif' => 'gif'
    ];
    return isset($extensions[$mime_type]) ? $extensions[$mime_type] : 'jpg';
}
?> 