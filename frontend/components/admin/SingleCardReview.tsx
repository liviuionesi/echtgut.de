'use client';

import React, { useState, useEffect, useCallback } from 'react';
import {
  fetchNextPendingDeal,
  promoteDeal,
  rejectDeal,
  RawDealResponse,
  PromoteDealRequest,
} from '@/lib/api';

/**
 * Single-card review screen component for curator admin portal (`/admin`).
 *
 * Displays one candidate deal from the airlock queue at a time, allowing the curator
 * to polish editorial details and promote or reject the candidate. On action completion,
 * automatically advances to the next pending deal without a full page reload.
 *
 * @returns The rendered SingleCardReview React component.
 */
export function SingleCardReview() {
  const [deal, setDeal] = useState<RawDealResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Rejection drawer state
  const [showRejectForm, setShowRejectForm] = useState<boolean>(false);
  const [rejectionReason, setRejectionReason] = useState<string>('');

  // Editorial refinement form state
  const [editorialTitle, setEditorialTitle] = useState<string>('');
  const [editorialDescription, setEditorialDescription] = useState<string>('');
  const [heroImageUrl, setHeroImageUrl] = useState<string>('');
  const [address, setAddress] = useState<string>('');
  const [lat, setLat] = useState<number | string>(0);
  const [lng, setLng] = useState<number | string>(0);
  const [affiliateUrl, setAffiliateUrl] = useState<string>('');
  const [bookingContact, setBookingContact] = useState<string>('');
  const [curatorNotes, setCuratorNotes] = useState<string>('');

  const populateForm = useCallback((dealData: RawDealResponse) => {
    setEditorialTitle(dealData.rawTitle || '');
    setEditorialDescription(dealData.rawDescription || '');
    setHeroImageUrl(dealData.rawImageUrl || '');
    setAddress(dealData.locationText || '');
    setLat(dealData.lat ?? 0);
    setLng(dealData.lng ?? 0);
    setAffiliateUrl('');
    setBookingContact('');
    setCuratorNotes('');
    setShowRejectForm(false);
    setRejectionReason('');
  }, []);

  const loadNextDeal = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const nextDeal = await fetchNextPendingDeal();
      setDeal(nextDeal);
      if (nextDeal) {
        populateForm(nextDeal);
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to load pending deal';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [populateForm]);

  useEffect(() => {
    let isMounted = true;
    fetchNextPendingDeal()
      .then((nextDeal) => {
        if (isMounted) {
          setDeal(nextDeal);
          if (nextDeal) {
            populateForm(nextDeal);
          }
          setIsLoading(false);
        }
      })
      .catch((err: unknown) => {
        if (isMounted) {
          const message = err instanceof Error ? err.message : 'Failed to load pending deal';
          setError(message);
          setIsLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [populateForm]);

  const handlePromote = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!deal) return;

    if (!editorialTitle.trim()) {
      setError('Editorial title is required.');
      return;
    }
    if (!editorialDescription.trim()) {
      setError('Editorial description is required.');
      return;
    }
    if (!heroImageUrl.trim()) {
      setError('Hero image URL is required.');
      return;
    }
    if (!address.trim()) {
      setError('Address is required.');
      return;
    }

    const numericLat = typeof lat === 'string' ? Number.parseFloat(lat) : lat;
    const numericLng = typeof lng === 'string' ? Number.parseFloat(lng) : lng;

    if (Number.isNaN(numericLat) || Number.isNaN(numericLng)) {
      setError('Latitude and Longitude must be valid numbers.');
      return;
    }

    const payload: PromoteDealRequest = {
      editorialTitle: editorialTitle.trim(),
      editorialDescription: editorialDescription.trim(),
      heroImageUrl: heroImageUrl.trim(),
      address: address.trim(),
      lat: numericLat,
      lng: numericLng,
      affiliateUrl: affiliateUrl.trim() || undefined,
      bookingContact: bookingContact.trim() || undefined,
      curatorNotes: curatorNotes.trim() || undefined,
      isPublished: true,
    };

    setIsSubmitting(true);
    setError(null);
    setSuccessMsg(null);

    try {
      await promoteDeal(deal.id, payload);
      setSuccessMsg(`Successfully promoted deal: "${editorialTitle}"`);
      await loadNextDeal();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to promote deal';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReject = async () => {
    if (!deal) return;

    setIsSubmitting(true);
    setError(null);
    setSuccessMsg(null);

    try {
      await rejectDeal(deal.id, { reason: rejectionReason.trim() || undefined });
      setSuccessMsg(`Rejected raw deal candidate.`);
      await loadNextDeal();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to reject deal';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="rounded-xl border border-border bg-bg-elevated p-12 text-center">
        <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-accent border-t-transparent"></div>
        <p className="mt-4 text-sm text-fg-muted">Fetching next candidate from airlock queue...</p>
      </div>
    );
  }

  if (error && !deal) {
    return (
      <div className="rounded-xl border border-red-500/30 bg-red-500/10 p-6 text-center">
        <p className="text-sm font-semibold text-red-400">Unable to load pending deal</p>
        <p className="mt-1 text-xs text-fg-muted">{error}</p>
        <button
          type="button"
          onClick={loadNextDeal}
          className="mt-4 rounded border border-border bg-bg px-4 py-2 text-xs font-semibold text-fg hover:bg-bg-elevated"
        >
          Retry Fetching Queue
        </button>
      </div>
    );
  }

  if (!deal) {
    return (
      <div className="rounded-xl border border-gold/30 bg-bg-elevated p-12 text-center">
        <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-gold/10 text-gold">
          ✨
        </div>
        <h2 className="font-display text-xl font-semibold text-fg">Queue Empty</h2>
        <p className="mt-2 text-sm text-fg-muted">
          All pending raw deals have been reviewed! The airlock queue is clean.
        </p>
        <button
          type="button"
          onClick={loadNextDeal}
          className="mt-6 inline-flex items-center gap-2 rounded-lg border border-border bg-bg px-4 py-2 text-xs font-semibold text-fg hover:bg-bg-elevated"
        >
          <span>Refresh Queue Status</span>
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {successMsg && (
        <div className="rounded-lg border border-accent/40 bg-accent/10 px-4 py-3 text-sm text-fg">
          {successMsg}
        </div>
      )}

      {error && (
        <div className="rounded-lg border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-300">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
        {/* Left Column: Raw Source Listing Details */}
        <div className="space-y-4 lg:col-span-5">
          <div className="space-y-4 rounded-xl border border-border bg-bg-elevated p-6">
            <div className="flex items-center justify-between">
              <span className="rounded-full border border-gold/30 bg-gold/10 px-3 py-1 text-xs font-semibold text-gold">
                Raw Candidate
              </span>
              <span className="font-mono text-xs text-fg-muted">
                {deal.source} &bull; {deal.sourceRef}
              </span>
            </div>

            <div>
              <h3 className="text-xs font-medium uppercase tracking-wider text-fg-muted">
                Raw Title
              </h3>
              <p className="mt-1 font-display text-lg font-semibold text-fg">{deal.rawTitle}</p>
            </div>

            <div>
              <h3 className="text-xs font-medium uppercase tracking-wider text-fg-muted">
                Raw Description
              </h3>
              <p className="mt-1 whitespace-pre-wrap text-sm leading-relaxed text-fg-muted">
                {deal.rawDescription || 'No description provided.'}
              </p>
            </div>

            {deal.rawImageUrl && (
              <div>
                <h3 className="mb-2 text-xs font-medium uppercase tracking-wider text-fg-muted">
                  Source Image Preview
                </h3>
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={deal.rawImageUrl}
                  alt={deal.rawTitle}
                  className="h-48 w-full rounded-lg border border-border object-cover"
                  onError={(e) => {
                    (e.target as HTMLElement).style.display = 'none';
                  }}
                />
              </div>
            )}

            <div className="grid grid-cols-2 gap-4 border-t border-border/60 pt-4 text-xs">
              <div>
                <span className="text-fg-muted">Location Text:</span>
                <p className="font-medium text-fg">{deal.locationText || 'N/A'}</p>
              </div>
              <div>
                <span className="text-fg-muted">Price Hint:</span>
                <p className="font-medium text-gold">{deal.priceHint || 'N/A'}</p>
              </div>
              <div>
                <span className="text-fg-muted">Coordinates:</span>
                <p className="font-mono font-medium text-fg">
                  {deal.lat ?? '0.0'}, {deal.lng ?? '0.0'}
                </p>
              </div>
              <div>
                <span className="text-fg-muted">Submitted By:</span>
                <p className="font-medium text-fg">{deal.submittedBy || 'System Feed'}</p>
              </div>
            </div>
          </div>

          {/* Quick Rejection Form */}
          <div className="rounded-xl border border-border bg-bg-elevated p-6">
            {!showRejectForm ? (
              <button
                type="button"
                onClick={() => setShowRejectForm(true)}
                disabled={isSubmitting}
                className="w-full rounded-lg border border-red-500/30 bg-red-500/10 py-2.5 text-xs font-semibold text-red-300 transition-colors hover:bg-red-500/20"
              >
                Reject Deal Candidate
              </button>
            ) : (
              <div className="space-y-3">
                <label
                  htmlFor="rejectionReason"
                  className="block text-sm font-semibold text-red-400"
                >
                  Reject Candidate
                </label>
                <textarea
                  id="rejectionReason"
                  value={rejectionReason}
                  onChange={(e) => setRejectionReason(e.target.value)}
                  placeholder="Optional rejection reason (e.g. low quality, duplicate)..."
                  rows={2}
                  className="w-full rounded-lg border border-border bg-bg p-2 text-xs text-fg focus:border-red-500 focus:outline-none"
                />
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={handleReject}
                    disabled={isSubmitting}
                    className="flex-1 rounded-lg bg-red-600 py-2 text-xs font-semibold text-white hover:bg-red-500 disabled:opacity-50"
                  >
                    {isSubmitting ? 'Rejecting...' : 'Confirm Reject'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowRejectForm(false)}
                    className="rounded-lg border border-border px-3 py-2 text-xs text-fg-muted hover:bg-bg"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Editorial Refinement Form */}
        <div className="lg:col-span-7">
          <form
            onSubmit={handlePromote}
            className="space-y-4 rounded-xl border border-border bg-bg-elevated p-6"
          >
            <div className="flex items-center justify-between border-b border-border pb-3">
              <h2 className="font-display text-lg font-semibold text-fg">Editorial Refinement</h2>
              <span className="text-xs text-fg-muted">Polish & publish to echtgut.de</span>
            </div>

            <div>
              <label
                htmlFor="editorialTitle"
                className="mb-1 block text-xs font-medium text-fg-muted"
              >
                Editorial Title <span className="text-red-400">*</span>
              </label>
              <input
                id="editorialTitle"
                type="text"
                value={editorialTitle}
                onChange={(e) => setEditorialTitle(e.target.value)}
                required
                className="w-full rounded-lg border border-border bg-bg px-3 py-2 text-sm text-fg focus:border-accent focus:outline-none"
              />
            </div>

            <div>
              <label
                htmlFor="editorialDescription"
                className="mb-1 block text-xs font-medium text-fg-muted"
              >
                Editorial Description <span className="text-red-400">*</span>
              </label>
              <textarea
                id="editorialDescription"
                value={editorialDescription}
                onChange={(e) => setEditorialDescription(e.target.value)}
                required
                rows={4}
                className="w-full rounded-lg border border-border bg-bg px-3 py-2 text-sm text-fg focus:border-accent focus:outline-none"
              />
            </div>

            <div>
              <label
                htmlFor="heroImageUrl"
                className="mb-1 block text-xs font-medium text-fg-muted"
              >
                Hero Image URL <span className="text-red-400">*</span>
              </label>
              <input
                id="heroImageUrl"
                type="url"
                value={heroImageUrl}
                onChange={(e) => setHeroImageUrl(e.target.value)}
                required
                className="w-full rounded-lg border border-border bg-bg px-3 py-2 font-mono text-sm text-fg focus:border-accent focus:outline-none"
              />
            </div>

            <div>
              <label htmlFor="address" className="mb-1 block text-xs font-medium text-fg-muted">
                Physical Address <span className="text-red-400">*</span>
              </label>
              <input
                id="address"
                type="text"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                required
                className="w-full rounded-lg border border-border bg-bg px-3 py-2 text-sm text-fg focus:border-accent focus:outline-none"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="lat" className="mb-1 block text-xs font-medium text-fg-muted">
                  Latitude <span className="text-red-400">*</span>
                </label>
                <input
                  id="lat"
                  type="number"
                  step="any"
                  value={lat}
                  onChange={(e) => setLat(e.target.value)}
                  required
                  className="w-full rounded-lg border border-border bg-bg px-3 py-2 font-mono text-sm text-fg focus:border-accent focus:outline-none"
                />
              </div>

              <div>
                <label htmlFor="lng" className="mb-1 block text-xs font-medium text-fg-muted">
                  Longitude <span className="text-red-400">*</span>
                </label>
                <input
                  id="lng"
                  type="number"
                  step="any"
                  value={lng}
                  onChange={(e) => setLng(e.target.value)}
                  required
                  className="w-full rounded-lg border border-border bg-bg px-3 py-2 font-mono text-sm text-fg focus:border-accent focus:outline-none"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label
                  htmlFor="affiliateUrl"
                  className="mb-1 block text-xs font-medium text-fg-muted"
                >
                  Affiliate Link (Optional)
                </label>
                <input
                  id="affiliateUrl"
                  type="url"
                  value={affiliateUrl}
                  onChange={(e) => setAffiliateUrl(e.target.value)}
                  placeholder="https://..."
                  className="w-full rounded-lg border border-border bg-bg px-3 py-2 font-mono text-sm text-fg focus:border-accent focus:outline-none"
                />
              </div>

              <div>
                <label
                  htmlFor="bookingContact"
                  className="mb-1 block text-xs font-medium text-fg-muted"
                >
                  Booking Contact (Optional)
                </label>
                <input
                  id="bookingContact"
                  type="text"
                  value={bookingContact}
                  onChange={(e) => setBookingContact(e.target.value)}
                  placeholder="Email or Phone"
                  className="w-full rounded-lg border border-border bg-bg px-3 py-2 text-sm text-fg focus:border-accent focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="curatorNotes"
                className="mb-1 block text-xs font-medium text-fg-muted"
              >
                Internal Curator Notes (Optional)
              </label>
              <input
                id="curatorNotes"
                type="text"
                value={curatorNotes}
                onChange={(e) => setCuratorNotes(e.target.value)}
                placeholder="Internal verification notes..."
                className="w-full rounded-lg border border-border bg-bg px-3 py-2 text-sm text-fg focus:border-accent focus:outline-none"
              />
            </div>

            <div className="flex items-center justify-end gap-3 border-t border-border pt-4">
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex items-center gap-2 rounded-lg bg-accent px-6 py-2.5 text-sm font-semibold text-fg transition-colors hover:bg-accent-strong disabled:opacity-50"
              >
                {isSubmitting ? (
                  <>
                    <div className="h-4 w-4 animate-spin rounded-full border-2 border-fg border-t-transparent"></div>
                    <span>Promoting...</span>
                  </>
                ) : (
                  <span>Approve & Promote Experience</span>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
