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
    die(json_encode(['success' => false, 'message' => 'Method not allowed']));
}

// Get POST data
$input = json_decode(file_get_contents('php://input'), true);

if (!$input) {
    http_response_code(400);
    die(json_encode(['success' => false, 'message' => 'Invalid JSON data']));
}

// Validate required fields
$required_fields = ['user_id', 'member_name', 'age', 'height', 'weight', 'gender', 'activity_factor'];
foreach ($required_fields as $field) {
    if (!isset($input[$field]) || empty($input[$field])) {
        http_response_code(400);
        die(json_encode(['success' => false, 'message' => "Missing required field: $field"]));
    }
}

$user_id = $input['user_id'];
$member_name = $input['member_name'];
$age = $input['age'];
$height = $input['height'];
$weight = $input['weight'];
$gender = $input['gender'];
$activity_factor = $input['activity_factor'];
$cuisine_type = isset($input['cuisine_type']) ? $input['cuisine_type'] : null;
$allergy_ingredients = isset($input['allergy_ingredients']) ? $input['allergy_ingredients'] : null;

// Calculate BMR using Mifflin-St Jeor Equation
$bmr = 0;
if (strtolower($gender) === 'male') {
    $bmr = (10 * $weight) + (6.25 * $height) - (5 * $age) + 5;
} else {
    $bmr = (10 * $weight) + (6.25 * $height) - (5 * $age) - 161;
}

// Calculate TDEE
$tdee = $bmr * $activity_factor;

// Check if we're updating an existing member or creating a new one
if (isset($input['member_id']) && !empty($input['member_id'])) {
    // Update existing member
    $member_id = $input['member_id'];
    
    $stmt = $conn->prepare("UPDATE user_members SET member_name = ?, age = ?, height = ?, weight = ?, gender = ?, activity_factor = ?, bmr = ?, tdee = ?, cuisine_type = ?, allergy_ingredients = ? WHERE user_id = ? AND member_id = ?");
    $stmt->bind_param("siiissddssii", $member_name, $age, $height, $weight, $gender, $activity_factor, $bmr, $tdee, $cuisine_type, $allergy_ingredients, $user_id, $member_id);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode(['success' => true, 'message' => 'Member updated successfully']);
        } else {
            echo json_encode(['success' => false, 'message' => 'Member not found or no changes made']);
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $stmt->error]);
    }
} else {
    // Create new member
    $stmt = $conn->prepare("INSERT INTO user_members (user_id, member_name, age, height, weight, gender, activity_factor, bmr, tdee, cuisine_type, allergy_ingredients) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("isiiissddss", $user_id, $member_name, $age, $height, $weight, $gender, $activity_factor, $bmr, $tdee, $cuisine_type, $allergy_ingredients);
    
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Member created successfully', 'member_id' => $stmt->insert_id]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $stmt->error]);
    }
}

$stmt->close();
$conn->close();
?>
