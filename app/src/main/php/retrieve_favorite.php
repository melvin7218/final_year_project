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

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;

if (!$user_id) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

$stmt = $conn->prepare("
    SELECT r.id, r.title, r.image_url, r.cuisine_type, r.time_recipe, r.visibility 
    FROM recipes r
    INNER JOIN favorite_recipes fr ON r.id = fr.recipe_id
    WHERE fr.user_id = ?
");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$recipes = [];
while ($row = $result->fetch_assoc()) {
    $recipes[] = $row;
}

echo json_encode(["status" => "success", "recipes" => $recipes]);

$stmt->close();
$conn->close();
?>
