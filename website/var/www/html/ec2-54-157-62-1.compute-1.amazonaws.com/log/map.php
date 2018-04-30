<!DOCTYPE html>
<html>
<?php
	session_start();

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

	if(!isset($_SESSION['username'])) {
		// not logged in
		header('Location: /login.php');
	}

	if(!isset($_GET['code'])) {
		// no run is selected
		header('Location: runs.php');
	}

	$runCode = $_GET['code'];
	
	
	//get username for runCode
	/*$sql = "SELECT user_id FROM Runs WHERE run_code = '" . $_POST['run-code']  . "';";
	$result = $conn->query($sql);
	while($row = $result->fetch_assoc()) {
		$run_id = $row['run_id'];
	}*/
	
	
	// go to download if run is over
	$sql = "Select is_active FROM Runs WHERE run_code = '" . $runCode . "';";
	$result = $conn->query($sql);
	while($row = $result->fetch_assoc()) {
		$isActive = $row['is_active'];
	}
	
	if (intval($isActive) == 0) {
		header('Location: kml.php?code=' . $runCode);
	}

	// end the run after user clicks on button
	// then go to the runs page
	if (isset($_GET['end']) && $_GET['end'] == 'true') {
		endRun($runCode);
		header('Location: kml.php?code=' . $runCode);
	}

	function endRun($runCode) {
		global $conn;
		$sql = "UPDATE Runs SET is_active = 0 WHERE run_code = '" . $runCode . "';";
		$conn->query($sql);
	}
?>

<head>
	<?php echo "<title>Map of Run " . $runCode  . "</title>"; ?>
	<meta name="viewport" content="initial-scale=1.0">
	<meta charset="utf-8">
	<style>
		/* Always set the map height explicitly to define the size of the div
		 * element that contains the map. */
		#map {
			height: 100%;
		}
		/* Optional: Makes the sample page fill the window. */
		/* html, body {
			height: 100%;
			width: 75%;
			margin: 0;
			padding: 0;
		}*/
	</style>
	<link rel='stylesheet' href='/stylesheet/operator.css'>
</head>
<body>
	<header>
	<h1>Area Monitoring Tool: Map</h1>
	</header>
	
	<p class = 'info-bar'>Logged in as <?php echo $_SESSION['username']; ?> • <a href="../logout.php">Log out</a> • <a href='runs.php'>Runs</a></p>

	<div class='container'>
		<section id='map-sidebar'>
			<h2>Run Code:</h2>
			<div id='code-box'>
				<span><?php echo $runCode;?></span>
			</div>
			<a href='#' id='end-run-button' onclick='endRun("<?php echo $runCode ?>");'>END RUN</a>

			<h2>Users:</h2>
			
			<ul id='user-list'>
			</ul>
			<div><span>no team leader</span><input type="radio" name="leader_radios" value="-1" checked onclick='radioLeaderUpdate(this)' /></div>
			
		</section>
		<section id='map-main'>
			<div id="map"></div>
		</section>
	</div>


	<!--<script>
	      /*
		function initMap() {
	        // Create a map object and specify the DOM element for display.
	        var map = new google.maps.Map(document.getElementById('map'), {
	          center: {lat: 39.711493, lng: -75.117101},
	          zoom: 15
	        });
	      }
	      */

	</script> -->
	<script
		src="https://code.jquery.com/jquery-3.3.1.min.js"
		integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8="
		crossorigin="anonymous">
	</script>
	<script src='/js/map.js'></script>
	<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAkwQ2Z4nVsENwFjdr1BaCeq2QuYLCk5wI&callback=initMap"
    async defer></script>
  </body>
</html>
