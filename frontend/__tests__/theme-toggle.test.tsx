import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach } from 'vitest';
import { ThemeToggle } from '@/components/theme-toggle';

/**
 * Unit test suite for the `ThemeToggle` component.
 *
 * Validates theme state detection from DOM `data-theme` attribute, interactive toggling,
 * localStorage persistence, and accessibility aria attributes. Updated for the light-default
 * ("Paper") flip introduced by ADR-002 — the absence of `data-theme` now means light, not dark.
 */
describe('ThemeToggle', () => {
  /**
   * Resets local storage and document root theme attributes before each test execution.
   */
  beforeEach(() => {
    document.documentElement.removeAttribute('data-theme');
    localStorage.clear();
  });

  /**
   * Verifies default light theme state when no `data-theme` attribute exists on `<html>`.
   */
  it('renders as light by default (matching the no data-theme attribute state)', () => {
    // 1. Given document is initialized without a data-theme attribute
    // 2. When rendering the ThemeToggle component
    render(<ThemeToggle />);
    const button = screen.getByRole('button', { name: /switch to dark theme/i });

    // 3. Then the button aria-pressed state should be false
    expect(button).toHaveAttribute('aria-pressed', 'false');
  });

  /**
   * Verifies that the toggle component detects dark theme from the DOM attribute on mount.
   */
  it('reflects an already-dark DOM state on mount, without flashing light first', () => {
    // 1. Given the document root already has data-theme="dark"
    document.documentElement.setAttribute('data-theme', 'dark');

    // 2. When rendering the ThemeToggle component
    render(<ThemeToggle />);

    // 3. Then the button text and aria-pressed state should reflect dark theme immediately
    expect(screen.getByRole('button', { name: /switch to light theme/i })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
  });

  /**
   * Verifies interactive transition from light to dark theme on button click.
   */
  it('switches to dark on click, sets the DOM attribute, and persists the choice', () => {
    // 1. Given the ThemeToggle component is rendered in light mode (the default)
    render(<ThemeToggle />);

    // 2. When clicking the theme toggle button
    fireEvent.click(screen.getByRole('button', { name: /switch to dark theme/i }));

    // 3. Then the DOM data-theme attribute, localStorage, and aria labels update to dark mode
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    expect(localStorage.getItem('theme')).toBe('dark');
    expect(screen.getByRole('button', { name: /switch to light theme/i })).toBeInTheDocument();
  });

  /**
   * Verifies toggling back from dark mode to light mode on subsequent click.
   */
  it('switches back to light on a second click and removes the attribute', () => {
    // 1. Given the ThemeToggle component is rendered
    render(<ThemeToggle />);
    const button = screen.getByRole('button');

    // 2. When clicking the button twice (light -> dark -> light)
    fireEvent.click(button); // -> dark
    fireEvent.click(button); // -> light

    // 3. Then the data-theme attribute is removed and theme="light" is persisted in localStorage
    expect(document.documentElement.hasAttribute('data-theme')).toBe(false);
    expect(localStorage.getItem('theme')).toBe('light');
  });
});
