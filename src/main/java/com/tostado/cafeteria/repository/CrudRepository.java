package com.tostado.cafeteria.repository;

import com.tostado.cafeteria.config.DatabaseConfig;
import java.sql.*;
import java.util.*;

public class CrudRepository {
    private static final Map<String,String> TABLES = Map.ofEntries(
            Map.entry("clientes","cafeteria_cliente"),
            Map.entry("productos","cafeteria_producto"),
            Map.entry("insumos","cafeteria_insumo"),
            Map.entry("proveedores","cafeteria_proveedor"),
            Map.entry("empleados","cafeteria_empleado"),
            Map.entry("mesas","cafeteria_mesa"),
            Map.entry("pedidos","cafeteria_orden"),
            Map.entry("pagos","cafeteria_pago"),
            Map.entry("recetas","cafeteria_recetaitem"),
            Map.entry("compras","cafeteria_comprainsumo"));

    public List<Map<String,Object>> list(String resource) throws SQLException {
        String table = TABLES.get(resource);
        if (table == null) throw new SQLException("Unknown resource: " + resource);
        String sql = "SELECT * FROM " + table + " ORDER BY id DESC LIMIT 500";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rows(rs);
        }
    }

    public void save(String resource, Map<String,String> values, Long id) throws SQLException {
        String table = TABLES.get(resource);
        if (table == null) throw new SQLException("Unknown resource");
        List<String> columns;
        List<String> allowed;
        switch (resource) {
            case "clientes" -> allowed = List.of("nombre","telefono","email","puntos","total_compras");
            case "productos" -> allowed = List.of("nombre","descripcion","precio","categoria","disponible","imagen");
            case "insumos" -> allowed = List.of("nombre","cantidad","unidad","nivel_minimo","costo","activo");
            case "proveedores" -> allowed = List.of("nombre","contacto","telefono","email","activo");
            case "empleados" -> allowed = List.of("nombre","rol","usuario","contrasena_hash","turno","activo");
            case "mesas" -> allowed = List.of("numero","capacidad","estado","posicion_x","posicion_y");
            case "pedidos" -> allowed = List.of("tipo","estado","nombre_cliente","descuento_global","subtotal","total","numero_espera","notas","cliente_id","empleado_id","mesa_id");
            case "pagos" -> allowed = List.of("metodo","monto","cambio","orden_id");
            default -> allowed = List.of();
        }
        columns = allowed.stream().filter(values::containsKey).toList();
        if (columns.isEmpty()) throw new SQLException("No supported fields supplied");
        StringBuilder sql = new StringBuilder();
        try (Connection c = DatabaseConfig.getConnection()) {
            if (id == null) {
                sql.append("INSERT INTO ").append(table).append(" (")
                        .append(String.join(",",columns)).append(") VALUES (")
                        .append(String.join(",",Collections.nCopies(columns.size(),"?"))).append(")");
                try (PreparedStatement ps=c.prepareStatement(sql.toString())) {
                    bind(ps,columns,values); ps.executeUpdate();
                }
            } else {
                sql.append("UPDATE ").append(table).append(" SET ")
                        .append(columns.stream().map(k->k+"=?").reduce((a,b)->a+","+b).orElseThrow()).append(" WHERE id=?");
                try (PreparedStatement ps=c.prepareStatement(sql.toString())) {
                    bind(ps,columns,values); ps.setLong(columns.size()+1,id); ps.executeUpdate();
                }
            }
        }
    }

    private void bind(PreparedStatement ps,List<String> columns,Map<String,String> values) throws SQLException {
        for (int i=0;i<columns.size();i++) ps.setString(i+1,values.get(columns.get(i)));
    }

    public void delete(String resource, long id) throws SQLException {
        String table = TABLES.get(resource);
        if (table == null) throw new SQLException("Unknown resource");
        String sql = "DELETE FROM " + table + " WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public Map<String,Object> dashboard() throws SQLException {
        Map<String,Object> result = new LinkedHashMap<>();
        try (Connection c = DatabaseConfig.getConnection()) {
            result.put("ventasHoy", scalar(c, "SELECT COALESCE(SUM(total),0) FROM cafeteria_orden WHERE fecha>=CURDATE() AND estado IN ('entregado','listo')"));
            result.put("productos", scalar(c, "SELECT COUNT(*) FROM cafeteria_producto WHERE disponible=1"));
            result.put("clientes", scalar(c, "SELECT COUNT(*) FROM cafeteria_cliente"));
            result.put("mesasOcupadas", scalar(c, "SELECT COUNT(*) FROM cafeteria_mesa WHERE estado='ocupada'"));
            result.put("alertasInventario", scalar(c, "SELECT COUNT(*) FROM cafeteria_insumo WHERE activo=1 AND cantidad<=nivel_minimo"));
        }
        return result;
    }

    private Object scalar(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getObject(1) : 0;
        }
    }

    private List<Map<String,Object>> rows(ResultSet rs) throws SQLException {
        List<Map<String,Object>> result = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        while (rs.next()) {
            Map<String,Object> row = new LinkedHashMap<>();
            for (int i=1;i<=md.getColumnCount();i++) row.put(md.getColumnName(i), rs.getObject(i));
            result.add(row);
        }
        return result;
    }
}
