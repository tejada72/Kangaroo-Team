<!DOCTYPE html>
<html>
<?php 
	session_start();
	if(!isset($_SESSION['username']))
	{
	    // not logged in, so go to login page
	    header('Location: /login.php');
	    //exit();
	}

	//set up database connection
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

	//get the operator id
	$sql = "SELECT op_id FROM Operators WHERE username = '" . $_SESSION['username']  . "';";	
       	$result = $conn->query($sql);
	while($row = $result->fetch_assoc()) {
		$operatorID =  $row["op_id"];
	}

	// checks to see if user is trying to create a run
	if (isset($_GET['create']) && $_GET['create'] == 'true' ) {
		// create a run
		$runCode = createNewRun($operatorID);
		header('Location: map.php?code=' . $runCode);
	}
	
	// tries to create a run with a random run code. If the code already
	// exists, it tries a new one. It returns the run code.
	function createNewRun($operatorID) {
		
		global $conn;

		$code = "";
		do {
			$code = createPossibleCode();
			//$code = "abc123";
			$sql = "INSERT INTO Runs (op_id, run_code) VALUES ("
			. $operatorID . ", '" . $code . "');";
			$isSuccess = $conn->query($sql);
		} while (!$isSuccess);
		return $code;
	}
	
	// creates a random 6 character long string to be used as a run code
	function createPossibleCode() {
		$CODE_LENGTH = 6;
		$code = "";
		$characters = array_merge(range('a','z'), range('0','9'));
		$max = count($characters) - 1;

		for($i = 0; $i < $CODE_LENGTH; $i++) {
			$code .= $characters[mt_rand(0, $max)];
		}

		return $code;
	}

	function getRuns($operatorID) {
		global $conn;
		$sql = "SELECT run_code, is_active, start_time from Runs WHERE op_id = " . $operatorID . " ORDER BY start_time DESC;";
		return $conn->query($sql);
	}
?>

<head>
	<title>Runs</title>
    <meta name="viewport" content="initial-scale=1.0">
    <meta charset="utf-8">
    <link rel="stylesheet" href="/stylesheet/operator.css">
</head>
<body>
	<header>
		<h1>Area Monitoring Tool: Runs</h1>
	</header>	
	<!--<?php
		if(isset($_SESSION['username'])) {	
			echo "<p>Hello " . $_SESSION['username']  . "!</p>";
		}
	?>-->
	
	<main class='center-text'>
		<a href="runs.php?create=true" id="create-run-button">CREATE RUN</a>
		<h2>Past Runs</h2>
		<table style="width: 100%;">
		<tr>
			<th>Run Code</th>
			<th>Start Time</th>
			<th>Is Active?</th>
		</tr>
		<?php
			$runs = getRuns($operatorID);
			while($row = $runs->fetch_assoc()) {
				echo "<tr>";
				echo "<td><a href='map.php?code=" . $row['run_code'] . "'>" . $row['run_code'] . "</a></td>";
				echo "<td><a href='map.php?code=" . $row['run_code'] . "'>" . $row['start_time'] . "</a></td>";
				echo "<td><a href='map.php?code=" . $row['run_code'] . "'>" . ($row['is_active'] == 1 ? "yes" : "no") . "</a></td>";
				echo "</tr>";
			}
		?>
		</table>
	</main>
	<footer></footer>
</body>
</html>
