<?php
header('Content-Type: application/json; charset=UTF-8');
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
$user_id = isset($input['user_id']) ? intval($input['user_id']) : null;
$recipe_id = isset($input['recipe_id']) ? intval($input['recipe_id']) : null;

if (!$user_id || !$recipe_id) {
    echo json_encode(["status" => "error", "message" => "Missing user_id or recipe_id"]);
    exit;
}

// Insert favorite (ignore if already exists)
$stmt = $conn->prepare("INSERT INTO favorite_recipes (user_id, recipe_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP");
$stmt->bind_param("ii", $user_id, $recipe_id);
if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Recipe added to favorites"]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to add favorite"]);
}
$stmt->close();
$conn->close();
?> 