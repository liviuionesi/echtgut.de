import React from 'react';
import { fetchTrendingPlaces, PlaceDto } from '@/lib/api';
import { PlaceCard } from '@/components/catalog/place-card';
import { MapPin, Sparkles, Navigation } from 'lucide-react';

export default async function PublicHomePage() {
  const trendingPlaces = await fetchTrendingPlaces();

  return (
    <main className="min-h-screen bg-bg pb-24 text-fg">
      {/* Hero Section */}
      <section className="relative w-full overflow-hidden bg-bg pb-20 pt-32">
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div className="absolute -right-1/4 -top-1/2 h-[1000px] w-[1000px] rounded-full bg-accent/5 blur-[120px]" />
          <div className="absolute -bottom-1/2 -left-1/4 h-[800px] w-[800px] rounded-full bg-emerald-500/5 blur-[120px]" />
        </div>

        <div className="container relative z-10 mx-auto px-4 text-center">
          <div className="mb-8 inline-flex items-center gap-2 rounded-full border border-accent/20 bg-accent/10 px-4 py-2 text-accent backdrop-blur-md">
            <Sparkles className="h-4 w-4" />
            <span className="text-sm font-medium uppercase tracking-wide">
              Der Live-Aggregator für Stuttgart
            </span>
          </div>

          <h1 className="mx-auto mb-6 max-w-4xl font-display text-5xl font-extrabold leading-tight tracking-tight text-fg md:text-7xl">
            Entdecke die besten Orte im Umkreis von{' '}
            <span className="bg-gradient-to-r from-accent to-emerald-500 bg-clip-text text-transparent">
              50km
            </span>
          </h1>

          <p className="mx-auto mb-10 max-w-2xl text-xl leading-relaxed text-fg-muted">
            Wir durchsuchen das Netz in Echtzeit nach den am besten bewerteten, geöffneten
            Locations, damit du direkt losziehen kannst.
          </p>

          <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
            <a
              href="#trending"
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-accent px-8 py-4 font-semibold text-white shadow-lg shadow-accent/25 transition-all hover:scale-105 hover:bg-accent/90 sm:w-auto"
            >
              <MapPin className="h-5 w-5" />
              Jetzt entdecken
            </a>
            <a
              href="/map"
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-border bg-bg-elevated px-8 py-4 font-semibold text-fg transition-all hover:border-accent/50 hover:bg-accent/5 sm:w-auto"
            >
              <Navigation className="h-5 w-5" />
              Auf der Karte zeigen
            </a>
          </div>
        </div>
      </section>

      {/* Trending Grid Section */}
      <section id="trending" className="container mx-auto px-4 pt-16">
        <div className="mb-10 flex items-center justify-between">
          <div>
            <h2 className="mb-2 font-display text-3xl font-bold text-fg">Live Top-Locations</h2>
            <p className="text-fg-muted">Basierend auf aktuellen Besucherzahlen und Bewertungen</p>
          </div>
          <div className="hidden gap-2 md:flex">
            <span className="rounded-full bg-accent/10 px-4 py-2 text-sm font-medium text-accent">
              Alle
            </span>
            <span className="cursor-pointer rounded-full border border-border px-4 py-2 text-sm font-medium text-fg-muted transition-colors hover:text-fg">
              Geöffnet
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-3">
          {trendingPlaces.map((place) => (
            <PlaceCard key={place.id} place={place} />
          ))}
          {trendingPlaces.length === 0 && (
            <div className="col-span-full rounded-2xl border border-dashed border-border py-20 text-center text-fg-muted">
              Keine Orte gefunden. Bitte lade die Seite neu.
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
