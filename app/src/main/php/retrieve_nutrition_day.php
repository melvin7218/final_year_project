// retrieve_meal_plan_nutrition.php
<?php
header("Content-Type: application/json");
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";
$conn = new mysqli($servername, $username, $password, $dbname);

$user_id = $_GET['user_id'] ?? null;
$meal_date = $_GET['meal_date'] ?? null;
if (!$user_id || !$meal_date) {
    http_response_code(400);
    die(json_encode(['status' => 'error', 'message' => 'Missing user_id or meal_date']));
}

// Get user calories
$userCalories = 0;
$stmt = $conn->prepare("SELECT SUM(calories) as total FROM meal_plan_recipe_user WHERE user_id = ? AND meal_date = ?");
$stmt->bind_param("is", $user_id, $meal_date);
$stmt->execute();
$stmt->bind_result($userCalories);
$stmt->fetch();
$stmt->close();

// Get member calories
$members = [];
$stmt = $conn->prepare("
    SELECT m.member_id, m.member_name, SUM(n.calories) as total
    FROM meal_plan_nutrition n
    JOIN meal_plan_recipe mpr ON n.meal_plan_recipe_id = mpr.id
    JOIN meal_plans mp ON mpr.meal_plan_id = mp.id
    JOIN user_members m ON n.member_id = m.member_id
    WHERE mp.user_id = ? AND mp.meal_date = ?
    GROUP BY m.member_id, m.member_name
");
$stmt->bind_param("is", $user_id, $meal_date);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $members[] = [
        'member_id' => $row['member_id'],
        'member_name' => $row['member_name'],
        'calories' => floatval($row['total'])
    ];
}
$stmt->close();
$conn->close();

echo json_encode([
    'status' => 'success',
    'user_calories' => floatval($userCalories),
    'members' => $members
]);
?>