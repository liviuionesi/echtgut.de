import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach } from 'vitest';
import { ThemeToggle } from '@/components/theme-toggle';

/**
 * Unit test suite for the `ThemeToggle` component.
 *
 * Validates theme state detection from DOM `data-theme` attribute, interactive toggling,
 * localStorage persistence, and accessibility aria attributes.
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
   * Verifies default dark theme state when no `data-theme` attribute exists on `<html>`.
   */
  it('renders as dark by default (matching the no data-theme attribute state)', () => {
    // 1. Given document is initialized without a data-theme attribute
    // 2. When rendering the ThemeToggle component
    render(<ThemeToggle />);
    const button = screen.getByRole('button', { name: /switch to light theme/i });

    // 3. Then the button aria-pressed state should be false
    expect(button).toHaveAttribute('aria-pressed', 'false');
  });

  /**
   * Verifies that the toggle component detects light theme from the DOM attribute on mount.
   */
  it('reflects an already-light DOM state on mount, without flashing dark first', () => {
    // 1. Given the document root already has data-theme="light"
    document.documentElement.setAttribute('data-theme', 'light');

    // 2. When rendering the ThemeToggle component
    render(<ThemeToggle />);

    // 3. Then the button text and aria-pressed state should reflect light theme immediately
    expect(screen.getByRole('button', { name: /switch to dark theme/i })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
  });

  /**
   * Verifies interactive transition from dark to light theme on button click.
   */
  it('switches to light on click, sets the DOM attribute, and persists the choice', () => {
    // 1. Given the ThemeToggle component is rendered in dark mode
    render(<ThemeToggle />);

    // 2. When clicking the theme toggle button
    fireEvent.click(screen.getByRole('button', { name: /switch to light theme/i }));

    // 3. Then the DOM data-theme attribute, localStorage, and aria labels update to light mode
    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
    expect(localStorage.getItem('theme')).toBe('light');
    expect(screen.getByRole('button', { name: /switch to dark theme/i })).toBeInTheDocument();
  });

  /**
   * Verifies toggling back from light mode to dark mode on subsequent click.
   */
  it('switches back to dark on a second click and removes the attribute', () => {
    // 1. Given the ThemeToggle component is rendered
    render(<ThemeToggle />);
    const button = screen.getByRole('button');

    // 2. When clicking the button twice (dark -> light -> dark)
    fireEvent.click(button); // -> light
    fireEvent.click(button); // -> dark

    // 3. Then the data-theme attribute is removed and theme="dark" is persisted in localStorage
    expect(document.documentElement.hasAttribute('data-theme')).toBe(false);
    expect(localStorage.getItem('theme')).toBe('dark');
  });
});
