import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import MapPage from '../app/(public)/map/page';
import * as api from '@/lib/api';

vi.mock('@/lib/api', () => ({
  fetchTrendingPlaces: vi.fn(),
}));

vi.mock('@/components/catalog/location-map-wrapper', () => ({
  LocationMapWrapper: ({ places }: { places?: Array<{ id: string }> }) => (
    <div data-testid="map-wrapper">{places?.length ?? 0} places</div>
  ),
}));

/**
 * Test suite for the `/map` page — an async Server Component fetching live places and handing
 * them to {@link LocationMapWrapper}. Both collaborators are mocked so this never touches a real
 * network call or Leaflet.
 */
describe('MapPage', () => {
  /**
   * Verifies the page heading renders and the fetched places reach the map wrapper.
   */
  it('renders the heading and forwards fetched places to the map wrapper', async () => {
    // 1. Given the aggregator returns two places
    vi.mocked(api.fetchTrendingPlaces).mockResolvedValue([
      { id: '1' } as never,
      { id: '2' } as never,
    ]);

    // 2. When the (async) map page is rendered
    render(await MapPage());

    // 3. Then the heading is present and the map wrapper received both places
    expect(screen.getByRole('heading', { name: 'Interaktive Karte' })).toBeInTheDocument();
    expect(screen.getByTestId('map-wrapper')).toHaveTextContent('2 places');
  });
});
