import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, beforeEach } from 'vitest';
import { ThemeToggle } from '@/components/theme-toggle';

describe('ThemeToggle', () => {
  beforeEach(() => {
    document.documentElement.removeAttribute('data-theme');
    localStorage.clear();
  });

  it('renders as dark by default (matching the no data-theme attribute state)', () => {
    render(<ThemeToggle />);
    const button = screen.getByRole('button', { name: /switch to light theme/i });
    expect(button).toHaveAttribute('aria-pressed', 'false');
  });

  it('reflects an already-light DOM state on mount, without flashing dark first', () => {
    document.documentElement.setAttribute('data-theme', 'light');
    render(<ThemeToggle />);
    expect(screen.getByRole('button', { name: /switch to dark theme/i })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
  });

  it('switches to light on click, sets the DOM attribute, and persists the choice', () => {
    render(<ThemeToggle />);
    fireEvent.click(screen.getByRole('button', { name: /switch to light theme/i }));

    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
    expect(localStorage.getItem('theme')).toBe('light');
    expect(screen.getByRole('button', { name: /switch to dark theme/i })).toBeInTheDocument();
  });

  it('switches back to dark on a second click and removes the attribute', () => {
    render(<ThemeToggle />);
    const button = screen.getByRole('button');
    fireEvent.click(button); // -> light
    fireEvent.click(button); // -> dark

    expect(document.documentElement.hasAttribute('data-theme')).toBe(false);
    expect(localStorage.getItem('theme')).toBe('dark');
  });
});
