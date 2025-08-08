<?php
header("Content-Type: application/json");
$servername = "localhost";
$username = "root"; // Replace with your database username
$password = ""; // Replace with your database password
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);


$input = json_decode(file_get_contents('php://input'), true);

$username = $input['username'] ?? '';

if (empty($username)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Username is required']);
    exit;
}

try {
    $stmt = $conn->prepare("SELECT user_id FROM users WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        echo json_encode(['success' => true, 'user_id' => $user['user_id']]);
    } else {
        http_response_code(404);
        echo json_encode(['success' => false, 'message' => 'User not found']);
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error: ' . $e->getMessage()]);
}

$stmt->close();
$conn->close();
?>