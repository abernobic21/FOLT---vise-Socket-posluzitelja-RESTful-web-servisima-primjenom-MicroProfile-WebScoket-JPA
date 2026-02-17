<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="edu.unizg.foi.nwtis.podaci.Partner"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>REST MVC - Detalji partnera</title>
<link rel="stylesheet"
	href="${pageContext.servletContext.contextPath}/resources/css/nwtis.css">
<style>
table {
	border-collapse: collapse;
	width: 100%;
}

th, td {
	border: 1px solid #333;
	padding: 8px;
}

th {
	text-align: center;
	font-weight: bold;
	background-color: #ECE3A5;
	width: 30%;
}

td {
	width: 70%;
}

a {
	color: #045491;
	font-weight: bold;
	text-decoration: none;
}

a:hover {
	text-decoration: underline;
}

.card {
	padding: 20px;
	width: 80%;
	margin-left: 270px; /* da ne pokriva navigacija */
	box-sizing: border-box;
}

.navigacija {
	position: fixed;
	top: 0;
	left: 0;
	width: 250px;
	height: 100vh;
	background-color: #ECE3A5;
	box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
	padding: 20px;
	box-sizing: border-box;
	overflow-y: auto;
	z-index: 1000;
}

.navigacija-text {
	color: #045491;
	font-weight: bold;
	text-decoration: none;
	display: block;
	margin-bottom: 10px;
}
</style>
</head>
<body>
	<ul class="navigacija">
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica</a></li>
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled
				partnera</a></li>
	</ul>

	<div class="card">
		<h1>REST MVC - Detalji partnera</h1>
		<br />
		<%
		Partner partner = (Partner) request.getAttribute("partner");
		if (partner != null) {
		%>
		<table>
			<tr>
				<th>ID:</th>
				<td><%=partner.id()%></td>
			</tr>
			<tr>
				<th>Naziv:</th>
				<td><%=partner.naziv()%></td>
			</tr>
			<tr>
				<th>Vrsta kuhinje:</th>
				<td><%=partner.vrstaKuhinje()%></td>
			</tr>
			<tr>
				<th>Adresa:</th>
				<td><%=partner.adresa()%></td>
			</tr>
			<tr>
				<th>Mrežna vrata:</th>
				<td><%=partner.mreznaVrata()%></td>
			</tr>
			<tr>
				<th>Mrežna vrata za kraj:</th>
				<td><%=partner.mreznaVrataKraj()%></td>
			</tr>
			<tr>
				<th>GPS širina:</th>
				<td><%=partner.gpsSirina()%></td>
			</tr>
			<tr>
				<th>GPS dužina:</th>
				<td><%=partner.gpsDuzina()%></td>
			</tr>
			<tr>
				<th>Sigurnosni kod:</th>
				<td><%=partner.sigurnosniKod()%></td>
			</tr>
			<tr>
				<th>Admin kod:</th>
				<td><%=partner.adminKod()%></td>
			</tr>
		</table>
		<%
		} else {
		%>
		<p>Nema podataka o partneru.</p>
		<%
		}
		%>
	</div>
</body>
</html>
