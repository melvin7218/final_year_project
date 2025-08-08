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
$required_fields = ['user_id', 'exercise_record_id'];
foreach ($required_fields as $field) {
    if (!isset($input[$field]) || empty($input[$field])) {
        http_response_code(400);
        die(json_encode(['status' => 'error', 'message' => 'Missing required field: ' . $field]));
    }
}

$user_id = $input['user_id'];
$exercise_record_id = $input['exercise_record_id'];

try {
    // Delete the exercise record
    $stmt = $conn->prepare("DELETE FROM user_exercise WHERE id = ? AND user_id = ?");
    $stmt->bind_param("ii", $exercise_record_id, $user_id);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            $stmt->close();
            $conn->close();
            
            echo json_encode([
                'status' => 'success',
                'message' => 'Exercise deleted successfully'
            ]);
        } else {
            $stmt->close();
            $conn->close();
            
            echo json_encode([
                'status' => 'error',
                'message' => 'Exercise not found or you do not have permission to delete it'
            ]);
        }
    } else {
        throw new Exception("Failed to delete exercise record");
    }
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $e->getMessage()]);
}
?>