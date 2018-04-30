<?php
	if (!isset($_GET['code'])) {
		header("Location: runs.php");
		exit();
	}
	
	header('Content-Type: application/vnd.google-earth.kml+xml kml');
	header('Content-Disposition: attachment; filename="run_' . $_GET['code'] . '.kml"');

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

	
	
	
	//get the run id from the database
	$sql = "SELECT run_id FROM Runs WHERE run_code = '" . $_GET['code']  . "';";
	$result = $conn->query($sql);
	while($row = $result->fetch_assoc()) {
		$run_id = $row['run_id'];
	}
	
	// start the document
	echo "<?xml version='1.0' encoding='UTF-8'?>\n";
	echo "<kml xmlns='http://www.opengis.net/kml/2.2' xmlns:gx='http://www.google.com/kml/ext/2.2' xmlns:kml='http://www.opengis.net/kml/2.2' xmlns:atom='http://www.w3.org/2005/Atom'>\n";
	echo "<Document>\n";
	echo "<Style id='flag-style'\n>"
	   . "<IconStyle>\n"
	   . "<Icon>\n"
	   . "<href>https://www.google.com/mapfiles/ms/micons/blue.png</href>\n"
	   . "</Icon>\n"
	   . "</IconStyle>\n"
	   . "</Style>\n";
	echo "<Style id='start-point-style'\n>"
	   . "<IconStyle>\n"
	   . "<Icon>\n"
	   . "<href>https://www.google.com/mapfiles/ms/micons/green.png</href>\n"
	   . "</Icon>\n"
	   . "</IconStyle>\n"
	   . "</Style>\n";
	echo "<Style id='middle-points-style'\n>"
	   . "<IconStyle>\n"
	   . "<Icon>\n"
	   . "<href>https://www.google.com/mapfiles/ms/micons/yellow.png</href>\n"
	   . "</Icon>\n"
	   . "</IconStyle>\n"
	   . "</Style>\n";
	echo "<Style id='end-point-style'\n>"
	   . "<IconStyle>\n"
	   . "<Icon>\n"
	   . "<href>https://www.google.com/mapfiles/ms/micons/red.png</href>\n"
	   . "</Icon>\n"
	   . "</IconStyle>\n"
	   . "</Style>\n";
	
	
	// query the database for users
	$sql = "SELECT user_id, unique_username, is_leader FROM Users WHERE run_id = " . $run_id . ";";
	$users = $conn->query($sql);
	
	//for each user in users
	while($user = $users->fetch_assoc()) {
		// query the database for locations
		$sql = "SELECT lon, lat, log_time, UNIX_TIMESTAMP(log_time) AS time_int FROM Locations WHERE run_id = "
			. $run_id . " AND user_id = " . $user['user_id'] . " ORDER BY log_time ASC;";
		//$sql = "SELECT lon, lat, log_time, FROM Locations WHERE run_id = 5453999 ORDER BY log_time ASC;";
		$locations = $conn->query($sql);
		
		// array storing all locations
		$loc_array = array();
		
		// make path lines for each user
		echo "<Placemark>\n";
		echo "<name>" . $user['unique_username'] . "</name>\n";
		//echo "<styleUrl>#random-style</styleUrl>\n";
		
		// line style
		$color = randomColor();
		echo "<Style\n>";
		echo "<LineStyle>\n";
		echo "<color>ff" . $color ."</color>";
		echo "<width>4</width>";
		echo "</LineStyle>\n";
		echo "<PolyStyle>";
		echo "<color>77" . $color ."</color>";
		echo "</PolyStyle>";
		echo "</Style>\n";
		
		echo "<description>User: " . $user['unique_username'];
		if (intval($user['is_leader']) == 1) {
			echo "\nTeam Leader";
		}
		echo "</description>\n";
		echo "<LineString>\n";
		echo "<extrude>1</extrude>\n";
		echo "<tessellate>1</tessellate>\n";
		echo "<altitudeMode>relativeToGround</altitudeMode>\n";
		
		echo "<coordinates>\n";
		while ($row = $locations->fetch_assoc()) {
			echo $row["lon"] . "," . $row["lat"] .",30\n";
			$loc_array[] = $row;
		}
		echo "</coordinates>\n";
		echo "</LineString>\n";
		echo "</Placemark>\n";
		//--end line
		
		
		// start Timing placemarks folder
		echo "<Folder>";
		echo "<name>Times for " . $user['unique_username'] . "</name>";
		// starting point
		echo "<Placemark>\n";
		echo "<styleUrl>#start-point-style</styleUrl>\n";
		echo "<name>" . $loc_array[0]["log_time"] . "</name>\n";
		echo "<description>Starting Point for\nUser: " . $user['unique_username'];
		echo "\nTime: " . $loc_array[0]["log_time"] . "</description>\n";
		echo "<Point>\n";
		echo "<extrude>1</extrude>\n";
		echo "<altitudeMode>relativeToGround</altitudeMode>\n";
		echo "<coordinates>" . $loc_array[0]["lon"] . "," . $loc_array[0]["lat"] . ",35</coordinates>\n";
		echo "</Point>\n";
		echo "</Placemark>\n";
		
		$INTERVAL = 120; // seconds in between the markers
		$next_time_cutoff = intval($loc_array[0]['time_int']) + $INTERVAL;
		$num_rows = sizeof($loc_array);
		// make markers along the line at least every 2 minutes
		for($i = 1; $i < $num_rows - 1; $i++){
			if ($loc_array[$i]['time_int'] >= $next_time_cutoff) {
				echo "<Placemark>\n";
				echo "<styleUrl>#middle-points-style</styleUrl>\n";
				echo "<name>" . $loc_array[$i]["log_time"] . "</name>\n";
				echo "<description>User: " . $user['unique_username'];
				echo "\nTime: " . $loc_array[$i]["log_time"] . "</description>\n";
				echo "<Point>\n";
				echo "<extrude>1</extrude>\n";
				echo "<altitudeMode>relativeToGround</altitudeMode>\n";
				echo "<coordinates>" . $loc_array[$i]["lon"] . "," . $loc_array[$i]["lat"] . ",35</coordinates>\n";
				echo "</Point>\n";
				echo "</Placemark>\n";
				
				//update time cutoff
				$next_time_cutoff = intval($loc_array[$i]['time_int']) + $INTERVAL;
			}
		}
		
		// ending point
		echo "<Placemark>\n";
		echo "<styleUrl>#end-point-style</styleUrl>\n";
		echo "<name>" . $loc_array[$num_rows - 1]["log_time"] . "</name>\n";
		echo "<description>Ending Point for\nUser: " . $user['unique_username'];
		echo "Time: " . $loc_array[$num_rows - 1]["log_time"] . "</description>\n";
		echo "<Point>\n";
		echo "<extrude>1</extrude>\n";
		echo "<altitudeMode>relativeToGround</altitudeMode>\n";
		echo "<coordinates>" . $loc_array[$num_rows - 1]["lon"] . "," . $loc_array[$num_rows - 1]["lat"] . ",35</coordinates>\n";
		echo "</Point>\n";
		echo "</Placemark>\n";
		
		echo "</Folder>";
		//--end time markers
		
		
		// query the database for flags
		$sql = "SELECT lon, lat, log_time, msg FROM Flags WHERE run_id = "
			. $run_id . " AND user_id = " . $user['user_id'] . " ORDER BY log_time ASC;";
		//$sql = "SELECT lon, lat, log_time, user_id, msg FROM Flags WHERE run_id = 5453999 ORDER BY log_time ASC;";
		$flags = $conn->query($sql);
		
		// make a point for each flag
		while ($flag = $flags->fetch_assoc()) {
			if(strlen($flag["msg"]) > 12) {
				$name = substr($flag["msg"], 0, 9) . "...";
			}
			else {
				$name = $flag["msg"];
			}
			
			echo "<Placemark>\n";
			echo "<styleUrl>#flag-style</styleUrl>\n";
			echo "<name>" . $name . "</name>\n";
			echo "<description>Flag set by User: " . $user['unique_username'];
			if (intval($user['is_leader']) == 1) {
				echo "\nTeam Leader";
			}
			echo "\nDescription: \n" . $flag["msg"] . "</description>\n";
			echo "<Point>\n";
			echo "<extrude>1</extrude>\n";
			echo "<altitudeMode>relativeToGround</altitudeMode>\n";
			echo "<coordinates>" . $flag["lon"] . "," . $flag["lat"] . ",40</coordinates>\n";
			echo "</Point>\n";
			echo "</Placemark>\n";
		}
		//--end flags
	}
	
	// end the document
	echo "</Document>\n";
	echo "</kml>";
	
	// generate a 2 character hex string
	function randomColorPart() {
		return str_pad( dechex( mt_rand( 0, 255 ) ), 2, '0', STR_PAD_LEFT);
	}

	// generate a 6 character hex string ( for color )
	function randomColor() {
		return randomColorPart() . randomColorPart() . randomColorPart();
	}
	
	//header("Location: runs.php");

?>