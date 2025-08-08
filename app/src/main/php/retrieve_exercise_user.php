<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

$userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;
$exerciseDate = isset($_GET['exercise_date']) ? $_GET['exercise_date'] : null;

$response = [
    'status' => 'error',
    'exercises' => [],
    'message' => 'No exercises found'
];

// Validate if `user_id` and `exercise_date` are provided
if ($userId === null || $exerciseDate === null) {
    echo json_encode(["status" => "error", "message" => "Missing parameters"]);
    exit;
}

// Helper function to get names of members who participated in the exercise
function getExerciseParticipants($conn, $exerciseRecordId, $userId) {
    $participants = [];

    // Check if user participated in this exercise
    $sqlUser = "SELECT 1 FROM user_exercise WHERE id = ? AND user_id = ?";
    $stmtUser = $conn->prepare($sqlUser);
    $stmtUser->bind_param("ii", $exerciseRecordId, $userId);
    $stmtUser->execute();
    $resultUser = $stmtUser->get_result();
    if ($resultUser->fetch_assoc()) {
        $participants[] = ['type' => 'user', 'name' => 'User'];
    }
    $stmtUser->close();

    // Get member participants from user_exercise_member table
    $sqlMembers = "
        SELECT 
            uem.member_id,
            uem.duration_minutes,
            uem.calories_burned,
            um.member_name,
            um.weight
        FROM user_exercise_member uem
        LEFT JOIN user_members um ON uem.member_id = um.member_id
        WHERE uem.user_exercise_id = ?
    ";
    $stmtMembers = $conn->prepare($sqlMembers);
    $stmtMembers->bind_param("i", $exerciseRecordId);
    $stmtMembers->execute();
    $resultMembers = $stmtMembers->get_result();
    
    while ($memberRow = $resultMembers->fetch_assoc()) {
        $participants[] = [
            'type' => 'member',
            'member_id' => $memberRow['member_id'],
            'name' => $memberRow['member_name'],
            'duration_minutes' => $memberRow['duration_minutes'],
            'calories_burned' => $memberRow['calories_burned'],
            'weight' => $memberRow['weight']
        ];
    }
    $stmtMembers->close();

    return $participants;
}

// Query to get exercises for the user and specific date (including both user-only and member exercises)
$sql = "SELECT ue.id AS exercise_record_id, ue.exercise_date, ue.duration_minutes, 
               ue.calories_burned, e.name AS exercise_name, e.MET, ue.starting_time, ue.ending_time
        FROM user_exercise ue
        JOIN exercise e ON ue.exercise_id = e.id
        WHERE ue.user_id = ? AND ue.exercise_date = ?
        ORDER BY ue.starting_time ASC";

// Prepare and execute the query
$stmt = $conn->prepare($sql);
$stmt->bind_param("is", $userId, $exerciseDate);
$stmt->execute();
$result = $stmt->get_result();

// Process results
$exercises = [];
while ($row = $result->fetch_assoc()) {
    $exerciseRecordId = $row['exercise_record_id'];
    
    // Add exercise to the list
    $exercises[] = [
        'exercise_record_id' => $exerciseRecordId,
        'exercise_name' => $row['exercise_name'],
        'exercise_date' => $row['exercise_date'],
        'duration_minutes' => $row['duration_minutes'],
        'calories_burned' => $row['calories_burned'],
        'met' => $row['MET'],
        'starting_time' => $row['starting_time'],
        'ending_time' => $row['ending_time'],
        'participants' => getExerciseParticipants($conn, $exerciseRecordId, $userId)
    ];
}

// If exercises are found, set success status
if (count($exercises) > 0) {
    $response['status'] = 'success';
    $response['exercises'] = $exercises;
    $response['message'] = 'Exercises retrieved successfully';
}

// Return the response
echo json_encode($response);

$conn->close();
?> 