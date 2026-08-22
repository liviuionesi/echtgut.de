import { describe, it, expect } from 'vitest';
import sitemap from '../app/sitemap';

/**
 * Unit test for the dynamic sitemap generator. Since ADR-003 there's no persisted per-place
 * detail route to enumerate, so this only needs to cover the static route list.
 */
describe('sitemap', () => {
  /**
   * Verifies the root and map routes are present with valid entries.
   */
  it('lists the root and map routes', async () => {
    // 1. Given the site URL env var is unset (falls back to the production default)
    // 2. When generating the sitemap
    const entries = await sitemap();

    // 3. Then both static routes are present
    const urls = entries.map((entry) => entry.url);
    expect(urls).toContain('https://echtgut.de');
    expect(urls).toContain('https://echtgut.de/map');
  });
});
