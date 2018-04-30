// this will hold the time value of the last location received from the database
var lastTime = "";

// array of users with their coordinates
var users = {};

// array of map points for the flags
var flags = [];

// this will hold the time value of the last flag received from the database
var lastTimeFlags = "";

// arrow symbol
var lineSymbol;

// the run ID from the database. it will be populated by getId()
var runId = -1;

// user id of the current leader (previous when changing leaders)
// -1 = no leader
var prevLeaderId = -1;

function initMap() {
	// Create a map object and specify the DOM element for display.
	var map = new google.maps.Map(document.getElementById('map'), {
		center: {lat: 39.711493, lng: -75.117101},
		zoom: 15
	});
	
	//arrow symbol
	lineSymbol = {
		path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW,
		scale: 4.5,
		fillOpacity: 1
	};
	
	// get the list of users on the web page
	var userList = document.getElementById('user-list');

	// get runid, then loop to get users and coordinates
	getId(map, userList, looper);
}

function getId(map, userList, callback) {
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
			runId = json;
			callback(runId, map, userList);
		}, // end success function
		error: function(xhr, desc, err) {
			console.log(xhr + "\n" + err);
		}
	}); //end ajax call
}

function looper(runId, map, userList) {
	getUsers(runId, map, userList, updateLocations, updateFlags);
	setInterval(function(){
		getUsers(runId, map, userList, updateLocations, updateFlags);
	}, 7000);
}

function getUsers(runId, map, userList, callbackA, callbackB) {
	//ajax call for users
	$.ajax({
		url: '/api/map-info.php',
		type: 'post',
		data: {'action': 'get-users', 'run-id': runId},
		cache: false,
		success: function(json) {
			//parse the json for the users
			$.each(json, function(i, item) {
				dbUserId = item.user_id;
				
				if (!users[dbUserId]) { 
					// if the user doesn't exist in the list
					// add a new entry
					users[dbUserId] = {
						"username" : item['unique_username'],
						"is_leader" : item['is_leader'],
						"status" : item['status'],
						"coords" : [],
						"times"  : [],
						"color"  : getColor()
					}
					// add the Polyline with the coords array just created
					users[dbUserId]["line"] = new google.maps.Polyline({
						path: users[dbUserId]['coords'],
						geodesic: true,
						strokeColor: users[dbUserId]['color'],
						strokeOpacity: 1.0,
						strokeWeight: 4,
						icons: [{
							icon: lineSymbol,
							offset: '100%'
						},
						{
							icon: {
								path: google.maps.SymbolPath.CIRCLE,
								fillOpacity: 1
							},
							offset: '0%'
						}]
					});
					
					// set the Polyline to the map
					users[dbUserId]['line'].setMap(map);
					
					// add the user to the list on the page
					addUserToList(dbUserId, userList);
					
					// if the user is the leader, update prevLeaderId
					if ( parseInt(users[dbUserId]['is_leader']) == 1 ) {
						prevLeaderId = dbUserId;
					}
				}
				else {
					// if the user already exists
					// update statuses
					users[dbUserId]["is_leader"] = item['is_leader'];
					users[dbUserId]["status"] = item['status'];
				}
			});
			callbackA(runId, map);
			callbackB(runId, map);
			updateUserStatuses();
		}, // end success function
		error: function(xhr, desc, err) {
			console.log(xhr + "\n" + err);
		}
	}); //end ajax call
}

function updateLocations(runId, map) {
	$.ajax({
		url: '/api/map-info.php',
		type: 'post',
		data: {'action': 'get-locations', 'run-id': runId, 'last-time': lastTime },
		cache: false,
		success: function(json) {
			//updates the last time
			if (json != null && json.length != 0) {
				lastTime = json[json.length - 1].log_time;
			}
			
			//parse the json for the locations and add as markers
			$.each(json, function(i, item) {
				var userId = item.user_id;
				
				// update the list of lat/long points
				users[userId].coords.push({lat: parseFloat(item.lat), lng: parseFloat(item.lon)});
				//update the list of times
				users[userId]['times'].push(item.log_time);
				
				// update the path itself
				users[userId]['line'].setPath(  users[userId]['coords']  );
				users[userId]['line'].setMap(map);
				
			});
		},
		error: function(xhr, desc, err) {
			console.log(xhr + "\n" + err);
		}
	});
}

function updateFlags(runId, map) {
	$.ajax({
		url: '/api/map-info.php',
		type: 'post',
		data: {'action': 'get-flags', 'run-id': runId, 'last-time': lastTimeFlags },
		cache: false,
		success: function(json) {
			//updates the last time
			if (json != null && json.length != 0) {
				lastTimeFlags = json[json.length - 1].log_time;
			}
			
			//parse the json for the locations and add as markers
			$.each(json, function(i, item) {
				var marker = new google.maps.Marker({
					position: {lat: parseFloat(item.lat), lng: parseFloat(item.lon)},
					map: map,
					title: item.msg
				});
				
				/*
				var leaderStatus = "";
				if (parseInt(users[item.user_id]['is_leader']) == 1) {
					leaderStatus = "<br />Team Leader";
				}
				*/
				
				var infowindow = new google.maps.InfoWindow({
					content: "<strong>Flag set by User:</strong> " + users[item.user_id]['username']
					 /*+ leaderStatus*/ + "<br /><strong>Description:</strong><br />" + item['msg']
				});
				
				marker.addListener('click', function() {
					infowindow.open(map, marker);
				});
				
				flags.push(marker);
			});
		},
		error: function(xhr, desc, err) {
			console.log(xhr + "\n" + err);
		}
	});
}

