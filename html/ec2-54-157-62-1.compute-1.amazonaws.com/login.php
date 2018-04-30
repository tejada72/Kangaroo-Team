<!DOCTYPE html>
<html>
<?php 
	session_start();
	$servername = "127.0.0.1";
        $username = "root";
        $password = "";
        $dbname = "area_monitoring";

        // Create connection
        $conn = new mysqli($servername, $username, $password, $dbname);

        // Check connection
        if ($conn->connect_error) {
                die("Connection failed: " . $conn->connect_error);
        }

	// invalid is true if the user entered the wrong username/password combination
	$invalid = false;

	if($_SERVER['REQUEST_METHOD'] == 'POST') {
		
		$sql = "SELECT password FROM Operators WHERE username='" . $_POST['username']  . "';";
        	$result = $conn->query($sql);
		while($row = $result->fetch_assoc()) {
			$actual_password =  $row["password"];
		}
		
		$password = isset($_POST['password']) ? hash("sha256", $_POST["password"]) : ' ';
		if(strcasecmp($password, $actual_password) == 0) {
			$_SESSION['username'] = $_POST['username'];
			header('Location: log/runs.php');
			//exit();
		} else {
			$invalid = true;
		}
	}
?>


	<head>
	<title>Login</title>
	<meta charset="utf-8">
	<link rel="stylesheet" media="all" href="stylesheet/operator.css"/>
	</head>

	<body>
		<header>
			<h1>Area Monitoring Tool: Login</h1>
		</header>
		<main>
			<form action="login.php" method="post" enctype="multipart/form-data">
				<input placeholder="Username" name="username" type="text" autofocus>
				<input placeholder="Password" name="password" type="password">
				<input name="login" type="submit" value="Login">
			</form>
			<?php
				if($invalid) {
					echo "<div class='errors'>Invalid username or password</div>";
				}
			?>
                </main>

	<footer>
		
	</footer>
	</body>
</html>
