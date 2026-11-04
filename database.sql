CREATE DATABASE IF NOT EXISTS `tostado_cafe`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `tostado_cafe`;

CREATE TABLE IF NOT EXISTS cafeteria_cliente (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  telefono VARCHAR(30) NULL,
  email VARCHAR(254) NULL,
  puntos INT UNSIGNED NOT NULL DEFAULT 0,
  total_compras DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  ultima_visita DATETIME(6) NULL,
  creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uq_cliente_telefono (telefono),
  UNIQUE KEY uq_cliente_email (email),
  KEY idx_cliente_nombre (nombre),
  CONSTRAINT chk_cliente_puntos CHECK (puntos >= 0),
  CONSTRAINT chk_cliente_total CHECK (total_compras >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_proveedor (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  contacto VARCHAR(120) NOT NULL DEFAULT '',
  telefono VARCHAR(30) NOT NULL DEFAULT '',
  email VARCHAR(254) NOT NULL DEFAULT '',
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  KEY idx_proveedor_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_insumo (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  cantidad DECIMAL(12,3) NOT NULL DEFAULT 0.000,
  unidad VARCHAR(30) NOT NULL,
  nivel_minimo DECIMAL(12,3) NOT NULL DEFAULT 0.000,
  costo DECIMAL(12,4) NOT NULL DEFAULT 0.0000,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uq_insumo_nombre_unidad (nombre, unidad),
  KEY idx_insumo_nombre (nombre),
  CONSTRAINT chk_insumo_cantidad CHECK (cantidad >= 0),
  CONSTRAINT chk_insumo_minimo CHECK (nivel_minimo >= 0),
  CONSTRAINT chk_insumo_costo CHECK (costo >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_producto (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  descripcion TEXT NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  categoria VARCHAR(80) NOT NULL,
  disponible TINYINT(1) NOT NULL DEFAULT 1,
  imagen VARCHAR(2048) NOT NULL DEFAULT '',
  creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  actualizado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_producto_categoria (categoria),
  KEY idx_producto_disponible (disponible),
  CONSTRAINT chk_producto_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_mesa (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  numero INT UNSIGNED NOT NULL,
  capacidad INT UNSIGNED NOT NULL DEFAULT 2,
  estado VARCHAR(15) NOT NULL DEFAULT 'disponible',
  posicion_x INT UNSIGNED NOT NULL DEFAULT 0,
  posicion_y INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uq_mesa_numero (numero),
  CONSTRAINT chk_mesa_numero CHECK (numero > 0),
  CONSTRAINT chk_mesa_capacidad CHECK (capacidad > 0),
  CONSTRAINT chk_mesa_estado CHECK (
    estado IN ('disponible', 'ocupada', 'reservada')
  )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_empleado (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  rol VARCHAR(20) NOT NULL,
  usuario VARCHAR(150) NOT NULL,
  contrasena_hash VARCHAR(255) NULL,
  turno VARCHAR(20) NOT NULL DEFAULT 'manana',
  activo TINYINT(1) NOT NULL DEFAULT 1,
  creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uq_empleado_usuario (usuario),
  CONSTRAINT chk_empleado_rol CHECK (
    rol IN ('administrador', 'cajero', 'barista', 'mesero')
  ),
  CONSTRAINT chk_empleado_turno CHECK (
    turno IN ('manana', 'tarde', 'noche')
  )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_orden (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tipo VARCHAR(15) NOT NULL,
  estado VARCHAR(15) NOT NULL DEFAULT 'pendiente',
  nombre_cliente VARCHAR(120) NOT NULL DEFAULT '',
  descuento_global DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  numero_espera INT UNSIGNED NULL,
  notas TEXT NOT NULL,
  cliente_id BIGINT UNSIGNED NULL,
  empleado_id BIGINT UNSIGNED NULL,
  mesa_id BIGINT UNSIGNED NULL,
  PRIMARY KEY (id),
  KEY idx_orden_fecha (fecha),
  KEY idx_orden_estado_fecha (estado, fecha),
  KEY idx_orden_cliente (cliente_id),
  KEY idx_orden_empleado (empleado_id),
  KEY idx_orden_mesa (mesa_id),
  CONSTRAINT chk_orden_tipo CHECK (
    tipo IN ('local', 'llevar', 'delivery')
  ),
  CONSTRAINT chk_orden_estado CHECK (
    estado IN ('pendiente', 'preparando', 'listo', 'entregado', 'cancelado')
  ),
  CONSTRAINT chk_orden_totales CHECK (
    descuento_global >= 0 AND subtotal >= 0 AND total >= 0
  ),
  CONSTRAINT fk_orden_cliente FOREIGN KEY (cliente_id)
    REFERENCES cafeteria_cliente(id) ON DELETE SET NULL,
  CONSTRAINT fk_orden_empleado FOREIGN KEY (empleado_id)
    REFERENCES cafeteria_empleado(id) ON DELETE SET NULL,
  CONSTRAINT fk_orden_mesa FOREIGN KEY (mesa_id)
    REFERENCES cafeteria_mesa(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_itemorden (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  cantidad INT UNSIGNED NOT NULL DEFAULT 1,
  precio_unitario DECIMAL(10,2) NOT NULL,
  descuento DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  notas VARCHAR(250) NOT NULL DEFAULT '',
  consumos_registrados TINYINT(1) NOT NULL DEFAULT 0,
  orden_id BIGINT UNSIGNED NOT NULL,
  producto_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  KEY idx_itemorden_orden (orden_id),
  KEY idx_itemorden_producto (producto_id),
  CONSTRAINT chk_itemorden_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_itemorden_precios CHECK (
    precio_unitario >= 0 AND descuento >= 0
  ),
  CONSTRAINT fk_itemorden_orden FOREIGN KEY (orden_id)
    REFERENCES cafeteria_orden(id) ON DELETE CASCADE,
  CONSTRAINT fk_itemorden_producto FOREIGN KEY (producto_id)
    REFERENCES cafeteria_producto(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_pago (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  metodo VARCHAR(15) NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  cambio DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  orden_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  KEY idx_pago_orden (orden_id),
  KEY idx_pago_fecha (fecha),
  CONSTRAINT chk_pago_metodo CHECK (
    metodo IN ('efectivo', 'tarjeta', 'billetera', 'qr')
  ),
  CONSTRAINT chk_pago_montos CHECK (monto >= 0 AND cambio >= 0),
  CONSTRAINT fk_pago_orden FOREIGN KEY (orden_id)
    REFERENCES cafeteria_orden(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_recetaitem (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  cantidad DECIMAL(12,3) NOT NULL,
  insumo_id BIGINT UNSIGNED NOT NULL,
  producto_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_receta_producto_insumo (producto_id, insumo_id),
  KEY idx_receta_insumo (insumo_id),
  CONSTRAINT chk_receta_cantidad CHECK (cantidad > 0),
  CONSTRAINT fk_receta_insumo FOREIGN KEY (insumo_id)
    REFERENCES cafeteria_insumo(id) ON DELETE RESTRICT,
  CONSTRAINT fk_receta_producto FOREIGN KEY (producto_id)
    REFERENCES cafeteria_producto(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_comprainsumo (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  cantidad DECIMAL(12,3) NOT NULL,
  costo_unitario DECIMAL(12,4) NOT NULL,
  fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  insumo_id BIGINT UNSIGNED NOT NULL,
  proveedor_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  KEY idx_compra_insumo (insumo_id),
  KEY idx_compra_proveedor (proveedor_id),
  KEY idx_compra_fecha (fecha),
  CONSTRAINT chk_compra_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_compra_costo CHECK (costo_unitario >= 0),
  CONSTRAINT fk_compra_insumo FOREIGN KEY (insumo_id)
    REFERENCES cafeteria_insumo(id) ON DELETE RESTRICT,
  CONSTRAINT fk_compra_proveedor FOREIGN KEY (proveedor_id)
    REFERENCES cafeteria_proveedor(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_consumoinsumo (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  cantidad DECIMAL(12,3) NOT NULL,
  insumo_id BIGINT UNSIGNED NOT NULL,
  item_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_consumo_item_insumo (item_id, insumo_id),
  KEY idx_consumo_insumo (insumo_id),
  CONSTRAINT chk_consumo_cantidad CHECK (cantidad > 0),
  CONSTRAINT fk_consumo_insumo FOREIGN KEY (insumo_id)
    REFERENCES cafeteria_insumo(id) ON DELETE RESTRICT,
  CONSTRAINT fk_consumo_item FOREIGN KEY (item_id)
    REFERENCES cafeteria_itemorden(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cafeteria_turno (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  inicio DATETIME(6) NOT NULL,
  fin DATETIME(6) NULL,
  empleado_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  KEY idx_turno_empleado_inicio (empleado_id, inicio),
  CONSTRAINT chk_turno_fechas CHECK (fin IS NULL OR fin >= inicio),
  CONSTRAINT fk_turno_empleado FOREIGN KEY (empleado_id)
    REFERENCES cafeteria_empleado(id) ON DELETE CASCADE
) ENGINE=InnoDB;

ALTER TABLE cafeteria_empleado DROP CHECK chk_empleado_turno;
ALTER TABLE cafeteria_empleado
  MODIFY turno VARCHAR(20) NOT NULL DEFAULT 'manana';
ALTER TABLE cafeteria_empleado
  ADD CONSTRAINT chk_empleado_turno CHECK (
    turno IN ('manana', 'tarde', 'noche')
  );

START TRANSACTION;

INSERT INTO cafeteria_cliente
  (id, nombre, telefono, email, puntos, total_compras)
VALUES
  (1, 'Maria Gonzalez', '3001234567', 'maria@email.com', 150, 250.50),
  (2, 'Carlos Rodriguez', '3009876543', 'carlos@email.com', 85, 175.00),
  (3, 'Ana Martinez', '3005551234', 'ana@email.com', 220, 450.75)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  telefono = VALUES(telefono),
  email = VALUES(email),
  puntos = VALUES(puntos),
  total_compras = VALUES(total_compras);

INSERT INTO cafeteria_empleado
  (id, nombre, rol, usuario, contrasena_hash, turno, activo)
VALUES
  (1, 'Ana Torres', 'administrador', 'ana.torres', NULL, 'manana', 1),
  (2, 'Luis Mendoza', 'barista', 'luis.mendoza', NULL, 'manana', 1),
  (3, 'Carla Rios', 'mesero', 'carla.rios', NULL, 'tarde', 1),
  (4, 'Jorge Salas', 'cajero', 'jorge.salas', NULL, 'tarde', 1),
  (5, 'Marta Lugo', 'barista', 'marta.lugo', NULL, 'noche', 0),
  (6, 'Administrador Demo', 'administrador', 'admin',
   '$2a$12$kuJOPOiCxQDDz4BnznxZre6awLI.YZ6NjG76L9f/MoXPf3KOn.fEO',
   'manana', 1)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  rol = VALUES(rol),
  usuario = VALUES(usuario),
  contrasena_hash = VALUES(contrasena_hash),
  turno = VALUES(turno),
  activo = VALUES(activo);

INSERT INTO cafeteria_proveedor
  (id, nombre, contacto, telefono, email, activo)
VALUES
  (1, 'Cafe Premium S.A.', 'Roberto Gomez', '6012345678',
   'ventas@cafepremium.com', 1),
  (2, 'Lacteos del Valle', 'Sandra Lopez', '6019876543',
   'pedidos@lacteosval.com', 1),
  (3, 'Panaderia Artesanal', 'Miguel Angel', '6015551234',
   'info@panartesanal.com', 1)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  contacto = VALUES(contacto),
  telefono = VALUES(telefono),
  email = VALUES(email),
  activo = VALUES(activo);

INSERT INTO cafeteria_insumo
  (id, nombre, cantidad, unidad, nivel_minimo, costo)
VALUES
  (1, 'Cafe en grano', 5000.000, 'g', 1000.000, 0.0150),
  (2, 'Leche', 20.000, 'L', 5.000, 1.2000),
  (3, 'Azucar', 3000.000, 'g', 500.000, 0.0020),
  (4, 'Vasos de papel 8 oz', 500.000, 'unidad', 100.000, 0.1500),
  (5, 'Vasos de papel 12 oz', 400.000, 'unidad', 100.000, 0.2000),
  (6, 'Chocolate en polvo', 1000.000, 'g', 200.000, 0.0100),
  (7, 'Te verde', 200.000, 'bolsitas', 50.000, 0.3000),
  (8, 'Croissants', 50.000, 'unidad', 10.000, 0.8000),
  (9, 'Pan para sandwich', 30.000, 'unidad', 10.000, 1.5000),
  (10, 'Jamon', 500.000, 'g', 100.000, 0.0120)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  unidad = VALUES(unidad),
  nivel_minimo = VALUES(nivel_minimo),
  costo = VALUES(costo);

INSERT INTO cafeteria_producto
  (id, nombre, descripcion, precio, categoria, disponible)
VALUES
  (1, 'Cafe Americano', 'Cafe negro tradicional', 2.50, 'Bebidas Calientes', 1),
  (2, 'Cafe Latte', 'Espresso con leche vaporizada', 3.50, 'Bebidas Calientes', 1),
  (3, 'Cappuccino', 'Espresso con leche y espuma', 3.75, 'Bebidas Calientes', 1),
  (4, 'Te Verde', 'Infusion de te verde', 2.00, 'Bebidas Calientes', 1),
  (5, 'Frappe de Chocolate', 'Bebida fria con chocolate', 4.50, 'Bebidas Frias', 1),
  (6, 'Jugo Natural', 'Jugo de fruta natural', 3.00, 'Bebidas Frias', 1),
  (7, 'Croissant', 'Croissant de mantequilla', 2.50, 'Reposteria', 1),
  (8, 'Muffin de Arandanos', 'Muffin casero con arandanos', 2.75, 'Reposteria', 1),
  (9, 'Sandwich de Jamon y Queso', 'Sandwich caliente', 4.00, 'Sandwiches', 1),
  (10, 'Tostada con Aguacate', 'Pan tostado con aguacate', 3.50, 'Sandwiches', 1)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  descripcion = VALUES(descripcion),
  precio = VALUES(precio),
  categoria = VALUES(categoria),
  disponible = VALUES(disponible);

INSERT INTO cafeteria_recetaitem
  (producto_id, insumo_id, cantidad)
VALUES
  (1, 1, 18.000), (1, 4, 1.000),
  (2, 1, 18.000), (2, 2, 0.250), (2, 5, 1.000),
  (3, 1, 18.000), (3, 2, 0.200), (3, 5, 1.000),
  (4, 7, 1.000), (4, 4, 1.000),
  (5, 2, 0.300), (5, 6, 30.000), (5, 5, 1.000),
  (7, 8, 1.000),
  (9, 9, 1.000), (9, 10, 50.000)
ON DUPLICATE KEY UPDATE
  cantidad = VALUES(cantidad);

INSERT INTO cafeteria_mesa
  (id, numero, capacidad, estado, posicion_x, posicion_y)
VALUES
  (1, 1, 2, 'disponible', 0, 0),
  (2, 2, 2, 'disponible', 1, 0),
  (3, 3, 4, 'disponible', 2, 0),
  (4, 4, 4, 'disponible', 0, 1),
  (5, 5, 6, 'disponible', 1, 1),
  (6, 6, 6, 'disponible', 2, 1),
  (7, 7, 2, 'disponible', 0, 2),
  (8, 8, 2, 'disponible', 1, 2)
ON DUPLICATE KEY UPDATE
  numero = VALUES(numero),
  capacidad = VALUES(capacidad),
  posicion_x = VALUES(posicion_x),
  posicion_y = VALUES(posicion_y);

COMMIT;

DROP VIEW IF EXISTS vista_inventario_alerta;
CREATE VIEW vista_inventario_alerta AS
SELECT
  id,
  nombre,
  cantidad,
  unidad,
  nivel_minimo,
  CASE
    WHEN cantidad <= 0 THEN 'agotado'
    WHEN cantidad <= nivel_minimo THEN 'bajo'
    ELSE 'correcto'
  END AS estado
FROM cafeteria_insumo
WHERE activo = 1;

DROP VIEW IF EXISTS vista_ventas_hoy;
CREATE VIEW vista_ventas_hoy AS
SELECT
  COUNT(*) AS pedidos,
  COALESCE(SUM(total), 0) AS total_ventas,
  COALESCE(AVG(total), 0) AS ticket_promedio
FROM cafeteria_orden
WHERE estado IN ('entregado', 'listo')
  AND fecha >= CURDATE()
  AND fecha < DATE_ADD(CURDATE(), INTERVAL 1 DAY);
