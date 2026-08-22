import Image from 'next/image';

/**
 * Props for {@link ExperienceCard}.
 */
export interface ExperienceCardProps {
  /** Curator-assigned taxonomy tag (FR-3.3), rendered as the badge overlaid on the image. */
  tag: string;
  /** Editorial headline, set in the display face (Fraunces). */
  title: string;
  /** One-line editorial hook shown below the image — not the full description. */
  hook: string;
  /** Image path or URL for the card's hero photo. */
  imageUrl: string;
  /** Descriptive alt text for the image. */
  imageAlt: string;
  /** Human-readable location shown under the title. */
  location: string;
  /**
   * Whether this card marks a curator's pick — swaps the badge from moss-green
   * to gold (ARCHITECTURE.md §4.1: gold is reserved for the curation/quality
   * marker, used sparingly). Defaults to false.
   */
  isCuratorPick?: boolean;
}

/**
 * Photo-led experience card — the primary discovery unit introduced by the
 * Atlas Obscura-inspired redesign (FR-4.6, ADR-002, Epic #49).
 *
 * Unlike the text-first layout it replaces, the hero image dominates the card
 * and the taxonomy badge overlays the image directly, rather than sitting in a
 * separate text row. Title and one-line hook sit below the image so the photo
 * itself carries the discovery decision, matching the reference's photo-led
 * browsing pattern.
 *
 * @param props - See {@link ExperienceCardProps}.
 * @returns The rendered card element.
 */
export function ExperienceCard({
  tag,
  title,
  hook,
  imageUrl,
  imageAlt,
  location,
  isCuratorPick = false,
}: Readonly<ExperienceCardProps>) {
  return (
    <article className="group overflow-hidden rounded-xl border border-border bg-bg-elevated transition-shadow duration-200 hover:shadow-lg">
      <div className="relative aspect-[4/3] w-full overflow-hidden">
        <Image
          src={imageUrl}
          alt={imageAlt}
          fill
          sizes="(min-width: 1024px) 33vw, (min-width: 640px) 50vw, 100vw"
          className="object-cover transition-transform duration-300 group-hover:scale-105"
        />
        {/*
          Fixed (non-theme-flipping) text colors, not the `fg`/`bg` tokens: this badge
          sits on top of an arbitrary photograph, its own enclosed color context, not
          the page's light/dark flow. `fg`/`bg` invert per theme and were computed for
          AA contrast against the *page* background, not against `accent`/`gold` as a
          fill — reusing them here produced near-invisible text in the light "Paper"
          default (verified: ~1.6:1 for accent+fg, ~2.9:1 for gold+bg). White-on-accent
          and ink-on-gold both clear AA (≥4.5:1) in both themes — see the redesign
          review notes on Epic #49 / Task #55.
        */}
        <span
          className={`absolute bottom-3 left-3 rounded-full px-3 py-1 text-xs font-semibold ${
            isCuratorPick ? 'bg-gold text-[#1B2620]' : 'bg-accent text-white'
          }`}
        >
          {tag}
        </span>
      </div>
      <div className="space-y-1.5 p-4">
        <h3 className="font-display text-lg font-semibold leading-snug text-fg">{title}</h3>
        <p className="text-sm text-fg-muted">{hook}</p>
        <p className="pt-1 text-xs uppercase tracking-wide text-fg-muted">{location}</p>
      </div>
    </article>
  );
}
