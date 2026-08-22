'use client';

import React, { useState, useEffect, useCallback } from 'react';
import {
  fetchNextPendingDeal,
  promoteDeal,
  rejectDeal,
  fetchAdminTags,
  createAdminTag,
  RawDealResponse,
  PromoteDealRequest,
  TagResponse,
} from '@/lib/api';

/**
 * Single-card review screen component for curator admin portal (`/admin`).
 *
 * Displays one candidate deal from the airlock queue at a time, allowing the curator
 * to polish editorial details, assign taxonomy tags, and promote or reject the candidate.
 * On action completion, automatically advances to the next pending deal without a full page reload.
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

  // Taxonomy tags state
  const [availableTags, setAvailableTags] = useState<TagResponse[]>([]);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [newTagName, setNewTagName] = useState<string>('');
  const [isCreatingTag, setIsCreatingTag] = useState<boolean>(false);

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
    setSelectedTags(['auszeit']); // Default initial mood tag
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
    Promise.all([fetchNextPendingDeal(), fetchAdminTags(false).catch(() => [])])
      .then(([nextDeal, tags]) => {
        if (isMounted) {
          setDeal(nextDeal);
          setAvailableTags(tags);
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

  const toggleTag = (slug: string) => {
    setSelectedTags((prev) =>
      prev.includes(slug) ? prev.filter((t) => t !== slug) : [...prev, slug],
    );
  };

  const handleCreateCustomTag = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!newTagName.trim()) return;

    setIsCreatingTag(true);
    setError(null);
    try {
      const created = await createAdminTag({ name: newTagName.trim() });
      setAvailableTags((prev) => [...prev, created]);
      setSelectedTags((prev) => [...prev, created.slug]);
      setNewTagName('');
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to create tag';
      setError(message);
    } finally {
      setIsCreatingTag(false);
    }
  };

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
      setError('Coordinates must be valid numbers.');
      return;
    }

    setIsSubmitting(true);
    setError(null);
    setSuccessMsg(null);

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
      tags: selectedTags,
    };

    try {
      await promoteDeal(deal.id, payload);
      setSuccessMsg(`Successfully promoted deal "${deal.rawTitle}"!`);
      await loadNextDeal();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to promote deal.';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReject = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!deal) return;

    setIsSubmitting(true);
    setError(null);
    setSuccessMsg(null);

    try {
      await rejectDeal(deal.id, { reason: rejectionReason.trim() || undefined });
      setSuccessMsg(`Deal "${deal.rawTitle}" rejected.`);
      await loadNextDeal();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to reject deal.';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center rounded-2xl border border-stone-800 bg-stone-900/50 p-8">
        <div className="flex items-center space-x-3 text-stone-400">
          <div className="h-5 w-5 animate-spin rounded-full border-2 border-emerald-500 border-t-transparent" />
          <span>Fetching next pending deal candidate...</span>
        </div>
      </div>
    );
  }

  if (!deal) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-stone-800/60 bg-stone-900/40 p-12 text-center shadow-xl">
        <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-400">
          <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h3 className="text-xl font-bold text-stone-100">Queue Empty</h3>
        <p className="mt-2 max-w-md text-sm text-stone-400">
          All pending deal candidates have been reviewed! Pristine deals are published to the public
          marketplace.
        </p>
        <button
          type="button"
          onClick={() => loadNextDeal()}
          className="mt-6 rounded-xl bg-stone-800 px-5 py-2.5 text-xs font-semibold text-stone-200 transition hover:bg-stone-700"
        >
          Check Again
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Toast notifications */}
      {error && (
        <div className="rounded-xl border border-red-500/30 bg-red-500/10 p-4 text-sm text-red-400">
          {error}
        </div>
      )}
      {successMsg && (
        <div className="rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-4 text-sm text-emerald-400">
          {successMsg}
        </div>
      )}

      {/* Main Single Card Container */}
      <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
        {/* Left Column: Original Raw Listing Payload */}
        <div className="space-y-6 lg:col-span-5">
          <div className="space-y-4 rounded-2xl border border-stone-800 bg-stone-900 p-6 shadow-lg">
            <div className="flex items-center justify-between border-b border-stone-800 pb-3">
              <span className="text-xs font-semibold uppercase tracking-wider text-emerald-400">
                Airlock Candidate
              </span>
              <span className="rounded-full bg-stone-800 px-2.5 py-1 font-mono text-[10px] text-stone-400">
                Source: {deal.source}
              </span>
            </div>

            <div>
              <h3 className="text-lg font-bold text-stone-100">{deal.rawTitle}</h3>
              {deal.priceHint && (
                <p className="mt-1 text-sm font-semibold text-emerald-400">{deal.priceHint}</p>
              )}
            </div>

            {deal.rawImageUrl ? (
              <div className="relative aspect-video w-full overflow-hidden rounded-xl bg-stone-950">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={deal.rawImageUrl}
                  alt={deal.rawTitle}
                  className="h-full w-full object-cover"
                />
              </div>
            ) : (
              <div className="flex aspect-video w-full items-center justify-center rounded-xl bg-stone-950 text-xs text-stone-500">
                No Raw Image Payload
              </div>
            )}

            <div className="space-y-2 text-xs text-stone-300">
              <p className="line-clamp-4 font-serif leading-relaxed text-stone-400">
                {deal.rawDescription || 'No raw description provided.'}
              </p>
              <div className="flex items-center justify-between border-t border-stone-800/60 pt-2 text-[11px] text-stone-500">
                <span>Location: {deal.locationText || 'Unspecified'}</span>
                <span>Ref: {deal.sourceRef}</span>
              </div>
            </div>

            {/* Quick Action Bar for Rejection */}
            <div className="flex items-center space-x-3 border-t border-stone-800 pt-4">
              <button
                type="button"
                onClick={() => setShowRejectForm(!showRejectForm)}
                className="flex-1 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-2.5 text-xs font-bold text-red-400 transition hover:bg-red-500/20"
              >
                {showRejectForm ? 'Cancel Rejection' : 'Reject Candidate'}
              </button>
            </div>
          </div>

          {/* Rejection Drawer Form */}
          {showRejectForm && (
            <form
              onSubmit={handleReject}
              className="space-y-4 rounded-2xl border border-red-500/30 bg-red-950/20 p-6"
            >
              <h4 className="text-sm font-bold text-red-400">Reject Candidate Deal</h4>
              <div>
                <label htmlFor="rejectionReason" className="mb-1 block text-xs text-stone-400">
                  Reason for rejection (optional)
                </label>
                <textarea
                  id="rejectionReason"
                  value={rejectionReason}
                  onChange={(e) => setRejectionReason(e.target.value)}
                  placeholder="e.g. Low quality, misleading offer, out of regional scope..."
                  className="w-full rounded-xl border border-stone-800 bg-stone-900 p-3 text-xs text-stone-200 focus:border-red-500 focus:outline-none"
                  rows={3}
                />
              </div>
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full rounded-xl bg-red-600 px-4 py-2.5 text-xs font-bold text-white transition hover:bg-red-500 disabled:opacity-50"
              >
                {isSubmitting ? 'Rejecting...' : 'Confirm Rejection'}
              </button>
            </form>
          )}
        </div>

        {/* Right Column: Editorial Refinement & Promotion Form */}
        <div className="lg:col-span-7">
          <form
            onSubmit={handlePromote}
            className="space-y-5 rounded-2xl border border-stone-800 bg-stone-900 p-6 shadow-lg"
          >
            <div className="flex items-center justify-between border-b border-stone-800 pb-3">
              <h3 className="text-sm font-bold uppercase tracking-wider text-emerald-400">
                Editorial Refinement (FR-3.5)
              </h3>
              <span className="text-xs text-stone-500">Pristine Curation</span>
            </div>

            <div>
              <label
                htmlFor="editorialTitle"
                className="mb-1 block text-xs font-semibold text-stone-300"
              >
                Editorial Title *
              </label>
              <input
                id="editorialTitle"
                type="text"
                value={editorialTitle}
                onChange={(e) => setEditorialTitle(e.target.value)}
                placeholder="Pristine engaging title..."
                className="w-full rounded-xl border border-stone-800 bg-stone-950 px-4 py-2.5 text-sm text-stone-100 focus:border-emerald-500 focus:outline-none"
                required
              />
            </div>

            <div>
              <label
                htmlFor="editorialDescription"
                className="mb-1 block text-xs font-semibold text-stone-300"
              >
                Editorial Narrative & Description *
              </label>
              <textarea
                id="editorialDescription"
                value={editorialDescription}
                onChange={(e) => setEditorialDescription(e.target.value)}
                placeholder="Evocative editorial narrative..."
                rows={4}
                className="w-full rounded-xl border border-stone-800 bg-stone-950 p-4 text-sm text-stone-100 focus:border-emerald-500 focus:outline-none"
                required
              />
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label
                  htmlFor="heroImageUrl"
                  className="mb-1 block text-xs font-semibold text-stone-300"
                >
                  Hero Image URL *
                </label>
                <input
                  id="heroImageUrl"
                  type="text"
                  value={heroImageUrl}
                  onChange={(e) => setHeroImageUrl(e.target.value)}
                  placeholder="https://images.unsplash.com/..."
                  className="w-full rounded-xl border border-stone-800 bg-stone-950 px-4 py-2.5 text-xs text-stone-100 focus:border-emerald-500 focus:outline-none"
                  required
                />
              </div>

              <div>
                <label
                  htmlFor="address"
                  className="mb-1 block text-xs font-semibold text-stone-300"
                >
                  Address / Location *
                </label>
                <input
                  id="address"
                  type="text"
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="Street, City, Region..."
                  className="w-full rounded-xl border border-stone-800 bg-stone-950 px-4 py-2.5 text-xs text-stone-100 focus:border-emerald-500 focus:outline-none"
                  required
                />
              </div>
            </div>

            {/* Taxonomy Tag Picker Grid (FR-4.1 / FR-5.2) */}
            <div className="space-y-3 rounded-xl border border-stone-800 bg-stone-950/60 p-4">
              <div className="flex items-center justify-between">
                <label className="text-xs font-semibold text-stone-300">
                  Taxonomy Tags (FR-4.1)
                </label>
                <span className="text-[10px] text-stone-500">Pick tags for catalog filtering</span>
              </div>

              {/* Tag Chip Selector */}
              <div className="flex flex-wrap gap-2">
                {availableTags.map((tag) => {
                  const isSelected = selectedTags.includes(tag.slug);
                  return (
                    <button
                      key={tag.id || tag.slug}
                      type="button"
                      onClick={() => toggleTag(tag.slug)}
                      className={`rounded-full px-3 py-1 text-xs font-semibold transition ${
                        isSelected
                          ? 'bg-emerald-500 text-stone-950 shadow-md shadow-emerald-500/20'
                          : 'bg-stone-800 text-stone-400 hover:bg-stone-700'
                      }`}
                    >
                      {isSelected ? '✓ ' : ''}
                      {tag.name}
                    </button>
                  );
                })}
              </div>

              {/* Inline Custom Tag Creator */}
              <div className="border-t border-stone-800/60 pt-2">
                <div className="flex items-center space-x-2">
                  <input
                    type="text"
                    value={newTagName}
                    onChange={(e) => setNewTagName(e.target.value)}
                    placeholder="New custom tag name..."
                    className="flex-1 rounded-lg border border-stone-800 bg-stone-900 px-3 py-1.5 text-xs text-stone-200 focus:border-emerald-500 focus:outline-none"
                  />
                  <button
                    type="button"
                    onClick={(e) => {
                      const formEvent = e as unknown as React.SyntheticEvent<HTMLFormElement>;
                      handleCreateCustomTag(formEvent);
                    }}
                    disabled={isCreatingTag || !newTagName.trim()}
                    className="rounded-lg bg-stone-800 px-3 py-1.5 text-xs font-semibold text-emerald-400 transition hover:bg-stone-700 disabled:opacity-50"
                  >
                    {isCreatingTag ? 'Creating...' : '+ Add Tag'}
                  </button>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label htmlFor="lat" className="mb-1 block text-xs font-semibold text-stone-300">
                  Latitude *
                </label>
                <input
                  id="lat"
                  type="number"
                  step="any"
                  value={lat}
                  onChange={(e) => setLat(e.target.value)}
                  className="w-full rounded-xl border border-stone-800 bg-stone-950 px-4 py-2.5 text-xs text-stone-100 focus:border-emerald-500 focus:outline-none"
                  required
                />
              </div>

              <div>
                <label htmlFor="lng" className="mb-1 block text-xs font-semibold text-stone-300">
                  Longitude *
                </label>
                <input
                  id="lng"
                  type="number"
                  step="any"
                  value={lng}
                  onChange={(e) => setLng(e.target.value)}
                  className="w-full rounded-xl border border-stone-800 bg-stone-950 px-4 py-2.5 text-xs text-stone-100 focus:border-emerald-500 focus:outline-none"
                  required
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="affiliateUrl"
                className="mb-1 block text-xs font-semibold text-stone-300"
              >
                Affiliate URL (Optional)
              </label>
              <input
                id="affiliateUrl"
                type="text"
                value={affiliateUrl}
                onChange={(e) => setAffiliateUrl(e.target.value)}
                placeholder="https://partner.com/..."
                className="w-full rounded-xl border border-stone-800 bg-stone-950 px-4 py-2.5 text-xs text-stone-100 focus:border-emerald-500 focus:outline-none"
              />
            </div>

            <div>
              <label
                htmlFor="bookingContact"
                className="mb-1 block text-xs font-semibold text-stone-300"
              >
                Direct Booking Contact (Optional)
              </label>
              <input
                id="bookingContact"
                type="text"
                value={bookingContact}
                onChange={(e) => setBookingContact(e.target.value)}
                placeholder="Phone or email contact..."
                className="w-full rounded-xl border border-stone-800 bg-stone-950 px-4 py-2.5 text-xs text-stone-100 focus:border-emerald-500 focus:outline-none"
              />
            </div>

            <div>
              <label
                htmlFor="curatorNotes"
                className="mb-1 block text-xs font-semibold text-stone-300"
              >
                Curator Notes (Internal)
              </label>
              <textarea
                id="curatorNotes"
                value={curatorNotes}
                onChange={(e) => setCuratorNotes(e.target.value)}
                placeholder="Notes regarding deal verification or editorial decision..."
                rows={2}
                className="w-full rounded-xl border border-stone-800 bg-stone-950 p-3 text-xs text-stone-100 focus:border-emerald-500 focus:outline-none"
              />
            </div>

            <div className="flex items-center justify-end space-x-4 border-t border-stone-800 pt-4">
              <button
                type="submit"
                disabled={isSubmitting}
                className="rounded-xl bg-emerald-500 px-6 py-3 text-sm font-bold text-stone-950 shadow-lg shadow-emerald-500/20 transition hover:bg-emerald-400 disabled:opacity-50"
              >
                {isSubmitting ? 'Promoting...' : 'Promote to Public Catalog ✓'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
