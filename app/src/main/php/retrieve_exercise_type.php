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

// Get user_id parameter
$user_id = $_GET['user_id'] ?? null;

if (!$user_id) {
    http_response_code(400);
    die(json_encode(['status' => 'error', 'message' => 'Missing user_id']));
}

try {
    // Retrieve all exercises from the exercise table
    $stmt = $conn->prepare("SELECT id, name, MET FROM exercise ORDER BY name ASC");
    $stmt->execute();
    $result = $stmt->get_result();
    
    $exercises = [];
    while ($row = $result->fetch_assoc()) {
        $exercises[] = [
            'id' => $row['id'],
            'name' => $row['name'],
            'met' => $row['MET']
        ];
    }
    
    // Get user weight from user_preferences table
    $weight_stmt = $conn->prepare("SELECT weight FROM user_preferences WHERE user_id = ?");
    $weight_stmt->bind_param("i", $user_id);
    $weight_stmt->execute();
    $weight_result = $weight_stmt->get_result();
    
    $user_weight = null;
    if ($weight_row = $weight_result->fetch_assoc()) {
        $user_weight = $weight_row['weight'];
    }
    
    $stmt->close();
    $weight_stmt->close();
    $conn->close();
    
    echo json_encode([
        'status' => 'success',
        'exercises' => $exercises,
        'user_weight' => $user_weight
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $e->getMessage()]);
}
?>