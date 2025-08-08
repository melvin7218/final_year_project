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
$required = ['user_id', 'allergy_ingredients', 'cuisine_type', 'age', 'height', 'weight', 'gender', 'activity_factor'];
foreach ($required as $field) {
    if (!isset($input[$field])) {
        http_response_code(400);
        die(json_encode(['success' => false, 'message' => "Missing field: $field"]));
    }
}

try {
    // Check if user exists
    $stmt = $conn->prepare("SELECT user_id FROM users WHERE user_id = ?");
    $stmt->bind_param("i", $input['user_id']);
    $stmt->execute();
    
    if ($stmt->get_result()->num_rows === 0) {
        http_response_code(404);
        die(json_encode(['success' => false, 'message' => 'User not found']));
    }

    // Insert/update preferences
    $stmt = $conn->prepare("INSERT INTO family_preferences
    (user_id, allergy_ingredients, age, height, weight, gender, activity_factor, bmr, tdee, cuisine_type) 
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE
    allergy_ingredients = VALUES(allergy_ingredients),
    age = VALUES(age),
    height = VALUES(height),
    weight = VALUES(weight),
    gender = VALUES(gender),
    activity_factor = VALUES(activity_factor),
    bmr = VALUES(bmr),
    tdee = VALUES(tdee),
    cuisine_type = VALUES(cuisine_type)");

    $stmt->bind_param("isiddssdds", 
        $input['user_id'],
        $input['allergy_ingredients'],
        $input['age'],
        $input['height'],
        $input['weight'],
        $input['gender'],
        $input['activity_factor'],
        $input['bmr'],
        $input['tdee'],
        $input['cuisine_type']);

    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Preferences saved']);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Database error']);
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}
?>