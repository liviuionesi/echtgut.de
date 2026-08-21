import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import PublicHomePage from '../app/(public)/page';

/**
 * Test suite for the public landing page component (`PublicHomePage`).
 *
 * Verifies that essential branding elements, primary heading, taglines, and accessible
 * elements render accurately.
 */
describe('PublicHomePage', () => {
  /**
   * Verifies that the primary brand title and subtitle render correctly on initial paint.
   */
  it('renders the headline and description', () => {
    // 1. Given the public home page is rendered
    render(<PublicHomePage />);

    // 2. When inspecting the document DOM
    // 3. Then the brand heading and subtitle text must be visible
    expect(screen.getByRole('heading', { level: 1, name: 'echtgut.de' })).toBeInTheDocument();
    expect(
      screen.getByText('Handverlesene, geprüfte lokale Geheimtipps und Erlebnisse.'),
    ).toBeInTheDocument();
  });
});
