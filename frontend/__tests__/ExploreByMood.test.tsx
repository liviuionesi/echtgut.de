import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ExploreByMood } from '@/components/catalog/explore-by-mood';

/**
 * Test suite for the `ExploreByMood` component.
 *
 * Verifies the taxonomy tile grid renders each tile as a distinct, labeled,
 * clickable entry point (FR-4.6).
 */
describe('ExploreByMood', () => {
  const tiles = [
    { label: 'Relaxation', description: 'Saunen und Spas.' },
    { label: 'Fix My Back', description: 'Bequeme Sessel.' },
    { label: 'Rainy Day', description: 'Drinnen-Ideen.', href: '/explore/rainy-day' },
  ];

  /**
   * Verifies every tile's label and description render.
   */
  it('renders a labeled tile for each taxonomy tag', () => {
    // 1. Given three taxonomy tiles
    render(<ExploreByMood tiles={tiles} />);

    // 2. When inspecting the rendered grid
    // 3. Then each tile's label and description text is present
    for (const tile of tiles) {
      expect(screen.getByText(tile.label)).toBeInTheDocument();
      expect(screen.getByText(tile.description)).toBeInTheDocument();
    }
  });

  /**
   * Verifies each tile is a distinct clickable link.
   */
  it('renders each tile as its own link', () => {
    // 1. Given three taxonomy tiles
    render(<ExploreByMood tiles={tiles} />);

    // 2. When counting the rendered links
    const links = screen.getAllByRole('link');

    // 3. Then there is exactly one link per tile
    expect(links).toHaveLength(tiles.length);
  });

  /**
   * Verifies a tile without an explicit href stubs to "#" rather than crashing.
   */
  it('stubs the href to "#" when a tile has none set', () => {
    // 1. Given a tile with no href
    render(<ExploreByMood tiles={[tiles[0]]} />);

    // 2. When inspecting its link element
    const link = screen.getByRole('link', { name: /Relaxation/i });

    // 3. Then it falls back to "#" instead of an undefined/missing href
    expect(link).toHaveAttribute('href', '#');
  });

  /**
   * Verifies an explicit href is used when a tile provides one.
   */
  it('uses the provided href when a tile sets one', () => {
    // 1. Given a tile with an explicit href
    render(<ExploreByMood tiles={[tiles[2]]} />);

    // 2. When inspecting its link element
    const link = screen.getByRole('link', { name: /Rainy Day/i });

    // 3. Then it points at that href
    expect(link).toHaveAttribute('href', '/explore/rainy-day');
  });
});
