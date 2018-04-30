<?php
	function is_request_post() {
		return $_SERVER['REQUEST_METHOD'] == 'POST';
	}
?>
