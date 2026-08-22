import { MetadataRoute } from 'next';

/**
 * Dynamic sitemap generator for Next.js App Router.
 *
 * Generates the site's sitemap entries. Since ADR-003's pivot to live aggregation, there is no
 * per-place detail page or stable slug to enumerate — places are fetched live from Google
 * Places/OSM, not persisted — so this lists only the static routes.
 *
 * @returns Array of sitemap route objects.
 */
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://echtgut.de';

  return [
    {
      url: baseUrl,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 1.0,
    },
    {
      url: `${baseUrl}/map`,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 0.8,
    },
  ];
}
