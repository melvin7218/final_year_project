<?php
header("Content-Type: application/json; charset=UTF-8");

// Database credentials
$host = "localhost";
$username = "root";
$password = "";
$database = "finalyearproject";

// Create database connection
$conn = new mysqli($host, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit();
}

// Get recipe ID safely
$id = isset($_GET['id']) ? intval($_GET['id']) : 0;
if ($id <= 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid Recipe ID"
    ]);
    exit();
}

// Fetch recipe details
$query = "SELECT * FROM recipes WHERE id = ?";
$stmt = $conn->prepare($query);
if (!$stmt) {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to prepare SQL statement: " . $conn->error
    ]);
    exit();
}
$stmt->bind_param("i", $id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Recipe not found"
    ]);
    exit();
}
$recipe = $result->fetch_assoc();

// Fetch ingredients (using amount_value and unit)
$ingredients = [];
$ingredients_query = "SELECT ingredient_name, amount_value, unit FROM recipe_ingredients WHERE recipe_id = ?";
$stmt = $conn->prepare($ingredients_query);
if (!$stmt) {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to prepare ingredients SQL statement: " . $conn->error
    ]);
    exit();
}
$stmt->bind_param("i", $id);
$stmt->execute();
$ingredients_result = $stmt->get_result();
while ($row = $ingredients_result->fetch_assoc()) {
    // Format amount as a string, e.g. "100g" or just the unit if value is null
    $amount = ($row['amount_value'] !== null && $row['unit'] !== null && $row['unit'] !== '')
        ? rtrim(rtrim($row['amount_value'], '0'), '.') . $row['unit']
        : ($row['amount_value'] !== null ? $row['amount_value'] : ($row['unit'] ?? ''));
    $ingredients[] = [
        "ingredient_name" => $row['ingredient_name'],
        "amount" => $amount
    ];
}

// Fetch instructions
$instructions = [];
$instructions_query = "SELECT instruction_text FROM recipe_instructions WHERE recipe_id = ? ORDER BY step_number ASC";
$stmt = $conn->prepare($instructions_query);
if (!$stmt) {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to prepare instructions SQL statement: " . $conn->error
    ]);
    exit();
}
$stmt->bind_param("i", $id);
$stmt->execute();
$instructions_result = $stmt->get_result();
while ($row = $instructions_result->fetch_assoc()) {
    $instructions[] = $row['instruction_text'];
}

// Fetch nutrition
$nutrition = [];
$nutrition_query = "SELECT calories,protein, fat, carbohydrates, fiber, sugar, sodium FROM recipe_nutrition WHERE recipe_id = ?";
$stmt = $conn->prepare($nutrition_query);
if (!$stmt) {
    echo json_encode([
        "status" => "error",
        "message" => "Failed to prepare nutrition SQL statement: " . $conn->error
    ]);
    exit();
}
$stmt->bind_param("i", $id);
$stmt->execute();
$nutrition_result = $stmt->get_result();
while ($row = $nutrition_result->fetch_assoc()) {
    $nutrition[] = [
        "calories" => $row['calories'],
        "fat" => $row['fat'],
        "carbohydrates" => $row['carbohydrates'],
        "fiber" => $row['fiber'],
        "sugar" => $row['sugar'],
        "sodium" => $row['sodium']
        
    ];
}

// Return JSON response
$response = [
    "status" => "success",
    "data" => [
        "id" => $recipe['id'],
        "title" => $recipe['title'],
        "description" => $recipe['description'],
        "time" => $recipe['time_recipe'],
        "servings" => $recipe['servings'],
        "image" => $recipe['image_url'],
        "cuisine" => $recipe['cuisine_type'],
        "dietary" => $recipe['dietary'],
        "ingredients" => $ingredients,
        "instructions" => $instructions,
        "nutrition" => $nutrition
    ]
];

echo json_encode($response, JSON_UNESCAPED_UNICODE);

// Close connection
$conn->close();
?>