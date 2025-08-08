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

// Retrieve user members from user_members table
$stmt = $conn->prepare("SELECT member_id, member_name, age, height, weight, gender, activity_factor, bmr, tdee, cuisine_type FROM user_members WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$members = [];
while ($row = $result->fetch_assoc()) {
    $members[] = $row;
}

if (count($members) > 0) {
    echo json_encode([
        'status' => 'success',
        'members' => $members
    ]);
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'No members found for this user'
    ]);
}

$stmt->close();
$conn->close();
?>