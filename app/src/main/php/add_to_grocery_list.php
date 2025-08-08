<?php
header('Content-Type: application/json');
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$con = new mysqli($servername, $username, $password, $dbname);
if ($con->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);
$user_id = $data['user_id'];
$recipe_id = $data['recipe_id'];
$ingredients = $data['ingredients'];
$week = isset($data['week']) ? $data['week'] : null; // <-- Get week from frontend

// Conversion factors (to base unit)
$unitConversions = [
    'g' => ['factor' => 1, 'type' => 'mass'],
    'gram' => ['factor' => 1, 'type' => 'mass'],
    'grams' => ['factor' => 1, 'type' => 'mass'],
    'kg' => ['factor' => 1000, 'type' => 'mass'],
    'ml' => ['factor' => 1, 'type' => 'volume'],
    'l' => ['factor' => 1000, 'type' => 'volume'],
    'tsp' => ['factor' => 4.93, 'type' => 'volume'],
    'tbsp' => ['factor' => 14.79, 'type' => 'volume'],
    'cup' => ['factor' => 240, 'type' => 'volume']
];

function parseAmount($amountStr, $unitConversions) {
    preg_match('/([\\d.]+)\\s*(\\w+)/i', $amountStr, $match);
    $value = isset($match[1]) ? floatval($match[1]) : 0;
    $unit = isset($match[2]) ? strtolower($match[2]) : '';

    if (isset($unitConversions[$unit])) {
        $factor = $unitConversions[$unit]['factor'];
        $type = $unitConversions[$unit]['type'];
        return [$value * $factor, $type, $unit];  // return base value, type, and original unit
    }
    return [$value, 'other', $unit]; // store as original if unknown
}

foreach ($ingredients as $ingredient) {
    $name = trim($ingredient['name']);
    $incomingAmountStr = trim($ingredient['amount']);
    list($incomingValue, $incomingType, $incomingUnit) = parseAmount($incomingAmountStr, $unitConversions);

    // Now also check for week in the WHERE clause
    $stmt = $con->prepare("SELECT id, amount FROM grocery_lists WHERE user_id = ? AND ingredient_name = ? AND purchased = 0 AND week = ?");
    $stmt->bind_param("iss", $user_id, $name, $week);
    $stmt->execute();
    $result = $stmt->get_result();

    $matched = false;

    while ($row = $result->fetch_assoc()) {
        list($existingValue, $existingType, $existingUnit) = parseAmount($row['amount'], $unitConversions);

        if ($incomingType === $existingType && $incomingType !== 'other') {
            // Known type and match — combine
            $total = round($existingValue + $incomingValue, 1);
            $unitLabel = ($incomingType === 'mass') ? 'g' : 'ml';
            $combinedAmount = $total . " " . $unitLabel;

            $update = $con->prepare("UPDATE grocery_lists SET amount = ? WHERE id = ?");
            $update->bind_param("si", $combinedAmount, $row['id']);
            $update->execute();
            $matched = true;
            break;
        }
    }

    if (!$matched) {
        // Store original string if unknown unit, or base-converted if known
        if ($incomingType === 'other') {
            $combinedAmount = $incomingAmountStr; // store as-is
        } else {
            $unitLabel = ($incomingType === 'mass') ? 'g' : 'ml';
            $combinedAmount = round($incomingValue, 1) . " " . $unitLabel;
        }

        // Insert with week value
        $insert = $con->prepare("INSERT INTO grocery_lists (user_id, recipe_id, ingredient_name, amount, purchased, week) VALUES (?, ?, ?, ?, 0, ?)");
        $insert->bind_param("iisss", $user_id, $recipe_id, $name, $combinedAmount, $week);
        $insert->execute();
    }
}

echo json_encode(['status' => 'success', 'message' => 'Ingredients added to grocery list.']);
?>