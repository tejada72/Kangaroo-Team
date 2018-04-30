<!DOCTYPE html>
<html>
<?php

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

	// status can be:
	//	0 - default, nothing entered in form yet
	//	1 - successful user creation
	//	2 - the user entered an invalid username
	//	3 - the user did not enter matching passwords
	$status = 0;


	if($_SERVER['REQUEST_METHOD'] == 'POST') {
		
		if ($_POST['password'] !== $_POST['confirm_password']) {
			$status = 3;
		} elseif ($_POST['username'] !== '' && $_POST['password'] !== '' ) {	
			$sql = "INSERT INTO Operators (username, password) VALUES ('" . $_POST['username']  . "', '" . hash('sha256', $_POST['password'])  . "');";
        		if( $conn->query($sql) ) {
				$status = 1;
			} else {
				$status = 2;
			}
		}

	}


?>

<head>
	<title>Register</title>
	<link rel="stylesheet" media="all" href="stylesheet/operator.css"/>
</head>

<body>
	<header>
		<h1>Area Monitoring Tool: Register</h1>
	</header>
	<main>
		<form action="register.php" method="post" enctype="multipart/form-data">
			<input placeholder="Username" name="username" type="text" autofocus required>
		</br>
			<input placeholder="Password" name="password" type="password" required>
		</br>
			<input placeholder="Confirm Password" name="confirm_password" type="password" required>
		</br>
			<input name="register" type="submit" value="Register">
		</form>
		<?php
			if ($status == 1) {
				echo "<div class='successes'>User created successfully!<br />";
				echo "<a href='login.php'>Log in now</a></div>";
			} elseif ($status == 2) {
				echo "<div class='errors'>That username is not available</div>";
			} elseif ($status == 3) {
				echo "<div class='errors'>The passwords do no match</div>";
			}

		?>
		
    </main>

    <footer></footer>
</body>

<html>
