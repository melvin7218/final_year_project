<?php
// Test file to verify meal time functionality
header("Content-Type: application/json");

// Simulate the JSON data that would be sent from Android
$testData = [
    'user_id' => 2,
    'recipe_id' => 1,
    'category' => 'Breakfast',
    'meal_date' => '2025-01-20',
    'meal_time' => '08:00:00', // This is the new time parameter
    'member_ids' => [1, 3],
    'portion_multipliers' => [
        'user' => 1.0,
        '1' => 1.2,
        '3' => 0.8
    ]
];

echo json_encode([
    'status' => 'success',
    'message' => 'Test data received correctly',
    'received_data' => $testData,
    'meal_time' => $testData['meal_time'],
    'formatted_time' => date('H:i', strtotime($testData['meal_time']))
]);
?> 