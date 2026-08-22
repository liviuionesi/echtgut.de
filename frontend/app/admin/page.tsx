'use client';

import React from 'react';
import { ThemeToggle } from '@/components/theme-toggle';
import { SingleCardReview } from '@/components/admin/SingleCardReview';

/**
 * Curator admin dashboard page component (`/admin`).
 *
 * Provides the administrative interface for curators to inspect raw deal feeds,
 * refine submissions, and approve promotion to curated experience listings.
 *
 * @returns The rendered admin dashboard React element.
 */
export default function AdminDashboardPage() {
  return (
    <div className="min-h-screen bg-bg p-8 text-fg">
      <div className="mx-auto max-w-7xl">
        <header className="mb-8 flex items-center justify-between border-b border-border pb-4">
          <div>
            <h1 className="font-display text-2xl font-semibold text-fg">Curator Admin Portal</h1>
            <p className="text-sm text-fg-muted">
              Review, refine, and promote incoming deal submissions
            </p>
          </div>
          <div className="flex items-center gap-3">
            <span className="rounded border border-gold/20 bg-gold/10 px-3 py-1 text-xs font-semibold text-gold">
              Curator Airlock Active
            </span>
            <ThemeToggle />
          </div>
        </header>

        <SingleCardReview />
      </div>
    </div>
  );
}
