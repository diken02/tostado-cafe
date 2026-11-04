<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width"><title>Mesas</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><%@ include file="_nav.jspf" %><main>
<div class="page-heading"><div><h1>Mesas</h1><p>Consulta la disponibilidad de tu salón.</p></div></div>
<section class="cards"><c:forEach items="${rows}" var="mesa"><article><h3>Mesa ${mesa.numero}</h3><p>Capacidad: ${mesa.capacidad} personas</p><strong class="status ${mesa.estado}">${mesa.estado}</strong></article></c:forEach></section>
</main></body></html>
