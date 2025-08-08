<?php
header('Content-Type: application/json; charset=UTF-8');
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
$user_id = isset($input['user_id']) ? intval($input['user_id']) : null;
$start_date = isset($input['start_date']) ? $input['start_date'] : null;
$end_date = isset($input['end_date']) ? $input['end_date'] : null;
$member_id = isset($input['member_id']) ? intval($input['member_id']) : null;

if (!$user_id || !$start_date || !$end_date) {
    echo json_encode(["status" => "error", "message" => "Missing parameters"]);
    exit;
}

// Get all days in the range
$period = new DatePeriod(
    new DateTime($start_date),
    new DateInterval('P1D'),
    (new DateTime($end_date))->modify('+1 day')
);

$results = [];

foreach ($period as $date) {
    $day = $date->format('Y-m-d');
    if ($member_id) {
        // For member: sum calories from meal_plan_nutrition for this member and day
        $stmt = $conn->prepare("
            SELECT SUM(n.calories) 
            FROM meal_plan_nutrition n
            JOIN meal_plan_recipe r ON n.meal_plan_recipe_id = r.id
            WHERE n.member_id = ? AND r.date = ? AND r.user_id = ?
        ");
        $stmt->bind_param("isi", $member_id, $day, $user_id);
    } else {
        // For user: sum calories from meal_plan_recipe_user for this user and day
        $stmt = $conn->prepare("
            SELECT SUM(calories) 
            FROM meal_plan_recipe_user 
            WHERE user_id = ? AND meal_date = ?
        ");
        $stmt->bind_param("is", $user_id, $day);
    }
    $stmt->execute();
    $stmt->bind_result($calories);
    if ($stmt->fetch() && $calories !== null) {
        $results[$day] = floatval($calories);
    } else {
        $results[$day] = 0;
    }
    $stmt->close();
}

echo json_encode([
    "status" => "success",
    "data" => $results
]);

$conn->close();
?>