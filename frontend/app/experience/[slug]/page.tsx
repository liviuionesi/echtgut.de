import React from 'react';
import Link from 'next/link';
import { Metadata } from 'next';
import { notFound } from 'next/navigation';
import { fetchExperienceBySlug } from '@/lib/api';
import { PracticalInfoPanel } from '@/components/catalog/practical-info-panel';

interface ExperienceDetailPageProps {
  params: Promise<{
    slug: string;
  }>;
}

/**
 * Generates SEO metadata (OpenGraph tags, Twitter card, canonical URL) for pristine experience detail pages.
 *
 * @param props Page props with slug parameter.
 * @returns Metadata object.
 */
export async function generateMetadata({ params }: ExperienceDetailPageProps): Promise<Metadata> {
  const { slug } = await params;
  try {
    const experience = await fetchExperienceBySlug(slug);
    const siteUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://echtgut.de';

    return {
      title: `${experience.title} | echtgut.de`,
      description: experience.description.slice(0, 160),
      openGraph: {
        title: experience.title,
        description: experience.description.slice(0, 160),
        url: `${siteUrl}/experience/${experience.slug}`,
        siteName: 'echtgut.de',
        images: [
          {
            url: experience.heroImageUrl,
            width: 1200,
            height: 630,
            alt: experience.title,
          },
        ],
        type: 'article',
      },
      twitter: {
        card: 'summary_large_image',
        title: experience.title,
        description: experience.description.slice(0, 160),
        images: [experience.heroImageUrl],
      },
    };
  } catch {
    return {
      title: 'Experience | echtgut.de',
      description: 'Pristine curated local experience on echtgut.de',
    };
  }
}

/**
 * Dynamic public experience detail page template (`/experience/[slug]`).
 *
 * Server-rendered with SSG/ISR support, rendering the pristine editorial narrative,
 * hero image overlay, taxonomy tags, JSON-LD schema, and PracticalInfoPanel facts block.
 */
export default async function ExperienceDetailPage({
  params,
}: Readonly<ExperienceDetailPageProps>) {
  const { slug } = await params;

  let experience;
  try {
    experience = await fetchExperienceBySlug(slug);
  } catch {
    notFound();
  }

  if (!experience) {
    notFound();
  }

  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'TouristAttraction',
    name: experience.title,
    description: experience.description,
    image: experience.heroImageUrl,
    address: {
      '@type': 'PostalAddress',
      streetAddress: experience.address,
    },
    geo: {
      '@type': 'GeoCoordinates',
      latitude: experience.lat,
      longitude: experience.lng,
    },
    url: `${process.env.NEXT_PUBLIC_SITE_URL || 'https://echtgut.de'}/experience/${experience.slug}`,
  };

  return (
    <main className="min-h-screen bg-stone-950 pb-16 text-stone-100">
      {/* JSON-LD Schema.org Structured Data */}
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      {/* Header Bar */}
      <header className="sticky top-0 z-40 border-b border-stone-800/80 bg-stone-950/80 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <Link
            href="/"
            className="text-xl font-bold text-stone-100 transition hover:text-emerald-400"
          >
            echtgut<span className="text-emerald-400">.de</span>
          </Link>
          <Link
            href="/"
            className="text-xs font-semibold text-stone-400 transition hover:text-stone-200"
          >
            ← Back to Experiences
          </Link>
        </div>
      </header>

      {/* Hero Image Section */}
      <section className="relative h-96 w-full overflow-hidden bg-stone-900">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={experience.heroImageUrl}
          alt={experience.title}
          className="h-full w-full object-cover opacity-80"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-stone-950 via-stone-950/40 to-transparent" />
        <div className="absolute bottom-0 left-0 right-0 mx-auto max-w-7xl space-y-3 px-6 pb-8">
          <div className="flex flex-wrap gap-2">
            {experience.tags.map((tag) => (
              <span
                key={tag}
                className="rounded-full border border-emerald-500/30 bg-emerald-500/20 px-3 py-1 text-xs font-bold text-emerald-400 backdrop-blur-md"
              >
                {tag}
              </span>
            ))}
          </div>
          <h1 className="text-3xl font-extrabold text-stone-50 sm:text-4xl md:text-5xl">
            {experience.title}
          </h1>
          <p className="text-sm font-semibold text-stone-300">{experience.address}</p>
        </div>
      </section>

      {/* Content Grid */}
      <section className="mx-auto grid max-w-7xl grid-cols-1 gap-12 px-6 pt-10 lg:grid-cols-12">
        {/* Left: Editorial Narrative */}
        <div className="space-y-6 lg:col-span-8">
          <div className="space-y-4 rounded-2xl border border-stone-800 bg-stone-900/40 p-8 shadow-xl">
            <h2 className="text-xl font-bold text-stone-100">The Experience</h2>
            <p className="whitespace-pre-line font-serif text-base leading-relaxed text-stone-300">
              {experience.description}
            </p>
          </div>
        </div>

        {/* Right: PracticalInfoPanel Facts Block */}
        <div className="lg:col-span-4">
          <PracticalInfoPanel
            address={experience.address}
            priceHint="Pristine Experience"
            bookingLabel="Book Experience"
            bookingHref={experience.affiliateUrl || '#'}
          />
        </div>
      </section>
    </main>
  );
}
