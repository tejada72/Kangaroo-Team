<?php
	//session_save_path("/tmp");
	session_start();
	
	if(isset($_SESSION['username']))
		echo $_SESSION['username'];
	else
		echo "Session not found";
?>

<html>
<head>
	<title>test1</title>
</head>

<body>
	<h1>test1</h1>
</body>

</html>
