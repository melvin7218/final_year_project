<?php
header('Content-Type: application/json');
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "finalyearproject";

// Connect to the database
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode([]);
    exit;
}

// Get the query parameter
$q = isset($_GET['q']) ? $_GET['q'] : '';
$q = $conn->real_escape_string($q);

// Query for up to 3 matching ingredient names that start with the query
$sql = "SELECT DISTINCT ingredient_name FROM ingredient_nutrition WHERE ingredient_name LIKE '$q%' LIMIT 3";
$result = $conn->query($sql);

$suggestions = [];
if ($result) {
    while ($row = $result->fetch_assoc()) {
        $suggestions[] = $row['ingredient_name'];
    }
}

$conn->close();
echo json_encode($suggestions);
?>