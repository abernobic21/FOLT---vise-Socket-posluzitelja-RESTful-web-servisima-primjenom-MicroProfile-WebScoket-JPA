<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Vježba 8 - zadaća 3 - Privatni dio Tvrtka</title>
<link rel="stylesheet"
	href="${pageContext.servletContext.contextPath}/resources/css/nwtis.css" />
</head>
<body>
	<h1>Vježba 8 - zadaća 3 - Početna stranica - privatno</h1>

	<div class="navigacija">
		<ul>
			<li><a class="navigacija-text"
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
					stranica Tvrtka</a></li>
			<li><a class="navigacija-text"
				href="${pageContext.servletContext.contextPath}/index.xhtml">Početna
					stranica Partner</a></li>
			<li><a class="navigacija-text"
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni">Pregled
					obračuna</a></li>
			<li><a class="navigacija-text"
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni/partner">Pregled
					obračuna partnera</a></li>
		</ul>
	</div>
</body>
</html>
