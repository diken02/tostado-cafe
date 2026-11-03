<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width"><title>${resource}</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"></head>
<body><%@ include file="_nav.jspf" %><main><h1>${resource}</h1><c:if test="${param.success != null}"><div class="flash">${param.success}</div></c:if>
<div class="table-wrap"><table><thead><c:if test="${not empty rows}"><tr><c:forEach items="${rows[0]}" var="col"><th>${col.key}</th></c:forEach><th>Acciones</th></tr></c:if></thead>
<tbody><c:forEach items="${rows}" var="row"><tr><c:forEach items="${row}" var="col"><td>${col.value}</td></c:forEach>
<td><form method="post" action="${pageContext.request.contextPath}/crud/${resource}"><input type="hidden" name="_method" value="DELETE"><input type="hidden" name="id" value="${row.id}"><button class="danger" type="submit">Eliminar</button></form></td></tr></c:forEach></tbody></table></div>
</main></body></html>
