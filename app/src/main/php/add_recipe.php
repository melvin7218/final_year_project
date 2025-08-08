<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode([
        'status' => 'error',
        'message' => 'Connection failed: ' . $conn->connect_error
    ]));
}

$json = file_get_contents("php://input");
$data = json_decode($json, true);

// Validate required fields
if(!isset($data['user_id']) || !isset($data['title']) || !isset($data['description']) || 
   !isset($data['time_recipe']) || !isset($data['cuisine_type']) || 
   !isset($data['dietary']) || !isset($data['visibility'])) {
    
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing required fields: ' . json_encode($data)
    ]);
    exit;
}

$user_id = intval($data['user_id']);
$title = $conn->real_escape_string($data['title']);
$description = $conn->real_escape_string($data['description']);
$time_recipe = $conn->real_escape_string($data['time_recipe']);
$cuisine_type = $conn->real_escape_string($data['cuisine_type']);
$dietary = $conn->real_escape_string($data['dietary']);
$visibility = $conn->real_escape_string($data['visibility']);

// Handle image upload
$image_url = '';
if(isset($data['image']) && !empty($data['image'])){
    $image_data = $data['image'];
    $filename = 'recipe_'.time().'_'.$user_id.'.jpg';
    $upload_path = 'uploads/'.$filename;

    $decoded_image = base64_decode($image_data);
    
    if(file_put_contents($upload_path, $decoded_image)){
        $image_url = 'http://'.$_SERVER['HTTP_HOST'].'/Final%20Year%20Project/uploads/'.$filename;
    } else {
        echo json_encode([
            'status' => 'error',
            'message' => 'Failed to save image'
        ]);
        exit;
    }
}

// Insert recipe
$sql = "INSERT INTO recipes (user_id, title, description, time_recipe, image_url, cuisine_type, dietary, visibility) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("isssssss", $user_id, $title, $description, $time_recipe, $image_url, $cuisine_type, $dietary, $visibility);

