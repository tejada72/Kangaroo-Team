function initMap() {
	// Create a map object and specify the DOM element for display.
	var map = new google.maps.Map(document.getElementById('map'), {
		center: {lat: 39.711493, lng: -75.117101},
		zoom: 15
	});
	
	// Get the points and place markers on the map
	getIdAndLocations(map)
}

function getIdAndLocations(map) {
	//console.log("getting locations");
	var runCode = $("#code-box span").text();
	console.log('run code is ' + runCode);

	//ajax call for run_id
	$.ajax({
		url: '/api/map-info.php',
		type: 'post',
		data: {'action': 'get-run-id', 'run-code': runCode},
		cache: false,
		success: function(json) {
			var runId = json;
			//get and update the locations
			updateLocations(runId, map);
		}, // end success function for run_id
		error: function(xhr, desc, err) {
			console.log(xhr + "\n" + err);
		}
	}); //end ajax call for run_id
}

function updateLocations(runId, map) {
	$.ajax({
		url: '/api/map-info.php',
		type: 'post',
		data: {'action': 'get-locations', 'run-id': runId },
		cache: false,
		success: function(json) {
			//parse the json for the locations and add as markers
			console.log(json);
			$.each(json, function(i, item) {
				var point = new google.maps.LatLng(item.lat, item.lon);
				var marker = new google.maps.Marker({
					position: point,
					map: map
				});
			});
		},
		error: function(xhr, desc, err) {
			console.log(xhr + "\n" + err);
		}
	});
}
