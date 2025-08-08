<?php 
$servername = "localhost";
$username = "root";
$password = "";
$database = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $database);
if($conn->connect_error){
    die("Connection failed: ".$conn->connect_error);
}

$email = $_POST['email'];

$sql = "SELECT * FROM users Where email=?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if($result->num_rows > 0){
    echo "Email Exists";
}else{
    echo "Email Not Found";
}

$stmt->close();
$conn->close();
?>