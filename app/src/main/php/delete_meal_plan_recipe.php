<?php
header('Content-Type: application/json');
$host = "localhost";
$username = "root";
$password = "";
$database = "finalyearproject";

$conn = new mysqli($host, $username, $password, $database);
if ($conn->connect_error) {
    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit();
}

$response = array();

$input = json_decode(file_get_contents('php://input'), true);
$id = $input['id'] ?? ($_POST['id'] ?? null);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if ($id) {
        $del = $conn->prepare("DELETE FROM meal_plan_recipe WHERE id = ?");
        $del->bind_param("i", $id);
        $del->execute();
        if ($del->affected_rows > 0) {
            $response['status'] = "success";
            $response['message'] = "Recipe removed from meal plan.";
        } else {
            $response['status'] = "error";
            $response['message'] = "No matching recipe found to delete.";
        }
        $del->close();
    } else {
        $response['status'] = "error";
        $response['message'] = "Missing required parameter: id.";
    }
} else {
    $response['status'] = "error";
    $response['message'] = "Invalid request method.";
}

echo json_encode($response);
?>