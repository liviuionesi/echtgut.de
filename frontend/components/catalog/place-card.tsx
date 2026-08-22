import React from 'react';
import Image from 'next/image';
import { PlaceDto } from '@/lib/api';
import { Star, MapPin, Clock } from 'lucide-react';

export function PlaceCard({ place }: { place: PlaceDto }) {
  return (
    <div className="group relative overflow-hidden rounded-2xl border border-border bg-bg-elevated shadow-lg transition-all hover:-translate-y-1 hover:shadow-xl">
      <div className="relative aspect-[4/3] w-full overflow-hidden">
        <Image
          src={place.imageUrl}
          alt={place.name}
          fill
          className="object-cover transition-transform duration-500 group-hover:scale-105"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />

        {/* Badges overlay */}
        <div className="absolute left-4 top-4 flex gap-2">
          {place.openNow ? (
            <span className="rounded-full bg-emerald-500/90 px-3 py-1 text-xs font-semibold text-white backdrop-blur-md">
              Geöffnet
            </span>
          ) : (
            <span className="rounded-full bg-red-500/90 px-3 py-1 text-xs font-semibold text-white backdrop-blur-md">
              Geschlossen
            </span>
          )}
          <span className="rounded-full border border-white/20 bg-black/50 px-3 py-1 text-xs font-semibold text-white backdrop-blur-md">
            {place.category}
          </span>
        </div>

        {/* Info overlay */}
        <div className="absolute bottom-4 left-4 right-4">
          <h3 className="mb-1 text-xl font-bold text-white drop-shadow-md">{place.name}</h3>

          <div className="flex items-center gap-4 text-sm text-white/90">
            <div className="flex items-center gap-1">
              <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
              <span className="font-semibold">{place.rating.toFixed(1)}</span>
              <span className="text-white/70">({place.reviewCount})</span>
            </div>
          </div>
        </div>
      </div>

      <div className="p-5">
        <p className="mb-4 line-clamp-2 text-sm text-fg-muted">{place.description}</p>

        <div className="flex items-start gap-2 text-sm text-fg-muted">
          <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-accent" />
          <span className="line-clamp-1">{place.address}</span>
        </div>
      </div>
    </div>
  );
}
