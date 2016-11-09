<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html >
<html xmlns:fb="http://www.facebook.com/2008/fbml">
<head>
	<title>Restless Realms</title>
	<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="css/restlessrealms.css" />
	<script type="text/javascript" language="javascript" src="restlessrealms/restlessrealms.nocache.js"></script>
</head>
<body>
	<div id="bodycontent">
		<div id="pageheader">
			<img id="gamelogo" alt="Restless Realms" src="img/interface/logo.png" />
		</div>
		<div id="pagecontent">
			<div id="panels">
				<div>
					<div id="container-main" class="toprow">
						<span>Restless Realms is now loading...</span>
					</div>
				</div>
				<div id="beltcontainer">
				</div>
				<div id="experiencecontainer"></div>
				<div>
					<div id="container-bottomleft" class="bottomrow leftcol">
					</div>
					<div id="container-bottomright" class="bottomrow rightcol">
					</div>
					<div id="container-bottomcenter" class="bottomrow centercol">
					</div>
				</div>
				<div id="notifications"></div>
			</div>
			<div id="tipcontainer"></div>
		</div>
	</div>
	<script src="http://static.ak.connect.facebook.com/js/api_lib/v0.4/FeatureLoader.js.php/en_US" type="text/javascript"></script>
	<script type="text/javascript">
		FB.init("6200f4c07b6ef91fa2c89856aeefd6e5", "/xd_receiver.htm");
	</script>
</body>
</html>