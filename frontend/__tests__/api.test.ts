import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { fetchPublicTags, fetchTrendingPlaces } from '@/lib/api';

/**
 * Unit tests for the public catalog API client helpers (`lib/api.ts`). `global.fetch` is mocked
 * so these never make a real network call; each test covers both the success path and the
 * non-ok-response error path.
 */
describe('api', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe('fetchPublicTags', () => {
    /**
     * Verifies a successful response is parsed and returned as-is.
     */
    it('returns the parsed tag list on a 200 response', async () => {
      // 1. Given the tags endpoint returns one tag
      const tags = [
        { id: '1', slug: 'auszeit', name: 'Auszeit', category: 'MOOD', isRetired: false },
      ];
      vi.mocked(fetch).mockResolvedValue({ ok: true, json: async () => tags } as Response);

      // 2. When fetching public tags
      const result = await fetchPublicTags();

      // 3. Then the parsed list is returned
      expect(result).toEqual(tags);
    });

    /**
     * Verifies a non-ok response throws instead of silently returning bad data.
     */
    it('throws when the response is not ok', async () => {
      // 1. Given the tags endpoint fails
      vi.mocked(fetch).mockResolvedValue({ ok: false, statusText: 'Server Error' } as Response);

      // 2. When / 3. Then fetching public tags rejects
      await expect(fetchPublicTags()).rejects.toThrow('Failed to fetch public tags');
    });
  });

  describe('fetchTrendingPlaces', () => {
    /**
     * Verifies a successful response is parsed and returned as-is.
     */
    it('returns the parsed place list on a 200 response', async () => {
      // 1. Given the trending-places endpoint returns one place
      const places = [
        {
          id: 'ChIJ-real-google-place-id',
          name: 'Bio Brotgarten',
          description: 'desc',
          category: 'Bakery',
          address: 'Berlin',
          lat: 52.53,
          lon: 13.4,
          imageUrl: 'https://example.com/brot.jpg',
        },
      ];
      vi.mocked(fetch).mockResolvedValue({ ok: true, json: async () => places } as Response);

      // 2. When fetching trending places
      const result = await fetchTrendingPlaces();

      // 3. Then the parsed list is returned
      expect(result).toEqual(places);
    });

    /**
     * Verifies a non-ok response throws instead of silently returning bad data.
     */
    it('throws when the response is not ok', async () => {
      // 1. Given the trending-places endpoint fails
      vi.mocked(fetch).mockResolvedValue({ ok: false, statusText: 'Server Error' } as Response);

      // 2. When / 3. Then fetching trending places rejects
      await expect(fetchTrendingPlaces()).rejects.toThrow('Failed to fetch trending places');
    });
  });
});
