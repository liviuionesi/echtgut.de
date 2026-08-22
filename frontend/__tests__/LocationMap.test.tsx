import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import LocationMap from '@/components/catalog/location-map';

// react-leaflet mounts a real Leaflet map bound to the DOM (requires layout APIs jsdom doesn't
// provide) — mocked here as trivial passthrough components so the test targets this component's
// own logic (marker placement, popup content, honest-rating rendering), not Leaflet's internals.
vi.mock('react-leaflet', () => ({
  MapContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="map-container">{children}</div>
  ),
  TileLayer: () => null,
  Marker: ({ children }: { children?: React.ReactNode }) => (
    <div data-testid="marker">{children}</div>
  ),
  Popup: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

/**
 * Unit tests for {@link LocationMap} — the client-only Leaflet map rendering aggregated places
 * as markers. `react-leaflet` is mocked (see above); `leaflet` itself is left real since {@code
 * L.divIcon} is plain object construction with no DOM dependency.
 */
describe('LocationMap', () => {
  /**
   * Verifies a marker with its popup content renders for a place that has rating data.
   */
  it('renders a marker with rating for a place that has one', () => {
    // 1. Given one place with real rating data
    render(
      <LocationMap
        places={[
          {
            id: 'ChIJ-real-google-place-id',
            name: 'Bio Brotgarten',
            description: 'desc',
            category: 'Bakery',
            address: 'Berlin',
            lat: 52.53,
            lon: 13.4,
            rating: 4.7,
            reviewCount: 823,
            imageUrl: 'https://example.com/brot.jpg',
          },
        ]}
      />,
    );

    // 2. When inspecting the rendered marker popup
    // 3. Then the place's name and real rating are shown
    expect(screen.getByText('Bio Brotgarten')).toBeInTheDocument();
    expect(screen.getByText('4.7')).toBeInTheDocument();
    expect(screen.getByText('(823)')).toBeInTheDocument();
  });

  /**
   * Verifies no rating is rendered (and none is fabricated) for a place without one.
   */
  it('renders no rating for a place the source has none for', () => {
    // 1. Given one place with no rating data (the OSM fallback shape)
    render(
      <LocationMap
        places={[
          {
            id: 'osm-123456',
            name: 'Schlossgarten',
            description: 'desc',
            category: 'Historisch',
            address: 'Stuttgart',
            lat: 48.78,
            lon: 9.18,
            imageUrl: 'https://example.com/placeholder.jpg',
          },
        ]}
      />,
    );

    // 2. When inspecting the rendered marker popup
    // 3. Then the place renders, but with no rating text at all
    expect(screen.getByText('Schlossgarten')).toBeInTheDocument();
    expect(screen.queryByText(/^\d\.\d$/)).not.toBeInTheDocument();
  });

  /**
   * Verifies an empty places list still renders a default center marker instead of crashing.
   */
  it('renders a default marker when given no places', () => {
    // 1. Given no places at all
    render(<LocationMap places={[]} />);

    // 2. When inspecting the map
    // 3. Then a single default marker renders instead of throwing
    expect(screen.getAllByTestId('marker')).toHaveLength(1);
  });
});
