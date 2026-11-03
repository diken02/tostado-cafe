<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Ingresar - Tostado</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><main class="login-card"><h1>Tostado Cafetería</h1><c:if xmlns:c="http://java.sun.com/jsp/jstl/core"></c:if>
<% if(request.getAttribute("error")!=null){ %><div class="error"><%=request.getAttribute("error")%></div><% } %>
<form method="post" action="${pageContext.request.contextPath}/login"><label>Usuario<input name="usuario" autocomplete="username" required></label>
<label>Contraseña<input name="contrasena" type="password" autocomplete="current-password" required></label><button type="submit">Ingresar</button></form></main></body></html>
