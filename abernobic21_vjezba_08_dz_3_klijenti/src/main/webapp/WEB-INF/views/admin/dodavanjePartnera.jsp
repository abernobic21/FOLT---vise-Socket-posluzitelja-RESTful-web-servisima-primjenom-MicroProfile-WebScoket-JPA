<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>REST MVC - Dodavanje partnera</title>
<link rel="stylesheet"
	href="${pageContext.servletContext.contextPath}/resources/css/nwtis.css" />
<style type="text/css">
.form-group {
	margin-bottom: 15px;
	display: flex;
	flex-direction: column;
}

.form-group label {
	font-weight: bold;
	margin-bottom: 5px;
}

.form-group input {
	padding: 6px 8px;
	font-size: 1rem;
	border: 1px solid #ccc;
	border-radius: 4px;
	max-width: 300px; /* po želji */
}
</style>
</head>
<body>
	<div class="main-wrapper">

		<h1>REST MVC - Dodavanje partnera</h1>

		<ul class="flex-container">
			<li><a class="navigacija-text"
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
					stranica</a></li>
		</ul>

		<div class="section-container">
			<form id="partnerForma"
				action="${pageContext.request.contextPath}/mvc/tvrtka/admin/partner"
				method="post">

				<div class="form-group">
					<label for="id">ID:</label> <input type="number" name="id" id="id"
						required />
				</div>

				<div class="form-group">
					<label for="naziv">Naziv:</label> <input type="text" name="naziv"
						id="naziv" required />
				</div>

				<div class="form-group">
					<label for="vrstaKuhinje">Vrsta kuhinje:</label> <input type="text"
						name="vrstaKuhinje" id="vrstaKuhinje" />
				</div>

				<div class="form-group">
					<label for="adresa">Adresa:</label> <input type="text"
						name="adresa" id="adresa" required />
				</div>

				<div class="form-group">
					<label for="mreznaVrata">Mrežna vrata:</label> <input type="number"
						name="mreznaVrata" id="mreznaVrata" required />
				</div>

				<div class="form-group">
					<label for="mreznaVrataKraj">Mrežna vrata kraj:</label> <input
						type="number" name="mreznaVrataKraj" id="mreznaVrataKraj" required />
				</div>

				<div class="form-group">
					<label for="gpsSirina">GPS širina:</label> <input type="number"
						step="0.000001" name="gpsSirina" id="gpsSirina" />
				</div>

				<div class="form-group">
					<label for="gpsDuzina">GPS dužina:</label> <input type="number"
						step="0.000001" name="gpsDuzina" id="gpsDuzina" />
				</div>

				<div class="form-group">
					<label for="sigurnosniKod">Sigurnosni kod:</label> <input
						type="text" name="sigurnosniKod" id="sigurnosniKod" required />
				</div>

				<div class="form-group">
					<label for="adminKod">Admin kod:</label> <input type="text"
						name="adminKod" id="adminKod" required />
				</div>

				<div class="submit-row" style="margin-top: 10px;">
					<button class="btn" type="submit">Registriraj</button>
				</div>
			</form>


			<div id="poruka" style="margin-top: 15px; color: red;"></div>
		</div>

	</div>

	<script>
	document.getElementById("partnerForma").addEventListener("submit", function(e) {
    e.preventDefault();

    const id =  document.getElementById("id").value.trim();
    const naziv = document.getElementById("naziv").value.trim();
    const sigurnosniKod = document.getElementById("sigurnosniKod").value.trim();
    const adminKod = document.getElementById("adminKod").value.trim();
    const mreznaVrata = document.getElementById("mreznaVrata").value.trim();
    const mreznaVrataKraj = document.getElementById("mreznaVrataKraj").value.trim();
    const gpsSirina = document.getElementById("gpsSirina").value.trim();
    const gpsDuzina = document.getElementById("gpsDuzina").value.trim();
    
    let errorMsg = "";

    if (!/^\d+$/.test(id)) {
        errorMsg += "Id mora biti nenegativan cijeli broj.<br>";
    }
    
    if (naziv === "") {
        errorMsg += "Naziv je obavezan.<br>";
    }

    if (sigurnosniKod.length == "") {
        errorMsg += "Sigurnosni kodje obavezan.<br>";
    }

    if (adminKod.length == "") {
        errorMsg += "Admin kod je obavezan.<br>";
    }
    
    if (!/^\d+$/.test(mreznaVrata)) {
        errorMsg += "Mrežna vrata moraju biti nenegativan cijeli broj.<br>";
    }

    if (!/^\d+$/.test(mreznaVrataKraj)) {
        errorMsg += "Mrežna vrata kraj moraju biti nenegativan cijeli broj.<br>";
    }

    if (gpsSirina && isNaN(parseFloat(gpsSirina))) {
        errorMsg += "GPS širina mora biti broj (float).<br>";
    }

    if (gpsDuzina && isNaN(parseFloat(gpsDuzina))) {
        errorMsg += "GPS dužina mora biti broj (float).<br>";
    }

    if (errorMsg) {
        document.getElementById("poruka").innerHTML = errorMsg;
    } else {
        document.getElementById("poruka").innerHTML = "";
        const form = this;
        const formData = new FormData(form);

        fetch(form.action, {
            method: "POST",
            body: new URLSearchParams(formData),
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            }
        })
        .then(response => {
            if (response.status == 201) {
                document.getElementById("poruka").innerText = "Uspješno kreiran partner.";
                form.reset();
            } else if(response.status == 409) {
            	document.getElementById("poruka").innerHTML = "Partner već postoji u bazi podataka.";
            }else{
            	document.getElementById("poruka").innerHTML = "Greška pri kreiranju partnera";
            }
        })
        .catch(err => {
            document.getElementById("poruka").innerHTML = "Greška pri kreiranju partnera";
        });
    }
});	
</script>

</body>
</html>


