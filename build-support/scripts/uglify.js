/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

const { minify } = require('terser');
const glob = require('glob');
const fs = require('fs');

const jsCollection = ['./index.js', ...glob.sync('src/**/*.js', {})];

jsCollection.forEach(jsFile => {
  minify({
    jsFile: fs.readFileSync(jsFile, 'utf8')
  }).then( value => {
    fs.writeFile(jsFile, value.code, 'utf8', (err) => {
      if (err) throw err;
    });
  });
});
