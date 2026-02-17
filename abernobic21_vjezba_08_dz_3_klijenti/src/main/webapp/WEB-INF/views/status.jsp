<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Zadaća 3 - status Tvrtka</title>
<link rel="stylesheet"
	href="${pageContext.servletContext.contextPath}/resources/css/nwtis.css">
</head>
<body>

	<ul class="navigacija">
		<li><a class="navigacija-text"
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica</a></li>
	</ul>

	<div style="margin-left: 270px; padding: 20px;">
		<h1>Zadaća 3 - status Tvrtka</h1>

		<div class="flex-container">

			<%
			if (request.getAttribute("status") != null) {
			%>
			<div class="card">
				<h2>Status operacije</h2>
				<div class="status-text">
					<%=request.getAttribute("status")%>
				</div>
			</div>
			<%
			}
			%>

			<%
			if (request.getAttribute("samoOperacija") != null
			    && !(Boolean) request.getAttribute("samoOperacija")) {
			%>
			<div class="card">
				<h2>Poslužitelj tvrtka</h2>
				<div class="status-text">
					<%=request.getAttribute("statusT")%>
				</div>
			</div>
			<%
			}
			%>

		</div>
	</div>

</body>
</html>
