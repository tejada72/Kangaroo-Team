<?php
	session_start();
	$_SESSION['username'] = 'hello';
	header('Location: test1.php');
	exit();
?>

<html>
<head>
	<title>test</title>
</head>

<body>
	<h1>Connected</h1>
</body>
</html>
