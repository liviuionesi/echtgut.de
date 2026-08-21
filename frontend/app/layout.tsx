import './globals.css';
import { ReactNode } from 'react';
import { Fraunces, Inter } from 'next/font/google';

// display: 'optional' — the font either loads in time for first paint or
// the fallback stack is used for that visit, permanently. No swap, no
// layout shift once text has rendered (CLS is a Lighthouse/NFR-2 metric,
// not just a nicety here).
const fraunces = Fraunces({
  subsets: ['latin'],
  variable: '--font-display',
  display: 'optional',
  axes: ['opsz', 'SOFT', 'WONK'],
});

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-body',
  display: 'optional',
});

export const metadata = {
  title: 'echtgut.de — Handverlesene lokale Entdeckungen',
  description: 'Geprüfte, authentische Empfehlungen und Angebote ohne Werbemüll.',
};

// Inline, synchronous, and deliberately NOT a React effect — this must run
// before first paint to avoid a flash of the wrong theme. A useEffect-based
// approach only fixes this after hydration, which is visibly too late.
// Order of precedence: explicit saved choice > system preference > dark
// (this brand's default — see globals.css).
const THEME_INIT_SCRIPT = `
(function () {
  try {
    var saved = localStorage.getItem('theme');
    var wantsLight = saved
      ? saved === 'light'
      : window.matchMedia('(prefers-color-scheme: light)').matches;
    if (wantsLight) document.documentElement.setAttribute('data-theme', 'light');
  } catch (e) {}
})();
`;

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="de" className={`${fraunces.variable} ${inter.variable}`}>
      <head>
        <script dangerouslySetInnerHTML={{ __html: THEME_INIT_SCRIPT }} />
      </head>
      <body className="min-h-screen font-sans antialiased">{children}</body>
    </html>
  );
}
