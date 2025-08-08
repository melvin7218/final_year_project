<?php
header('Content-Type: application/json');
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

if (!isset($data["recipe_id"], $data["user_id"], $data["ingredients"])) {
    echo json_encode(["status" => "error", "message" => "Missing parameters"]);
    exit;
}

$recipe_id = intval($data["recipe_id"]);
$user_id = intval($data["user_id"]);
$ingredients = $data["ingredients"];

// Get current ingredients for the recipe
$current_ingredients = [];
$sql = "SELECT ingredient_name, amount FROM recipe_ingredients WHERE recipe_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $recipe_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $current_ingredients[$row['ingredient_name']] = $row['amount'];
}
$stmt->close();

$changed = false;
$nutrition_update = [
    "calories" => 0,
    "protein" => 0,
    "carbohydrates" => 0,
    "fat" => 0,
    "fiber" => 0
];

foreach ($ingredients as $ingredient) {
    $name = $ingredient["ingredient_name"];
    $amount = $ingredient["amount"];

    // Try to extract numeric value in grams from amount (e.g., "10g" or "10 g")
    if (preg_match('/([\\d.]+)\\s*g/i', $amount, $matches)) {
        $grams = floatval($matches[1]);
    } else {
        // If not in grams, skip nutrition update for this ingredient
        $grams = 0;
    }

    if (isset($current_ingredients[$name])) {
        // If amount changed, update
        if ($current_ingredients[$name] != $amount) {
            $update_sql = "UPDATE recipe_ingredients SET amount = ? WHERE recipe_id = ? AND ingredient_name = ?";
            $stmt = $conn->prepare($update_sql);
            $stmt->bind_param("sis", $amount, $recipe_id, $name);
            $stmt->execute();
            $stmt->close();
            $changed = true;
        }
    } else {
        // Check if ingredient exists in ingredient_nutrition
        $nutri_sql = "SELECT * FROM ingredient_nutrition WHERE ingredient_name = ?";
        $stmt = $conn->prepare($nutri_sql);
        $stmt->bind_param("s", $name);
        $stmt->execute();
        $nutri_result = $stmt->get_result();
        if ($nutri_result->num_rows > 0) {
            // Add new ingredient to recipe_ingredients
            $insert_sql = "INSERT INTO recipe_ingredients (recipe_id, ingredient_name, amount) VALUES (?, ?, ?)";
            $stmt2 = $conn->prepare($insert_sql);
            $stmt2->bind_param("iss", $recipe_id, $name, $amount);
            $stmt2->execute();
            $stmt2->close();
            $changed = true;

            // Update recipe nutrition
            $nutri = $nutri_result->fetch_assoc();
            if ($grams > 0) {
                $nutrition_update["calories"] += ($nutri["calories"] * $grams) / 100.0;
                $nutrition_update["protein"] += ($nutri["protein"] * $grams) / 100.0;
                $nutrition_update["carbohydrates"] += ($nutri["carbohydrates"] * $grams) / 100.0;
                $nutrition_update["fat"] += ($nutri["fat"] * $grams) / 100.0;
                $nutrition_update["fiber"] += ($nutri["fiber"] * $grams) / 100.0;
            }
        } else {
            echo json_encode(["status" => "error", "message" => "did not had this ingredient in the database"]);
            $stmt->close();
            $conn->close();
            exit;
        }
        $stmt->close();
    }
}

// Update the recipe_nutrition table if there are nutrition updates
if ($changed && (
    $nutrition_update["calories"] > 0 ||
    $nutrition_update["protein"] > 0 ||
    $nutrition_update["carbohydrates"] > 0 ||
    $nutrition_update["fat"] > 0 ||
    $nutrition_update["fiber"] > 0
)) {
    // Get current nutrition values
    $sql = "SELECT calories, protein, carbohydrates, fat, fiber FROM recipe_nutrition WHERE recipe_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $recipe_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $current_nutrition = $result->fetch_assoc();
    $stmt->close();

    // Add new values
    $new_calories = $current_nutrition["calories"] + $nutrition_update["calories"];
    $new_protein = $current_nutrition["protein"] + $nutrition_update["protein"];
    $new_carbohydrates = $current_nutrition["carbohydrates"] + $nutrition_update["carbohydrates"];
    $new_fat = $current_nutrition["fat"] + $nutrition_update["fat"];
    $new_fiber = $current_nutrition["fiber"] + $nutrition_update["fiber"];

    // Update the table
    $update_sql = "UPDATE recipe_nutrition SET calories = ?, protein = ?, carbohydrates = ?, fat = ?, fiber = ? WHERE recipe_id = ?";
    $stmt = $conn->prepare($update_sql);
    $stmt->bind_param("dddddi", $new_calories, $new_protein, $new_carbohydrates, $new_fat, $new_fiber, $recipe_id);
    $stmt->execute();
    $stmt->close();
}

if ($changed) {
    echo json_encode(["status" => "success", "message" => "Ingredients updated."]);
} else {
    echo json_encode(["status" => "success", "message" => "No changes detected."]);
}
$conn->close();
?>