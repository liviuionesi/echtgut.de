import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import PublicHomePage from '../app/(public)/page';

/**
 * Test suite for the public landing page component (`PublicHomePage`).
 *
 * Verifies that essential branding elements render, and — since the Atlas
 * Obscura-inspired redesign (ADR-002, Epic #49) — that the page also
 * showcases the new editorial components (`ExploreByMood`, `ExperienceCard`,
 * `PracticalInfoPanel`) against sample content.
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

  /**
   * Verifies the "Explore by mood" taxonomy grid is present with FR-4.1's example tags.
   */
  it('renders the "Explore by mood" taxonomy grid with the FR-4.1 example tags', () => {
    // 1. Given the public home page is rendered
    render(<PublicHomePage />);

    // 2. When inspecting the Explore by mood section
    // 3. Then all three FR-4.1 example taxonomy tags are present as tiles
    expect(screen.getByRole('heading', { name: 'Entdecke nach Stimmung' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Relaxation/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Fix My Back/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Rainy Day/i })).toBeInTheDocument();
  });

  /**
   * Verifies a photo-led ExperienceCard renders for each sample listing.
   */
  it('renders a sample ExperienceCard grid', () => {
    // 1. Given the public home page is rendered
    render(<PublicHomePage />);

    // 2. When inspecting the sample listings section
    // 3. Then the sample experience titles render as card headings — the first
    //    sample (the featured item) appears twice: once as its card heading,
    //    once as the detail-showcase narrative heading below.
    expect(
      screen.getAllByRole('heading', { name: 'Die Sauna am Waldsee, die niemand kennt' }),
    ).toHaveLength(2);
    expect(
      screen.getByRole('heading', {
        name: 'Das Atelier im Hinterhof, das aussieht wie ein Museum',
      }),
    ).toBeInTheDocument();
  });

  /**
   * Verifies the PracticalInfoPanel showcase renders with team-level attribution.
   */
  it('renders the PracticalInfoPanel showcase with team-level attribution', () => {
    // 1. Given the public home page is rendered
    render(<PublicHomePage />);

    // 2. When inspecting the sample detail showcase section
    // 3. Then the Practical Info panel's booking CTA and attribution are present
    expect(screen.getByRole('link', { name: 'Termin anfragen' })).toBeInTheDocument();
    expect(screen.getByText('Kuratiert vom echtgut-Team')).toBeInTheDocument();
  });
});
