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
    $portionMultipliers = $data['portion_multipliers'] ?? [];

    // Validation
    if (!$userId ) {
        echo json_encode(["status" => "error", "message" => "Missing user_id"]);
        exit;
    }
    if (!$recipeId) {
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

// STEP 2: Insert into meal_plan_recipe (with date and time columns)
$insertRecipe = $conn->prepare("INSERT INTO meal_plan_recipe (meal_plan_id, recipe_id, category, user_id, date, time) VALUES (?, ?, ?, ?, ?, ?)");
$insertRecipe->bind_param("iisiss", $mealPlanId, $recipeId, $category, $userId, $mealDate, $mealTime);
$insertRecipe->execute();
$mealPlanRecipeId = $insertRecipe->insert_id;
$insertRecipe->close();

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

// STEP 4: Calculate user multiplier and nutrition
$userMultiplier = 1.0;
if (isset($portionMultipliers['user'])) {
    $userMultiplier = floatval($portionMultipliers['user']);
} elseif (isset($portionMultipliers[(string)$userId])) {
    $userMultiplier = floatval($portionMultipliers[(string)$userId]);
}
$userCalories = $nutrition['calories'] * $userMultiplier;
$userProtein = $nutrition['protein'] * $userMultiplier;
$userFat = $nutrition['fat'] * $userMultiplier;
$userCarbs = $nutrition['carbs'] * $userMultiplier;

// STEP 5: Insert into meal_plan_recipe_user for the user
$insertUserNutrition = $conn->prepare("INSERT INTO meal_plan_recipe_user (user_id, meal_plan_id, meal_plan_recipe_id, recipe_id, meal_date, category, portion_multiplier, percent, calories, protein, fat, carbs) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
$percent = 100.0;
$insertUserNutrition->bind_param(
    "iiiissdddddd",
    $userId,
    $mealPlanId,
    $mealPlanRecipeId,
    $recipeId,
    $mealDate,
    $category,
    $userMultiplier,
    $percent,
    $userCalories,
    $userProtein,
    $userFat,
    $userCarbs
);
$insertUserNutrition->execute();
$insertUserNutrition->close();

// STEP 6: Update total calories in meal_plan
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

// Respond with inserted nutrition row for verification
echo json_encode([
    "status" => "success",
    "meal_plan_id" => $mealPlanId,
    "meal_plan_recipe_id" => $mealPlanRecipeId,
    "meal_time" => $mealTime,
    "nutrition" => [
        'user_id' => $userId,
        'portion_multiplier' => $userMultiplier,
        'percent' => $percent,
        'calories' => $userCalories,
        'protein' => $userProtein,
        'fat' => $userFat,
        'carbs' => $userCarbs
    ]
]);
$conn->close();
} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Server error: " . $e->getMessage()
    ]);
}
?> 