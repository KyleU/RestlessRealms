<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html >
<%@ taglib prefix="og" tagdir="/WEB-INF/tags/og" %>

<html xmlns:fb="http://www.facebook.com/2008/fbml">
<head>
	<title>Object Graph</title>
	<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
</head>
<body>
	<h3>Object Types</h3>
	<ul>
		<c:forEach items="${classMetadata}" var="entry">
			<li>
				<a href="objectgraph/${entry.value}.${extension}">${entry.key}</a>
			</li>
		</c:forEach>
	</ul>
</body>
</html>