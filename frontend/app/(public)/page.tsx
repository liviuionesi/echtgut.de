import { ThemeToggle } from '@/components/theme-toggle';
import { ExperienceCard } from '@/components/catalog/experience-card';
import { ExploreByMood, MoodTile } from '@/components/catalog/explore-by-mood';
import { PracticalInfoPanel } from '@/components/catalog/practical-info-panel';
import { SAMPLE_EXPERIENCES } from '@/lib/sample-experiences';

/**
 * "Explore by mood" tiles, derived from FR-4.1's own example taxonomy tags
 * ("Relaxation," "Fix My Back," "Rainy Day") — see REQUIREMENTS.md FR-4.6.
 */
const MOOD_TILES: MoodTile[] = [
  { label: 'Relaxation', description: 'Saunen, Spas und stille Orte zum Runterkommen.' },
  { label: 'Fix My Back', description: 'Bequeme Sessel, gute Haltung, echte Erholung.' },
  { label: 'Rainy Day', description: 'Drinnen-Ideen für Tage ohne Sonne.' },
];

/**
 * Public landing page component (`/`).
 *
 * Serves as the primary public entry point for visitors. As of the Atlas
 * Obscura-inspired redesign (ADR-002, Epic #49), it also doubles as a live
 * showcase of the new editorial components — `ExploreByMood`, `ExperienceCard`,
 * and `PracticalInfoPanel` — against hand-written sample content
 * (`lib/sample-experiences.ts`), since the real catalog API (Task #34) and
 * SSG/ISR listing/detail templates (Task #35) don't exist yet. Once those
 * ship, this page is replaced by the real listing route and this showcase
 * content is removed.
 *
 * @returns The rendered public home page React element.
 */
export default function PublicHomePage() {
  const [featured] = SAMPLE_EXPERIENCES;

  return (
    <main className="min-h-screen bg-bg text-fg">
      <div className="flex justify-end px-4 pt-6">
        <ThemeToggle />
      </div>

      <div className="container mx-auto px-4 py-16 text-center">
        <div className="mb-6 inline-block rounded-full border border-gold/20 bg-gold/10 px-4 py-1 text-sm font-medium text-gold">
          Echt &amp; Kuratiert
        </div>
        <h1 className="mb-6 font-display text-5xl font-semibold tracking-tight text-fg md:text-7xl">
          echtgut.de
        </h1>
        <p className="mx-auto mb-8 max-w-2xl text-lg text-fg-muted md:text-xl">
          Handverlesene, geprüfte lokale Geheimtipps und Erlebnisse.
        </p>
      </div>

      <div className="container mx-auto space-y-16 px-4 pb-24">
        <ExploreByMood tiles={MOOD_TILES} />

        <section aria-labelledby="sample-listings-heading" className="space-y-4">
          <h2 id="sample-listings-heading" className="font-display text-2xl font-semibold text-fg">
            So sieht eine Kuratierung aus
          </h2>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {SAMPLE_EXPERIENCES.map((experience) => (
              <ExperienceCard
                key={experience.slug}
                tag={experience.tag}
                title={experience.title}
                hook={experience.hook}
                imageUrl={experience.imageUrl}
                imageAlt={experience.imageAlt}
                location={experience.location}
                isCuratorPick={experience.slug === featured.slug}
              />
            ))}
          </div>
        </section>

        <section aria-labelledby="sample-detail-heading" className="space-y-4">
          <h2 id="sample-detail-heading" className="font-display text-2xl font-semibold text-fg">
            Und so trennt eine Detailseite Geschichte von Praxis
          </h2>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <div className="space-y-3 rounded-xl border border-border bg-bg-elevated p-6 lg:col-span-2">
              <h3 className="font-display text-xl font-semibold text-fg">{featured.title}</h3>
              <p className="text-sm leading-relaxed text-fg-muted">{featured.description}</p>
            </div>
            <PracticalInfoPanel
              address={featured.location}
              priceHint={featured.priceHint}
              bookingLabel={featured.bookingLabel}
            />
          </div>
        </section>
      </div>
    </main>
  );
}
