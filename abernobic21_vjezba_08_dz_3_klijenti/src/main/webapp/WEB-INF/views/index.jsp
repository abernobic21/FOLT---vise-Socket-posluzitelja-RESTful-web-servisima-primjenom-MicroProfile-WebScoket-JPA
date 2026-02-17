<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Vježba 8 - zadaća 3 - Početna stranica</title>
<link rel="stylesheet"
	href="${pageContext.servletContext.contextPath}/resources/css/nwtis.css" />
</head>
<body>
	<h1>Vježba 8 - zadaća 3 - Početna stranica</h1>
	<ul class="navigacija">
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica Tvrtka</a></li>
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/index.xhtml">Početna
				stranica Partner</a></li>
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/status">Status
				poslužitelja Tvrtka</a></li>
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled
				partnera</a></li>
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno">Privatni
				dio tvrtka</a></li>
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin">Admin
				dio tvrtka</a></li>
	</ul>
</body>
</html>
