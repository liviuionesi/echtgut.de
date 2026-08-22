import { MetadataRoute } from 'next';
import { fetchPublicExperiences } from '@/lib/api';

/**
 * Dynamic sitemap generator for Next.js App Router.
 *
 * Generates XML sitemap index entries for the root marketplace and all published
 * experience detail pages.
 *
 * @returns Array of sitemap route objects.
 */
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://echtgut.de';

  let experiences: Array<{ slug: string; publishedAt: string }> = [];
  try {
    const res = await fetchPublicExperiences(undefined, undefined, 0, 100);
    experiences = res.content || [];
  } catch {
    experiences = [];
  }

  const experienceUrls: MetadataRoute.Sitemap = experiences.map((exp) => ({
    url: `${baseUrl}/experience/${exp.slug}`,
    lastModified: exp.publishedAt ? new Date(exp.publishedAt) : new Date(),
    changeFrequency: 'weekly',
    priority: 0.8,
  }));

  return [
    {
      url: baseUrl,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 1.0,
    },
    ...experienceUrls,
  ];
}
