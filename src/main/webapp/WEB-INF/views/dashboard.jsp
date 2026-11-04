<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width"><title>Dashboard</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><%@ include file="_nav.jspf" %><main class="dashboard-main">
<div class="dashboard-heading"><h1>Dashboard</h1></div>
<section class="dashboard-metrics">
  <article class="metric-card metric-sales"><h3>Ventas hoy <span>↗</span></h3><strong>$ <fmt:formatNumber value="${dashboard.ventasHoy}" type="number" minFractionDigits="2" maxFractionDigits="2"/></strong><p class="metric-up">↗ +0.0% vs ayer</p></article>
  <article class="metric-card metric-orders"><h3>Pedidos hoy</h3><strong>${dashboard.pedidosHoy != null ? dashboard.pedidosHoy : 0}</strong><p>Registros del día</p></article>
  <article class="metric-card metric-ticket"><h3>Ticket promedio</h3><strong>$ <fmt:formatNumber value="${dashboard.ticketPromedio}" type="number" minFractionDigits="2" maxFractionDigits="2"/></strong><p>Por pedido entregado</p></article>
  <article class="metric-card metric-products"><h3>Productos</h3><strong>${dashboard.productos}</strong><p>Disponibles</p></article>
</section>
<section class="dashboard-status">
  <article class="status-card production"><div><h3>EN PRODUCCIÓN</h3><strong>${dashboard.enProduccion}</strong><p>${dashboard.enProduccion} pendiente(s)</p></div><span class="status-icon">◉</span></article>
  <article class="status-card delivered"><div><h3>ENTREGADOS</h3><strong>${dashboard.entregados}</strong><p>${dashboard.entregados} listo(s) para entregar</p></div><span class="status-icon">✓</span></article>
</section>
<section class="sales-panel"><h2>Últimas ventas del día</h2><div class="sales-empty"><span>▦</span><p>Sin ventas registradas hoy</p></div></section>
</main></body></html>
