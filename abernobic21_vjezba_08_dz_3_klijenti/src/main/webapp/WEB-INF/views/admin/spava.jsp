<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>REST MVC - Spavanje tvrtke</title>
<link rel="stylesheet"
	href="${pageContext.servletContext.contextPath}/resources/css/nwtis.css" />
</head>
<body>
	<div class="main-wrapper">

		<h1>REST MVC - Spavanje tvrtke</h1>

		<ul class="flex-container">
			<li><a class="navigacija-text"
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
					stranica</a></li>
		</ul>

		<div class="section-container">
			<form id="forma"
				action="${pageContext.request.contextPath}/mvc/tvrtka/admin/spava"
				method="post">
				<label for="brojSekundi">Vrijeme spavanja (ms):</label> <input
					type="number" name="brojSekundi" id="brojSekundi" required />

				<div class="submit-row" style="margin-top: 10px;">
					<button class="btn" type="submit">Spavaj</button>
				</div>
			</form>

			<div id="poruka" style="margin-top: 15px; color: black;"></div>
		</div>

	</div>
	<script>
	document.getElementById("forma").addEventListener("submit", function(e) {
    e.preventDefault();

    const brojSekundi =  document.getElementById("brojSekundi").value.trim();
    
    let errorMsg = "";

    if (!/^\d+$/.test(brojSekundi)) {
        errorMsg += "Vrijeme spavanja mora biti nenegativan cijeli broj.<br>";
    }

    if (errorMsg) {
        document.getElementById("poruka").innerHTML = errorMsg;
    } else {
        document.getElementById("poruka").innerHTML = "";
        const form = this;
		const url = form.action + "?vrijeme=" + brojSekundi;
        document.getElementById("poruka").innerText = "Spava.";

        fetch(url, {
            method: "GET"
        })
        .then(response => {
            if (response.status == 200) {
                document.getElementById("poruka").innerText = "Spavanje uspješno određeno.";
                form.reset();
            }else{
            	document.getElementById("poruka").innerHTML = "Greška pri pokušaju spavanja";
            }
        })
        .catch(err => {
        	document.getElementById("poruka").innerHTML = "Greška pri pokušaju spavanja";
        });
    }
});	
</script>

</body>
</html>


