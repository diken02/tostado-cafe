// Inyecta la barra lateral y la barra superior compartidas (solo UI).
(function () {
  window.tostadoApi = "http://localhost:3000";
  window.tostadoToken = sessionStorage.getItem("tostadoToken");
  window.tostadoApiFetch = function (url, options) {
    options = options || {};
    options.headers = Object.assign({"Content-Type":"application/json"}, options.headers || {});
    if (window.tostadoToken) options.headers.Authorization = "Bearer " + window.tostadoToken;
    return fetch(window.tostadoApi + url, options);
  };
  var links = [
    ['index.html', '🏠', 'Inicio'],
    ['admin.html', '📊', 'Administración'],
    ['inventario.html', '📦', 'Inventario'],
    ['menu.html', '📋', 'Menú'],
    ['mesas.html', '🪑', 'Mesas'],
    ['pos.html', '🧾', 'POS / Productos'],
    ['reportes.html', '📈', 'Reportes'],
    ['empleados.html', '👥', 'Empleados'],
    ['clientes.html', '👤', 'Clientes']
  ];
  var current = location.pathname.split('/').pop() || 'index.html';
  var title = document.body.getAttribute('data-title') || '';
  var nav = links.map(function (l) {
    return '<a href="' + l[0] + '"' + (l[0] === current ? ' class="active"' : '') + '><span>' + l[1] + '</span>' + l[2] + '</a>';
  }).join('');
  var sidebar = '<aside class="sidebar" id="sidebar"><div class="brand">☕ Tostado<small>Cafetería</small></div><nav class="nav">' +
    '<div class="sep">Gestión</div>' + nav + '<div class="sep">Cuenta</div><a href="index.html"><span>🚪</span>Cerrar sesión</a></nav></aside>';
  var topbar = '<header class="topbar"><div><strong>' + title +
    '</strong></div><div class="user"><span>Ana Torres · Administradora</span><div class="avatar">AT</div></div></header>';
  var content = document.getElementById('app');
  var main = document.createElement('div');
  main.className = 'main';
  main.innerHTML = topbar;
  content.className = 'content';
  content.parentNode.insertBefore(main, content);
  main.appendChild(content);
  var wrap = document.createElement('div');
  wrap.className = 'layout';
  main.parentNode.insertBefore(wrap, main);
  wrap.insertAdjacentHTML('afterbegin', sidebar);
  wrap.appendChild(main);
  var mobileNav = '<nav class="mobile-admin-nav" aria-label="Navegación móvil">' +
    '<a href="index.html"' + (current === 'index.html' ? ' class="active"' : '') + '><span>⌂</span>Inicio</a>' +
    '<a href="admin.html"' + (current === 'admin.html' ? ' class="active"' : '') + '><span>▦</span>Admin</a>' +
    '<a href="pos.html"' + (current === 'pos.html' ? ' class="active"' : '') + '><span>▣</span>Venta</a>' +
    '<a href="mesas.html"' + (current === 'mesas.html' ? ' class="active"' : '') + '><span>◉</span>Mesas</a>' +
    '<a href="clientes.html"' + (current === 'clientes.html' ? ' class="active"' : '') + '><span>♙</span>Clientes</a>' +
    '<a href="empleados.html"' + (current === 'empleados.html' ? ' class="active"' : '') + '><span>•••</span>Más</a>' +
    '</nav>';
  document.body.insertAdjacentHTML('beforeend', mobileNav);
})();
