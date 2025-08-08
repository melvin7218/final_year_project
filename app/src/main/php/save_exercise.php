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
    die(json_encode(['status' => 'error', 'message' => 'Connection failed: ' . $conn->connect_error]));
}

// Check if it's a POST request
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    die(json_encode(['status' => 'error', 'message' => 'Method not allowed']));
}

// Get JSON input
$input = json_decode(file_get_contents('php://input'), true);

// Validate required fields
$required_fields = ['user_id', 'exercise_name', 'exercise_date', 'start_time', 'duration_minutes'];
foreach ($required_fields as $field) {
    if (!isset($input[$field]) || empty($input[$field])) {
        http_response_code(400);
        die(json_encode(['status' => 'error', 'message' => 'Missing required field: ' . $field]));
    }
}

$user_id = $input['user_id'];
$exercise_name = $input['exercise_name'];
$exercise_date = $input['exercise_date'];
$start_time = $input['start_time'];
$duration_minutes = intval($input['duration_minutes']);
$member_ids = $input['member_ids'] ?? [];

// Validate duration
if ($duration_minutes <= 0) {
    http_response_code(400);
    die(json_encode(['status' => 'error', 'message' => 'Duration must be greater than 0']));
}

try {
    // First, get the exercise_id and MET from the exercise table based on name
    $stmt = $conn->prepare("SELECT id, MET FROM exercise WHERE name = ?");
    $stmt->bind_param("s", $exercise_name);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        http_response_code(400);
        die(json_encode(['status' => 'error', 'message' => 'Exercise not found: ' . $exercise_name]));
    }
    
    $exercise_row = $result->fetch_assoc();
    $exercise_id = $exercise_row['id'];
    $met = $exercise_row['MET'];
    $stmt->close();
    
    // Get user weight from user_preferences table
    $weight_stmt = $conn->prepare("SELECT weight FROM user_preferences WHERE user_id = ?");
    $weight_stmt->bind_param("i", $user_id);
    $weight_stmt->execute();
    $weight_result = $weight_stmt->get_result();
    
    $user_weight = 70.0; // Default weight if not found
    if ($weight_row = $weight_result->fetch_assoc()) {
        $user_weight = $weight_row['weight'];
    }
    $weight_stmt->close();
    
    // Calculate end time from start time and duration
    $start_timestamp = strtotime($start_time);
    if ($start_timestamp === false) {
        http_response_code(400);
        die(json_encode(['status' => 'error', 'message' => 'Invalid start time format']));
    }
    
    $end_timestamp = $start_timestamp + ($duration_minutes * 60); // Add duration in seconds
    $end_time = date('H:i:s', $end_timestamp);
    
    $duration_hours = $duration_minutes / 60.0;
    
    // Calculate calories burned using the formula: Calories burned = MET × Weight (kg) × Duration (hours)
    $calories_burned = $met * $user_weight * $duration_hours;
    
    // Insert into user_exercise table with start and end times
    $stmt = $conn->prepare("INSERT INTO user_exercise (user_id, exercise_id, duration_minutes, calories_burned, exercise_date, starting_time, ending_time) VALUES (?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("iiddsss", $user_id, $exercise_id, $duration_minutes, $calories_burned, $exercise_date, $start_time, $end_time);
    
    if ($stmt->execute()) {
        $exercise_record_id = $conn->insert_id;
        
        // If member_ids are provided, you can create additional records for each member
        // or create a separate table to track which members participated
        if (!empty($member_ids)) {
            // You might want to create a user_exercise_members table to track this
            // For now, we'll just log that members were included
            error_log("Exercise saved with members: " . implode(',', $member_ids));
        }
        
        $stmt->close();
        $conn->close();
        
        echo json_encode([
            'status' => 'success',
            'message' => 'Exercise saved successfully',
            'exercise_id' => $exercise_record_id,
            'calculated_data' => [
                'duration_minutes' => $duration_minutes,
                'duration_hours' => $duration_hours,
                'start_time' => $start_time,
                'end_time' => $end_time,
                'calories_burned' => round($calories_burned, 2),
                'met' => $met,
                'user_weight' => $user_weight
            ]
        ]);
    } else {
        throw new Exception("Failed to insert exercise record");
    }
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $e->getMessage()]);
}
?> 