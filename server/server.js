const express = require("express");
const cors = require("cors");
const path = require("path");
const dotenv = require("dotenv");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

dotenv.config({ path: path.join(__dirname, ".env") });

const pool = require("./db");
const app = express();
const port = Number(process.env.PORT || 3000);
const allowedOrigins = (process.env.CORS_ORIGINS || "http://localhost:8000,http://127.0.0.1:8000,http://localhost:5500,http://127.0.0.1:5500")
  .split(",")
  .map((origin) => origin.trim())
  .filter(Boolean);
const jwtSecret = process.env.JWT_SECRET || "change-this-development-secret";

app.use(
  cors({
    origin(origin, callback) {
      if (!origin || allowedOrigins.includes(origin)) {
        callback(null, true);
      } else {
        callback(new Error("Origen no permitido por CORS"));
      }
    }
  })
);
app.use(express.json({ limit: "1mb" }));

function publicError(response, status, message, error) {
  return response.status(status).json({
    ok: false,
    error: message,
    motivo: error ? (error.code || error.message) : undefined
  });
}

function authRequired(request, response, next) {
  const header = request.headers.authorization || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : null;

  if (!token) {
    return publicError(response, 401, "Autenticación requerida");
  }

  try {
    request.user = jwt.verify(token, jwtSecret);
    next();
  } catch {
    publicError(response, 401, "Sesión inválida o vencida");
  }
}

function roleRequired(...roles) {
  return (request, response, next) => {
    if (!request.user || !roles.includes(request.user.rol)) {
      return publicError(response, 403, "No tienes permisos para esta operación");
    }
    next();
  };
}

app.get("/", (request, response) => {
  response.sendFile(path.join(__dirname, "..", "index.html"));
});

// Frontend estático. Permite abrir la aplicación en http://localhost:3000/index.html
app.use(express.static(path.join(__dirname, "..")));

app.get("/api/health", async (request, response) => {
  try {
    const [resultado] = await pool.query(
      "SELECT DATABASE() AS base_actual, 1 AS conexion"
    );
    response.json({
      ok: true,
      conexion: resultado[0].conexion === 1,
      base: resultado[0].base_actual
    });
  } catch (error) {
    console.error("Error al conectar con MySQL:", error);
    publicError(response, 500, "No fue posible conectar con MySQL", error);
  }
});

app.post("/api/auth/login", async (request, response) => {
  try {
    const usuario = String(request.body.usuario || "").trim();
    const password = String(request.body.password || "");

    if (!usuario || !password) {
      return publicError(response, 400, "Usuario y contraseña son requeridos");
    }

    const [empleados] = await pool.query(
      `SELECT id, nombre, usuario, rol, activo, contrasena_hash
       FROM cafeteria_empleado
       WHERE usuario = ?
       LIMIT 1`,
      [usuario]
    );

    if (empleados.length === 0 || empleados[0].activo !== 1) {
      return publicError(response, 401, "Usuario o contraseña incorrectos");
    }

    let hash = empleados[0].contrasena_hash;

    if (!hash) {
      if (usuario === "admin" && password === "admin123") {
        hash = await bcrypt.hash(password, 12);
        await pool.query(
          "UPDATE cafeteria_empleado SET contrasena_hash = ? WHERE id = ?",
          [hash, empleados[0].id]
        );
      } else {
        return publicError(response, 401, "Usuario o contraseña incorrectos");
      }
    } else if (!(await bcrypt.compare(password, hash))) {
      return publicError(response, 401, "Usuario o contraseña incorrectos");
    }

    const token = jwt.sign(
      {
        id: empleados[0].id,
        usuario: empleados[0].usuario,
        rol: empleados[0].rol,
        nombre: empleados[0].nombre
      },
      jwtSecret,
      { expiresIn: process.env.JWT_EXPIRES_IN || "8h" }
    );

    response.json({
      ok: true,
      token,
      usuario: {
        id: empleados[0].id,
        nombre: empleados[0].nombre,
        usuario: empleados[0].usuario,
        rol: empleados[0].rol
      }
    });
  } catch (error) {
    console.error("Error de autenticación:", error);
    publicError(response, 500, "No fue posible iniciar sesión", error);
  }
});

