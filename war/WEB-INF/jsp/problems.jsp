<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html >
<html>
	<head>
		<title>Restless Realms</title>
		<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
	
		<link rel="stylesheet" type="text/css" href="css/restlessrealms.css" />
	</head>
	<body>
		<div id="bodycontent">
			<div id="pageheader">
				<img id="gamelogo" alt="Restless Realms" src="img/interface/logo.png" />
				<div id="gameslogan">
				</div>
			</div>
			<div id="pagecontent">
				If you got a problem, yo - I'll solve it.<br/>
				Describe your issue below, while my DJ revolves it.<br/>
				<br/>
				<form action="problems.html" method="POST">
					<textarea rows="5" cols="80" name="detail"></textarea><br/><br/>
					<button class="button-small">Go!</button>
				</form>
			</div>
		</div>
	</body>
</html>