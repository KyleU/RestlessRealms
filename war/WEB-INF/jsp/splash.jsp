<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html >
<html xmlns:fb="http://www.facebook.com/2008/fbml">
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
				<c:if test="${error != null}">
					${error}
				</c:if>
				<c:if test="${error == null}">
					<c:choose>
						<c:when test="${state == 'characters'}">
							Welcome to Restless Realms. Choose or create your character below.
						</c:when>
						<c:when test="${state == 'create'}">
							Welcome to Restless Realms. Choose or create your character below.
						</c:when>
						<c:when test="${state == 'signin'}">
							
						</c:when>
						<c:otherwise>???</c:otherwise>
					</c:choose>
				</c:if>
			</div>
			<c:choose>
				<c:when test="${state == 'signin' || state == 'characters'}">
					<div id="splashheaderbutton">
						<a href="problems.html">Can't sign in?</a>
					</div>
				</c:when>
			</c:choose>

		</div>
		<div id="pagecontent">
			<div id="panels">
				<div>
					<div id="container-main" class="toprow">
						<c:choose>
							<c:when test="${state == 'characters'}">
								<%@ include file="include/characters.jsp" %>
							</c:when>
							<c:when test="${state == 'create'}">
								<%@ include file="include/create.jsp" %>
							</c:when>
							<c:when test="${state == 'signin'}">
								<%@ include file="include/signin.jsp" %>
							</c:when>
							<c:otherwise>???</c:otherwise>
						</c:choose>
					</div>
				</div>
				<div style="margin-top:5px;">
					<div id="updates">
						<div class="title" style="width:498px;">
							Updates
						</div>
						<div style="overflow:auto;height:295px;">
							<%@ include file="include/updates.jsp" %>
						</div>
					</div>
					<div class="feature rightcol">
						<div id="feature1">
						
						</div>
					</div>
					<div class="feature rightcol">
						<div id="feature2">
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<c:if test="${state == 'signin' || state == 'characters'}">
		<script src="http://static.ak.connect.facebook.com/js/api_lib/v0.4/FeatureLoader.js.php/en_US" type="text/javascript"></script>
		<script type="text/javascript">
			function onConnect() {
				FB.Connect.ifUserConnected("index.html",null);
			}
	
			FB.init("6200f4c07b6ef91fa2c89856aeefd6e5", "/xd_receiver.htm");
		</script>
	</c:if>
</body>
</html>