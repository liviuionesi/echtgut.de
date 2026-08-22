import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { LocationMapWrapper } from '@/components/catalog/location-map-wrapper';

// The wrapper's whole job is `next/dynamic(..., { ssr: false })` — mock the lazily-imported
// module itself so the test exercises the wrapper's prop pass-through, not Leaflet.
vi.mock('@/components/catalog/location-map', () => ({
  default: ({ places }: { places?: Array<{ id: string }> }) => (
    <div data-testid="real-location-map">{places?.length ?? 0} places</div>
  ),
}));

/**
 * Unit test for {@link LocationMapWrapper} — the `next/dynamic` boundary that defers loading the
 * real Leaflet-backed {@link LocationMap} to the client.
 */
describe('LocationMapWrapper', () => {
  /**
   * Verifies the wrapper forwards its props through to the dynamically-loaded component.
   */
  it('lazily renders the underlying LocationMap with the given places', async () => {
    // 1. Given one place
    render(<LocationMapWrapper places={[{ id: 'p1' } as never]} />);

    // 2. When the dynamic import resolves
    // 3. Then the real (mocked) map receives the places prop
    expect(await screen.findByTestId('real-location-map')).toHaveTextContent('1 places');
  });
});
