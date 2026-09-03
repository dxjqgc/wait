// 把 pca-code.json 转成 region.sql
// 运行: node gen-region-sql.js
// 输出: ../V3__region.sql

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const inputPath = path.join(__dirname, 'pca-code.json');
const outputPath = path.join(__dirname, '..', 'V3__region.sql');

const data = JSON.parse(fs.readFileSync(inputPath, 'utf8'));

const rows = [];
rows.push('-- V3: 全国行政区划数据（来源：modood/Administrative-divisions-of-China）');
rows.push('-- 省/市/区县三级，parent_id 递归关联');
rows.push('USE `wait`;');
rows.push('');
rows.push('CREATE TABLE IF NOT EXISTS `region` (');
rows.push('  `code`      VARCHAR(6)  NOT NULL COMMENT \'行政区划代码（省2位/市4位/区县6位）\',');
rows.push('  `name`      VARCHAR(64) NOT NULL,');
rows.push('  `level`     TINYINT     NOT NULL COMMENT \'1-省 2-市 3-区县\',');
rows.push('  `parent_code` VARCHAR(6) DEFAULT NULL,');
rows.push('  PRIMARY KEY (`code`),');
rows.push('  KEY `idx_parent` (`parent_code`)');
rows.push(') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=\'全国行政区划\';');
rows.push('');
rows.push('-- 清空旧数据（首次执行无影响）');
rows.push('TRUNCATE TABLE `region`;');
rows.push('');

const values = [];

function walk(node, level, parentCode) {
  // 保持原代码长度：省 2 位 / 市 4 位 / 区县 6 位，padStart 保持字段统一为 CHAR(6)
  const code = String(node.code);
  const name = node.name.replace(/'/g, "''");
  values.push(`('${code}', '${name}', ${level}, ${parentCode ? "'" + parentCode + "'" : 'NULL'})`);

  if (node.children) {
    for (const c of node.children) {
      walk(c, level + 1, code);
    }
  }
}

for (const province of data) {
  walk(province, 1, null);
}

// 分批 INSERT，每批 500 条
const batch = 500;
for (let i = 0; i < values.length; i += batch) {
  const slice = values.slice(i, i + batch);
  rows.push('INSERT INTO `region` (`code`, `name`, `level`, `parent_code`) VALUES');
  rows.push(slice.join(',\n') + ';');
}

rows.push('');
rows.push(`-- 共 ${values.length} 条记录`);

fs.writeFileSync(outputPath, rows.join('\n') + '\n', 'utf8');
console.log(`Generated ${outputPath} with ${values.length} rows`);
