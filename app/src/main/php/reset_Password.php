<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "finalyearproject";

$conn = new mysqli($servername, $username, $password, $database);
if($conn->connect_error){
    die("Connection failed: ".conn->connect_error);
}

$email = isset($_POST['email']) ? $_POST['email'] : '';
$newPassword = isset($_POST['password']) ? password_hash($_POST['password'], PASSWORD_DEFAULT) : '';

if(empty($email) || empty($newPassword)){
    echo "Missing Data";
    exit();
}


$sql = "UPDATE users SET password=? WHERE email=?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $newPassword, $email);

if($stmt->execute()){
    echo "Password Updated";
}else{
    echo "Error Updating Password!";
}

$stmt->close();
$conn->close();
?>