<?php
header("Content-Type: application/json");

$conn = new mysqli("localhost", "root", "", "finalyearproject");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Database connection failed"]);
    exit();
}

$user_id = $_GET['user_id'] ?? null;

if (!$user_id) {
    echo json_encode(["success" => false, "message" => "Missing user_id"]);
    exit();
}

$stmt = $conn->prepare("SELECT username, email, profile_picture FROM users WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    // If profile_picture is just filename, convert to full URL
    if (!empty($row['profile_picture']) && !str_starts_with($row['profile_picture'], 'http')) {
        $row['profile_picture'] = 'http://' . $_SERVER['HTTP_HOST'] . '/Final%20Year%20Project/uploads/' . $row['profile_picture'];
    }

    echo json_encode([
        "success" => true,
        "data" => $row
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "User not found"
    ]);
}
?>
