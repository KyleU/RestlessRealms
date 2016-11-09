<script type="text/javascript">
	function inputFocus(i) {
		if(i.value == "Name") {
			i.value = "";
		}
	}

	function inputBlur(i) {
		if(i.value == "") {
			i.value = "Name";
		}
	}
	function selectGender(gender) {
		document.getElementById('gender').value = gender;
		var male = document.getElementById('male');
		var female = document.getElementById('female');
		if(gender == 'M') {
			male.style.backgroundPosition = "0px -30px";
			female.style.backgroundPosition = "-30px 0px";
		} else if(gender == 'F') {
			male.style.backgroundPosition = "0px 0px";
			female.style.backgroundPosition = "-30px -30px";
		} else {
			//?
		}
	}
</script>

<div id="create" class="create-${profession}">
	<form id="createform" action="create.html" method="POST">
	 	<input type="hidden" id="gender" name="gender" value="M" />
	 	<input type="hidden" id="profession" name="profession" value="${profession}" />

		<input id="name" type="text" name="name" onfocus="inputFocus(this);" onblur="inputBlur(this);" value="Name" />

	 	<div id="male" title="Male" onclick="selectGender('M');" class="gendericon"></div>
	 	<div id="female" title="Female" onclick="selectGender('F');" class="gendericon"></div>

		<button id="createbutton" onclick="document.getElementById('createform').submit();return false;" class="button button-small" />Create</button>
		<button id="cancelbutton" onclick="location.href='index.html';return false;" class="button button-small" />Cancel</button>
	</form>
</div>