'use client';

import { useState } from 'react';

/**
 * Toggles between the "Paper" (light, default) and "Ink" (dark) themes defined
 * in app/globals.css, persisting the explicit choice to localStorage.
 *
 * Reads its initial state from the DOM (`data-theme` on <html>) via lazy state initializer using
 * `dataset.theme` rather than defaulting to a hardcoded value — the inline script in app/layout.tsx
 * has already set that attribute correctly before this component ever mounts, so there is no
 * flash-of-wrong-theme on load and no server/client hydration mismatch to reconcile.
 *
 * State is tracked as `isDark` (not `isLight`) because light is the default per
 * ADR-002 — the absence of `data-theme` now means "light," so the toggle's internal
 * truth mirrors that: it only ever sets/clears the `dark` attribute value.
 */
export function ThemeToggle() {
  const [isDark, setIsDark] = useState(
    () => typeof document !== 'undefined' && document.documentElement.dataset.theme === 'dark',
  );

  function toggle() {
    const next = isDark ? 'light' : 'dark';
    if (next === 'dark') {
      document.documentElement.dataset.theme = 'dark';
    } else {
      delete document.documentElement.dataset.theme;
    }
    localStorage.setItem('theme', next);
    setIsDark(next === 'dark');
  }

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={isDark ? 'Switch to light theme' : 'Switch to dark theme'}
      aria-pressed={isDark}
      className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-border text-fg-muted transition-colors duration-200 hover:border-accent hover:text-accent focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
    >
      <span aria-hidden="true">{isDark ? '☾' : '☀'}</span>
    </button>
  );
}
