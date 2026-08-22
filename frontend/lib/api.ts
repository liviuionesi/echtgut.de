/**
 * Types and API client helpers for the Curator Admin Portal endpoints.
 */

export interface RawDealResponse {
  id: string;
  source: string;
  sourceRef: string;
  rawTitle: string;
  rawDescription: string | null;
  rawImageUrl: string | null;
  locationText: string | null;
  lat: number | null;
  lng: number | null;
  priceHint: string | null;
  status: 'PENDING' | 'REJECTED' | 'PROMOTED';
  rejectionReason?: string | null;
  submittedBy?: string | null;
  promotedExperienceId?: string | null;
  ingestedAt?: string | null;
  reviewedAt?: string | null;
}

export interface PromoteDealRequest {
  slug?: string;
  editorialTitle: string;
  editorialDescription: string;
  heroImageUrl: string;
  address: string;
  lat: number;
  lng: number;
  affiliateUrl?: string;
  bookingContact?: string;
  curatorNotes?: string;
  isPublished?: boolean;
}

export interface CuratedExperienceResponse {
  id: string;
  rawDealId: string;
  slug: string;
  title: string;
  description: string;
  heroImageUrl: string;
  address: string;
  lat: number;
  lng: number;
  affiliateUrl?: string | null;
  bookingContact?: string | null;
  curatorNotes?: string | null;
  isPublished: boolean;
  publishedAt?: string | null;
  updatedAt?: string | null;
}

export interface RejectDealRequest {
  reason?: string;
}

const getBaseUrl = (): string => {
  return process.env.NEXT_PUBLIC_API_URL || '';
};

/**
 * Fetches the next pending raw deal candidate for curator review.
 *
 * @returns Pending deal or null if status code is 204 (queue empty).
 */
export async function fetchNextPendingDeal(): Promise<RawDealResponse | null> {
  const baseUrl = getBaseUrl();
  const res = await fetch(`${baseUrl}/api/admin/pending-deals`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    cache: 'no-store',
  });

  if (res.status === 204) {
    return null;
  }

  if (!res.ok) {
    throw new Error(`Failed to fetch pending deal: ${res.statusText}`);
  }

  return res.json();
}

/**
 * Promotes a raw candidate deal into a curated experience.
 *
 * @param id Raw deal candidate UUID.
 * @param payload Promotion data payload.
 * @returns Promoted curated experience response.
 */
export async function promoteDeal(
  id: string,
  payload: PromoteDealRequest,
): Promise<CuratedExperienceResponse> {
  const baseUrl = getBaseUrl();
  const res = await fetch(`${baseUrl}/api/admin/deals/${id}/promote`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const errorText = await res.text().catch(() => '');
    throw new Error(`Failed to promote deal (${res.status}): ${errorText || res.statusText}`);
  }

  return res.json();
}

/**
 * Rejects a raw candidate deal with an optional reason.
 *
 * @param id Raw deal candidate UUID.
 * @param payload Optional rejection reason payload.
 * @returns Updated raw deal response.
 */
export async function rejectDeal(
  id: string,
  payload?: RejectDealRequest,
): Promise<RawDealResponse> {
  const baseUrl = getBaseUrl();
  const res = await fetch(`${baseUrl}/api/admin/deals/${id}/reject`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload || {}),
  });

  if (!res.ok) {
    const errorText = await res.text().catch(() => '');
    throw new Error(`Failed to reject deal (${res.status}): ${errorText || res.statusText}`);
  }

  return res.json();
}
