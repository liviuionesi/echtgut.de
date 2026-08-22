import React from 'react';
import { LocationMapWrapper } from '@/components/catalog/location-map-wrapper';

/**
 * Props for {@link PracticalInfoPanel}.
 */
export interface PracticalInfoPanelProps {
  /** Verified location/address (matches `curated_experiences.address`). */
  address: string;
  /** Free-form price hint (matches `raw_deals.price_hint`). */
  priceHint: string;
  /** Label for the Book Now / View Deal CTA (FR-4.3). */
  bookingLabel: string;
  /** Destination href for the Book Now CTA. Defaults to `#` for the sample showcase. */
  bookingHref?: string;
  /**
   * Curatorial attribution line. Defaults to team-level attribution
   * ("Kuratiert vom echtgut-Team") rather than an individual curator's name,
   * per ADR-002 §4 — exposing an individual curator publicly would add new
   * personal-data exposure (NFR-4/GDPR) for no product benefit at MVP scale.
   * personal-data exposure (NFR-4/GDPR) for no product benefit at MVP scale.
   */
  attribution?: string;
  /** Latitude coordinate (optional, needed for the map). */
  lat?: number;
  /** Longitude coordinate (optional, needed for the map). */
  lng?: number;
}

/**
 * "Practical Info" panel — the signature element of the Atlas Obscura-inspired
 * redesign (FR-4.7, ADR-002, Epic #49).
 *
 * Visually separates the transactional facts (address, price hint, Book Now
 * CTA) and curatorial attribution from the editorial narrative that precedes
 * it on a detail page. This is a deliberate layout decision, not just a
 * styling choice: it makes the product's actual pitch — read the curation
 * first, the transaction second — a literal, visible structure rather than
 * just copy.
 *
 * @param props - See {@link PracticalInfoPanelProps}.
 * @returns The rendered panel element.
 */
export function PracticalInfoPanel({
  address,
  priceHint,
  bookingLabel,
  bookingHref = '#',
  attribution = 'Kuratiert vom echtgut-Team',
  lat,
  lng,
}: Readonly<PracticalInfoPanelProps>) {
  return (
    <aside
      aria-label="Praktische Informationen"
      className="space-y-4 rounded-xl border border-gold/30 bg-bg-elevated p-6"
    >
      {lat !== undefined && lng !== undefined && <LocationMapWrapper lat={lat} lng={lng} />}

      <dl className="space-y-3 text-sm">
        <div>
          <dt className="text-xs font-medium uppercase tracking-wide text-fg-muted">Adresse</dt>
          <dd className="mt-0.5 text-fg">{address}</dd>
        </div>
        <div>
          <dt className="text-xs font-medium uppercase tracking-wide text-fg-muted">Preis</dt>
          <dd className="mt-0.5 text-fg">{priceHint}</dd>
        </div>
      </dl>

      {/*
        Fixed `text-white`, not the `fg` token: `fg` is computed for AA contrast
        against the *page* background and inverts per theme, but `accent` is a fill
        here, not a page background — reusing `fg` produced ~1.6:1 contrast (fails
        AA) in the light "Paper" default. White-on-accent clears AA (≥6:1) in both
        themes. See the redesign review notes on Epic #49 / Task #58.
      */}
      <a
        href={bookingHref}
        className="block w-full rounded-lg bg-accent px-4 py-2.5 text-center text-sm font-semibold text-white transition-colors duration-200 hover:bg-accent-strong focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
      >
        {bookingLabel}
      </a>

      <p className="border-t border-border pt-3 text-xs text-fg-muted">{attribution}</p>
    </aside>
  );
}
