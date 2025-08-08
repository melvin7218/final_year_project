<?php
header("Content-Type: application/json");
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    die(json_encode(['status' => 'error', 'message' => 'Method not allowed']));
}

// Get POST data
$input = json_decode(file_get_contents('php://input'), true);

if (!$input || !isset($input['user_id'])) {
    http_response_code(400);
    die(json_encode(['status' => 'error', 'message' => 'Missing user_id']));
}

$user_id = $input['user_id'];

// Retrieve user's TDEE from user_preferences table
$stmt = $conn->prepare("SELECT tdee FROM user_preferences WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $tdee = $row['tdee'];
    
    echo json_encode([
        'status' => 'success',
        'tdee' => $tdee
    ]);
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'User preferences not found'
    ]);
}

$stmt->close();
$conn->close();
?> 