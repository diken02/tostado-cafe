<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width"><title>POS y Pedidos</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><%@ include file="_nav.jspf" %><main>
<div class="page-heading"><div><h1>Punto de venta</h1><p>Administración de pedidos recientes.</p></div></div>
<div class="table-card table-wrap"><table><thead><tr><th>ID</th><th>Tipo</th><th>Estado</th><th>Total</th><th>Cliente</th></tr></thead>
<tbody><c:forEach items="${rows}" var="pedido"><tr><td>#${pedido.id}</td><td>${pedido.tipo}</td><td><span class="status">${pedido.estado}</span></td><td>$ ${pedido.total}</td><td>${pedido.nombre_cliente}</td></tr></c:forEach></tbody></table>
<c:if test="${empty rows}"><div class="empty">Todavía no hay pedidos registrados.</div></c:if></div></main></body></html>
