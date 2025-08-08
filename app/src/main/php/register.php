<?php
header("Content-Type: application/json");
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $username = $_POST['username'];
    $email = $_POST['email'];
    $password = password_hash($_POST['password'], PASSWORD_BCRYPT);

    // Check if username or email already exists
    $checkUser = $conn->prepare("SELECT user_id FROM users WHERE username = ? OR email = ?");
    $checkUser->bind_param("ss", $username, $email);
    $checkUser->execute();
    $checkUser->store_result();

    if ($checkUser->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "Username or email already exists"]);
    } else {
        // Insert new user
        $stmt = $conn->prepare("INSERT INTO users (username, password, email) VALUES (?, ?, ?)");
        $stmt->bind_param("sss", $username, $password, $email);

        if ($stmt->execute()) {
            $user_id = $stmt->insert_id;
            echo json_encode([
                "status" => "success", 
                "message" => "Registration successful",
                "user_id" => $user_id
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Failed to register"]);
        }
    }
    $checkUser->close();
    $stmt->close();
}
$conn->close();
?>