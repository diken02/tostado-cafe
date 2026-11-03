<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width"><title>Dashboard</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><%@ include file="_nav.jspf" %><main><h1>Dashboard</h1><section class="cards">
<article><h3>Ventas hoy</h3><strong>${dashboard.ventasHoy}</strong></article><article><h3>Productos</h3><strong>${dashboard.productos}</strong></article>
<article><h3>Clientes</h3><strong>${dashboard.clientes}</strong></article><article><h3>Mesas ocupadas</h3><strong>${dashboard.mesasOcupadas}</strong></article>
<article><h3>Alertas inventario</h3><strong>${dashboard.alertasInventario}</strong></article></section></main></body></html>
