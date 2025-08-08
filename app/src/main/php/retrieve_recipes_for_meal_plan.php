<?php
header('Content-Type: application/json; charset=UTF-8');
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

// Get optional parameters for filtering
$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;
$category = isset($_GET['category']) ? $_GET['category'] : null;
$search_keyword = isset($_GET['search']) ? $_GET['search'] : null;

// Build the query based on parameters
$query = "
    SELECT 
        r.id,
        r.title,
        r.image_url,
        r.servings,
        r.cuisine_type,
        r.time_recipe,
        r.visibility,
        r.is_user_recipe,
        rn.calories,
        rn.protein,
        rn.fat,
        rn.carbohydrates
    FROM recipes r
    LEFT JOIN recipe_nutrition rn ON r.id = rn.recipe_id
    WHERE 1=1
";

$params = [];
$types = "";

// Add user_id filter if provided
if ($user_id) {
    $query .= " AND r.user_id = ?";
    $params[] = $user_id;
    $types .= "i";
}

// Add category filter if provided
if ($category && $category !== "all") {
    if ($category === "my_recipe") {
        $query .= " AND r.is_user_recipe = 1";
    } elseif ($category === "low_fat") {
        $query .= " AND rn.fat <= 10";
    } elseif ($category === "dinner") {
        $query .= " AND r.cuisine_type IN ('dinner', 'main_course')";
    }
}

// Add search keyword filter if provided
if ($search_keyword && !empty(trim($search_keyword))) {
    $query .= " AND (r.title LIKE ? OR r.description LIKE ?)";
    $search_param = "%" . $search_keyword . "%";
    $params[] = $search_param;
    $params[] = $search_param;
    $types .= "ss";
}

// Add visibility filter to show public recipes
$query .= " AND r.visibility = 'public'";

// Order by recipe title
$query .= " ORDER BY r.title ASC";

$stmt = $conn->prepare($query);

if (!empty($params)) {
    $stmt->bind_param($types, ...$params);
}

$stmt->execute();
$result = $stmt->get_result();

$recipes = [];
while ($row = $result->fetch_assoc()) {
    // Format nutrition values to 2 decimal places
    $row['calories'] = number_format((float)$row['calories'], 2);
    $row['protein'] = number_format((float)$row['protein'], 2);
    $row['fat'] = number_format((float)$row['fat'], 2);
    $row['carbohydrates'] = number_format((float)$row['carbohydrates'], 2);
    
    $recipes[] = $row;
}

echo json_encode([
    "status" => "success", 
    "recipes" => $recipes,
    "total_count" => count($recipes)
]);

$stmt->close();
$conn->close();
?> 