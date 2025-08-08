<?php
header("Content-Type: application/json");
ini_set('display_errors', 1);
error_reporting(E_ALL);

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

try {
    $data = json_decode(file_get_contents("php://input"), true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        echo json_encode(["status" => "error", "message" => "Invalid JSON: " . json_last_error_msg()]);
        exit;
    }
    
    $userId = $data['user_id'] ?? null;
    $exerciseName = $data['exercise_name'] ?? null;
    $exerciseDate = $data['exercise_date'] ?? null;
    $startTime = $data['start_time'] ?? null;
    $durationMinutes = $data['duration_minutes'] ?? null;
    $memberIds = $data['member_ids'] ?? [];

    // Validation
    if (!$userId) {
        echo json_encode(["status" => "error", "message" => "Missing user_id"]);
        exit;
    }
    if (!$exerciseName) {
        echo json_encode(["status" => "error", "message" => "Missing exercise_name"]);
        exit;
    }
    if (!$exerciseDate) {
        echo json_encode(["status" => "error", "message" => "Missing exercise_date"]);
        exit;
    }
    if (!$startTime) {
        echo json_encode(["status" => "error", "message" => "Missing start_time"]);
        exit;
    }
    if (!$durationMinutes || $durationMinutes <= 0) {
        echo json_encode(["status" => "error", "message" => "Invalid duration_minutes"]);
        exit;
    }

    // Get exercise_id and MET from exercise table
    $exerciseQuery = $conn->prepare("SELECT id, MET FROM exercise WHERE name = ?");
    $exerciseQuery->bind_param("s", $exerciseName);
    $exerciseQuery->execute();
    $exerciseResult = $exerciseQuery->get_result();
    
    if ($exerciseResult->num_rows === 0) {
        echo json_encode(["status" => "error", "message" => "Exercise not found: " . $exerciseName]);
        exit;
    }
    
    $exerciseRow = $exerciseResult->fetch_assoc();
    $exerciseId = $exerciseRow['id'];
    $met = $exerciseRow['MET'];
    $exerciseQuery->close();

    // Get user weight from user_preferences
    $weightQuery = $conn->prepare("SELECT weight FROM user_preferences WHERE user_id = ?");
    $weightQuery->bind_param("i", $userId);
    $weightQuery->execute();
    $weightResult = $weightQuery->get_result();
    
    $userWeight = 70.0; // Default weight
    if ($weightRow = $weightResult->fetch_assoc()) {
        $userWeight = $weightRow['weight'];
    }
    $weightQuery->close();

    // Calculate duration in hours
    $durationHours = $durationMinutes / 60.0;

    // Calculate calories burned for user
    $userCaloriesBurned = $met * $userWeight * $durationHours;

    // Calculate end time
    $startDateTime = new DateTime($startTime);
    $endDateTime = clone $startDateTime;
    $endDateTime->add(new DateInterval('PT' . $durationMinutes . 'M'));
    $endTime = $endDateTime->format('H:i:s');

    // Insert into user_exercise table for the user with start and end times
    $insertUserExercise = $conn->prepare("INSERT INTO user_exercise (user_id, exercise_id, duration_minutes, calories_burned, exercise_date, starting_time, ending_time) VALUES (?, ?, ?, ?, ?, ?, ?)");
    $insertUserExercise->bind_param("iiddsss", $userId, $exerciseId, $durationMinutes, $userCaloriesBurned, $exerciseDate, $startTime, $endTime);
    $insertUserExercise->execute();
    $exerciseRecordId = $insertUserExercise->insert_id;
    $insertUserExercise->close();

    $exerciseResults = [];
    $exerciseResults[] = [
        'user_id' => $userId,
        'calories_burned' => round($userCaloriesBurned, 2),
        'duration_minutes' => $durationMinutes,
        'weight' => $userWeight,
        'type' => 'user'
    ];

    // If member_ids are provided, calculate calories for each member
    if (!empty($memberIds) && is_array($memberIds)) {
        // Get member weights from user_members table
        $memberWeightQuery = $conn->prepare("SELECT member_id, weight FROM user_members WHERE member_id IN (" . str_repeat('?,', count($memberIds) - 1) . '?)');
        $memberWeightQuery->bind_param(str_repeat('i', count($memberIds)), ...$memberIds);
        $memberWeightQuery->execute();
        $memberWeightResult = $memberWeightQuery->get_result();
        
        $memberWeights = [];
        while ($memberWeightRow = $memberWeightResult->fetch_assoc()) {
            $memberWeights[$memberWeightRow['member_id']] = $memberWeightRow['weight'] ?? 70.0; // Default weight if not found
        }
        $memberWeightQuery->close();

        // Insert into user_exercise_member table for each member with start and end times
        $insertMemberExercise = $conn->prepare("INSERT INTO user_exercise_member (user_exercise_id, member_id, duration_minutes, calories_burned, starting_time, ending_time) VALUES (?, ?, ?, ?, ?, ?)");
        
        foreach ($memberIds as $memberId) {
            $memberWeight = $memberWeights[$memberId] ?? 70.0;
            $memberCaloriesBurned = $met * $memberWeight * $durationHours;
            
            $insertMemberExercise->bind_param("iiddss", $exerciseRecordId, $memberId, $durationMinutes, $memberCaloriesBurned, $startTime, $endTime);
            $insertMemberExercise->execute();
            
            $exerciseResults[] = [
                'member_id' => $memberId,
                'calories_burned' => round($memberCaloriesBurned, 2),
                'duration_minutes' => $durationMinutes,
                'weight' => $memberWeight,
                'type' => 'member'
            ];
        }
        $insertMemberExercise->close();
    }

    // Respond with success
    echo json_encode([
        "status" => "success",
        "message" => "Exercise saved successfully",
        "exercise_record_id" => $exerciseRecordId,
        "calculated_data" => [
            "duration_minutes" => $durationMinutes,
            "duration_hours" => $durationHours,
            "start_time" => $startTime,
            "end_time" => $endTime,
            "met" => $met,
            "total_participants" => count($exerciseResults),
            "participants" => $exerciseResults
        ]
    ]);
    $conn->close();
} catch (Exception $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Server error: " . $e->getMessage()
    ]);
}
?> 