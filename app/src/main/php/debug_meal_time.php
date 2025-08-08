<?php
header("Content-Type: application/json");
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Log the raw input
$raw_input = file_get_contents('php://input');
error_log("DEBUG: Raw input received: " . $raw_input);

// Parse JSON
$data = json_decode($raw_input, true);

// Log the parsed data
error_log("DEBUG: Parsed data: " . print_r($data, true));

// Check if meal_time exists
if (isset($data['meal_time'])) {
    error_log("DEBUG: meal_time found: " . $data['meal_time']);
    $meal_time = $data['meal_time'];
} else {
    error_log("DEBUG: meal_time NOT found in data");
    $meal_time = 'NOT_FOUND';
}

// Return debug information
echo json_encode([
    'status' => 'debug',
    'raw_input' => $raw_input,
    'parsed_data' => $data,
    'meal_time_received' => $meal_time,
    'meal_time_exists' => isset($data['meal_time']),
    'all_keys' => array_keys($data ?? [])
]);
?> 