<div id="characters">
	<c:set var="profession" value="warrior" />
	<c:set var="character" value="${warrior}" />
	<%@ include file="character.jsp" %>

	<c:set var="profession" value="wizard" />
	<c:set var="character" value="${wizard}" />
	<%@ include file="character.jsp" %>

	<c:set var="profession" value="cleric" />
	<c:set var="character" value="${cleric}" />
	<%@ include file="character.jsp" %>
</div>
