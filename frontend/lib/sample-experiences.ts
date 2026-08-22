/**
 * Sample (placeholder) editorial content for the Atlas Obscura-inspired homepage
 * showcase — see ADR-002 and Epic #49 (Editorial Redesign).
 *
 * This is deliberately NOT wired to the backend. The public catalog API
 * (`GET /api/experiences`, Task #34) and the SSG/ISR listing/detail templates
 * (Task #35) don't exist yet, so `ExperienceCard` and `PracticalInfoPanel`
 * ship first against this fixed, hand-written data — proving the visual
 * pattern out — and get rewired to `CuratedExperienceResponse` (see
 * `lib/api.ts`) once the real endpoint lands. Every field here mirrors that
 * response shape so the swap is a data-source change, not a component rewrite.
 */

/**
 * A single sample editorial listing, shaped to match the eventual
 * `CuratedExperienceResponse` fields the real catalog API will return.
 */
export interface SampleExperience {
  /** Stable identifier, doubles as the SSG route slug once real routing exists. */
  slug: string;
  /** Curator-assigned taxonomy tag (FR-3.3), rendered as the card's badge. */
  tag: string;
  /** Editorial headline, set in the display face (Fraunces). */
  title: string;
  /** One-line editorial hook shown below the card image — not the full description. */
  hook: string;
  /** Longer editorial narrative shown on the detail-page showcase. */
  description: string;
  /** Local sample illustration path (see frontend/public/sample/). */
  imageUrl: string;
  /** Descriptive alt text for the sample illustration. */
  imageAlt: string;
  /** Human-readable location, matching `curated_experiences.address`. */
  location: string;
  /** Free-form price hint, matching `raw_deals.price_hint` once promoted. */
  priceHint: string;
  /** Label for the Book Now / View Deal CTA (FR-4.3). */
  bookingLabel: string;
}

/**
 * Three representative sample experiences, one per FR-4.1 example taxonomy tag
 * ("Relaxation," "Fix My Back," "Rainy Day"), used to populate the homepage
 * showcase (`ExperienceCard` grid + `PracticalInfoPanel` pairing).
 */
export const SAMPLE_EXPERIENCES: SampleExperience[] = [
  {
    slug: 'waldsee-sauna',
    tag: 'Relaxation',
    title: 'Die Sauna am Waldsee, die niemand kennt',
    hook: 'Ein Holzsteg, ein stiller See und eine Sauna, die seit 1974 kaum jemand findet.',
    description:
      'Fünf Kilometer außerhalb der Stadt, hinter einem unscheinbaren Waldweg, liegt eine ' +
      'Blockhaus-Sauna direkt am Ufer eines kleinen Sees. Kein Schild, keine Warteschlange — ' +
      'nur der Betreiber, zwei Aufgüsse pro Stunde und ein Sprung ins kalte Wasser danach. ' +
      'Unser Kurator hat dreimal vorbeigeschaut, bevor er sie freigegeben hat.',
    imageUrl: '/sample/waldsee-sauna.svg',
    imageAlt: 'Illustration einer Holzsauna am Ufer eines ruhigen Waldsees',
    location: 'Grunewald, Berlin',
    priceHint: 'ab 18 €',
    bookingLabel: 'Termin anfragen',
  },
  {
    slug: 'hinterhof-atelier',
    tag: 'Rainy Day',
    title: 'Das Atelier im Hinterhof, das aussieht wie ein Museum',
    hook: 'Eine ehemalige Druckerei, drei Künstler, keine Eintrittskarte — nur klingeln.',
    description:
      'Hinter einer unauffälligen Hoftür verbirgt sich eine ehemalige Druckerei, die drei ' +
      'lokale Künstler seit Jahren als offenes Atelier nutzen. Es gibt keine Kasse und keine ' +
      'Öffnungszeiten im klassischen Sinn — nur einen Klingelknopf und die Gewissheit, dass ' +
      'samstags fast immer jemand da ist.',
    imageUrl: '/sample/hinterhof-atelier.svg',
    imageAlt: 'Illustration eines Ateliers mit Bilderrahmen und Staffeleien',
    location: 'Prenzlauer Berg, Berlin',
    priceHint: 'kostenlos, Spende erwünscht',
    bookingLabel: 'Adresse anzeigen',
  },
  {
    slug: 'bibliothekscafe',
    tag: 'Fix My Back',
    title: 'Das Bibliothekscafé mit den besten Stühlen der Stadt',
    hook: 'Regen draußen, ein Kaffee, ein Sessel, der tatsächlich den Rücken stützt.',
    description:
      'Ein kleines Café im Erdgeschoss einer stillgelegten Bezirksbibliothek — die alten ' +
      'Lesesessel sind geblieben, restauriert und erstaunlich rückenfreundlich. An Regentagen ' +
      'die verlässlichste Adresse der Stadt, um drei Stunden lang nichts zu tun außer zu lesen.',
    imageUrl: '/sample/bibliothekscafe.svg',
    imageAlt: 'Illustration eines Bibliothekscafés mit Bücherregal bei Regen',
    location: 'Kreuzberg, Berlin',
    priceHint: 'ab 4,50 €',
    bookingLabel: 'Route anzeigen',
  },
];
