<?php

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get the ingredient name from the request
$ingredient_name = isset($_GET['ingredient_name']) ? $_GET['ingredient_name'] : '';

// Validate ingredient_name
if (empty($ingredient_name)) {
    echo json_encode(["message" => "Ingredient name is missing"]);
    exit();
}

// Fetch nutrition data from database
$sql = "SELECT * FROM nutrition WHERE ingredient_name = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $ingredient_name);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    // Nutrition data found in the database, return it
    $row = $result->fetch_assoc();
    echo json_encode($row);
} else {
    // Fetch nutrition data from USDA API
    $api_key = "Jsbh9n8n2xeQVd4f4nYPia1qAXMP7DLMQjJPSF83";  // Replace with your USDA API key
    $url = "https://api.nal.usda.gov/fdc/v1/foods/search?query=" . urlencode($ingredient_name) . "&api_key=" . $api_key;
    $response = file_get_contents($url);
    $data = json_decode($response, true);

    if (isset($data['foods'][0])) {
        $food = $data['foods'][0];

        // Extract the nutritional values
        $calories = 0;
        $protein = 0;
        $carbs = 0;
        $fat = 0;
        $fiber = 0;

        foreach ($food['foodNutrients'] as $nutrient) {
            if ($nutrient['nutrientName'] == "Energy") {
                $calories = $nutrient['value'];
            } elseif ($nutrient['nutrientName'] == "Protein") {
                $protein = $nutrient['value'];
            } elseif ($nutrient['nutrientName'] == "Carbohydrate, by difference") {
                $carbs = $nutrient['value'];
            } elseif ($nutrient['nutrientName'] == "Total lipid (fat)") {
                $fat = $nutrient['value'];
            } elseif ($nutrient['nutrientName'] == "Fiber, total dietary") {
                $fiber = $nutrient['value'];
            }
        }

        // Insert the fetched data into the database
        $stmt = $conn->prepare("INSERT INTO nutrition (ingredient_name, calories, protein, carbs, fat, fiber) VALUES (?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("ssssss", $ingredient_name, $calories, $protein, $carbs, $fat, $fiber);
        $stmt->execute();

        // Return the nutrition data
        echo json_encode(array(
            "ingredient_name" => $ingredient_name,
            "calories" => $calories,
            "protein" => $protein,
            "carbs" => $carbs,
            "fat" => $fat,
            "fiber" => $fiber
        ));
    } else {
        echo json_encode(["message" => "Nutrition data not found for ingredient"]);
    }
}

$stmt->close();
$conn->close();
?>
