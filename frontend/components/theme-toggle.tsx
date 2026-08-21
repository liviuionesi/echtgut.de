'use client';

import { useEffect, useState } from 'react';

/**
 * Toggles between the "Ink" (dark) and "Paper" (light) themes defined in
 * app/globals.css, persisting the explicit choice to localStorage.
 *
 * Reads its initial state from the DOM (`data-theme` on <html>) rather
 * than defaulting to a hardcoded value — the inline script in
 * app/layout.tsx has already set that attribute correctly before this
 * component ever mounts, so there is no flash-of-wrong-theme on load and
 * no server/client hydration mismatch to reconcile.
 */
export function ThemeToggle() {
  const [isLight, setIsLight] = useState(false);

  useEffect(() => {
    setIsLight(document.documentElement.getAttribute('data-theme') === 'light');
  }, []);

  function toggle() {
    const next = isLight ? 'dark' : 'light';
    if (next === 'light') {
      document.documentElement.setAttribute('data-theme', 'light');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
    localStorage.setItem('theme', next);
    setIsLight(next === 'light');
  }

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={isLight ? 'Switch to dark theme' : 'Switch to light theme'}
      aria-pressed={isLight}
      className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-border text-fg-muted transition-colors duration-200 hover:border-accent hover:text-accent focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
    >
      <span aria-hidden="true">{isLight ? '☀' : '☾'}</span>
    </button>
  );
}