if($stmt->execute()){
    $recipe_id = $stmt->insert_id;
    $conn->begin_transaction();

    try {
        // Insert ingredients
        if(isset($data['ingredients']) && is_array($data['ingredients'])){
            foreach($data['ingredients'] as $ingredient){
                if(!empty($ingredient['ingredient_name'])){
                    $ingredient_name = $conn->real_escape_string($ingredient['ingredient_name']);
                    $amount_raw = isset($ingredient['amount']) ? trim($ingredient['amount']) : '';
                    $unit_raw = isset($ingredient['unit']) ? trim($ingredient['unit']) : '';
                    $amount = $conn->real_escape_string($amount_raw . $unit_raw);

                    $stmt_ingredient = $conn->prepare("INSERT INTO recipe_ingredients (recipe_id, amount_value, ingredient_name, unit) VALUES (?, ?, ?, ?)");
                    if(!$stmt_ingredient) throw new Exception("Ingredient prepare failed: ".$conn->error);

                    $stmt_ingredient->bind_param("isss", $recipe_id, $amount, $ingredient_name, $unit);
                    if(!$stmt_ingredient->execute()) throw new Exception("Ingredient insert failed: ".$stmt_ingredient->error);
                    $stmt_ingredient->close();
                }
            }
        }

        // Insert instructions
        if(isset($data['instructions']) && is_array($data['instructions'])){
            $step_number = 1;
            foreach($data['instructions'] as $instruction){
                if(!empty($instruction['instruction'])){
                    $instruction_text = $conn->real_escape_string($instruction['instruction']);

                    $stmt_instruction = $conn->prepare("INSERT INTO recipe_instructions (recipe_id, instruction_text, step_number) VALUES (?, ?, ?)");
                    if(!$stmt_instruction) throw new Exception("Instruction prepare failed: ".$conn->error);

                    $stmt_instruction->bind_param("isi", $recipe_id, $instruction_text, $step_number);
                    if(!$stmt_instruction->execute()) throw new Exception("Instruction insert failed: ".$stmt_instruction->error);
                    $stmt_instruction->close();
                    $step_number++;
                }
            }
        }

        // --- START: Calculate and Insert Total Nutrition ---
        $total_calories = 0;
        $total_fat = 0;
        $total_protein = 0;
        $total_carbs = 0;

        // Loop through each ingredient in the recipe
        if(isset($data['ingredients']) && is_array($data['ingredients'])){
            foreach($data['ingredients'] as $ingredient){
                if(!empty($ingredient['ingredient_name'])){
                    $ingredient_name = $conn->real_escape_string($ingredient['ingredient_name']);
                    $amount_raw = isset($ingredient['amount']) ? trim($ingredient['amount']) : '';
                    $unit_raw = isset($ingredient['unit']) ? trim($ingredient['unit']) : '';
                    
                    // Try to extract numeric amount (assume grams if possible)
                    $amount = 0;
                    if (preg_match('/([\d.]+)/', $amount_raw, $matches)) {
                        $amount = floatval($matches[1]);
                    }
                    
                    // Convert common units to grams for nutrition calculation
                    $amount_in_grams = $amount;
                    $unit_lower = strtolower(trim($unit_raw));
                    
                    // Unit conversion to grams
                    switch($unit_lower) {
                        case 'tbsp':
                        case 'tablespoon':
                        case 'tablespoons':
                            $amount_in_grams = $amount * 15; // 1 tbsp ≈ 15g
                            break;
                        case 'tsp':
                        case 'teaspoon':
                        case 'teaspoons':
                            $amount_in_grams = $amount * 5; // 1 tsp ≈ 5g
                            break;
                        case 'cup':
                        case 'cups':
                            $amount_in_grams = $amount * 240; // 1 cup ≈ 240g
                            break;
                        case 'ml':
                        case 'milliliter':
                        case 'milliliters':
                            $amount_in_grams = $amount; // 1ml ≈ 1g for most ingredients
                            break;
                        case 'l':
                        case 'liter':
                        case 'liters':
                            $amount_in_grams = $amount * 1000; // 1L = 1000g
                            break;
                        case 'oz':
                        case 'ounce':
                        case 'ounces':
                            $amount_in_grams = $amount * 28.35; // 1 oz ≈ 28.35g
                            break;
                        case 'lb':
                        case 'pound':
                        case 'pounds':
                            $amount_in_grams = $amount * 453.59; // 1 lb ≈ 453.59g
                            break;
                        case 'g':
                        case 'gram':
                        case 'grams':
                        default:
                            $amount_in_grams = $amount; // Already in grams
                            break;
                    }

                    // Get nutrition per 100g for this ingredient
                    $sql_nutrition = "SELECT calories, fat, protein, carbohydrates FROM ingredient_nutrition WHERE ingredient_name = ?";
                    $stmt_nutrition = $conn->prepare($sql_nutrition);
                    $stmt_nutrition->bind_param("s", $ingredient_name);
                    $stmt_nutrition->execute();
                    $result_nutrition = $stmt_nutrition->get_result();
                    
                    if ($row = $result_nutrition->fetch_assoc()) {
                        // Calculate contribution: (ingredient amount / 100) * nutrition_value
                        $factor = $amount_in_grams / 100.0;
                        $total_calories += $row['calories'] * $factor;
                        $total_fat += $row['fat'] * $factor;
                        $total_protein += $row['protein'] * $factor;
                        $total_carbs += $row['carbohydrates'] * $factor;
                    }
                    $stmt_nutrition->close();
                }
            }
        }

        // Insert total nutrition into recipe_nutrition table
        $stmt_nutrition = $conn->prepare("INSERT INTO recipe_nutrition (recipe_id, calories, fat, protein, carbohydrates) VALUES (?, ?, ?, ?, ?)");
        if (!$stmt_nutrition) throw new Exception("Nutrition prepare failed: ".$conn->error);
        $stmt_nutrition->bind_param("idddd", $recipe_id, $total_calories, $total_fat, $total_protein, $total_carbs);
        if (!$stmt_nutrition->execute()) throw new Exception("Nutrition insert failed: ".$stmt_nutrition->error);
        $stmt_nutrition->close();
        // --- END: Calculate and Insert Total Nutrition ---

        $conn->commit();

        echo json_encode([
            'status' => 'success',
            'message' => 'Recipe added successfully',
            'recipe_id' => $recipe_id
        ]);
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode([
            'status' => 'error',
            'message' => 'Failed to add recipe details: '.$e->getMessage()
        ]);
        exit;
    }
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Failed to add recipe: '.$conn->error
    ]);
}

$stmt->close();
$conn->close();
?>