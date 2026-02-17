<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List,edu.unizg.foi.nwtis.podaci.Partner"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>REST MVC - Pregled partnera</title>
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
}

.desno {
	text-align: right;
}

a {
	color: #045491;
	font-weight: bold;
	text-decoration: none;
}

a:hover {
	text-decoration: underline;
}
/* Dodajem padding u card */
.card {
	padding: 20px;
	width: 80%;
	margin-left: 270px;
	/* da ne ide ispod fiksne navigacije širine 250px + margin */
}
</style>
</head>
<body>
	<ul class="navigacija">
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica</a></li>
	</ul>

	<div class="card">
		<h1>REST MVC - Pregled partnera</h1>
		<br />
		<table>
			<tr>
				<th>R.br.</th>
				<th>Korisnik</th>
				<th>Naziv</th>
				<th>Adresa</th>
				<th>Mrežna vrata</th>
				<th>Mrežna vrata za kraj</th>
				<th>Admin kod</th>
				<th>Detalji</th>
			</tr>
			<%
			int i = 0;
			List<Partner> partneri = (List<Partner>) request.getAttribute("partneri");
			for (Partner p : partneri) {
			  i++;
			%>
			<tr>
				<td class="desno"><%=i%></td>
				<td><%=p.id()%></td>
				<td><%=p.naziv()%></td>
				<td><%=p.adresa()%></td>
				<td><%=p.mreznaVrata()%></td>
				<td><%=p.mreznaVrataKraj()%></td>
				<td><%=p.adminKod()%></td>
				<td><a
					href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner/<%= p.id() %>">Detalji</a></td>
			</tr>
			<%
			}
			%>
		</table>
	</div>
</body>
</html>