app.get("/api/productos", async (request, response) => {
  try {
    const [productos] = await pool.query(`
      SELECT id, nombre, descripcion, precio, categoria, disponible, imagen
      FROM cafeteria_producto
      WHERE disponible = 1
      ORDER BY categoria, nombre
    `);
    response.json(productos);
  } catch (error) {
    publicError(response, 500, "No fue posible obtener los productos", error);
  }
});

app.get("/api/admin/productos", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const [productos] = await pool.query(`
      SELECT id, nombre, descripcion, precio, categoria, disponible, imagen
      FROM cafeteria_producto
      ORDER BY categoria, nombre
    `);
    response.json(productos);
  } catch (error) {
    publicError(response, 500, "No fue posible obtener el menú", error);
  }
});

app.post("/api/productos", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const nombre = String(request.body.nombre || "").trim();
    const descripcion = String(request.body.descripcion || "").trim();
    const precio = Number(request.body.precio);
    const categoria = String(request.body.categoria || "").trim();
    const imagen = String(request.body.imagen || "").trim();
    const disponible = request.body.disponible === false ? 0 : 1;
    if (!nombre || !categoria || !Number.isFinite(precio) || precio < 0) {
      return publicError(response, 400, "Datos de producto inválidos");
    }
    const [result] = await pool.query(
      `INSERT INTO cafeteria_producto
       (nombre, descripcion, precio, categoria, disponible, imagen)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [nombre, descripcion, precio, categoria, disponible, imagen]
    );
    response.status(201).json({ ok: true, productoId: result.insertId });
  } catch (error) {
    publicError(response, 500, "No fue posible crear el producto", error);
  }
});

app.patch("/api/productos/:id", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const nombre = String(request.body.nombre || "").trim();
    const descripcion = String(request.body.descripcion || "").trim();
    const precio = Number(request.body.precio);
    const categoria = String(request.body.categoria || "").trim();
    const imagen = String(request.body.imagen || "").trim();
    const disponible = request.body.disponible ? 1 : 0;
    if (!nombre || !categoria || !Number.isFinite(precio) || precio < 0) {
      return publicError(response, 400, "Datos de producto inválidos");
    }
    const [result] = await pool.query(
      `UPDATE cafeteria_producto
       SET nombre = ?, descripcion = ?, precio = ?, categoria = ?, disponible = ?, imagen = ?
       WHERE id = ?`,
      [nombre, descripcion, precio, categoria, disponible, imagen, Number(request.params.id)]
    );
    if (result.affectedRows !== 1) return publicError(response, 404, "Producto no encontrado");
    response.json({ ok: true });
  } catch (error) {
    publicError(response, 500, "No fue posible actualizar el producto", error);
  }
});

app.delete("/api/productos/:id", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const [result] = await pool.query(
      "UPDATE cafeteria_producto SET disponible = 0 WHERE id = ?",
      [Number(request.params.id)]
    );
    if (result.affectedRows !== 1) return publicError(response, 404, "Producto no encontrado");
    response.json({ ok: true });
  } catch (error) {
    publicError(response, 500, "No fue posible desactivar el producto", error);
  }
});

app.post("/api/pedidos", async (request, response) => {
  const connection = await pool.getConnection();
  try {
    const { tipo, nombreCliente, mesaId, productos } = request.body;
    if (!Array.isArray(productos) || productos.length === 0) {
      connection.release();
      return publicError(response, 400, "Debes enviar al menos un producto");
    }

    const orderType = ["local", "llevar", "delivery"].includes(tipo) ? tipo : "local";
    await connection.beginTransaction();

    let subtotal = 0;
    const validatedItems = [];

    for (const item of productos) {
      const productoId = Number(item.productoId);
      const cantidad = Number(item.cantidad);

      if (!Number.isInteger(productoId) || productoId <= 0 ||
          !Number.isInteger(cantidad) || cantidad <= 0) {
        throw Object.assign(new Error("Producto o cantidad inválidos"), {
          publicStatus: 400
        });
      }

      const [rows] = await connection.query(
        "SELECT id, precio FROM cafeteria_producto WHERE id = ? AND disponible = 1",
        [productoId]
      );

      if (rows.length !== 1) {
        throw Object.assign(new Error("Producto no disponible"), {
          publicStatus: 400
        });
      }

      const unitPrice = Number(rows[0].precio);
      subtotal += unitPrice * cantidad;
      validatedItems.push({ productoId, cantidad, unitPrice });
    }

    subtotal = Number(subtotal.toFixed(2));
    const [turnoRows] = await connection.query(`
      SELECT COALESCE(MAX(numero_espera), 0) AS ultimo_turno
      FROM cafeteria_orden
      WHERE fecha >= CURDATE()
        AND fecha < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
    `);
    const numeroEspera = Number(turnoRows[0].ultimo_turno) + 1;

    const [result] = await connection.query(`
      INSERT INTO cafeteria_orden
        (tipo, estado, nombre_cliente, descuento_global, subtotal, total, mesa_id, numero_espera, notas)
      VALUES (?, 'pendiente', ?, 0.00, ?, ?, ?, ?, '')
    `, [
      orderType,
      String(nombreCliente || "").trim().slice(0, 120),
      subtotal,
      subtotal,
      orderType === "local" && Number(mesaId) > 0 ? Number(mesaId) : null,
      numeroEspera
    ]);

    const ordenId = result.insertId;

    for (const item of validatedItems) {
      await connection.query(`
        INSERT INTO cafeteria_itemorden
          (cantidad, precio_unitario, descuento, orden_id, producto_id, notas)
        VALUES (?, ?, 0.00, ?, ?, '')
      `, [item.cantidad, item.unitPrice, ordenId, item.productoId]);
    }

    await connection.commit();
    response.status(201).json({
      ok: true,
      ordenId,
      numeroEspera,
      tipo: orderType,
      subtotal,
      total: subtotal
    });
  } catch (error) {
    await connection.rollback();
    console.error("Error al crear pedido:", error);
    publicError(
      response,
      error.publicStatus || 500,
      error.publicStatus ? error.message : "No se pudo registrar el pedido",
      error
    );
  } finally {
    connection.release();
  }
});

app.get("/api/pedidos", authRequired, roleRequired("administrador", "cajero", "barista", "mesero"), async (request, response) => {
  try {
    const [pedidos] = await pool.query(`
      SELECT o.id, o.tipo, o.estado, o.nombre_cliente, o.subtotal, o.total,
             o.fecha, o.numero_espera, o.descuento_global, m.numero AS mesa
      FROM cafeteria_orden o
      LEFT JOIN cafeteria_mesa m ON m.id = o.mesa_id
      ORDER BY o.fecha DESC
      LIMIT 500
    `);
    response.json(pedidos);
  } catch (error) {
    publicError(response, 500, "No fue posible obtener los pedidos", error);
  }
});

app.patch("/api/pedidos/:id/estado", authRequired, roleRequired("administrador", "cajero", "barista", "mesero"), async (request, response) => {
  try {
    const estado = String(request.body.estado || "");
    const validStates = ["pendiente", "preparando", "listo", "entregado", "cancelado"];
    if (!validStates.includes(estado)) {
      return publicError(response, 400, "Estado de pedido inválido");
    }
    const [result] = await pool.query(
      "UPDATE cafeteria_orden SET estado = ? WHERE id = ?",
      [estado, Number(request.params.id)]
    );
    if (result.affectedRows !== 1) {
      return publicError(response, 404, "Pedido no encontrado");
    }
    response.json({ ok: true, estado });
  } catch (error) {
    publicError(response, 500, "No fue posible actualizar el pedido", error);
  }
});

app.get("/api/mesas", async (request, response) => {
  try {
    const [mesas] = await pool.query(
      "SELECT id, numero, capacidad, estado, posicion_x, posicion_y FROM cafeteria_mesa ORDER BY numero"
    );
    response.json(mesas);
  } catch (error) {
    publicError(response, 500, "No fue posible obtener las mesas", error);
  }
});

app.patch("/api/mesas/:id", authRequired, roleRequired("administrador", "mesero", "cajero"), async (request, response) => {
  try {
    const estado = String(request.body.estado || "");
    if (!["disponible", "ocupada", "reservada"].includes(estado)) {
      return publicError(response, 400, "Estado de mesa inválido");
    }
    const numero = request.body.numero == null ? null : Number(request.body.numero);
    const capacidad = request.body.capacidad == null ? null : Number(request.body.capacidad);
    if (numero !== null && (!Number.isInteger(numero) || numero <= 0)) {
      return publicError(response, 400, "Número de mesa inválido");
    }
    if (capacidad !== null && (!Number.isInteger(capacidad) || capacidad <= 0)) {
      return publicError(response, 400, "Capacidad inválida");
    }
    if (numero !== null || capacidad !== null) {
      await pool.query(
        "UPDATE cafeteria_mesa SET numero = COALESCE(?, numero), capacidad = COALESCE(?, capacidad), estado = ? WHERE id = ?",
        [numero, capacidad, estado, Number(request.params.id)]
      );
      return response.json({ ok: true, estado });
    }
    const [result] = await pool.query(
      "UPDATE cafeteria_mesa SET estado = ? WHERE id = ?",
      [estado, Number(request.params.id)]
    );
    if (result.affectedRows !== 1) {
      return publicError(response, 404, "Mesa no encontrada");
    }
    response.json({ ok: true, estado });
  } catch (error) {
    publicError(response, 500, "No fue posible actualizar la mesa", error);
  }
});

app.post("/api/mesas", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const numero = Number(request.body.numero);
    const capacidad = Number(request.body.capacidad || 2);
    const estado = String(request.body.estado || "disponible");
    if (!Number.isInteger(numero) || numero <= 0 ||
        !Number.isInteger(capacidad) || capacidad <= 0 ||
        !["disponible", "ocupada", "reservada"].includes(estado)) {
      return publicError(response, 400, "Datos de mesa inválidos");
    }
    const [result] = await pool.query(
      "INSERT INTO cafeteria_mesa (numero, capacidad, estado) VALUES (?, ?, ?)",
      [numero, capacidad, estado]
    );
    response.status(201).json({ ok: true, mesaId: result.insertId });
  } catch (error) {
    publicError(response, error.code === "ER_DUP_ENTRY" ? 409 : 500,
      error.code === "ER_DUP_ENTRY" ? "El número de mesa ya existe" : "No fue posible crear la mesa",
      error);
  }
});

app.delete("/api/mesas/:id", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const [result] = await pool.query(
      "DELETE FROM cafeteria_mesa WHERE id = ?",
      [Number(request.params.id)]
    );
    if (result.affectedRows !== 1) return publicError(response, 404, "Mesa no encontrada");
    response.json({ ok: true });
  } catch (error) {
    publicError(response, 500, "No fue posible eliminar la mesa", error);
  }
});

app.get("/api/clientes", authRequired, roleRequired("administrador", "cajero", "mesero"), async (request, response) => {
  try {
    const search = String(request.query.search || "").trim();
    const [clientes] = search
      ? await pool.query(
          "SELECT * FROM cafeteria_cliente WHERE nombre LIKE ? OR telefono LIKE ? OR email LIKE ? ORDER BY nombre LIMIT 200",
          [`%${search}%`, `%${search}%`, `%${search}%`]
        )
      : await pool.query("SELECT * FROM cafeteria_cliente ORDER BY nombre LIMIT 200");
    response.json(clientes);
  } catch (error) {
    publicError(response, 500, "No fue posible obtener los clientes", error);
  }
});

app.post("/api/clientes", authRequired, roleRequired("administrador", "cajero", "mesero"), async (request, response) => {
  try {
    const nombre = String(request.body.nombre || "").trim();
    const telefono = String(request.body.telefono || "").trim() || null;
    const email = String(request.body.email || "").trim() || null;
    if (!nombre) return publicError(response, 400, "El nombre es requerido");

    const [result] = await pool.query(
      `INSERT INTO cafeteria_cliente (nombre, telefono, email)
       VALUES (?, ?, ?)`,
      [nombre, telefono, email]
    );
    response.status(201).json({ ok: true, clienteId: result.insertId });
  } catch (error) {
    publicError(response, error.code === "ER_DUP_ENTRY" ? 409 : 500,
      error.code === "ER_DUP_ENTRY" ? "El teléfono o correo ya existe" : "No fue posible crear el cliente",
      error);
  }
});

app.patch("/api/clientes/:id", authRequired, roleRequired("administrador", "cajero"), async (request, response) => {
  try {
    const nombre = String(request.body.nombre || "").trim();
    const telefono = String(request.body.telefono || "").trim() || null;
    const email = String(request.body.email || "").trim() || null;
    if (!nombre) return publicError(response, 400, "El nombre es requerido");

    const [result] = await pool.query(
      `UPDATE cafeteria_cliente
       SET nombre = ?, telefono = ?, email = ?
       WHERE id = ?`,
      [nombre, telefono, email, Number(request.params.id)]
    );
    if (result.affectedRows !== 1) {
      return publicError(response, 404, "Cliente no encontrado");
    }
    response.json({ ok: true });
  } catch (error) {
    publicError(response, error.code === "ER_DUP_ENTRY" ? 409 : 500,
      error.code === "ER_DUP_ENTRY" ? "El teléfono o correo ya existe" : "No fue posible actualizar el cliente",
      error);
  }
});

app.delete("/api/clientes/:id", authRequired, roleRequired("administrador", "cajero"), async (request, response) => {
  try {
    const [result] = await pool.query(
      "DELETE FROM cafeteria_cliente WHERE id = ?",
      [Number(request.params.id)]
    );
    if (result.affectedRows !== 1) {
      return publicError(response, 404, "Cliente no encontrado");
    }
    response.json({ ok: true });
  } catch (error) {
    publicError(response, 500, "No fue posible eliminar el cliente", error);
  }
});

app.patch("/api/clientes/:id/puntos", authRequired, roleRequired("administrador", "cajero", "mesero"), async (request, response) => {
  try {
    const puntos = Number(request.body.puntos);
    if (!Number.isInteger(puntos)) return publicError(response, 400, "Cantidad de puntos inválida");
    const [result] = await pool.query(
      "UPDATE cafeteria_cliente SET puntos = GREATEST(puntos + ?, 0) WHERE id = ?",
      [puntos, Number(request.params.id)]
    );
    if (result.affectedRows !== 1) return publicError(response, 404, "Cliente no encontrado");
    response.json({ ok: true });
  } catch (error) {
    publicError(response, 500, "No fue posible actualizar los puntos", error);
  }
});

app.post("/api/clientes/:id/canjear", authRequired, roleRequired("administrador", "cajero", "mesero"), async (request, response) => {
  const connection = await pool.getConnection();
  try {
    const productoId = Number(request.body.productoId);
    if (!Number.isInteger(productoId) || productoId <= 0) {
      connection.release();
      return publicError(response, 400, "Selecciona un producto válido");
    }
    await connection.beginTransaction();
    const [clientes] = await connection.query(
      "SELECT id, puntos FROM cafeteria_cliente WHERE id = ? FOR UPDATE",
      [Number(request.params.id)]
    );
    const [productos] = await connection.query(
      "SELECT id, nombre FROM cafeteria_producto WHERE id = ? AND disponible = 1",
      [productoId]
    );
    if (clientes.length !== 1) throw Object.assign(new Error("Cliente no encontrado"), { publicStatus: 404 });
    if (productos.length !== 1) throw Object.assign(new Error("Producto no disponible"), { publicStatus: 400 });
    if (Number(clientes[0].puntos) < 100) {
      throw Object.assign(new Error("Se requieren al menos 100 puntos"), { publicStatus: 400 });
    }
    await connection.query(
      "UPDATE cafeteria_cliente SET puntos = puntos - 100 WHERE id = ?",
      [Number(request.params.id)]
    );
    await connection.commit();
    response.json({ ok: true, recompensa: productos[0].nombre, puntosUsados: 100 });
  } catch (error) {
    await connection.rollback();
    publicError(response, error.publicStatus || 500,
      error.publicStatus ? error.message : "No fue posible canjear los puntos", error);
  } finally {
    connection.release();
  }
});

app.get("/api/empleados", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const [empleados] = await pool.query(
      `SELECT id, nombre, rol, usuario, turno, activo, creado_en
       FROM cafeteria_empleado ORDER BY nombre`
    );
    response.json(empleados);
  } catch (error) {
    publicError(response, 500, "No fue posible obtener los empleados", error);
  }
});

app.post("/api/empleados", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const nombre = String(request.body.nombre || "").trim();
    const usuario = String(request.body.usuario || "").trim();
    const rol = String(request.body.rol || "");
    const turno = String(request.body.turno || "manana")
      .normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
    const password = String(request.body.password || "");
    if (!nombre || !usuario || !password) {
      return publicError(response, 400, "Nombre, usuario y contraseña son requeridos");
    }
    if (!["administrador", "cajero", "barista", "mesero"].includes(rol)) {
      return publicError(response, 400, "Rol inválido");
    }
    const hash = await bcrypt.hash(password, 12);
    const [result] = await pool.query(
      `INSERT INTO cafeteria_empleado
       (nombre, rol, usuario, contrasena_hash, turno, activo)
       VALUES (?, ?, ?, ?, ?, 1)`,
      [nombre, rol, usuario, hash, turno]
    );
    response.status(201).json({ ok: true, empleadoId: result.insertId });
  } catch (error) {
    publicError(response, error.code === "ER_DUP_ENTRY" ? 409 : 500,
      error.code === "ER_DUP_ENTRY" ? "El usuario ya existe" : "No fue posible crear el empleado",
      error);
  }
});

app.patch("/api/empleados/:id", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const nombre = String(request.body.nombre || "").trim();
    const usuario = String(request.body.usuario || "").trim();
    const rol = String(request.body.rol || "");
    const turno = String(request.body.turno || "")
      .normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
    const activo = request.body.activo === false ? 0 : 1;
    const password = String(request.body.password || "");
    if (!nombre || !usuario || !["administrador", "cajero", "barista", "mesero"].includes(rol)) {
      return publicError(response, 400, "Datos de empleado inválidos");
    }
    const [existing] = await pool.query(
      "SELECT id FROM cafeteria_empleado WHERE id = ? LIMIT 1",
      [Number(request.params.id)]
    );
    if (existing.length !== 1) return publicError(response, 404, "Empleado no encontrado");
    if (password) {
      const hash = await bcrypt.hash(password, 12);
      await pool.query(
        "UPDATE cafeteria_empleado SET nombre=?, usuario=?, rol=?, turno=?, activo=?, contrasena_hash=? WHERE id=?",
        [nombre, usuario, rol, turno, activo, hash, Number(request.params.id)]
      );
    } else {
      await pool.query(
        "UPDATE cafeteria_empleado SET nombre=?, usuario=?, rol=?, turno=?, activo=? WHERE id=?",
        [nombre, usuario, rol, turno, activo, Number(request.params.id)]
      );
    }
    response.json({ ok: true });
  } catch (error) {
    publicError(response, error.code === "ER_DUP_ENTRY" ? 409 : 500,
      error.code === "ER_DUP_ENTRY" ? "El usuario ya existe" : "No fue posible actualizar el empleado", error);
  }
});

app.delete("/api/empleados/:id", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const [result] = await pool.query(
      "DELETE FROM cafeteria_empleado WHERE id = ?",
      [Number(request.params.id)]
    );
    if (result.affectedRows !== 1) return publicError(response, 404, "Empleado no encontrado");
    response.json({ ok: true });
  } catch (error) {
    publicError(response, 500, "No fue posible eliminar el empleado", error);
  }
});

app.get("/api/inventario", authRequired, roleRequired("administrador", "barista", "cajero"), async (request, response) => {
  try {
    const [insumos] = await pool.query(`
      SELECT id, nombre, cantidad, unidad, nivel_minimo, costo, activo,
             CASE
               WHEN cantidad <= 0 THEN 'agotado'
               WHEN cantidad <= nivel_minimo THEN 'bajo'
               ELSE 'correcto'
             END AS estado
      FROM cafeteria_insumo
      WHERE activo = 1
      ORDER BY nombre
    `);
    response.json(insumos);
  } catch (error) {
    publicError(response, 500, "No fue posible obtener el inventario", error);
  }
});

app.patch("/api/inventario/:id", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const cantidad = Number(request.body.cantidad);
    const nivelMinimo = Number(request.body.nivel_minimo);
    if (!Number.isFinite(cantidad) || cantidad < 0 ||
        !Number.isFinite(nivelMinimo) || nivelMinimo < 0) {
      return publicError(response, 400, "Cantidad o nivel mínimo inválido");
    }
    const [result] = await pool.query(
      "UPDATE cafeteria_insumo SET cantidad = ?, nivel_minimo = ? WHERE id = ?",
      [cantidad, nivelMinimo, Number(request.params.id)]
    );
    if (result.affectedRows !== 1) {
      return publicError(response, 404, "Insumo no encontrado");
    }
    response.json({ ok: true });
  } catch (error) {
    publicError(response, 500, "No fue posible actualizar el inventario", error);
  }
});

app.post("/api/inventario", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const nombre = String(request.body.nombre || "").trim();
    const unidad = String(request.body.unidad || "").trim();
    const cantidad = Number(request.body.cantidad || 0);
    const nivelMinimo = Number(request.body.nivel_minimo || 0);
    const costo = Number(request.body.costo || 0);
    if (!nombre || !unidad || cantidad < 0 || nivelMinimo < 0 || costo < 0) {
      return publicError(response, 400, "Datos de inventario inválidos");
    }
    const [result] = await pool.query(
      `INSERT INTO cafeteria_insumo (nombre, cantidad, unidad, nivel_minimo, costo, activo)
       VALUES (?, ?, ?, ?, ?, 1)`,
      [nombre, cantidad, unidad, nivelMinimo, costo]
    );
    response.status(201).json({ ok: true, insumoId: result.insertId });
  } catch (error) {
    publicError(response, 500, "No fue posible crear el insumo", error);
  }
});

app.delete("/api/inventario/:id", authRequired, roleRequired("administrador"), async (request, response) => {
  try {
    const [result] = await pool.query(
      "UPDATE cafeteria_insumo SET activo = 0 WHERE id = ?",
      [Number(request.params.id)]
    );
    if (result.affectedRows !== 1) {
      return publicError(response, 404, "Insumo no encontrado");
    }
    response.json({ ok: true });
  } catch (error) {
    publicError(response, 500, "No fue posible eliminar el insumo", error);
  }
});

app.get("/api/reportes/resumen", authRequired, roleRequired("administrador", "cajero"), async (request, response) => {
  try {
    const [[ventas]] = await pool.query(`
      SELECT COUNT(*) AS pedidos,
             COALESCE(SUM(CASE WHEN fecha >= CURDATE() AND fecha < DATE_ADD(CURDATE(), INTERVAL 1 DAY) THEN total ELSE 0 END), 0) AS ventasHoy,
             COALESCE(SUM(total), 0) AS ventasHistoricas,
             SUM(fecha >= CURDATE() AND fecha < DATE_ADD(CURDATE(), INTERVAL 1 DAY)) AS pedidosHoy
      FROM cafeteria_orden
      WHERE estado <> 'cancelado'
    `);
    const [porDia] = await pool.query(`
      SELECT DATE(fecha) AS dia, COUNT(*) AS pedidos, COALESCE(SUM(total), 0) AS ventas
      FROM cafeteria_orden WHERE estado <> 'cancelado'
      GROUP BY DATE(fecha) ORDER BY dia DESC LIMIT 7
    `);
    const [porCategoria] = await pool.query(`
      SELECT p.categoria, COALESCE(SUM(i.cantidad * i.precio_unitario), 0) AS ventas
      FROM cafeteria_itemorden i
      JOIN cafeteria_producto p ON p.id = i.producto_id
      JOIN cafeteria_orden o ON o.id = i.orden_id
      WHERE o.estado <> 'cancelado'
      GROUP BY p.categoria ORDER BY ventas DESC
    `);
    const [productos] = await pool.query(`
      SELECT p.nombre, SUM(i.cantidad) AS unidades,
             COALESCE(SUM(i.cantidad * i.precio_unitario), 0) AS ingresos
      FROM cafeteria_itemorden i
      JOIN cafeteria_producto p ON p.id = i.producto_id
      JOIN cafeteria_orden o ON o.id = i.orden_id
      WHERE o.estado <> 'cancelado'
      GROUP BY p.id, p.nombre ORDER BY unidades DESC LIMIT 10
    `);
    const [[clientes]] = await pool.query("SELECT COUNT(*) AS cantidad FROM cafeteria_cliente");
    const [[inventario]] = await pool.query(`
      SELECT COUNT(*) AS cantidad FROM cafeteria_insumo
      WHERE activo = 1 AND cantidad <= nivel_minimo
    `);
    const [estados] = await pool.query(`
      SELECT estado, COUNT(*) AS cantidad FROM cafeteria_orden GROUP BY estado
    `);
    response.json({
      ventas: ventas || {},
      porDia,
      porCategoria,
      productos,
      clientes: clientes.cantidad,
      alertasInventario: inventario.cantidad,
      estados
    });
  } catch (error) {
    publicError(response, 500, "No fue posible generar el reporte", error);
  }
});

app.post("/api/pagos", authRequired, roleRequired("administrador", "cajero"), async (request, response) => {
  const connection = await pool.getConnection();
  try {
    const ordenId = Number(request.body.ordenId);
    const metodo = String(request.body.metodo || "").toLower();
    const monto = Number(request.body.monto);
    const cambio = Number(request.body.cambio || 0);

    if (!Number.isInteger(ordenId) || ordenId <= 0 ||
        !["efectivo", "tarjeta", "billetera", "qr"].includes(metodo) ||
        !Number.isFinite(monto) || monto < 0 ||
        !Number.isFinite(cambio) || cambio < 0) {
      connection.release();
      return publicError(response, 400, "Datos de pago inválidos");
    }

    await connection.beginTransaction();
    const [ordenRows] = await connection.query(
      "SELECT id, total, estado FROM cafeteria_orden WHERE id = ?",
      [ordenId]
    );
    if (ordenRows.length !== 1) {
      throw Object.assign(new Error("Pedido no encontrado"), { publicStatus: 404 });
    }
    if (ordenRows[0].estado === "entregado" || ordenRows[0].estado === "cancelado") {
      throw Object.assign(new Error("El pedido ya fue cerrado"), { publicStatus: 409 });
    }
    if (monto < Number(ordenRows[0].total)) {
      throw Object.assign(new Error("El monto pagado es insuficiente"), { publicStatus: 400 });
    }

    await connection.query(
      "INSERT INTO cafeteria_pago (metodo, monto, cambio, orden_id) VALUES (?, ?, ?, ?)",
      [metodo, monto, cambio, ordenId]
    );

    const [items] = await connection.query(
      "SELECT id, producto_id, cantidad FROM cafeteria_itemorden WHERE orden_id = ? AND consumos_registrados = 0",
      [ordenId]
    );

    for (const item of items) {
      const [recetas] = await connection.query(
        "SELECT insumo_id, cantidad FROM cafeteria_recetaitem WHERE producto_id = ?",
        [item.producto_id]
      );

      for (const receta of recetas) {
        const cantidadRequerida = Number(receta.cantidad) * Number(item.cantidad);
        const [stockRows] = await connection.query(
          "SELECT cantidad FROM cafeteria_insumo WHERE id = ? FOR UPDATE",
          [receta.insumo_id]
        );
        if (stockRows.length !== 1 || Number(stockRows[0].cantidad) < cantidadRequerida) {
          throw Object.assign(new Error("Inventario insuficiente para completar el pago"), {
            publicStatus: 409
          });
        }
        await connection.query(
          "UPDATE cafeteria_insumo SET cantidad = cantidad - ? WHERE id = ?",
          [cantidadRequerida, receta.insumo_id]
        );
        await connection.query(
          `INSERT INTO cafeteria_consumoinsumo (cantidad, insumo_id, item_id)
           VALUES (?, ?, ?)
           ON DUPLICATE KEY UPDATE cantidad = VALUES(cantidad)`,
          [cantidadRequerida, receta.insumo_id, item.id]
        );
      }

      await connection.query(
        "UPDATE cafeteria_itemorden SET consumos_registrados = 1 WHERE id = ?",
        [item.id]
      );
    }

    await connection.query(
      "UPDATE cafeteria_orden SET estado = 'entregado' WHERE id = ?",
      [ordenId]
    );
    await connection.commit();
    response.status(201).json({ ok: true, ordenId, metodo, monto, cambio });
  } catch (error) {
    await connection.rollback();
    console.error("Error al registrar pago:", error);
    publicError(response, error.publicStatus || 500,
      error.publicStatus ? error.message : "No fue posible registrar el pago", error);
  } finally {
    connection.release();
  }
});

app.use((error, request, response, next) => {
  if (error.message === "Origen no permitido por CORS") {
    return publicError(response, 403, error.message);
  }
  next(error);
});

if (require.main === module) {
  app.listen(port, () => {
    console.log(`API ejecutándose en http://localhost:${port}`);
  });
}

module.exports = { app, pool };
