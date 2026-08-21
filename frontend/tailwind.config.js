/** @type {import('tailwindcss').Config} */
module.exports = {
  // No `darkMode`/`dark:` variant in use — theming is done via the
  // `bg`/`fg`/`accent`/... token classes below, which resolve through CSS
  // custom properties that app/globals.css redefines per data-theme
  // attribute (see the ThemeToggle component). A `dark:` variant would be
  // redundant with that and easy to drift out of sync with it.
  content: ['./app/**/*.{js,ts,jsx,tsx,mdx}', './components/**/*.{js,ts,jsx,tsx,mdx}'],
  theme: {
    extend: {
      colors: {
        bg: 'rgb(var(--color-bg) / <alpha-value>)',
        'bg-elevated': 'rgb(var(--color-bg-elevated) / <alpha-value>)',
        fg: 'rgb(var(--color-fg) / <alpha-value>)',
        'fg-muted': 'rgb(var(--color-fg-muted) / <alpha-value>)',
        accent: 'rgb(var(--color-accent) / <alpha-value>)',
        'accent-strong': 'rgb(var(--color-accent-strong) / <alpha-value>)',
        gold: 'rgb(var(--color-gold) / <alpha-value>)',
        border: 'rgb(var(--color-border) / <alpha-value>)',
      },
      fontFamily: {
        display: ['var(--font-display)', 'Georgia', 'serif'],
        sans: ['var(--font-body)', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
