<?php
header("Content-Type: application/json");

$servername = "localhost";
$username = "root";
$password = "";
$database = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $database);
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed: " . $conn->connect_error]));
}

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

$user_id = $_GET["user_id"] ?? null;

if (!$user_id) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

try {
    $stmt = $conn->prepare("
        SELECT 
            g.ingredient_name, 
            g.amount, 
            r.title AS recipe_name,
            r.image_url AS recipe_image,
            g.purchased
        FROM grocery_lists g
        LEFT JOIN recipes r ON g.recipe_id = r.id
        WHERE g.user_id = ? AND g.purchased = 0
    ");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $groceryList = [];
    while ($row = $result->fetch_assoc()) {
        $groceryList[] = $row;
    }

    echo json_encode([
        "status" => "success",
        "data" => $groceryList
    ]);
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
?>
