<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;

$response = [
    'other_users_recipes' => [],
    'own_recipes' => [],
    'spoonacular_recipes' => [],
    'customized_recipes' => []
];

function getRecipes($conn, $where) {
    $sql = "SELECT r.id, r.title, r.image_url, r.cuisine_type, r.dietary, r.time_recipe, r.servings,
                   n.calories, n.protein, n.fat, n.carbohydrates
            FROM recipes r
            LEFT JOIN recipe_nutrition n ON r.id = n.recipe_id
            $where";
    $result = $conn->query($sql);
    $recipes = [];
    if ($result && $result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            $recipes[] = $row;
        }
    }
    return $recipes;
}

// Public recipes from other users
$where = "WHERE r.visibility = 'public'";
if ($userId !== null) {
    $where .= " AND r.user_id != $userId";
}
$response['other_users_recipes'] = getRecipes($conn, $where);

// User's own recipes
if ($userId !== null) {
    $where = "WHERE r.user_id = $userId";
    $response['own_recipes'] = getRecipes($conn, $where);
}

// Customized recipes
$where = "WHERE r.customize = 1";
$response['customized_recipes'] = getRecipes($conn, $where);

// API recipes
$where = "WHERE r.is_user_recipe = 0";
$response['spoonacular_recipes'] = getRecipes($conn, $where);

echo json_encode($response);
$conn->close();
?>