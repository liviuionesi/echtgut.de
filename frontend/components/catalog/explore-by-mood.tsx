/**
 * A single taxonomy tile: a curator-assigned tag rendered as a browsable entry
 * point (FR-4.6), not just a filter-dropdown option.
 */
export interface MoodTile {
  /** Taxonomy tag label, matching FR-3.3's curator-assigned tags. */
  label: string;
  /** Short editorial description of what the mood/tag covers. */
  description: string;
  /**
   * Target href for this tile. Stubbed to `#` until the tag-filtered browse
   * route exists (Epic #32, "Public Marketplace Site") — see Task #56.
   */
  href?: string;
}

/**
 * Props for {@link ExploreByMood}.
 */
export interface ExploreByMoodProps {
  /** The taxonomy tiles to render. */
  tiles: MoodTile[];
}

/**
 * "Explore by mood" taxonomy tile grid — the redesign's browsable entry point
 * for the tag taxonomy (FR-4.6, ADR-002, Epic #49).
 *
 * Atlas Obscura surfaces its own category taxonomy as a browsable front door
 * rather than a filter control buried in a sidebar; this component adopts that
 * pattern for echtgut's curator-assigned tags (FR-3.3), turning "Relaxation,"
 * "Fix My Back," "Rainy Day" (FR-4.1's own examples) into clickable tiles on
 * the homepage instead of an implicit dropdown-only filter.
 *
 * @param props - See {@link ExploreByMoodProps}.
 * @returns The rendered taxonomy tile grid.
 */
export function ExploreByMood({ tiles }: Readonly<ExploreByMoodProps>) {
  return (
    <section aria-labelledby="explore-by-mood-heading" className="space-y-4">
      <h2 id="explore-by-mood-heading" className="font-display text-2xl font-semibold text-fg">
        Entdecke nach Stimmung
      </h2>
      <ul className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
        {tiles.map((tile) => (
          <li key={tile.label}>
            <a
              href={tile.href ?? '#'}
              className="block h-full rounded-lg border border-border bg-bg-elevated p-4 transition-colors duration-200 hover:border-accent focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
            >
              <span className="block font-display text-base font-semibold text-fg">
                {tile.label}
              </span>
              <span className="mt-1 block text-xs text-fg-muted">{tile.description}</span>
            </a>
          </li>
        ))}
      </ul>
    </section>
  );
}
