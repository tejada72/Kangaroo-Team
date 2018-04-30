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

	if($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['action'])) {
		if($_POST['action'] == 'get-run-id') {
			//get the run id from the database
			$sql = "SELECT run_id FROM Runs WHERE run_code = '" . $_POST['run-code']  . "';";
			$result = $conn->query($sql);
			while($row = $result->fetch_assoc()) {
				$run_id = $row['run_id'];
			}
			
			//return the run_id
			header("content-type:application/json");
			echo json_encode($run_id);
			exit();
		} elseif ($_POST['action'] == 'get-locations') {
			//query for the location data
			$sql = "SELECT lon, lat, log_time, user_id FROM Locations WHERE run_id = "
			. $_POST['run-id']  . " AND log_time > '" . $_POST['last-time'] . "' ORDER BY log_time ASC;";
			$result = $conn->query($sql);
			$data = array();
			while ($row = $result->fetch_assoc()) {
				$data[] = $row;
			}

			//return the location data
			header("content-type:application/json");
			echo json_encode($data);
			exit();
		} elseif ($_POST['action'] == 'get-users') {
			//query for the users
			$sql = "SELECT user_id, unique_username, is_leader, status FROM Users WHERE run_id = " . $_POST['run-id']  . ";";
			$result = $conn->query($sql);
			$data = array();
			while ($row = $result->fetch_assoc()) {
				$data[] = $row;
			}
			//return the users
			header("content-type:application/json");
			echo json_encode($data);
			exit();
		} elseif ($_POST['action'] == 'update-leader') {
			$data = "success";
			//query to demote previous leader, if exists
			if ($_POST['prev-id'] !== '-1') {
				$sql = "UPDATE Users Set is_leader = 0 WHERE user_id = " . $_POST['prev-id'] . ";";
				$result = $conn->query($sql);
				if ($result === false) {
					$data = "error";
				}
			}
			
			//query to promote new user to leader
			$sql = "UPDATE Users Set is_leader = 1 WHERE user_id = " . $_POST['new-id'] . ";";
			$result = $conn->query($sql);
			if ($result === false) {
				$data = "error";
			}
			
			header("content-type:application/json");
			echo json_encode($data);
			exit();
		} elseif ($_POST['action'] == 'get-flags') {
			//query for the flag data
			$sql = "SELECT lon, lat, log_time, user_id, msg FROM Flags WHERE run_id = "
			. $_POST['run-id']  . " AND log_time > '" . $_POST['last-time'] . "' ORDER BY log_time ASC;";
			$result = $conn->query($sql);
			$data = array();
			while ($row = $result->fetch_assoc()) {
				$data[] = $row;
			}

			//return the flag data
			header("content-type:application/json");
			echo json_encode($data);
			exit();
		}
	}
?>
