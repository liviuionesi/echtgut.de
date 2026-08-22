import './globals.css';
import { Metadata } from 'next';
import { ReactNode } from 'react';
import { Fraunces, Inter } from 'next/font/google';

/**
 * Primary display font configuration (Fraunces variable font).
 *
 * Configured with `display: 'optional'` to guarantee zero Cumulative Layout Shift (CLS),
 * meeting Core Web Vitals targets for Lighthouse and user experience.
 */
const fraunces = Fraunces({
  subsets: ['latin'],
  variable: '--font-display',
  display: 'optional',
  axes: ['opsz', 'SOFT', 'WONK'],
});

/**
 * Primary body font configuration (Inter variable font).
 *
 * Configured with `display: 'optional'` for optimum rendering performance and stability.
 */
const inter = Inter({
  subsets: ['latin'],
  variable: '--font-body',
  display: 'optional',
});

/**
 * Root metadata configuration for SEO and social sharing.
 */
export const metadata: Metadata = {
  title: 'echtgut.de — Handverlesene lokale Entdeckungen',
  description: 'Geprüfte, authentische Empfehlungen und Angebote ohne Werbemüll.',
};

/**
 * Synchronous inline theme initialization script.
 *
 * Runs before first paint to prevent Flash of Unstyled Content (FOUC) when switching
 * between light and dark themes. Precedence: saved preference > system color scheme > light
 * default (flipped from dark by ADR-002 — see docs/architecture/adr/002-atlas-obscura-inspired-editorial-redesign.md).
 */
const THEME_INIT_SCRIPT = `
(function () {
  try {
    var saved = localStorage.getItem('theme');
    var wantsDark = saved
      ? saved === 'dark'
      : window.matchMedia('(prefers-color-scheme: dark)').matches;
    if (wantsDark) document.documentElement.dataset.theme = 'dark';
  } catch (e) {}
})();
`;

/**
 * Props for the root layout component.
 */
interface RootLayoutProps {
  /** Page content elements rendered inside the root HTML shell. */
  children: ReactNode;
}

/**
 * Root layout component providing structural HTML shell, global typography CSS variables,
 * inline theme initialization script, and base body styling.
 *
 * @param props - Layout component properties containing page children.
 * @returns The root HTML layout tree.
 */
export default function RootLayout({ children }: Readonly<RootLayoutProps>) {
  return (
    <html lang="de" className={`${fraunces.variable} ${inter.variable}`}>
      <head>
        <script dangerouslySetInnerHTML={{ __html: THEME_INIT_SCRIPT }} />
      </head>
      <body className="min-h-screen font-sans antialiased">{children}</body>
    </html>
  );
}
