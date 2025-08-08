<?php
header("Content-Type: application/json");

$apiKey = "d4574df105914e0fa1cb3c47bd80ea00";
$input = json_decode(file_get_contents("php://input"), true);

if (!isset($input["ingredients"]) || !is_array($input["ingredients"])) {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
    exit;
}

$ingredientLines = [];
foreach ($input["ingredients"] as $item) {
    $ingredientLines[] = $item["amount"] . " g " . $item["name"];  // e.g. "14 g olive oil"
}

$body = json_encode([
    "title" => "Custom Recipe",
    "ingr" => $ingredientLines
]);

$url = "https://api.spoonacular.com/recipes/analyzeNutrition?apiKey=" . $apiKey;

$ch = curl_init($url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, ["Content-Type: application/json"]);
curl_setopt($ch, CURLOPT_POSTFIELDS, $body);

$response = curl_exec($ch);
curl_close($ch);

$data = json_decode($response, true);

if (!$data || !isset($data["nutrients"])) {
    echo json_encode(["status" => "error", "message" => "Spoonacular API error"]);
    exit;
}

// Extract required nutrients
$summary = [
    "calories" => 0,
    "fat" => 0,
    "protein" => 0,
    "carbohydrates" => 0,
    "fiber" => 0
];

foreach ($data["nutrients"] as $nutrient) {
    $key = strtolower($nutrient["name"]);
    if (strpos($key, "calories") !== false) $summary["calories"] = $nutrient["amount"];
    elseif (strpos($key, "fat") !== false) $summary["fat"] = $nutrient["amount"];
    elseif (strpos($key, "protein") !== false) $summary["protein"] = $nutrient["amount"];
    elseif (strpos($key, "carbohydrate") !== false) $summary["carbohydrates"] = $nutrient["amount"];
    elseif (strpos($key, "fiber") !== false) $summary["fiber"] = $nutrient["amount"];
}

echo json_encode([
    "status" => "success",
    "nutrition" => $summary
]);
?>
