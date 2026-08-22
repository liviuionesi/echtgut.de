import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ExperienceCard } from '@/components/catalog/experience-card';

/**
 * Test suite for the `ExperienceCard` component.
 *
 * Verifies the photo-led card renders its taxonomy badge, title, hook, and
 * location, and that the curator's-pick styling swaps the badge accent as
 * documented in ARCHITECTURE.md §4.1.1 (ADR-002).
 */
describe('ExperienceCard', () => {
  const baseProps = {
    tag: 'Relaxation',
    title: 'Die Sauna am Waldsee, die niemand kennt',
    hook: 'Ein Holzsteg, ein stiller See und eine Sauna von 1974.',
    imageUrl: '/sample/waldsee-sauna.svg',
    imageAlt: 'Illustration einer Sauna am Waldsee',
    location: 'Grunewald, Berlin',
  };

  /**
   * Verifies the card's core editorial content renders.
   */
  it('renders the title, hook, taxonomy badge, and location', () => {
    // 1. Given a fully-populated ExperienceCard
    render(<ExperienceCard {...baseProps} />);

    // 2. When inspecting the rendered card
    // 3. Then the headline, hook, tag badge, and location text are all present
    expect(screen.getByRole('heading', { level: 3, name: baseProps.title })).toBeInTheDocument();
    expect(screen.getByText(baseProps.hook)).toBeInTheDocument();
    expect(screen.getByText(baseProps.tag)).toBeInTheDocument();
    expect(screen.getByText(baseProps.location)).toBeInTheDocument();
  });

  /**
   * Verifies the image is rendered with descriptive alt text (accessibility, NFR-8).
   */
  it('renders the hero image with its alt text', () => {
    // 1. Given an ExperienceCard with imageAlt set
    render(<ExperienceCard {...baseProps} />);

    // 2. When looking up the image by its accessible name
    // 3. Then it is present in the document
    expect(screen.getByAltText(baseProps.imageAlt)).toBeInTheDocument();
  });

  /**
   * Verifies the gold curator's-pick badge accent is used when flagged.
   */
  it('applies the gold badge accent when isCuratorPick is true', () => {
    // 1. Given an ExperienceCard flagged as a curator's pick
    render(<ExperienceCard {...baseProps} isCuratorPick />);

    // 2. When inspecting the taxonomy badge element
    const badge = screen.getByText(baseProps.tag);

    // 3. Then it carries the gold accent class with a fixed dark-ink text color
    //    (not the theme-flipping `fg` token — see the component's inline comment
    //    on why: `fg` inverts per theme and fails AA contrast against a gold fill).
    expect(badge.className).toContain('bg-gold');
    expect(badge.className).toContain('text-[#1B2620]');
  });

  /**
   * Verifies the default (non-curator-pick) badge uses the moss accent.
   */
  it('applies the moss accent badge with fixed white text by default', () => {
    // 1. Given an ExperienceCard without isCuratorPick set
    render(<ExperienceCard {...baseProps} />);

    // 2. When inspecting the taxonomy badge element
    const badge = screen.getByText(baseProps.tag);

    // 3. Then it carries the default accent class, fixed white text (not the
    //    theme-flipping `fg` token), and not the gold curator-pick styling
    expect(badge.className).toContain('bg-accent');
    expect(badge.className).toContain('text-white');
    expect(badge.className).not.toContain('bg-gold');
  });
});
