<?php
header("Content-Type: application/json");
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

if (isset($_GET['user_id'])) {
    $user_id = $_GET['user_id'];

    $stmt = $conn->prepare("SELECT allergy_ingredients, age, height, weight, cuisine_type FROM user_preferences WHERE user_id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($row = $result->fetch_assoc()) {
        echo json_encode(["success" => true, "data" => $row]);
    } else {
        echo json_encode(["success" => false, "message" => "No data found"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "user_id required"]);
}
?>
