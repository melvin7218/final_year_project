<?php
$host = "localhost";
$username = "root";
$password = "";
$database = "finalyearproject";

// Create database connection
$conn = new mysqli($host, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit();
}

header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);
$recipe_id = $input['recipe_id'] ?? null;

if (!$recipe_id) {
    echo json_encode(['status' => 'error', 'message' => 'Missing recipe_id']);
    exit;
}

// First delete related meal plan entries manually
$stmt1 = $conn->prepare("DELETE FROM meal_plan_recipe WHERE recipe_id = ?");
$stmt1->bind_param("i", $recipe_id);
if (!$stmt1->execute()) {
    echo json_encode(['status' => 'error', 'message' => 'Failed to delete from meal_plan_recipe', 'error' => $stmt1->error]);
    exit;
}

// Then delete from recipes table
$stmt2 = $conn->prepare("DELETE FROM recipes WHERE id = ?");
$stmt2->bind_param("i", $recipe_id);
if ($stmt2->execute()) {
    echo json_encode(['status' => 'success', 'message' => 'Recipe deleted successfully']);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Failed to delete recipe', 'error' => $stmt2->error]);
}
?>
