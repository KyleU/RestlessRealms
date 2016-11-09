<%@page contentType="text/html; charset=UTF-8" %><!DOCTYPE html >
<%@ taglib prefix="og" tagdir="/WEB-INF/tags/og" %>
<html>
<head>
	<title>Object Graph</title>
	<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
</head>
<body>
	<h3>${typeName} List</h3>
	<table>
		<c:forEach items="${results}" var="result" varStatus="status">
			<c:if test="${status.first}">
				<thead>
					<tr>
				<c:forEach items="${result}" var="resultEntry">
					<c:if test="${idProperty == resultEntry.key}">
						<th style="white-space:nowrap;">
							${resultEntry.key}
						</th>
					</c:if>
				</c:forEach>
				<c:forEach items="${result}" var="resultEntry">
					<c:if test="${idProperty != resultEntry.key}">
						<th style="white-space:nowrap;">
							${resultEntry.key}
						</th>
					</c:if>
				</c:forEach>
					</tr>
				</thead>
				<tbody>
			</c:if>
			<tr>
				<c:forEach items="${result}" var="resultEntry">
					<c:if test="${idProperty == resultEntry.key}">
						<td style="white-space:nowrap;">
							<a href="${type}/${resultEntry.value}.${extension}">${resultEntry.value}</a>
						</td>
					</c:if>
				</c:forEach>
				<c:forEach items="${result}" var="resultEntry">
					<c:if test="${idProperty != resultEntry.key}">
						<td style="white-space:nowrap;">
							${resultEntry.value}
						</td>
					</c:if>
				</c:forEach>
			</tr>
			<c:if test="${status.last}">
				</tbody>
			</c:if>
		</c:forEach>
	</table>
</body>
</html>