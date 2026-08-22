/**
 * Types and API client helpers for the public catalog (ADR-003: live-aggregated places, no
 * curator-facing endpoints remain — see docs/ARCHITECTURE.md §3).
 */

export interface TagDto {
  id: string;
  slug: string;
  name: string;
  category: string;
  isRetired: boolean;
  description?: string;
}

/**
 * One aggregated place as served by `GET /api/places/trending`. `rating`/`reviewCount`/`openNow`
 * are absent whenever the source (OSM) has no such data — never a fabricated placeholder value.
 */
export interface PlaceDto {
  id: string;
  name: string;
  description: string;
  category: string;
  address: string;
  lat: number;
  lon: number;
  rating?: number;
  reviewCount?: number;
  openNow?: boolean;
  imageUrl: string;
}

const getBaseUrl = (): string => {
  return process.env.NEXT_PUBLIC_API_URL || '';
};

/**
 * Fetches public taxonomy tags (excluding retired ones).
 *
 * @returns Array of tags
 */
export async function fetchPublicTags(): Promise<TagDto[]> {
  const response = await fetch(`${getBaseUrl()}/api/tags`, {
    // Revalidate occasionally, but since tags don't change every minute,
    // ISR is a good fit here.
    next: { revalidate: 3600 },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch public tags: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Fetches trending places aggregated from real-world data.
 */
export async function fetchTrendingPlaces(): Promise<PlaceDto[]> {
  const response = await fetch(`${getBaseUrl()}/api/places/trending`, {
    next: { revalidate: 60 }, // Revalidate every minute for live status updates
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch trending places: ${response.statusText}`);
  }

  return response.json();
}