// add the user to the list on the page
function addUserToList(userId, userList) {
	var name = users[userId].username;
	var color = users[userId].color;
	var leader = users[userId].is_leader;
	var checked = (leader == "1") ? "checked" : "";
	
	// inserts new li with the right properties as defined above
	userList.insertAdjacentHTML('beforeend',
		"<li id='" + userId + "'><div class='circle' style='background-color: " + color + ";'></div><span class = 'name'>"
		+ name + "</span><span class='inactive'> - inactive</span><span class='sos'> - SOS</span><input type='radio' name='leader_radios' value='" + userId +
		"' " + checked + " onclick='radioLeaderUpdate(this)' /></li>"
	);
}

function updateUserStatuses() {
	Object.keys(users).forEach( function(key) {
		console.log(users[key]["status"]);
		if(users[key]["status"] == "0") {
			//inactive
			$('#' + key + ' .inactive').css('display', 'inline');
			$('#' + key + ' .sos').css('display', 'none');
			$('#' + key + ' span').css({'color': '#4f4f4f', 'font-style': 'italic', 'font-weight': 'normal'});
			users[key]['line'].setOptions({strokeWeight: 4});
		} else if(users[key]["status"] == "1") {
			//active
			$('#' + key + ' .inactive').css('display', 'none');
			$('#' + key + ' .sos').css('display', 'none');
			$('#' + key + ' span').css({'color': '#000', 'font-style': 'normal', 'font-weight': 'normal'});
			users[key]['line'].setOptions({strokeWeight: 4});
		} else if(users[key]["status"] == "2") {
			//sos
			$('#' + key + ' .inactive').css('display', 'none');
			$('#' + key + ' .sos').css('display', 'inline');
			$('#' + key + ' span').css({'color': '#da2525', 'font-style': 'normal', 'font-weight': 'bold'});
			users[key]['line'].setOptions({strokeWeight: 8});
		}
		//$('#id').css('display', 'none');
	});

}

var colorIndex = 0;
var colors = 
	['hsl(0, 85%, 50%)', 'hsl(120, 85%, 50%)', 'hsl(240, 85%, 50%)',
	 'hsl(60, 85%, 50%)', 'hsl(180, 85%, 50%)', 'hsl(300, 85%, 50%)',
	 'hsl(30, 85%, 50%)', 'hsl(150, 85%, 50%)', 'hsl(270, 85%, 50%)',
	 'hsl(90, 85%, 50%)', 'hsl(210, 85%, 50%)', 'hsl(330, 85%, 50%)',
	 'hsl(0, 100%, 35%)', 'hsl(120, 100%, 35%)', 'hsl(240, 100%, 35%)',
	 'hsl(60, 100%, 35%)', 'hsl(180, 100%, 35%)', 'hsl(300, 100%, 35%)',
	 'hsl(30, 100%, 35%)', 'hsl(150, 100%, 35%)', 'hsl(270, 100%, 35%)',
	 'hsl(90, 100%, 35%)', 'hsl(210, 100%, 35%)', 'hsl(330, 100%, 35%)',
	 'hsl(0, 90%, 65%)', 'hsl(120, 90%, 65%)', 'hsl(240, 90%, 65%)',
	 'hsl(60, 90%, 65%)', 'hsl(180, 90%, 65%)', 'hsl(300, 90%, 65%)',
	 'hsl(30, 90%, 65%)', 'hsl(150, 90%, 65%)', 'hsl(270, 90%, 65%)',
	 'hsl(90, 90%, 65%)', 'hsl(210, 90%, 65%)', 'hsl(330, 90%, 65%)'
	 ];
// returns the next color in the defined list
function getColor() {
	var color = colors[colorIndex];
	colorIndex = (colorIndex + 1) % colors.length ;
	return color;
}



// listener for the radio buttons to promote users to team leaders
// e : the input element
function radioLeaderUpdate(e) {
	newValue = e.value;
	//console.log(newValue);
	updateLeader(newValue, prevLeaderId);
	
	//if(newValue !== prevLeaderId)
	prevLeaderId = newValue;
}

function updateLeader(newId, prevId) {
	//ajax call for update-leader
	$.ajax({
		url: '/api/map-info.php',
		type: 'post',
		data: {'action': 'update-leader', 'run-id': runId, 'new-id': newId, 'prev-id': prevId},
		cache: false,
		success: function(json) {
			var result = json;
			/*console.log("{'action': 'update-leader', 'run-id': " + runId + ", 'new-id': " + newId + ", 'prev-id': " + prevId + "}");
			console.log(result);
			console.log(users); */
			
		}, // end success function
		error: function(xhr, desc, err) {
			console.log(xhr + "\n" + err);
		}
	}); //end ajax call
}

function endRun(runCode) {
	if (confirm("Are you sure you want to end this run? This cannot be undone.")) {
		window.location.href = "map.php?code=" + runCode + "&end=true";
	} // else do nothing
}
