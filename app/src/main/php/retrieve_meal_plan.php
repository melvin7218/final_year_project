<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$servername = "localhost";
$username = "root"; // Replace with your database username
$password = ""; // Replace with your database password
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

$userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;
$mealDate = isset($_GET['meal_date']) ? $_GET['meal_date'] : null;

$response = [
    'status' => 'error', // Default status if something goes wrong
    'meal_plans' => [],
    'message' => 'No meal plans found',
    'nutrition_summary' => [
        'user_tdee' => 0,
        'total_calories' => 0,
        'total_calories_burned' => 0,
        'calories_remaining' => 0
    ]
];

// Validate if `user_id` and `meal_date` are provided
if ($userId === null || $mealDate === null) {
    echo json_encode(["status" => "error", "message" => "Missing parameters"]);
    exit;
}

// Helper function to get names of members and user who had the food
function getHadByNames($conn, $meal_plan_recipe_id, $userId) {
    $names = [];

    // Get member names
    $sql = "SELECT um.member_name 
            FROM meal_plan_recipe_member mprm
            JOIN user_members um ON mprm.member_id = um.member_id
            WHERE mprm.meal_plan_recipe_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $meal_plan_recipe_id);
    $stmt->execute();
    $result = $stmt->get_result();
    while ($row = $result->fetch_assoc()) {
        $names[] = $row['member_name'];
    }
    $stmt->close();

    // Check if user had this food
    $sqlUser = "SELECT 1 FROM meal_plan_recipe_user WHERE meal_plan_recipe_id = ? AND user_id = ?";
    $stmtUser = $conn->prepare($sqlUser);
    $stmtUser->bind_param("ii", $meal_plan_recipe_id, $userId);
    $stmtUser->execute();
    $resultUser = $stmtUser->get_result();
    if ($resultUser->fetch_assoc()) {
        $names[] = 'user';
    }
    $stmtUser->close();

    return $names;
}

// Helper function to get nutrition summary for the user and date
function getNutritionSummary($conn, $userId, $mealDate) {
    $summary = [
        'user_tdee' => 0,
        'total_calories' => 0,
        'total_calories_burned' => 0,
        'calories_remaining' => 0,
        'total_protein' => 0,
        'total_fat' => 0,
        'total_carbs' => 0,
        'breakfast_calories' => 0,
        'breakfast_protein' => 0,
        'breakfast_fat' => 0,
        'breakfast_carbs' => 0,
        'lunch_calories' => 0,
        'lunch_protein' => 0,
        'lunch_fat' => 0,
        'lunch_carbs' => 0,
        'dinner_calories' => 0,
        'dinner_protein' => 0,
        'dinner_fat' => 0,
        'dinner_carbs' => 0
    ];

    // Get user TDEE from user_preferences
    $sqlTdee = "SELECT tdee FROM user_preferences WHERE user_id = ?";
    $stmtTdee = $conn->prepare($sqlTdee);
    $stmtTdee->bind_param("i", $userId);
    $stmtTdee->execute();
    $resultTdee = $stmtTdee->get_result();
    if ($rowTdee = $resultTdee->fetch_assoc()) {
        $summary['user_tdee'] = floatval($rowTdee['tdee']);
    }
    $stmtTdee->close();

    // Get total calories from meal plans for the date
    $sqlCalories = "SELECT total_calories FROM meal_plans WHERE user_id = ? AND meal_date = ?";
    $stmtCalories = $conn->prepare($sqlCalories);
    $stmtCalories->bind_param("is", $userId, $mealDate);
    $stmtCalories->execute();
    $resultCalories = $stmtCalories->get_result();
    while ($rowCalories = $resultCalories->fetch_assoc()) {
        $summary['total_calories'] += floatval($rowCalories['total_calories']);
    }
    $stmtCalories->close();

    // Get total calories burned from user_exercise for the date
    $sqlBurned = "SELECT SUM(calories_burned) as total_burned FROM user_exercise WHERE user_id = ? AND exercise_date = ?";
    $stmtBurned = $conn->prepare($sqlBurned);
    $stmtBurned->bind_param("is", $userId, $mealDate);
    $stmtBurned->execute();
    $resultBurned = $stmtBurned->get_result();
    if ($rowBurned = $resultBurned->fetch_assoc()) {
        $summary['total_calories_burned'] = floatval($rowBurned['total_burned'] ?? 0);
    }
    $stmtBurned->close();

    // Get total protein, fat, and carbs from meal_plan_nutrition for the date
    $sqlNutrition = "SELECT SUM(mpn.protein) as total_protein, SUM(mpn.fat) as total_fat, SUM(mpn.carbs) as total_carbs
                     FROM meal_plan_nutrition mpn
                     JOIN meal_plan_recipe mpr ON mpn.meal_plan_recipe_id = mpr.id
                     JOIN meal_plans mp ON mpr.meal_plan_id = mp.id
                     WHERE mp.user_id = ? AND mp.meal_date = ?";
    $stmtNutrition = $conn->prepare($sqlNutrition);
    $stmtNutrition->bind_param("is", $userId, $mealDate);
    $stmtNutrition->execute();
    $resultNutrition = $stmtNutrition->get_result();
    if ($rowNutrition = $resultNutrition->fetch_assoc()) {
        $summary['total_protein'] = floatval($rowNutrition['total_protein'] ?? 0);
        $summary['total_fat'] = floatval($rowNutrition['total_fat'] ?? 0);
        $summary['total_carbs'] = floatval($rowNutrition['total_carbs'] ?? 0);
    }
    $stmtNutrition->close();

    // Get nutrition data by category (Breakfast, Lunch, Dinner)
    $sqlCategoryNutrition = "SELECT mpr.category, 
                                   SUM(mpn.calories) as category_calories,
                                   SUM(mpn.protein) as category_protein,
                                   SUM(mpn.fat) as category_fat,
                                   SUM(mpn.carbs) as category_carbs
                            FROM meal_plan_recipe_user mpn
                            JOIN meal_plan_recipe mpr ON mpn.meal_plan_recipe_id = mpr.id
                            JOIN meal_plans mp ON mpr.meal_plan_id = mp.id
                            WHERE mp.user_id = ? AND mp.meal_date = ?
                            GROUP BY mpr.category";
    $stmtCategoryNutrition = $conn->prepare($sqlCategoryNutrition);
    $stmtCategoryNutrition->bind_param("is", $userId, $mealDate);
    $stmtCategoryNutrition->execute();
    $resultCategoryNutrition = $stmtCategoryNutrition->get_result();
    
    while ($rowCategory = $resultCategoryNutrition->fetch_assoc()) {
        $category = strtolower($rowCategory['category']);
        $summary[$category . '_calories'] = floatval($rowCategory['category_calories'] ?? 0);
        $summary[$category . '_protein'] = floatval($rowCategory['category_protein'] ?? 0);
        $summary[$category . '_fat'] = floatval($rowCategory['category_fat'] ?? 0);
        $summary[$category . '_carbs'] = floatval($rowCategory['category_carbs'] ?? 0);
    }
    $stmtCategoryNutrition->close();

    // Calculate calories remaining: TDEE - total_calories + total_calories_burned
    $summary['calories_remaining'] = $summary['user_tdee'] - $summary['total_calories'] + $summary['total_calories_burned'];

    return $summary;
}

