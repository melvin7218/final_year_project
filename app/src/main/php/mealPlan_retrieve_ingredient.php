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

// Prepare statement for ingredient lookup
$sqlIngredient = "SELECT ingredient_name, amount FROM recipe_ingredients WHERE recipe_id = ?";
$stmtIngredient = $conn->prepare($sqlIngredient);

// Prepare statement to count how many times each recipe is in the meal plan for this user and week
$sqlCount = "SELECT COUNT(*) as cnt FROM meal_plan_recipe WHERE recipe_id = ? AND user_id = ? AND date BETWEEN ? AND ?";
$stmtCount = $conn->prepare($sqlCount);

$ingredientTotals = [];

// Helper to parse amount string (e.g., "100 g" => [100, "g"])
function parse_amount($amountStr) {
    if (preg_match('/([\\d.]+)\\s*([a-zA-Z]*)/', $amountStr, $matches)) {
        $value = floatval($matches[1]);
        $unit = isset($matches[2]) ? trim($matches[2]) : '';
        return [$value, $unit];
    }
    return [null, null];
}

foreach ($uniqueRecipeIds as $recipeId) {
    // Get how many times this recipe is in the meal plan for this user and week
    $stmtCount->bind_param("iiss", $recipeId, $userId, $weekStart, $weekEnd);
    $stmtCount->execute();
    $resultCount = $stmtCount->get_result();
    $countRow = $resultCount->fetch_assoc();
    $count = isset($countRow['cnt']) ? intval($countRow['cnt']) : 1;

    // Get ingredients for this recipe
    $stmtIngredient->bind_param("i", $recipeId);
    $stmtIngredient->execute();
    $resultIngredient = $stmtIngredient->get_result();
    while ($row = $resultIngredient->fetch_assoc()) {
        $name = strtolower(trim($row['ingredient_name']));
        $amountStr = $row['amount'];
        list($value, $unit) = parse_amount($amountStr);

        if ($value !== null && $unit !== '') {
            // Numeric value with unit
            $key = $name . '|' . $unit;
            if (!isset($ingredientTotals[$key])) {
                $ingredientTotals[$key] = ['name' => $name, 'amount' => 0, 'unit' => $unit];
            }
            $ingredientTotals[$key]['amount'] += $value * $count;
        } else {
            // Non-numeric or missing unit, just repeat/concatenate
            $key = $name . '|text';
            if (!isset($ingredientTotals[$key])) {
                $ingredientTotals[$key] = ['name' => $name, 'amounts' => []];
            }
            for ($i = 0; $i < $count; $i++) {
                $ingredientTotals[$key]['amounts'][] = $amountStr;
            }
        }
    }
}

$ingredients = [];
foreach ($ingredientTotals as $key => $data) {
    if (isset($data['unit'])) {
        $ingredients[] = [
            'name' => $data['name'],
            'amount' => $data['amount'] . ' ' . $data['unit']
        ];
    } else {
        $ingredients[] = [
            'name' => $data['name'],
            'amount' => implode(' + ', $data['amounts'])
        ];
    }
}

echo json_encode([
    "status" => "success",
    "ingredients" => $ingredients
]);

$stmtIngredient->close();
$stmtCount->close();
$conn->close();
?> 