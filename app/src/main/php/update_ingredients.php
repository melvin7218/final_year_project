<?php
header("Content-Type: application/json");
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Database connection
$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data["recipe_id"], $data["user_id"], $data["ingredients"])) {
    echo json_encode(["status" => "error", "message" => "Missing parameters"]);
    exit;
}

$recipe_id = intval($data["recipe_id"]);
$user_id = intval($data["user_id"]);
$ingredients = $data["ingredients"];

if (empty($ingredients)) {
    echo json_encode([
        "status" => "error",
        "message" => "Ingredient list cannot be empty."
    ]);
    $conn->close();
    exit;
}

// Update amount_value and unit for existing ingredients
$update_sql = "UPDATE recipe_ingredients SET amount_value = ?, unit = ? WHERE recipe_id = ? AND ingredient_name = ?";
$stmt = $conn->prepare($update_sql);

foreach ($ingredients as $ingredient) {
    $name = $ingredient["ingredient_name"];
    $amount_value = isset($ingredient["amount_value"]) ? $ingredient["amount_value"] : null;
    $unit = isset($ingredient["unit"]) ? $ingredient["unit"] : null;

    // If amount_value is empty string, set to null
    if ($amount_value === "" || $amount_value === null) {
        $amount_value = null;
    }
    // If unit is empty string, set to null
    if ($unit === "" || $unit === null) {
        $unit = null;
    }

    $stmt->bind_param("dsis", $amount_value, $unit, $recipe_id, $name);
    $stmt->execute();
}
$stmt->close();

// --- Nutrition Calculation Section ---

// Initialize nutrition totals
$total_calories = 0.0;
$total_protein = 0.0;
$total_fat = 0.0;
$total_carbohydrates = 0.0;
$total_fiber = 0.0;
$total_sugar = 0.0;
$total_sodium = 0.0;

// For each ingredient, get its nutrition and sum up
foreach ($ingredients as $ingredient) {
    $ingredient_name = $ingredient["ingredient_name"];
    $amount_value = isset($ingredient["amount_value"]) ? floatval($ingredient["amount_value"]) : 0;
    $unit = isset($ingredient["unit"]) ? $ingredient["unit"] : null;

    // Get nutrition per 100g or per unit
    $sql = "SELECT * FROM ingredient_nutrition WHERE ingredient_name = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $ingredient_name);
    $stmt->execute();
    $result = $stmt->get_result();
    $nutrition = $result->fetch_assoc();
    $stmt->close();

    if ($nutrition) {
        // Default: treat amount_value as grams
        $grams = $amount_value;

        // If not grams, try to convert using ingredient_conversions table
        if ($unit && strtolower($unit) != 'g' && strtolower($unit) != 'gram' && strtolower($unit) != 'grams') {
            $conv_sql = "SELECT grams_per_unit FROM ingredient_conversions WHERE ingredient_name = ? AND unit = ?";
            $conv_stmt = $conn->prepare($conv_sql);
            $conv_stmt->bind_param("ss", $ingredient_name, $unit);
            $conv_stmt->execute();
            $conv_result = $conv_stmt->get_result();
            $conv = $conv_result->fetch_assoc();
            $conv_stmt->close();
            if ($conv && $conv['grams_per_unit'] > 0) {
                $grams = $amount_value * floatval($conv['grams_per_unit']);
            }
        }

        // Calculate nutrition for this ingredient
        $factor = $grams / 100.0; // nutrition table is per 100g

        $total_calories      += $nutrition['calories']      * $factor;
        $total_protein       += $nutrition['protein']       * $factor;
        $total_fat           += $nutrition['fat']           * $factor;
        $total_carbohydrates += $nutrition['carbohydrates'] * $factor;
        $total_fiber         += isset($nutrition['fiber']) ? $nutrition['fiber'] * $factor : 0;
        $total_sugar         += isset($nutrition['sugar']) ? $nutrition['sugar'] * $factor : 0;
        $total_sodium        += isset($nutrition['sodium']) ? $nutrition['sodium'] * $factor : 0;
    }
}

// Update or insert into recipe_nutrition table
$check_sql = "SELECT id FROM recipe_nutrition WHERE recipe_id = ?";
$stmt = $conn->prepare($check_sql);
$stmt->bind_param("i", $recipe_id);
$stmt->execute();
$stmt->store_result();
if ($stmt->num_rows > 0) {
    // Update
    $update_nutrition_sql = "UPDATE recipe_nutrition SET calories=?, protein=?, fat=?, carbohydrates=?, fiber=?, sugar=?, sodium=? WHERE recipe_id=?";
    $update_stmt = $conn->prepare($update_nutrition_sql);
    $update_stmt->bind_param("dddddddi", $total_calories, $total_protein, $total_fat, $total_carbohydrates, $total_fiber, $total_sugar, $total_sodium, $recipe_id);
    $update_stmt->execute();
    $update_stmt->close();
} else {
    // Insert
    $insert_nutrition_sql = "INSERT INTO recipe_nutrition (recipe_id, calories, protein, fat, carbohydrates, fiber, sugar, sodium) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    $insert_stmt = $conn->prepare($insert_nutrition_sql);
    $insert_stmt->bind_param("iddddddd", $recipe_id, $total_calories, $total_protein, $total_fat, $total_carbohydrates, $total_fiber, $total_sugar, $total_sodium);
    $insert_stmt->execute();
    $insert_stmt->close();
}
$stmt->close();

$conn->close();

echo json_encode([
    "status" => "success",
    "message" => "Ingredient amounts and recipe nutrition updated.",
    "recipe_id" => $recipe_id,
    "nutrition" => [
        "calories" => $total_calories,
        "protein" => $total_protein,
        "fat" => $total_fat,
        "carbohydrates" => $total_carbohydrates,
        "fiber" => $total_fiber,
        "sugar" => $total_sugar,
        "sodium" => $total_sodium
    ]
]);
?>