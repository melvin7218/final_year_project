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

$stmt = $conn->prepare("DELETE FROM favorite_recipes WHERE user_id = ? AND recipe_id = ?");
$stmt->bind_param("ii", $user_id, $recipe_id);
if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["status" => "success", "message" => "Recipe removed from favorites"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Favorite not found"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Failed to remove favorite"]);
}
$stmt->close();
$conn->close();
?> 