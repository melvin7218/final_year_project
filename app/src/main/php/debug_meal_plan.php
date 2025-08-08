<?php
header("Content-Type: application/json");
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Test database connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed: " . $conn->connect_error]);
    exit;
}

// Get the raw input
$rawInput = file_get_contents("php://input");
echo json_encode([
    "status" => "debug",
    "raw_input" => $rawInput,
    "input_length" => strlen($rawInput)
]);

// Try to decode JSON
$data = json_decode($rawInput, true);
if (json_last_error() !== JSON_ERROR_NONE) {
    echo json_encode([
        "status" => "error", 
        "message" => "JSON decode error: " . json_last_error_msg(),
        "raw_input" => $rawInput
    ]);
    exit;
}

// Check if required fields exist
$userId = $data['user_id'] ?? null;
$recipeId = $data['recipe_id'] ?? null;
$category = $data['category'] ?? null;
$mealDate = $data['meal_date'] ?? null;

echo json_encode([
    "status" => "debug",
    "parsed_data" => [
        "user_id" => $userId,
        "recipe_id" => $recipeId,
        "category" => $category,
        "meal_date" => $mealDate
    ]
]);

// Check if recipe exists
if ($recipeId) {
    $checkRecipe = $conn->prepare("SELECT id, title FROM recipes WHERE id = ?");
    $checkRecipe->bind_param("i", $recipeId);
    $checkRecipe->execute();
    $checkRecipe->store_result();
    
    if ($checkRecipe->num_rows > 0) {
        $checkRecipe->bind_result($recipeIdResult, $recipeTitle);
        $checkRecipe->fetch();
        echo json_encode([
            "status" => "debug",
            "recipe_found" => true,
            "recipe_title" => $recipeTitle
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Recipe with ID $recipeId not found"
        ]);
    }
    $checkRecipe->close();
}

// Check if recipe has nutrition data
if ($recipeId) {
    $checkNutrition = $conn->prepare("SELECT calories, protein, fat, carbohydrates FROM recipe_nutrition WHERE recipe_id = ?");
    $checkNutrition->bind_param("i", $recipeId);
    $checkNutrition->execute();
    $checkNutrition->store_result();
    
    if ($checkNutrition->num_rows > 0) {
        $checkNutrition->bind_result($calories, $protein, $fat, $carbs);
        $checkNutrition->fetch();
        echo json_encode([
            "status" => "debug",
            "nutrition_found" => true,
            "nutrition_data" => [
                "calories" => $calories,
                "protein" => $protein,
                "fat" => $fat,
                "carbs" => $carbs
            ]
        ]);
    } else {
        echo json_encode([
            "status" => "debug",
            "nutrition_found" => false,
            "message" => "No nutrition data found for recipe $recipeId"
        ]);
    }
    $checkNutrition->close();
}

$conn->close();
?> 