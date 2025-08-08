<?php
header('Content-Type: application/json; charset=UTF-8');
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

// Accept POST with JSON body: { "recipe_ids": [1, 2, 3, ...], "user_id": 1, "week_start": "2024-06-24", "week_end": "2024-06-30" }
$input = json_decode(file_get_contents('php://input'), true);
if (!isset($input['recipe_ids']) || !is_array($input['recipe_ids']) || count($input['recipe_ids']) === 0
    || !isset($input['user_id']) || !isset($input['week_start']) || !isset($input['week_end'])) {
    echo json_encode(["status" => "error", "message" => "Missing or invalid parameters"]);
    exit;
}

$recipeIds = $input['recipe_ids'];
$userId = intval($input['user_id']);
$weekStart = $input['week_start'];
$weekEnd = $input['week_end'];
$uniqueRecipeIds = array_unique($recipeIds);

// Prepare statement for ingredient lookup with additional recipe info
$sqlIngredient = "SELECT ri.ingredient_name, ri.amount_value, ri.unit, r.title as recipe_name 
                  FROM recipe_ingredients ri
                  JOIN recipes r ON ri.recipe_id = r.id
                  WHERE ri.recipe_id = ?";
$stmtIngredient = $conn->prepare($sqlIngredient);

// Prepare statement to count recipe occurrences in the meal plan
$sqlCount = "SELECT COUNT(*) as cnt FROM meal_plan_recipe 
             WHERE recipe_id = ? AND user_id = ? AND date BETWEEN ? AND ?";
$stmtCount = $conn->prepare($sqlCount);

$ingredientTotals = [];
$recipeCounts = []; // Track how many times each recipe is used

// First pass: count occurrences of each recipe in the meal plan
foreach ($uniqueRecipeIds as $recipeId) {
    $stmtCount->bind_param("iiss", $recipeId, $userId, $weekStart, $weekEnd);
    $stmtCount->execute();
    $resultCount = $stmtCount->get_result();
    $countRow = $resultCount->fetch_assoc();
    $recipeCounts[$recipeId] = isset($countRow['cnt']) ? intval($countRow['cnt']) : 1;
}

// Second pass: get ingredients and multiply by counts
foreach ($uniqueRecipeIds as $recipeId) {
    $count = $recipeCounts[$recipeId];
    $stmtIngredient->bind_param("i", $recipeId);
    $stmtIngredient->execute();
    $resultIngredient = $stmtIngredient->get_result();
    while ($row = $resultIngredient->fetch_assoc()) {
        $name = strtolower(trim($row['ingredient_name']));
        $amountValue = $row['amount_value'];
        $unit = $row['unit'];
        $recipeName = $row['recipe_name'];
        if ($amountValue !== null && $unit !== null && $unit !== '') {
            // Numeric value with unit
            $key = $name . '|' . $unit;
            if (!isset($ingredientTotals[$key])) {
                $ingredientTotals[$key] = [
                    'name' => $name,
                    'amount' => 0,
                    'unit' => $unit,
                    'recipes' => [] // Track which recipes contribute to this ingredient
                ];
            }
            $ingredientTotals[$key]['amount'] += floatval($amountValue) * $count;
            // Record which recipe contributed this amount
            $ingredientTotals[$key]['recipes'][] = [
                'recipe_name' => $recipeName,
                'times_used' => $count,
                'original_amount' => $amountValue . $unit
            ];
        } else {
            // Non-numeric or missing unit, just repeat/concatenate
            $key = $name . '|text';
            if (!isset($ingredientTotals[$key])) {
                $ingredientTotals[$key] = [
                    'name' => $name,
                    'amounts' => [],
                    'recipes' => []
                ];
            }
            for ($i = 0; $i < $count; $i++) {
                $ingredientTotals[$key]['amounts'][] = $row['amount_value'] ?? '';
            }
            $ingredientTotals[$key]['recipes'][] = [
                'recipe_name' => $recipeName,
                'times_used' => $count,
                'original_amount' => $row['amount_value'] ?? ''
            ];
        }
    }
}

// Format the output
$ingredients = [];
foreach ($ingredientTotals as $key => $data) {
    if (isset($data['unit'])) {
        // Remove trailing .0 for whole numbers
        $amountStr = (intval($data['amount']) == $data['amount']) ? intval($data['amount']) : $data['amount'];
        $ingredients[] = [
            'name' => $data['name'],
            'amount' => $amountStr . $data['unit'], // No space between number and unit
            'recipes' => $data['recipes'] // Include recipe contribution details
        ];
    } else {
        $ingredients[] = [
            'name' => $data['name'],
            'amount' => implode(' + ', $data['amounts']),
            'recipes' => $data['recipes'] // Include recipe contribution details
        ];
    }
}

echo json_encode([
    "status" => "success",
    "ingredients" => $ingredients,
    "recipe_counts" => $recipeCounts // Show how many times each recipe was used
]);

$stmtIngredient->close();
$stmtCount->close();
$conn->close();
?>