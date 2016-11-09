<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html >
<html>
	<head>
		<title>Restless Realms</title>
		<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
	
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/restlessrealms.css" />
	</head>
	<body>
		<div id="bodycontent">
			<div id="pageheader">
				<img id="gamelogo" alt="Restless Realms" src="${pageContext.request.contextPath}/img/interface/logo.png" />
				<div id="gameslogan">
					Error: ${code}
				</div>
			</div>
			<div id="pagecontent">
				<div style="padding-top:20px;font-size:16px;">${path}</div>
				<div style="padding-top:5px;font-size:16px;">${message}</div>
			</div>
		</div>
	</body>
</html>