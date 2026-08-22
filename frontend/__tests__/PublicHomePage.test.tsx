import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import PublicHomePage from '../app/(public)/page';
import * as api from '@/lib/api';

vi.mock('@/lib/api', () => ({
  fetchTrendingPlaces: vi.fn(),
}));

/**
 * Test suite for the public landing page component (`PublicHomePage`).
 *
 * Since ADR-003's pivot to automated aggregation, the homepage is an async Server Component
 * rendering the live `fetchTrendingPlaces()` result as a `PlaceCard` grid — the previous
 * editorial-redesign sample content (`ExploreByMood`, `ExperienceCard`, `PracticalInfoPanel`)
 * was removed with the pipeline it showcased. `fetchTrendingPlaces` is mocked so tests never hit
 * a real network call, and the async component is awaited before rendering (React Testing
 * Library can't render a function that returns a Promise directly).
 */
describe('PublicHomePage', () => {
  /**
   * Verifies that the hero headline renders on initial paint.
   */
  it('renders the hero headline', async () => {
    // 1. Given the aggregator returns no places
    vi.mocked(api.fetchTrendingPlaces).mockResolvedValue([]);

    // 2. When the (async) home page is rendered
    render(await PublicHomePage());

    // 3. Then the hero heading is present
    expect(
      screen.getByRole('heading', { level: 1, name: /Entdecke die besten Orte/i }),
    ).toBeInTheDocument();
  });

  /**
   * Verifies a PlaceCard renders for each place the aggregator returns.
   */
  it('renders a PlaceCard for each aggregated place', async () => {
    // 1. Given the aggregator returns one place
    vi.mocked(api.fetchTrendingPlaces).mockResolvedValue([
      {
        id: 'ChIJ-real-google-place-id',
        name: 'Bio Brotgarten',
        description: 'Entdeckt via Google Places API: Bio Brotgarten',
        category: 'Bakery',
        address: 'Kastanienallee 12, Berlin',
        lat: 52.53,
        lon: 13.4,
        rating: 4.7,
        reviewCount: 823,
        openNow: true,
        imageUrl: 'https://example.com/brot.jpg',
      },
    ]);

    // 2. When the (async) home page is rendered
    render(await PublicHomePage());

    // 3. Then the place's card content is present
    expect(screen.getByText('Bio Brotgarten')).toBeInTheDocument();
    expect(screen.getByText('Kastanienallee 12, Berlin')).toBeInTheDocument();
  });

  /**
   * Verifies the empty-state message renders when the aggregator returns nothing.
   */
  it('renders an empty-state message when no places are returned', async () => {
    // 1. Given the aggregator returns no places
    vi.mocked(api.fetchTrendingPlaces).mockResolvedValue([]);

    // 2. When the (async) home page is rendered
    render(await PublicHomePage());

    // 3. Then the empty-state message is shown instead of a card grid
    expect(screen.getByText(/Keine Orte gefunden/i)).toBeInTheDocument();
  });
});
