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
    die(json_encode([
        'status' => 'error',
        'message' => 'Connection failed: ' . $conn->connect_error
    ]));
}


$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data["user_id"], $data["profile_picture"])) {
    echo json_encode(["success" => false, "message" => "Missing user_id or profile_picture"]);
    exit();
}

$user_id = $data["user_id"];
$image_data = $data["profile_picture"];
$image_url = '';

if (!empty($image_data)) {
    $filename = 'profile_' . uniqid() . '.' . time() . '.jpg';
    $upload_path = 'uploads/' . $filename;

    if (!file_exists('uploads')) {
        mkdir('uploads', 0777, true);
    }

    $decoded_image = base64_decode($image_data);

    if (file_put_contents($upload_path, $decoded_image)) {
        $image_url = 'http://' . $_SERVER['HTTP_HOST'] . '/Final%20Year%20Project/uploads/' . $filename;
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Failed to save image'
        ]);
        exit;
    }
}

$stmt = $conn->prepare("UPDATE users SET profile_picture = ? WHERE user_id = ?");
$stmt->bind_param("si", $image_url, $user_id);

if ($stmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => 'Profile picture updated',
        'profile_picture_url' => $image_url
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Failed to update database'
    ]);
}
?>
