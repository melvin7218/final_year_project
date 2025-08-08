<?php
header("Content-Type: application/json");

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

// Validate required fields
if (!isset($data["title"], $data["image"], $data["time"], $data["servings"])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

// Extract data
$title = $data["title"];
$image = $data["image"]; // maps to image_url in DB
$time = $data["time"];   // maps to time_recipe
$servings = intval($data["servings"]); // new
$is_user_recipe = 0;     // Spoonacular = not user
$cuisine_type = isset($data["cuisine"]) ? $data["cuisine"] : "";
$dietary = isset($data["dietary"]) ? $data["dietary"] : "";
$visibility = "public";  // default
$customize = 0;
$user_id = isset($data["user_id"]) ? $data["user_id"] : null; // optional

// Insert into recipes table (make sure your DB has the 'servings' column)
$sql = "INSERT INTO recipes (user_id, title, time_recipe, image_url, cuisine_type, dietary, visibility, is_user_recipe, customize, servings)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("issssssiii", $user_id, $title, $time, $image, $cuisine_type, $dietary, $visibility, $is_user_recipe, $customize, $servings);

if ($stmt->execute()) {
    $recipe_id = $stmt->insert_id;

    // ➤ Insert ingredients (portion size)
    if (isset($data["ingredients"])) {
        foreach ($data["ingredients"] as $ing) {
            $name = $ing["ingredient_name"];
            $amount_raw = $ing["amount"];

            // Extract numeric value and unit
            preg_match('/([\d\.]+)/', $amount_raw, $matches);
            $amount_val = isset($matches[1]) ? floatval($matches[1]) : 0;
            $unit = trim(str_replace($matches[0], '', $amount_raw));

            $portion_amount = round($amount_val / max($servings, 1), 2);
            $amount = $portion_amount . ' ' . $unit;

            $stmt_ing = $conn->prepare("INSERT INTO recipe_ingredients (recipe_id, ingredient_name, amount) VALUES (?, ?, ?)");
            $stmt_ing->bind_param("iss", $recipe_id, $name, $amount);
            $stmt_ing->execute();
        }
    }

    // ➤ Insert instructions
    if (isset($data["instructions"]) && is_array($data["instructions"])) {
        $step_number = 1;
        foreach ($data["instructions"] as $step) {
            $stmt_instr = $conn->prepare("INSERT INTO recipe_instructions (recipe_id, step_number, instruction_text) VALUES (?, ?, ?)");
            $stmt_instr->bind_param("iis", $recipe_id, $step_number, $step);
            $stmt_instr->execute();
            $step_number++;
        }
    }

    // ➤ Insert nutrition (per serving)
    $totalCalories = 0;
    $totalFat = 0;
    $totalFiber = 0;
    $totalCarbs = 0;

    if (isset($data["nutrition"])) {
        foreach ($data["nutrition"] as $nutri) {
            $name = strtolower($nutri["name"]);
            $amount = floatval($nutri["amount"]) / max($servings, 1); // per serving

            if (str_contains($name, "calories")) $totalCalories += $amount;
            if (str_contains($name, "fat")) $totalFat += $amount;
            if (str_contains($name, "fiber")) $totalFiber += $amount;
            if (str_contains($name, "carbohydrate")) $totalCarbs += $amount;
        }
    }

    $stmt_nutri = $conn->prepare("INSERT INTO recipe_nutrition (recipe_id, name, calories, fat, fiber, carbohydrates)
                                  VALUES (?, 'total', ?, ?, ?, ?)");
    $stmt_nutri->bind_param("idddd", $recipe_id, $totalCalories, $totalFat, $totalFiber, $totalCarbs);
    $stmt_nutri->execute();

    echo json_encode([
        "status" => "success",
        "message" => "Recipe, ingredients, nutrition, and instructions saved.",
        "recipe_id" => $recipe_id
    ]);

} else {
    echo json_encode(["status" => "error", "message" => "Failed to insert recipe."]);
}

$conn->close();
?>
