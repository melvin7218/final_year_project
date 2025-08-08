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

$stmt = $conn->prepare("SELECT 1 FROM favorite_recipes WHERE user_id = ? AND recipe_id = ? LIMIT 1");
$stmt->bind_param("ii", $user_id, $recipe_id);
$stmt->execute();
$stmt->store_result();
$is_favorite = $stmt->num_rows > 0;
$stmt->close();
$conn->close();

echo json_encode([
    "status" => "success",
    "is_favorite" => $is_favorite
]);
?> 