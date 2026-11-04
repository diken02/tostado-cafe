<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Bienvenido · Tostado Cafetería</title>
  <style>
    :root{--cafe:#4a2c1d;--crema:#fffaf1;--borde:#eadcbd;--shadow:0 14px 35px rgba(74,44,29,.16)}
    *{box-sizing:border-box}
    body{margin:0;font-family:"Segoe UI",system-ui,sans-serif;color:#3a261b}
    .home-page{min-height:100vh;background:linear-gradient(180deg,#fffaf1 0%,#fffdf8 55%,#fff9eb 100%);display:flex;flex-direction:column;align-items:center;padding:140px 20px 38px}
    main{width:100%;display:flex;flex-direction:column;align-items:center}
    .home-logo{width:116px;height:116px;border-radius:50px;background:linear-gradient(145deg,#bd7023,#f4a235);display:grid;place-items:center;box-shadow:0 10px 28px rgba(168,100,31,.20);font-size:52px;margin-bottom:18px}
    .home-title{text-align:center;margin-bottom:38px}
    .home-title h1{font-family:Georgia,"Times New Roman",serif;font-size:48px;line-height:1;margin:0;color:#7f371e}
    .subtitle{margin-top:12px;font-size:16px;letter-spacing:.30em;color:#d58027}
    .question{margin-top:18px;color:#c47a31;font-size:15px}
    .home-actions{width:min(390px,100%);text-align:center}
    .menu-button{height:68px;width:100%;border-radius:22px;border:0;background:linear-gradient(90deg,#d9781f,#ef5d29);color:#fff;display:flex;align-items:center;justify-content:center;gap:12px;font-size:17px;font-weight:800;text-decoration:none;box-shadow:0 14px 26px rgba(218,112,38,.20)}
    .menu-button span{font-size:25px}
    .menu-caption{margin:14px 0 0;color:#efb12b;font-size:13px}
    .home-divider{width:min(390px,100%);height:1px;background:#eadcbd;margin:38px 0 30px;position:relative}
    .home-divider:after{content:"";position:absolute;left:50%;top:-5px;width:9px;height:9px;border-radius:50%;background:#fff;transform:translateX(-50%) rotate(45deg);border:1px solid #e9b844;box-sizing:border-box}
    .staff-area{width:min(390px,100%);position:relative}
    .staff-button{width:100%;height:48px;border-radius:14px;border:1px solid #ebcfa7;background:rgba(255,255,255,.75);color:#bf7330;font-size:14px;font-weight:700;cursor:pointer;display:flex;align-items:center;justify-content:center;gap:8px}
    .staff-button:hover{background:#fff}
    .staff-area.open-login .staff-button{visibility:hidden}
    .staff-panel{display:none;width:min(390px,100%);position:absolute;top:0;left:0;padding:24px 20px 18px;border-radius:16px;background:#fff;box-shadow:var(--shadow);z-index:12}
    .staff-area.open-login .staff-panel{display:block}
    .staff-panel-head{height:38px;display:flex;align-items:center;justify-content:center;position:relative;margin-bottom:14px;color:var(--cafe);font-weight:800;font-size:15px}
    .staff-panel-head span{position:absolute;left:18px;color:#d97727;font-size:20px}
    .staff-panel-close{position:absolute;right:4px;top:-2px;border:0;background:transparent;color:#e79b32;font-size:24px;cursor:pointer}
    .staff-login-field{height:50px;margin-bottom:12px;border-radius:12px;border:2px solid #f0a333;background:#fff;padding:0 14px;display:flex;align-items:center;gap:10px;color:#efa72d}
    .staff-login-field input{border:0;background:transparent;padding:0;height:100%;outline:none;color:var(--cafe);font-size:14px;width:100%}
    .staff-login-field input::placeholder{color:#efb233}
    .staff-login-field:focus-within{border-color:#d97727}
    .staff-login-submit{width:100%;height:44px;border:0;border-radius:12px;background:linear-gradient(90deg,#93461e,#7f391e);color:#fff;font-size:14px;font-weight:800;cursor:pointer}
    .error{margin-bottom:12px;padding:9px 12px;border-radius:10px;background:#fde8e7;color:#7a271a;font-size:13px;text-align:center}
    .staff-demo{margin:12px 0 0;text-align:center;color:#efa72d;font-size:12px}
    .home-footer{margin-top:auto;padding-top:55px;color:#e2a95f;font-size:12px;text-align:center}
    @media(max-width:600px){.home-page{padding-top:70px}.home-logo{width:92px;height:92px;border-radius:38px;font-size:42px}.home-title h1{font-size:40px}.home-title{margin-bottom:30px}.home-divider{margin-top:28px}}
  </style>
</head>
<body class="home-page">
  <main>
    <div class="home-logo" aria-hidden="true">☕</div>
    <div class="home-title">
      <h1>Tostado</h1>
      <div class="subtitle">CAFETERÍA</div>
      <div class="question">¿Qué deseas tomar?</div>
    </div>
    <div class="home-actions">
      <a class="menu-button" href="${pageContext.request.contextPath}/pedido.html"><span>▣</span> Ver Menú y Ordenar</a>
      <p class="menu-caption">Explora nuestro menú y realiza tu pedido.</p>
    </div>
    <div class="home-divider"></div>
    <div class="staff-area<%= request.getAttribute("error") != null ? " open-login" : "" %>" id="staffArea">
      <button class="staff-button" id="staffButton" type="button"><span>♙</span> Acceso del personal <span>⌄</span></button>
      <form class="staff-panel" id="staffPanel" method="post" action="${pageContext.request.contextPath}/login">
        <div class="staff-panel-head"><span>♙</span>Acceso del personal<button class="staff-panel-close" id="staffPanelClose" type="button" aria-label="Cerrar">×</button></div>
        <% if(request.getAttribute("error") != null){ %><div class="error"><%=request.getAttribute("error")%></div><% } %>
        <label class="staff-login-field"><span>♙</span><input id="staffUser" name="usuario" type="text" placeholder="Usuario" autocomplete="username" required></label>
        <label class="staff-login-field"><span>▣</span><input id="staffPassword" name="contrasena" type="password" placeholder="Contraseña" autocomplete="current-password" required></label>
        <button class="staff-login-submit" type="submit">Ingresar al sistema</button>
        <p class="staff-demo">Demo: <strong>admin / admin123</strong></p>
      </form>
    </div>
  </main>
  <footer class="home-footer">© 2026 Tostado Cafetería - Todos los derechos reservados</footer>
  <script>
    document.getElementById("staffButton").addEventListener("click",function(){document.getElementById("staffArea").classList.add("open-login")});
    document.getElementById("staffPanelClose").addEventListener("click",function(){document.getElementById("staffArea").classList.remove("open-login")});
  </script>
</body>
</html>
