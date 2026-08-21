import { dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { FlatCompat } from '@eslint/eslintrc';

// ESLint 9 requires flat config; eslint-config-next's shareable config is
// still eslintrc-format, so FlatCompat bridges the two. Replaces the
// original .eslintrc.json, which ESLint 9 silently can't load at all
// (confirmed live: `npx eslint` errored "couldn't find eslint.config.js"
// before this file existed — not a hypothetical fix).
const compat = new FlatCompat({
  baseDirectory: dirname(fileURLToPath(import.meta.url)),
});

const eslintConfig = [
  { ignores: ['coverage/**', '.next/**'] },
  ...compat.extends('next/core-web-vitals'),
];

export default eslintConfig;
