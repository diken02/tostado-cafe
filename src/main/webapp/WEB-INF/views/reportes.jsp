<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width"><title>Reportes</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><%@ include file="_nav.jspf" %><main>
<div class="page-heading"><div><h1>Reportes</h1><p>Indicadores generales de operación.</p></div></div>
<section class="cards"><article><h3>Ventas hoy</h3><strong>$ ${dashboard.ventasHoy}</strong><p>Ingresos completados</p></article><article><h3>Ticket promedio</h3><strong>Consulta</strong><p>Disponible desde el dashboard de ventas</p></article></section></main></body></html>