// Query to get meal plans for the user and specific meal date (now including time)
$sql = "SELECT mp.id AS meal_plan_id, mp.meal_date, mp.total_calories, r.id AS recipe_id, r.title, r.image_url, mpr.category, mpr.is_user_recipe, mpr.id AS meal_plan_recipe_id, mpr.time
        FROM meal_plans mp
        JOIN meal_plan_recipe mpr ON mp.id = mpr.meal_plan_id
        JOIN recipes r ON mpr.recipe_id = r.id
        WHERE mp.user_id = ? AND mp.meal_date = ?
        ORDER BY mpr.time ASC";

// Prepare and execute the query
$stmt = $conn->prepare($sql);
$stmt->bind_param("is", $userId, $mealDate);
$stmt->execute();
$result = $stmt->get_result();

// Process results
$mealPlans = [];
while ($row = $result->fetch_assoc()) {
    $mealPlanId = $row['meal_plan_id'];
    $meal_plan_recipe_id = $row['meal_plan_recipe_id'];

    // Add meal plan if it's not already in the array
    if (!isset($mealPlans[$mealPlanId])) {
        $mealPlans[$mealPlanId] = [
            'meal_plan_id' => $mealPlanId,
            'meal_date' => $row['meal_date'],
            'total_calories' => $row['total_calories'],
            'recipes' => []
        ];
    }

    // Add recipe to the meal plan (now including time)
    $mealPlans[$mealPlanId]['recipes'][] = [
        'recipe_id' => $row['recipe_id'],
        'title' => $row['title'],
        'image_url' => $row['image_url'],
        'category' => $row['category'],
        'is_user_recipe' => $row['is_user_recipe'],
        'meal_plan_recipe_id' => $meal_plan_recipe_id, // Needed for deletion
        'time' => $row['time'], // NEW: time from database
        'had_by_names' => getHadByNames($conn, $meal_plan_recipe_id, $userId) // who had this food
    ];
}

// Get nutrition summary
$nutritionSummary = getNutritionSummary($conn, $userId, $mealDate);
$response['nutrition_summary'] = $nutritionSummary;

// If meal plans are found, set success status
if (count($mealPlans) > 0) {
    $response['status'] = 'success';
    $response['meal_plans'] = array_values($mealPlans);
    $response['message'] = 'Meal plans retrieved successfully';
} else {
    // Even if no meal plans, still return nutrition summary
    $response['status'] = 'success';
    $response['message'] = 'No meal plans found for this date, but nutrition summary available';
}

// Return the response
echo json_encode($response);

$conn->close();
?> 