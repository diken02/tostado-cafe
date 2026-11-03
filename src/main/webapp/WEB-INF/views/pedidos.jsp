<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width"><title>POS y Pedidos</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><%@ include file="_nav.jspf" %><main><h1>Punto de venta</h1><h2>Pedidos</h2><div class="table-wrap"><table><tr><th>ID</th><th>Tipo</th><th>Estado</th><th>Total</th><th>Cliente</th></tr><c:forEach items="${rows}" var="pedido"><tr><td>${pedido.id}</td><td>${pedido.tipo}</td><td>${pedido.estado}</td><td>${pedido.total}</td><td>${pedido.nombre_cliente}</td></tr></c:forEach></table></div></main></body></html>
