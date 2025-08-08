<?php
header("Content-Type: application/json");

// Database configuration
$servername = "localhost";
$username = "root";
$password = "";
$database = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die(json_encode([
        "status" => "error", 
        "message" => "Database connection failed: " . $conn->connect_error
    ]));
}
$user_id = $_GET['user_id'];
$meal_date = $_GET['meal_date'];

// 1. Get TDEE from user_preference
$tdee_query = $conn->prepare("SELECT tdee FROM user_preferences WHERE user_id = ?");
$tdee_query->bind_param("i", $user_id);
$tdee_query->execute();
$tdee_result = $tdee_query->get_result();
$tdee = $tdee_result->fetch_assoc()['tdee'] ?? 0;

// 2. Get total calories directly from meal_plan
$cal_query = $conn->prepare("SELECT total_calories FROM meal_plans WHERE user_id = ? AND meal_date = ?");
$cal_query->bind_param("is", $user_id, $meal_date);
$cal_query->execute();
$cal_result = $cal_query->get_result();
$total_calories = $cal_result->fetch_assoc()['total_calories'] ?? 0;

// Return response
echo json_encode([
    "tdee" => (int)$tdee,
    "total_calories" => (int)$total_calories,
    "status" => "success"
]);
?>