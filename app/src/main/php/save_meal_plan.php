<?php
header("Content-Type: application/json");
ini_set('display_errors', 1);
error_reporting(E_ALL);
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

try {
    $data = json_decode(file_get_contents("php://input"), true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        echo json_encode(["status" => "error", "message" => "Invalid JSON: " . json_last_error_msg()]);
        exit;
    }
    
    $userId = $data['user_id'] ?? null;
    $recipeId = $data['recipe_id'] ?? null;
    $category = $data['category'] ?? null;
    $mealDate = $data['meal_date'] ?? null;
    $mealTime = $data['meal_time'] ?? '00:00:00'; // Default time if not provided
    $memberIds = $data['member_ids'] ?? [];
    $portionMultipliers = $data['portion_multipliers'] ?? [];
    // portionMultipliers: either array (same order as memberIds) or associative array member_id => multiplier

    if (!$userId ) {
        echo json_encode(["status" => "error", "message" => "Missing user_id"]);
        exit;
    }
    if ( !$recipeId) {
        echo json_encode(["status" => "error", "message" => "Missing recipe_id"]);
        exit;
    }
    if (!$category) {
        echo json_encode(["status" => "error", "message" => "Missing category"]);
        exit;
    }
    if (!$mealDate) {
        echo json_encode(["status" => "error", "message" => "Missing meal date"]);
        exit;
    }
    if (empty($memberIds) || !is_array($memberIds)) {
        echo json_encode(["status" => "error", "message" => "Missing or invalid member_ids"]);
        exit;
    }

// STEP 1: Find or create meal plan for this user + date
$selectMealPlan = $conn->prepare("SELECT id FROM meal_plans WHERE user_id = ? AND meal_date = ?");
$selectMealPlan->bind_param("is", $userId, $mealDate);
$selectMealPlan->execute();
$selectMealPlan->store_result();
$selectMealPlan->bind_result($mealPlanId);

if ($selectMealPlan->num_rows > 0) {
    $selectMealPlan->fetch();
} else {
    // Create a new meal plan
    $insertMealPlan = $conn->prepare("INSERT INTO meal_plans (user_id, meal_date, total_calories) VALUES (?, ?, 0)");
    $insertMealPlan->bind_param("is", $userId, $mealDate);
    $insertMealPlan->execute();
    $mealPlanId = $insertMealPlan->insert_id;
    $insertMealPlan->close();
}
$selectMealPlan->close();

// STEP 2: Insert into meal_plan_recipe (now with date and time columns)
$insertRecipe = $conn->prepare("INSERT INTO meal_plan_recipe (meal_plan_id, recipe_id, category, user_id, date, time) VALUES (?, ?, ?, ?, ?, ?)");
$insertRecipe->bind_param("iisiss", $mealPlanId, $recipeId, $category, $userId, $mealDate, $mealTime);
$insertRecipe->execute();
$mealPlanRecipeId = $insertRecipe->insert_id;
$insertRecipe->close();

// STEP 2.5: Insert into meal_plan_recipe_member for each member
$insertMember = $conn->prepare("INSERT INTO meal_plan_recipe_member (meal_plan_recipe_id, member_id) VALUES (?, ?)");
foreach ($memberIds as $memberId) {
    $insertMember->bind_param("ii", $mealPlanRecipeId, $memberId);
    $insertMember->execute();
}
$insertMember->close();

// STEP 3: Get recipe nutrition
$nutrition = [
    'calories' => 0,
    'protein' => 0,
    'fat' => 0,
    'carbs' => 0
];

// Check if nutrition data exists for this recipe
$nutritionQuery = $conn->prepare("SELECT calories, protein, fat, carbohydrates FROM recipe_nutrition WHERE recipe_id = ? LIMIT 1");
if ($nutritionQuery) {
    $nutritionQuery->bind_param("i", $recipeId);
    $nutritionQuery->execute();
    $nutritionQuery->bind_result($calories, $protein, $fat, $carbs);
    if ($nutritionQuery->fetch()) {
        $nutrition['calories'] = $calories;
        $nutrition['protein'] = $protein;
        $nutrition['fat'] = $fat;
        $nutrition['carbs'] = $carbs;
    }
    $nutritionQuery->close();
} else {
    // If nutrition table doesn't exist or query fails, use default values
    error_log("Warning: Could not prepare nutrition query for recipe_id: $recipeId");
}

// STEP 5: Calculate multipliers and distribute nutrition by percent for all eaters (user + members)
// Build a list of all eaters: user (by user_id) + members (by member_id)
$allEaterKeys = [];
$allEaterKeys[] = 'user'; // special key for user
foreach ($memberIds as $mid) {
    $allEaterKeys[] = $mid;
}

// Assign multipliers (default 1.0 for user if not specified)
$memberMultipliers = [];
$totalMultiplier = 0;
foreach ($allEaterKeys as $key) {
    if ($key === 'user') {
        // User's multiplier: check for 'user' or user_id as string
        if (isset($portionMultipliers['user'])) {
            $multiplier = floatval($portionMultipliers['user']);
        } elseif (isset($portionMultipliers[(string)$userId])) {
            $multiplier = floatval($portionMultipliers[(string)$userId]);
        } else {
            $multiplier = 1.0;
        }
    } else {
        $multiplier = isset($portionMultipliers[$key]) ? floatval($portionMultipliers[$key]) : 1.0;
    }
    $memberMultipliers[$key] = $multiplier;
    $totalMultiplier += $multiplier;
}

$insertNutrition = $conn->prepare("INSERT INTO meal_plan_nutrition (meal_plan_recipe_id, member_id, portion_multiplier, calories, protein, fat, carbs) VALUES (?, ?, ?, ?, ?, ?, ?)");
$nutritionRows = [];
foreach ($allEaterKeys as $key) {
    $multiplier = $memberMultipliers[$key];
    $percent = $totalMultiplier > 0 ? ($multiplier / $totalMultiplier) * 100 : 0;
    $memberCalories = $nutrition['calories'] * ($percent / 100);
    $memberProtein = $nutrition['protein'] * ($percent / 100);
    $memberFat = $nutrition['fat'] * ($percent / 100);
    $memberCarbs = $nutrition['carbs'] * ($percent / 100);

    if ($key === 'user') {
        // Insert into meal_plan_recipe_user for the user (only once)
        $insertUserNutrition = $conn->prepare("INSERT INTO meal_plan_recipe_user (user_id, meal_plan_id, meal_plan_recipe_id, recipe_id, meal_date, category, portion_multiplier, percent, calories, protein, fat, carbs) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        $insertUserNutrition->bind_param(
            "iiiissdddddd",
            $userId,
            $mealPlanId,
            $mealPlanRecipeId,
            $recipeId,
            $mealDate,
            $category,
            $multiplier,
            $percent,
            $memberCalories,
            $memberProtein,
            $memberFat,
            $memberCarbs
        );
        $insertUserNutrition->execute();
        $insertUserNutrition->close();
        $nutritionRows[] = [
            'user_id' => $userId,
            'portion_multiplier' => $multiplier,
            'percent' => round($percent, 2),
            'calories' => $memberCalories,
            'protein' => $memberProtein,
            'fat' => $memberFat,
            'carbs' => $memberCarbs
        ];
    } else {
        // Insert into meal_plan_nutrition for members
        $insertNutrition->bind_param("iiddddd", $mealPlanRecipeId, $key, $multiplier, $memberCalories, $memberProtein, $memberFat, $memberCarbs);
        $insertNutrition->execute();
        $nutritionRows[] = [
            'member_id' => $key,
            'portion_multiplier' => $multiplier,
            'percent' => round($percent, 2),
            'calories' => $memberCalories,
            'protein' => $memberProtein,
            'fat' => $memberFat,
            'carbs' => $memberCarbs
        ];
    }
}
$insertNutrition->close();

// STEP 5: Calculate total calories and update meal_plan
$updateCalories = $conn->prepare("
    UPDATE meal_plans
    SET total_calories = (
        SELECT IFNULL(SUM(n.calories), 0)
        FROM meal_plan_recipe r
        JOIN recipe_nutrition n ON r.recipe_id = n.recipe_id
        WHERE r.meal_plan_id = ?
    )
    WHERE id = ?
");
$updateCalories->bind_param("ii", $mealPlanId, $mealPlanId);
$updateCalories->execute();
$updateCalories->close();

// Respond with inserted nutrition rows for verification
echo json_encode([
    "status" => "success",
    "meal_plan_id" => $mealPlanId,
    "meal_plan_recipe_id" => $mealPlanRecipeId,
    "meal_time" => $mealTime,
    "nutrition" => $nutritionRows
]);
$conn->close();
} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Server error: " . $e->getMessage()
    ]);
}
?> 