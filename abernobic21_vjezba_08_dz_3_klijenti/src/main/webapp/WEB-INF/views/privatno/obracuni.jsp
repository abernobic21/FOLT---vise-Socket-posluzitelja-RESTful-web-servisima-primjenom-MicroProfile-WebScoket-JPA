<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Obracun"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>REST MVC - Pregled obračuna</title>
<link rel="stylesheet"
	href="${pageContext.servletContext.contextPath}/resources/css/nwtis.css">
</head>
<body>
	<div class="main-wrapper">

		<h1>REST MVC - Pregled REST MVC - Pregled obračuna</h1>

		<ul class="flex-container">
			<li><a class="navigacija-text"
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
					stranica</a></li>
		</ul>

		<div class="section-container">
			<h3>Filter obračuna po vremenu</h3>
			<form id="filterForma" onsubmit="return false;">
				<label for="vrijemeOd">Od:</label> <input type="datetime-local"
					id="vrijemeOd" name="od" /> <label for="vrijemeDo">Do:</label> <input
					type="datetime-local" id="vrijemeDo" name="do" /> <label
					for="vrsta">Vrsta:</label> <select id="vrsta" name="vrsta">
					<option value="sve" selected>Sve</option>
					<option value="jelo">Jelo</option>
					<option value="pice">Piće</option>
				</select>

				<button class="btn" onclick="dohvatiObracune()">Filtriraj</button>
			</form>
		</div>

		<br />

		<div id="tablicaObracuna" class="section-container">
			<jsp:include page="tablicaObracuni.jsp" />
		</div>

	</div>

	<script>
	function dohvatiObracune() {
	    const odString = document.getElementById("vrijemeOd").value;
	    const doString = document.getElementById("vrijemeDo").value;

	    const vrijemeOd = new Date(odString).getTime();
	    const vrijemeDo = new Date(doString).getTime();

	    console.log(window.location.pathname);
	    console.log(window.location.origin);

	    let vrsta = document.getElementById("vrsta").value;
	    
	    let url = window.location.pathname +"/" + vrsta;
	    let params = [];

	    if (vrijemeOd != null && !isNaN(vrijemeOd)) {
	        params.push("od=" + vrijemeOd);
	    }
	    if (vrijemeDo != null && !isNaN(vrijemeDo)) {
	        params.push("do=" + vrijemeDo);
	    }

	    if (params.length > 0) {
	        url += "?" + params.join("&");
	    }

	    console.log("url:", url);
	    fetch(url)
	    .then(odgovor => {
	    	console.log(odgovor.status);
	        if (!odgovor.ok) throw new Error("Neuspješan odgovor servera");
	        return odgovor.text();
	    })
	    .then(novaTablica => {
	    	console.log(novaTablica);
		    const tablica = document.querySelector("#tablicaObracuna tbody");
		    tablica.innerHTML = novaTablica; 
		});
	}
	</script>

</body>
</html>
