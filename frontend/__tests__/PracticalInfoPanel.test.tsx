import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { PracticalInfoPanel } from '@/components/catalog/practical-info-panel';

/**
 * Test suite for the `PracticalInfoPanel` component.
 *
 * Verifies the signature "narrative vs. facts" panel (FR-4.7, ADR-002) renders
 * its transactional facts, Book Now CTA, and default team-level curatorial
 * attribution — never an individual curator's name unless explicitly overridden.
 */
describe('PracticalInfoPanel', () => {
  const baseProps = {
    address: 'Grunewald, Berlin',
    priceHint: 'ab 18 €',
    bookingLabel: 'Termin anfragen',
  };

  /**
   * Verifies the address, price hint, and booking CTA all render.
   */
  it('renders the address, price hint, and booking CTA', () => {
    // 1. Given a fully-populated panel
    render(<PracticalInfoPanel {...baseProps} />);

    // 2. When inspecting the rendered panel
    // 3. Then the address, price, and CTA label are all present
    expect(screen.getByText(baseProps.address)).toBeInTheDocument();
    expect(screen.getByText(baseProps.priceHint)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: baseProps.bookingLabel })).toBeInTheDocument();
  });

  /**
   * Verifies the default attribution is team-level, not an individual curator's name.
   */
  it('defaults the attribution to team-level, not an individual curator', () => {
    // 1. Given a panel with no explicit attribution override
    render(<PracticalInfoPanel {...baseProps} />);

    // 2. When inspecting the attribution line
    // 3. Then it reads the team-level default (ADR-002 §4 / NFR-4)
    expect(screen.getByText('Kuratiert vom echtgut-Team')).toBeInTheDocument();
  });

  /**
   * Verifies the CTA uses a fixed, contrast-safe text color rather than the
   * theme-flipping `fg` token (which fails AA contrast against the `accent`
   * fill in the light "Paper" default — see the component's inline comment).
   */
  it('renders the booking CTA with fixed white text on the accent fill', () => {
    // 1. Given a fully-populated panel
    render(<PracticalInfoPanel {...baseProps} />);

    // 2. When inspecting the CTA link's classes
    const link = screen.getByRole('link', { name: baseProps.bookingLabel });

    // 3. Then it uses the fixed white/accent combination, not `text-fg`
    expect(link.className).toContain('bg-accent');
    expect(link.className).toContain('text-white');
    expect(link.className).not.toContain('text-fg');
  });

  /**
   * Verifies a caller-provided attribution overrides the team-level default.
   */
  it('renders a caller-provided attribution override', () => {
    // 1. Given a panel with an explicit attribution string
    render(<PracticalInfoPanel {...baseProps} attribution="Kuratiert von Liviu" />);

    // 2. When inspecting the attribution line
    // 3. Then the override text is shown instead of the default
    expect(screen.getByText('Kuratiert von Liviu')).toBeInTheDocument();
    expect(screen.queryByText('Kuratiert vom echtgut-Team')).not.toBeInTheDocument();
  });

  /**
   * Verifies the booking CTA falls back to "#" when no href is provided.
   */
  it('stubs the booking CTA href to "#" when none is provided', () => {
    // 1. Given a panel with no bookingHref
    render(<PracticalInfoPanel {...baseProps} />);

    // 2. When inspecting the CTA link
    const link = screen.getByRole('link', { name: baseProps.bookingLabel });

    // 3. Then it falls back to "#" instead of an undefined/missing href
    expect(link).toHaveAttribute('href', '#');
  });
});
