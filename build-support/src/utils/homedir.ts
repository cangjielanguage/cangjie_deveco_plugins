/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

import os from 'os';

const home = process.env.HOME;
const user = process.env.LOGNAME ?? process.env.USER ?? process.env.LNAME ?? process.env.USERNAME;

export function getHomedir(): string | null {
  if (os.homedir) {
    return os.homedir();
  }
  if (process.platform === 'win32') {
    return win32HomeDir();
  }
  if (process.platform === 'darwin') {
    return darwinHomeDir();
  }
  if (process.platform === 'linux') {
    return linuxHomeDir();
  }
  return home ?? null;
}

function win32HomeDir(): string | null {
  let candidate: string | undefined;
  if (process.env.HOMEDRIVE && process.env.HOMEPATH) {
    candidate = process.env.HOMEDRIVE + process.env.HOMEPATH;
  }
  return process.env.USERPROFILE ?? candidate ?? home ?? null;
}

function darwinHomeDir(): string | null {
  return home ?? (user ? `/Users/${user}` : null);
}

function linuxHomeDir(): string | null {
  const userDirTemp = user ? `/home/${user}` : null;
  return home ?? (process.getuid() === 0 ? '/root' : userDirTemp);
}
