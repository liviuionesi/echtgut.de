import React from 'react';
import { fetchTrendingPlaces } from '@/lib/api';
import { LocationMapWrapper } from '@/components/catalog/location-map-wrapper';

// See app/(public)/page.tsx — same reasoning: live aggregated data, not build-time content.
export const dynamic = 'force-dynamic';

export default async function MapPage() {
  const trendingPlaces = await fetchTrendingPlaces();

  return (
    <main className="min-h-screen bg-bg text-fg">
      <div className="container mx-auto h-[calc(100vh-64px)] px-4 py-8">
        <div className="mb-6">
          <h1 className="mb-2 font-display text-3xl font-bold text-fg">Interaktive Karte</h1>
          <p className="text-fg-muted">Entdecke alle spannenden Orte in und um Stuttgart.</p>
        </div>

        <div className="h-[calc(100%-100px)] w-full overflow-hidden rounded-2xl border border-border">
          <LocationMapWrapper places={trendingPlaces} height="h-full" />
        </div>
      </div>
    </main>
  );
}
