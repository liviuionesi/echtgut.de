import nextConfig from 'eslint-config-next';

/** @type {import('eslint').Linter.Config[]} */
const eslintConfig = [
  { ignores: ['coverage/**', '.next/**'] },
  ...(Array.isArray(nextConfig) ? nextConfig : [nextConfig]),
];

export default eslintConfig;
