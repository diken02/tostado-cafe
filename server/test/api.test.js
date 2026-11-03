const test = require("node:test");
const assert = require("node:assert/strict");
const request = require("supertest");
const { app, pool } = require("../server");

test.after(async () => {
  await pool.end();
});

test("GET / devuelve el estado básico de la API", async () => {
  const response = await request(app).get("/");
  assert.equal(response.status, 200);
  assert.equal(response.body.estado, "Servidor activo");
});

test("Pedidos protegidos requieren autenticación", async () => {
  const response = await request(app).get("/api/pedidos");
  assert.equal(response.status, 401);
});

test("Pagos protegidos requieren autenticación", async () => {
  const response = await request(app)
    .post("/api/pagos")
    .send({ ordenId: 1, metodo: "efectivo", monto: 10 });
  assert.equal(response.status, 401);
});
