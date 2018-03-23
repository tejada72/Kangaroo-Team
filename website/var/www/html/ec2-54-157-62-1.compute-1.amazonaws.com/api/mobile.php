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
		header('Content-type:application/json');
		$data = array(
			'error' => false,
			'error-msg' => '',
			'data' => array()
		);

		if($_POST['action'] == 'log-in') {
			if(!isset($_POST['run-code']) || !isset($_POST['username'])) {
				//the parameters were not set
				$data['error'] = true;
				$data['error-msg'] = 'Both a run-code and username need to be passed as parameters.';
				echo json_encode($data);
				exit();
			}
			
			// get the run id from the run code
			$sql = "SELECT run_id FROM Runs WHERE run_code = '"
			. $_POST['run-code'] . "';";
			$idResult = $conn->query($sql);
			if (mysqli_num_rows($idResult) == 0) {
				// the query failed
				$data['error'] = true;
				$data['error-msg'] = 'Invalid run code.';
				echo json_encode($data);
				exit();
			}
			
			while ($row = $idResult->fetch_assoc()) {
				$runId = $row['run_id'];
			}
			
			// check if the run is active
			$sql = "SELECT is_active FROM Runs WHERE run_id = " . $runId . ";";
			$activeResult = $conn->query($sql);
			while ($row = $activeResult->fetch_assoc()) {
				$isActive = $row['is_active'];
			}
			if ($isActive == 0) {
				// the run is no longer active
				// and the user can't join it
				$data['error'] = true;
				$data['error-msg'] = 'This run is no longer active. You cannot join it.';
				echo json_encode($data);
				exit();
			}

			// insert the new user into the database
			$sql = "INSERT INTO Users (run_id, unique_username)"
			. "VALUES (" . $runId . ", '"
			. $_POST['username'] . "');";
			$newUserResult = $conn->query($sql);
			if ($newUserResult === false) {
				// the query failed
				$data['error'] = true;
				$data['error-msg'] = 'Invalid username.';
				echo json_encode($data);
				exit();
			}

			//get the user id of the new user
			$sql = "SELECT user_id FROM Users WHERE run_id = "
			. $runId . " AND unique_username = '"
			. $_POST['username'] . "';";
			$userIdResult = $conn->query($sql);
			while ($row = $userIdResult->fetch_assoc()) {
				$userId = $row['user_id'];
			}

			// return the userId and the runId
			$data['data'] = array(
				'user-id' => $userId,
				'run-id' => $runId
			);
			echo json_encode($data);
			exit();
			
		} elseif($_POST['action'] == 'update-location') {
			if (!isset($_POST['run-id']) || !isset($_POST['user-id'])
			    || !isset($_POST['lat']) || !isset($_POST['lon'])) {
				//the parameters were not set
				$data['error'] = true;
				$data['error-msg'] = 'The parameters needed are run-id, user-id, loc, and lat; '
					. 'and log-time is optional.';
				echo json_encode($data);
				exit();
			}

			//check if the user id is associated with the run id
			$sql = "SELECT run_id FROM Users WHERE user_id = "
			. $_POST['user-id'] . ";";
			$userRunResult = $conn->query($sql);
			if (mysqli_num_rows($userRunResult) == 0) {
				// the query failed
				$data['error'] = true;
				$data['error-msg'] = 'Invalid user id.';
				echo json_encode($data);
				exit();
			}
			while ($row = $userRunResult->fetch_assoc()) {
				$dbRunId = $row['run_id'];
			}
			if ($dbRunId != $_POST['run-id']) {
				// the run-id does not match the
				// one in the database
				$data['error'] = true;
				$data['error-msg'] = 'The user is not a part of that run.';
				echo json_encode($data);
				exit();
			}

			//check if the run is still active
			$sql = "SELECT is_active FROM Runs WHERE run_id = "
			. $_POST['run-id'] . ";";
			$runActiveResult = $conn->query($sql);
			if (mysqli_num_rows($runActiveResult) == 0) {
				// the query failed
				$data['error'] = true;
				$data['error-msg'] = 'Invalid run id.';
				echo json_encode($data);
				exit();
			}
			while ($row = $runActiveResult->fetch_assoc()) {
				$isActive = $row['is_active'];
			}
			
			if ($isActive == 0) {
				// the run has ended and is no longer active
				$sql = "UPDATE Users SET is_active = 0 WHERE user_id = "
				. $_POST['user-id'] . ";";
				$userUpdateResult = $conn->query($sql);

				// tell user that the run has ended
				$data['data'] = array(
					'is-active' => false
				);
				echo json_encode($data);
				exit();
			} elseif (isset($_POST['log-time'])) {
				// update the location with log time if it's set
				$sql = "INSERT INTO Locations (lon, lat, log_time, user_id, run_id)"
				. " VALUES (" . $_POST['lon'] . ", " . $_POST['lat']
				. ", " . $_POST['log-time'] . ", " . $_POST['user-id']
				. ", " . $_POST['run-id'] . ");";
				$locResult = $conn->query($sql);
			} else {
				// update the location without the log time
				// the database will use the default value
				$sql = "INSERT INTO Locations (lon, lat, user_id, run_id)"
				. " VALUES (" . $_POST['lon'] . ", " . $_POST['lat']
				. ", " . $_POST['user-id'] . ", " . $_POST['run-id'] . ");";
				$locResult = $conn->query($sql);
			}
			if ($locResult === false) {
				// the query failed
				$data['error'] = true;
				$data['error-msg'] = "Invalid input. "
				. "lon and lat should be floating point numbers; "
				. "user-id, run-id, and log-time (if it's set) should be integers";
				echo json_encode($data);
				exit();
			} else {
				// the query succeeded
				$data['data'] = array(
					'is-active' => true
				);
				echo json_encode($data);
				exit();
			}
		}
	}
	
	exit();

?>
