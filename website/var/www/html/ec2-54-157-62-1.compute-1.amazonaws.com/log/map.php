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

	// end the run after user clicks on button
	// then go to the runs page
	if (isset($_GET['end']) && $_GET['end'] == 'true') {
		endRun($runCode);
		header('Location: runs.php');
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
	<?php
		if(isset($_SESSION['username'])) {	
			echo "<p>Logged in as  " . $_SESSION['username']  . ".</p>";
		}
	?>

	<div class='container'>
		<section id='map-sidebar'>
			<h2>Run Code:</h2>
			<div id='code-box'>
				<span><?php echo $runCode;?></span>
			</div>
			<?php echo "<a href='map.php?code=" . $runCode  . "&end=true' id='end-run-button'>END RUN</a>"; ?>
		</section>
		<section id='map-main'>
			<div id="map"></div>
		</section>
	</div>


	<script>
	      /*
		function initMap() {
	        // Create a map object and specify the DOM element for display.
	        var map = new google.maps.Map(document.getElementById('map'), {
	          center: {lat: 39.711493, lng: -75.117101},
	          zoom: 15
	        });
	      }
	      */

	</script>
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

