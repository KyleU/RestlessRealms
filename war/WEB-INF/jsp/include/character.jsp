<c:if test="${character != null}">
	<div id="${profession}-name">${character.name}</div>
	<div id="${profession}-level">Level ${character.level}</div>
	<div id="${character.profession}-portrait" class="portrait">
		<a href="?activate=${character.name}"><img src="img/splash/${character.profession}.png" /></a>
	</div>
	<button onclick="location.href='?activate=${character.name}';" id="${profession}-button" class="button button-large">Play Now</button>
	<a href="delete.html?name=${character.name}" onclick="return confirm('Are you quite sure you wish to delete your ${profession}? This can\'t be undone unless you ask very politely.');" id="${profession}-delete">Delete</a>
</c:if>
<c:if test="${character == null}">
	<div id="${profession}-select">
		<a href="?create=${profession}"><img src="img/splash/${profession}.png" /></a>
	</div>
	<button onclick="location.href='?create=${profession}';" id="${profession}-button" class="button button-large">New ${profession}</button>
</c:if>
