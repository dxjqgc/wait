// SQL runner: reads SQL files, splits into statements (handling DELIMITER),
// and executes them sequentially via mysql2.

import fs from 'node:fs';
import mysql from 'mysql2/promise';

const files = process.argv.slice(2);
if (files.length === 0) {
  console.error('usage: node run-sql.mjs <file1.sql> [file2.sql ...]');
  process.exit(2);
}

const pool = mysql.createPool({
  host: '::1',
  port: 3306,
  user: 'root',
  password: 'X3#pK7!mDz@2vRtQ',
  database: 'wait',
  multipleStatements: false,
  charset: 'utf8mb4',
});

// Parse SQL into statements. Handles DELIMITER directives and $$ ... $$ blocks.
function parseStatements(sql) {
  const statements = [];
  let buf = '';
  let delimiter = ';';
  let i = 0;
  const lines = sql.split(/\r?\n/);

  for (const line of lines) {
    const trimmed = line.trim();
    const delimMatch = trimmed.match(/^DELIMITER\s+(\S+)$/i);
    if (delimMatch) {
      // flush any pending buffer (should be empty)
      if (buf.trim()) statements.push(buf.trim());
      buf = '';
      delimiter = delimMatch[1];
      continue;
    }
    buf += line + '\n';
    // If line ends with delimiter, we have a complete statement
    const re = new RegExp(delimiter.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '\\s*$');
    if (re.test(trimmed)) {
      // remove trailing delimiter
      const stmt = buf.replace(new RegExp(delimiter.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '\\s*$'), '').trim();
      if (stmt) statements.push(stmt);
      buf = '';
    }
  }
  if (buf.trim()) statements.push(buf.trim());
  return statements;
}

const skipErrors = [
  /^Duplicate column name/,
  /^Duplicate key name/,
  /^Can't DROP/,
  /^Unknown database/,
];

function isOkToSkip(msg) {
  return skipErrors.some((re) => re.test(msg));
}

let total = 0;
let failed = 0;
for (const file of files) {
  const sql = fs.readFileSync(file, 'utf8');
  const stmts = parseStatements(sql);
  console.log(`\n=== ${file} (${stmts.length} statements) ===`);
  for (let i = 0; i < stmts.length; i++) {
    const stmt = stmts[i];
    const preview = stmt.split(/\r?\n/)[0].slice(0, 80);
    try {
      const [result] = await pool.query(stmt);
      total++;
      const affected = result?.affectedRows ?? 0;
      const kind = result?.constructor?.name ?? '';
      console.log(`  [${i + 1}/${stmts.length}] OK  ${preview}  (${affected} rows${kind === 'OkPacket' ? '' : ' / ' + kind})`);
    } catch (e) {
      const msg = e.message;
      if (isOkToSkip(msg)) {
        total++;
        console.log(`  [${i + 1}/${stmts.length}] SKIP(${msg}) ${preview}`);
      } else {
        failed++;
        console.error(`  [${i + 1}/${stmts.length}] FAIL ${preview}`);
        console.error(`      ${msg}`);
      }
    }
  }
}

await pool.end();
console.log(`\nDone. ${total} executed, ${failed} failed.`);
process.exit(failed > 0 ? 1 : 0);
