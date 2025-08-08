<?php
header("Content-Type: application/json");

// Database Connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

// Get Input Data Securely
$user = isset($_POST['username']) ? trim($_POST['username']) : null;
$pass = isset($_POST['password']) ? trim($_POST['password']) : null;

// Input Validation
if (empty($user)) {
    echo json_encode(["status" => "error", "message" => "Username is required"]);
    exit();
}

if (empty($pass)) {
    echo json_encode(["status" => "error", "message" => "Password is required"]);
    exit();
}

// Use Prepared Statement to Prevent SQL Injection
$sql = "SELECT user_id, password FROM users WHERE username = ?";
$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode(["status" => "error", "message" => "Database query preparation failed"]);
    exit();
}

$stmt->bind_param("s", $user);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();

    // Verify Password
    if (password_verify($pass, $row['password'])) {
        // Login successful
        echo json_encode([
            "status" => "success",
            "message" => "Login successful",
            "user_id" => $row['user_id']
        ]);
    } else {
        // Invalid password
        echo json_encode(["status" => "error", "message" => "Invalid password"]);
    }
} else {
    // Username not found
    echo json_encode(["status" => "error", "message" => "Username not found"]);
}

$stmt->close();
$conn->close();
?>