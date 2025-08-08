<?php
header("Content-Type: application/json");
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

$input = json_decode(file_get_contents('php://input'), true);

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    die(json_encode(['success' => false, 'message' => 'Method not allowed']));
}

if (json_last_error() !== JSON_ERROR_NONE) {
    http_response_code(400);
    die(json_encode(['success' => false, 'message' => 'Invalid JSON']));
}

// Required fields
$required = ['user_id', 'member_name', 'age', 'height', 'weight', 'gender', 'activity_factor', 'cuisine_type'];
foreach ($required as $field) {
    if (!isset($input[$field])) {
        http_response_code(400);
        die(json_encode(['success' => false, 'message' => "Missing field: $field"]));
    }
}

$user_id = $input['user_id'];
$member_name = $input['member_name'];
$age = $input['age'];
$height = $input['height'];
$weight = $input['weight'];
$gender = strtolower($input['gender']);
$activity_factor = $input['activity_factor'];
$cuisine_type = $input['cuisine_type'];

// Calculate BMR
if ($gender === 'male') {
    $bmr = 88.362 + (13.397 * $weight) + (4.799 * $height) - (5.677 * $age);
} elseif ($gender === 'female') {
    $bmr = 447.593 + (9.247 * $weight) + (3.098 * $height) - (4.330 * $age);
} else {
    http_response_code(400);
    die(json_encode(['success' => false, 'message' => 'Invalid gender value. Use "male" or "female".']));
}

// Calculate TDEE
$tdee = $bmr * $activity_factor;

try {
    // Insert member data
    $stmt = $conn->prepare("INSERT INTO user_members (user_id, member_name, age, height, weight, gender, activity_factor, bmr, tdee, cuisine_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("isiddssdds", $user_id, $member_name, $age, $height, $weight, $gender, $activity_factor, $bmr, $tdee, $cuisine_type);
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Member added successfully', 'bmr' => $bmr, 'tdee' => $tdee]);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Database error']);
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}
?>
