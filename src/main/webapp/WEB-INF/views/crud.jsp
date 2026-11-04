<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width">
  <title>${resource == 'empleados' ? 'Gestión de Cuentas' : resource}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css">
</head>
<body>
<%@ include file="_nav.jspf" %>
<main class="${resource == 'empleados' ? 'accounts-main' : ''}">
<c:if test="${param.success != null}"><div class="flash">${param.success}</div></c:if>

<c:choose>
  <c:when test="${resource == 'empleados'}">
    <c:set var="adminCount" value="${0}"/>
    <c:set var="cajeroCount" value="${0}"/>
    <c:set var="baristaCount" value="${0}"/>
    <c:forEach items="${rows}" var="employee">
      <c:choose>
        <c:when test="${employee.rol == 'administrador'}"><c:set var="adminCount" value="${adminCount + 1}"/></c:when>
        <c:when test="${employee.rol == 'cajero'}"><c:set var="cajeroCount" value="${cajeroCount + 1}"/></c:when>
        <c:when test="${employee.rol == 'barista'}"><c:set var="baristaCount" value="${baristaCount + 1}"/></c:when>
      </c:choose>
    </c:forEach>

    <div class="accounts-heading">
      <div><h1>Gestión de Cuentas</h1><p>${empty rows ? 0 : rows.size()} cuenta(s) registrada(s)</p></div>
      <a class="new-account-button" href="#nueva-cuenta">＋ Nueva cuenta</a>
    </div>

    <section class="account-grid">
      <c:forEach items="${rows}" var="employee">
        <article class="account-card">
          <div class="account-card-top">
            <span class="account-icon ${employee.rol}">
              <c:choose>
                <c:when test="${employee.rol == 'administrador'}">◇</c:when>
                <c:when test="${employee.rol == 'cajero'}">▰</c:when>
                <c:otherwise>♨</c:otherwise>
              </c:choose>
            </span>
            <div class="account-info">
              <div class="account-name-row">
                <strong>${employee.nombre}</strong>
                <span class="role-badge ${employee.rol}">
                  <c:out value="${employee.rol == 'administrador' ? 'Administrador' : employee.rol == 'cajero' ? 'Cajero' : employee.rol == 'barista' ? 'Barista' : employee.rol}"/>
                </span>
              </div>
              <p>${employee.usuario}</p>
              <span class="account-email">${employee.usuario}@cafeteria.com</span>
            </div>
          </div>
          <div class="account-card-actions">
            <span class="account-active">✓ Activo</span>
            <div>
            <a class="icon-action" href="${pageContext.request.contextPath}/cuentas?edit=${employee.id}#nueva-cuenta" title="Editar">✎</a>
              <form method="post" action="${pageContext.request.contextPath}/cuentas" onsubmit="return confirm('¿Eliminar esta cuenta?')">
                <input type="hidden" name="_method" value="DELETE">
                <input type="hidden" name="id" value="${employee.id}">
                <button class="icon-action delete-icon" type="submit" title="Eliminar">♲</button>
              </form>
            </div>
          </div>
        </article>
      </c:forEach>
    </section>

    <c:if test="${empty rows}"><div class="account-empty">No hay cuentas registradas.</div></c:if>

    <section class="role-summary">
      <h2>Resumen por rol</h2>
      <div class="role-summary-list">
        <span class="role-summary-badge admin-summary">◇ Administrador <strong>${adminCount}</strong></span>
        <span class="role-summary-badge cashier-summary">▰ Cajero <strong>${cajeroCount}</strong></span>
        <span class="role-summary-badge barista-summary">♨ Barista <strong>${baristaCount}</strong></span>
      </div>
    </section>

    <section class="management-card employee-create" id="nueva-cuenta">
      <h2><c:choose><c:when test="${not empty editRow}">Editar cuenta</c:when><c:otherwise>Crear nueva cuenta</c:otherwise></c:choose></h2>
      <form class="crud-form" method="post" action="${pageContext.request.contextPath}/cuentas">
        <c:if test="${not empty editRow}"><input type="hidden" name="id" value="${editRow.id}"></c:if>
        <div class="field"><label>Nombre</label><input name="nombre" value="${editRow.nombre}" required></div>
        <div class="field"><label>Rol</label><select name="rol"><option value="administrador" ${editRow.rol == 'administrador' ? 'selected' : ''}>administrador</option><option value="cajero" ${editRow.rol == 'cajero' ? 'selected' : ''}>cajero</option><option value="barista" ${editRow.rol == 'barista' ? 'selected' : ''}>barista</option><option value="mesero" ${editRow.rol == 'mesero' ? 'selected' : ''}>mesero</option></select></div>
        <div class="field"><label>Usuario</label><input name="usuario" value="${editRow.usuario}" required></div>
        <div class="field"><label>Contraseña</label><input name="contrasena_hash" type="password" <c:if test="${empty editRow}">required</c:if> placeholder="${not empty editRow ? 'Dejar vacío para conservar' : ''}"></div>
        <div class="field"><label>Turno</label><select name="turno"><option value="manana" ${editRow.turno == 'manana' ? 'selected' : ''}>manana</option><option value="tarde" ${editRow.turno == 'tarde' ? 'selected' : ''}>tarde</option><option value="noche" ${editRow.turno == 'noche' ? 'selected' : ''}>noche</option></select></div>
        <div class="field"><label>Activo</label><select name="activo"><option value="1" ${editRow.activo == true ? 'selected' : ''}>Sí</option><option value="0" ${editRow.activo == false ? 'selected' : ''}>No</option></select></div>
        <div class="form-actions"><button type="submit">Guardar cuenta</button><a class="button secondary" href="${pageContext.request.contextPath}/cuentas">Cerrar</a></div>
      </form>
    </section>
  </c:when>

  <c:otherwise>
    <div class="page-heading"><div><h1>${resource}</h1><p>Gestiona los registros de ${resource}.</p></div></div>
    <section class="management-card">
      <h2>Crear o actualizar registro</h2>
      <form class="crud-form" method="post" action="${pageContext.request.contextPath}/crud/${resource}">
        <div class="field"><label for="recordId">ID para actualizar</label><input id="recordId" name="id" type="number" min="1" placeholder="Vacío para crear"></div>
        <c:choose>
          <c:when test="${resource == 'clientes'}">
            <div class="field"><label>Nombre</label><input name="nombre" required></div><div class="field"><label>Teléfono</label><input name="telefono"></div><div class="field"><label>Email</label><input name="email" type="email"></div><div class="field"><label>Puntos</label><input name="puntos" type="number"></div><div class="field"><label>Total compras</label><input name="total_compras" type="number" step="0.01"></div>
          </c:when>
          <c:when test="${resource == 'productos'}">
            <div class="field"><label>Nombre</label><input name="nombre" required></div><div class="field"><label>Descripción</label><input name="descripcion"></div><div class="field"><label>Precio</label><input name="precio" type="number" step="0.01" required></div><div class="field"><label>Categoría</label><input name="categoria"></div><div class="field"><label>Disponible</label><select name="disponible"><option value="1">Sí</option><option value="0">No</option></select></div><div class="field"><label>Imagen URL</label><input name="imagen" type="url"></div>
          </c:when>
          <c:when test="${resource == 'insumos'}">
            <div class="field"><label>Nombre</label><input name="nombre" required></div><div class="field"><label>Cantidad</label><input name="cantidad" type="number" step="0.01"></div><div class="field"><label>Unidad</label><input name="unidad"></div><div class="field"><label>Nivel mínimo</label><input name="nivel_minimo" type="number" step="0.01"></div><div class="field"><label>Costo</label><input name="costo" type="number" step="0.01"></div><div class="field"><label>Activo</label><select name="activo"><option value="1">Sí</option><option value="0">No</option></select></div>
          </c:when>
          <c:otherwise><div class="field"><label>Dato principal</label><input name="nombre"></div></c:otherwise>
        </c:choose>
        <div class="form-actions"><button type="submit">Guardar registro</button><button class="secondary" type="reset">Limpiar</button></div>
      </form>
    </section>
    <div class="table-card table-wrap">
      <table><thead><c:if test="${not empty rows}"><tr><c:forEach items="${rows[0]}" var="col"><th>${col.key}</th></c:forEach><th>Acciones</th></tr></c:if></thead>
      <tbody><c:forEach items="${rows}" var="row"><tr><c:forEach items="${row}" var="col"><td>${col.value}</td></c:forEach><td><form method="post" action="${pageContext.request.contextPath}/crud/${resource}" onsubmit="return confirm('¿Eliminar este registro?')"><input type="hidden" name="_method" value="DELETE"><input type="hidden" name="id" value="${row.id}"><button class="danger" type="submit">Eliminar</button></form></td></tr></c:forEach></tbody></table>
      <c:if test="${empty rows}"><div class="empty">No hay registros para mostrar.</div></c:if>
    </div>
  </c:otherwise>
</c:choose>
</main>
</body>
</html>
