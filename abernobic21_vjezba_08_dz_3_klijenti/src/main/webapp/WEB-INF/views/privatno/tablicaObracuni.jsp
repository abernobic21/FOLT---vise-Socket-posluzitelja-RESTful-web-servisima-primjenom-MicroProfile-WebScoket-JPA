<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Obracun, java.text.SimpleDateFormat"%>
<table>
			<tr>
				<th>Redni broj</th>
				<th>Partner</th>
				<th>ID</th>
				<th>Jelo</th>
				<th>Količina</th>
				<th>Cijena</th>
				<th>Vrijeme</th>
			</tr>
			<%
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
			if (obracuni != null && !obracuni.isEmpty()) {
				int i = 0;
				for (Obracun o : obracuni) {
					i++;
			%>
			<tr>
				<td class="desno"><%=i%></td>
				<td><%=o.partner()%></td>
				<td><%=o.id()%></td>
				<td><%=o.jelo()%></td>
				<td class="desno"><%=o.kolicina()%></td>
				<td class="desno"><%=o.cijena()%></td>
				<td><%=sdf.format( new java.util.Date(o.vrijeme()))%></td>
			</tr>
			<%
			}
			} else {
			%>
			<tr>
				<td colspan="7" style="text-align: center;">Nema obračuna za prikaz</td>
			</tr>
			<%
			}
			%>
		</table>