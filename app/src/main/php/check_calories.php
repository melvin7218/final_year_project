<?php
header("Content-Type: application/json");

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);
$user_id = $data['user_id'] ?? null;
$meal_date = $data['meal_date'] ?? null;

if (!$user_id || !$meal_date) {
    echo json_encode(["status" => "error", "message" => "Missing parameters"]);
    exit;
}

// Get meal_plan ID
$query = "SELECT id FROM meal_plan WHERE user_id = ? AND meal_date = ?";
$stmt = $conn->prepare($query);
$stmt->bind_param("is", $user_id, $meal_date);
$stmt->execute();
$result = $stmt->get_result();
$row = $result->fetch_assoc();
$mealPlanId = $row['id'] ?? null;

if (!$mealPlanId) {
    echo json_encode(["status" => "error", "message" => "Meal plan not found"]);
    exit;
}

// Calculate total calories
$calQuery = "SELECT SUM(rn.calories) AS total_calories
             FROM meal_plan_recipe mpr
             JOIN recipe_nutrition rn ON mpr.recipe_id = rn.recipe_id
             WHERE mpr.meal_plan_id = ?";
$stmt = $conn->prepare($calQuery);
$stmt->bind_param("i", $mealPlanId);
$stmt->execute();
$result = $stmt->get_result();
$row = $result->fetch_assoc();
$totalCalories = $row['total_calories'] ?? 0;

// Update meal_plan
$update = "UPDATE meal_plan SET total_calories = ? WHERE id = ?";
$stmt = $conn->prepare($update);
$stmt->bind_param("ii", $totalCalories, $mealPlanId);
$stmt->execute();

echo json_encode(["status" => "success", "updated_total" => $totalCalories]);
?>
