<?php
header("Content-Type: application/json");
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    die(json_encode(['success' => false, 'message' => 'Method not allowed']));
}

if (!isset($_GET['user_id'])) {
    http_response_code(400);
    die(json_encode(['success' => false, 'message' => 'Missing user_id parameter']));
}

$user_id = $_GET['user_id'];

// Retrieve user members (all details)
$stmt = $conn->prepare("SELECT member_id, member_name, age, height, weight, gender, activity_factor, bmr, tdee, cuisine_type FROM user_members WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$members = [];
while ($row = $result->fetch_assoc()) {
    $members[] = $row;
}

$response = [
    'success' => true,
    'members' => $members
];

echo json_encode($response);
?> 