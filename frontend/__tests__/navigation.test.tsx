import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { Navigation } from '@/components/navigation';

vi.mock('next/navigation', () => ({
  usePathname: () => '/',
}));

/**
 * Unit tests for the {@link Navigation} top bar — brand link plus the primary nav items
 * (Entdecken/Karte/Top Bewertet), rendered on every page via `app/layout.tsx`.
 */
describe('Navigation', () => {
  /**
   * Verifies the brand link and every declared nav item render.
   */
  it('renders the brand link and all nav items', () => {
    // 1. Given the navigation bar is rendered
    render(<Navigation />);

    // 2. When inspecting the rendered links
    // 3. Then the brand and each nav item are present
    expect(screen.getByText('echtgut.de')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Entdecken/i })).toHaveAttribute('href', '/');
    expect(screen.getByRole('link', { name: /Karte/i })).toHaveAttribute('href', '/map');
    expect(screen.getByRole('link', { name: /Top Bewertet/i })).toHaveAttribute('href', '/top');
  });

  /**
   * Verifies the nav item matching the current path is styled active.
   */
  it('marks the nav item matching the current path as active', () => {
    // 1. Given the current path is the home route ('/', mocked above)
    render(<Navigation />);

    // 2. When inspecting the "Entdecken" link (href="/")
    // 3. Then it carries the active-state accent class, and the others don't
    expect(screen.getByRole('link', { name: /Entdecken/i })).toHaveClass('text-accent');
    expect(screen.getByRole('link', { name: /Karte/i })).not.toHaveClass('text-accent');
  });
});
