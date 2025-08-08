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

// Get the input data
$data = json_decode(file_get_contents('php://input'), true);
$ingredientName = $data['ingredient_name'];
$userId = $data['user_id'];

// Use correct connection variable
$stmt = $conn->prepare("DELETE FROM grocery_lists WHERE ingredient_name = ? AND user_id = ?");
$stmt->bind_param("si", $ingredientName, $userId);

if ($stmt->execute()) {
    echo json_encode(['status' => 'success', 'message' => 'Item deleted.']);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Delete failed: ' . $stmt->error]);
}
?>
