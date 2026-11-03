const { mkdirSync } = require("fs");
const path = require("path");
const { spawnSync } = require("child_process");
const dotenv = require("dotenv");

dotenv.config({ path: path.join(__dirname, ".env") });

const backupDirectory = path.join(__dirname, "backups");
mkdirSync(backupDirectory, { recursive: true });

const stamp = new Date().toISOString().replace(/[:.]/g, "-");
const outputFile = path.join(backupDirectory, `tostado-cafe-${stamp}.sql`);
const mysqldump = process.env.MYSQLDUMP_PATH || "mysqldump";

const result = spawnSync(mysqldump, [
  "--host", process.env.DB_HOST || "localhost",
  "--port", process.env.DB_PORT || "3306",
  "--user", process.env.DB_USER || "root",
  "--result-file", outputFile,
  "--single-transaction",
  "--routines",
  "--triggers",
  process.env.DB_NAME || "tostado_cafe"
], {
  env: { ...process.env, MYSQL_PWD: process.env.DB_PASSWORD || "" },
  encoding: "utf8",
  windowsHide: true
});

if (result.status !== 0) {
  console.error(result.stderr || "No fue posible ejecutar mysqldump.");
  process.exit(result.status || 1);
}

console.log(`Copia creada: ${outputFile}`);
