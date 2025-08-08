<?php
header('Content-Type: application/json');

// Database connection
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


// Get POST data
$data = json_decode(file_get_contents('php://input'), true);
if (!isset($data['recipe_ids']) || !is_array($data['recipe_ids'])) {
    echo json_encode(['status' => 'error', 'message' => 'No recipe IDs provided']);
    exit;
}

$recipeIds = $data['recipe_ids'];
if (count($recipeIds) === 0) {
    echo json_encode(['status' => 'error', 'message' => 'Empty recipe ID list']);
    exit;
}

// Prepare SQL to get all ingredients for the given recipes
$placeholders = implode(',', array_fill(0, count($recipeIds), '?'));
$sql = "
    SELECT ingredient_name, amount
    FROM recipe_ingredients
    WHERE recipe_id IN ($placeholders)
";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(['status' => 'error', 'message' => 'SQL prepare failed']);
    exit;
}

// Bind parameters dynamically
$types = str_repeat('i', count($recipeIds));
$stmt->bind_param($types, ...$recipeIds);

$stmt->execute();
$result = $stmt->get_result();

$ingredientMap = [];
while ($row = $result->fetch_assoc()) {
    $name = strtolower(trim($row['ingredient_name']));
    $amount = $row['amount'];

    // If you want to sum numeric values, you need to parse and add them here.
    // For now, just concatenate amounts for the same ingredient.
    if (!isset($ingredientMap[$name])) {
        $ingredientMap[$name] = [];
    }
    $ingredientMap[$name][] = $amount;
}

$ingredients = [];
foreach ($ingredientMap as $name => $amounts) {
    // You can improve this to sum numeric values if your data is standardized.
    $totalAmount = implode(' + ', $amounts);
    $ingredients[] = [
        'name' => $name,
        'amount' => $totalAmount
    ];
}

echo json_encode([
    'status' => 'success',
    'ingredients' => $ingredients
]);

$stmt->close();
$conn->close();
?>