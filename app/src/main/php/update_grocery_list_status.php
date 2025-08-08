<?php
header("Content-Type: application/json");

$servername = "localhost";
$username = "root";
$password = "";
$database = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $database);
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed."]));
}

$data = json_decode(file_get_contents("php://input"), true);

$ingredient_name = $data["ingredient_name"] ?? "";
$purchased = $data["purchased"] ?? false;

if (empty($ingredient_name)) {
    echo json_encode(["status" => "error", "message" => "Missing ingredient name."]);
    exit;
}

try {
    $stmt = $conn->prepare("UPDATE grocery_lists SET purchased = ? WHERE ingredient_name = ?");
    $stmt->bind_param("is", $purchased, $ingredient_name);
    $stmt->execute();

    echo json_encode(["status" => "success", "message" => "Purchase status updated."]);
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
?>
