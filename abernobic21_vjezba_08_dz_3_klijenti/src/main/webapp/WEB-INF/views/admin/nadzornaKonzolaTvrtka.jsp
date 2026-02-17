<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Vježba 8 - zadaća 3 - Nadzorna konzola Tvrtka</title>
</head>
<body>
	<link rel="stylesheet"
		href="${pageContext.servletContext.contextPath}/resources/css/nwtis.css">
</head>
<body>

	<h1>Vježba 8 - zadaća 3 - Nadzorna konzola Tvrtka</h1>
	<ul class="navigacija">
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica</a></li>
	</ul>
	<br />

	<div class="flex-container">
		<div class="card">
			<h2>Poslužitelj registracija</h2>
			<div class="status-text" id="divStatus1">
				Status: <span id="status1"><%=request.getAttribute("statusT1")%></span>
			</div>
			<button class="btn btn-secondary" onclick="pauziraj(1)">Pauza</button>
			<button class="btn" onclick="pokreni(1)">Start</button>
		</div>

		<div class="card">
			<h2>Poslužitelj partneri</h2>
			<div class="status-text" id="divStatus2">
				Status: <span id="status2"><%=request.getAttribute("statusT2")%></span>
			</div>
			<button class="btn btn-secondary" onclick="pauziraj(2)">Pauza</button>
			<button class="btn" onclick="pokreni(2)">Start</button>
		</div>
	</div>

	<div class="section-container">
		<h2>Poslužitelj Tvrtka</h2>
		<div class="status-info">
			<p>
				<strong>Status rada:</strong> <span id="statusTvrtka"></span>
			</p>
			<p>
				<strong>Broj primljenih obračuna:</strong> <span id="brojObracuna"></span>
			</p>
			<p>
				<strong>Interna poruka:</strong> <span id="internaPoruka"></span>
			</p>
		</div>
		<form onsubmit="posaljiInternuPoruku(); return false;">
			<label for="porukaInput"><strong>Pošalji internu
					poruku:</strong></label><br> <input type="text" id="porukaInput"
				name="porukaInput" placeholder="Unesi poruku" required />
			<button class="btn" type="submit">Pošalji</button>
		</form>
		<button class="btn" onclick="kraj()">Kraj</button>
	</div>

	<script type="text/javascript">
		var wsocket;
		function connect() {
			var adresa = window.location.pathname;
			var dijelovi = adresa.split("/");
			adresa = "ws://" + window.location.hostname + ":"
					+ window.location.port + "/" + dijelovi[1] + "/ws/tvrtka";
			if ('WebSocket' in window) {
				wsocket = new WebSocket(adresa);
			} else if ('MozWebSocket' in window) {
				wsocket = new MozWebSocket(adresa);
			} else {
				alert('WebSocket nije podržan od web preglednika.');
				return;
			}
			wsocket.onmessage = onMessage;
		}

		function onMessage(evt) {
			var poruka = evt.data;
		    var dijelovi = poruka.split(";");

		    var status = dijelovi[0];
		    var brojObracuna = dijelovi[1];
		    var internaPoruka = dijelovi[2];

		    var statusElem = document.getElementById("statusTvrtka");
		    var brojElem = document.getElementById("brojObracuna");
		    var internaElem = document.getElementById("internaPoruka");

		    statusElem.textContent = status;
		    brojElem.textContent = brojObracuna;
		    internaElem.textContent = internaPoruka;

		    if(status === "RADI") {
		        statusElem.style.color = "green";
		    } else {
		        statusElem.style.color = "red";
		    }
		}

		window.addEventListener("load", connect, false);
		
		function pauziraj(id){
			var adresa = window.location.pathname;
			var dijelovi = adresa.split("/");
			adresa = "http://" + window.location.hostname + ":"
					+ window.location.port + "/" + dijelovi[1] + "/" + dijelovi[2] + "/" + dijelovi[3] + "/" + dijelovi[4]+ "/pauza/" + id;
			
			fetch(adresa).then(odgovor =>{
				const status = odgovor.status;
				if(status != 200){
					azurirajStatus(id);
					alert("Poslužitelj je već pauziran!");
				}else{
					azurirajStatus(id);
				}
			})
		}
		
		function pokreni(id){
			var adresa = window.location.pathname;
			var dijelovi = adresa.split("/");
			adresa = "http://" + window.location.hostname + ":"
					+ window.location.port + "/" + dijelovi[1] + "/" + dijelovi[2] + "/" + dijelovi[3] + "/" + dijelovi[4]+ "/start/" + id;
			
			fetch(adresa).then(odgovor =>{
				const status = odgovor.status;
				if(status != 200){
					azurirajStatus(id);
					alert("Poslužitelj je već pokrenut!");
				}else{
					azurirajStatus(id);
				}
			})
		}
		
		function azurirajStatus(id){
				var adresa = window.location.pathname;
				var dijelovi = adresa.split("/");
				adresa = "http://" + window.location.hostname + ":"
						+ window.location.port + "/" + dijelovi[1] + "/" + dijelovi[2] + "/" + dijelovi[3] + "/" + dijelovi[4]+ "/status/" + id;
				
				fetch(adresa).then(odgovor =>{
					const status = odgovor.status;
					document.getElementById("status" + id).innerHTML = status
				})
		}
		
		function kraj(){
			var adresa = window.location.pathname;
			var dijelovi = adresa.split("/");
			adresa = "http://" + window.location.hostname + ":"
					+ window.location.port + "/" + dijelovi[1] + "/" + dijelovi[2] + "/" + dijelovi[3] + "/" + dijelovi[4]+ "/kraj";
			
			fetch(adresa).then(odgovor =>{
				const status = odgovor.status;
				if(status == 200){
					alert("Poslužitelj tvrtka prestao je s radom.")
				}else{
					alert("Neuspješno gašenje poslužitelja tvrtka.")
				}
			})
		}
		
		function posaljiInternuPoruku(){
			const porukaInput = document.getElementById("porukaInput");
			const poruka = porukaInput.value.trim();


			  if (!wsocket || wsocket.readyState !== WebSocket.OPEN) {
			    alert("Veza nije otvorena. Pokušajte ponovno kasnije.");
			    return;
			  }

			  wsocket.send(poruka);
			  porukaInput.value = '';
		}
	</script>
</body>


</html>
