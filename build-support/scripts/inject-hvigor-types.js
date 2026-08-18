/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */
'use strict';

const fs = require('fs');
const path = require('path');

const MARK = '/* cangjie-hvigor-extra-exports */';
const SNIPPET_FILE = path.join(__dirname, 'hvigor-extra-exports.txt');

function findTarget() {
  let dir = __dirname;
  for (let i = 0; i < 10; i++) {
    const candidate = path.join(dir, 'node_modules', '@ohos', 'hvigor', 'index.d.ts');
    if (fs.existsSync(candidate)) return candidate;
    const parent = path.dirname(dir);
    if (parent === dir) break;
    dir = parent;
  }
  return null;
}

function main() {
  const target = findTarget();
  if (!target) {
    return;
  }

  if (!fs.existsSync(SNIPPET_FILE)) {
    process.exit(1);
  }

  const snippet = fs.readFileSync(SNIPPET_FILE, 'utf8').trimEnd();
  const content = fs.readFileSync(target, 'utf8');

  const exportLines = snippet
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l.startsWith('export '));

  const missing = exportLines.filter((l) => !content.includes(l));

  if (missing.length === 0) {
    return;
  }

  const prefix = content.endsWith('\n') ? content : `${content}\n`;
  const injected = `${prefix}\n${MARK}\n${snippet}\n`;

  fs.writeFileSync(target, injected);
}

main();
