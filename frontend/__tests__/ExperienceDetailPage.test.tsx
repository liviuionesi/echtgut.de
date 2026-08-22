import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import ExperienceDetailPage, { generateMetadata } from '../app/experience/[slug]/page';
import * as api from '../lib/api';

vi.mock('../lib/api', () => ({
  fetchExperienceBySlug: vi.fn(),
}));

const mockDetail: api.PublicExperienceDetail = {
  id: 'exp-100',
  slug: 'bio-brotgarten-berlin',
  title: 'Bio Brotgarten Berlin',
  description: 'Fresh organic sourdough bread baked daily.',
  heroImageUrl: 'https://example.com/brot.jpg',
  address: 'Kastanienallee 12, Berlin',
  lat: 52.535,
  lng: 13.408,
  affiliateUrl: 'https://partner.com/brot',
  bookingContact: 'contact@brotgarten.de',
  publishedAt: '2026-08-22T10:00:00Z',
  tags: ['feinschmecker', 'auszeit'],
};

describe('ExperienceDetailPage', () => {
  it('renders experience details, title, tags, and practical info panel', async () => {
    vi.mocked(api.fetchExperienceBySlug).mockResolvedValue(mockDetail);

    const jsx = await ExperienceDetailPage({
      params: Promise.resolve({ slug: 'bio-brotgarten-berlin' }),
    });
    render(jsx);

    expect(
      screen.getByRole('heading', { level: 1, name: 'Bio Brotgarten Berlin' }),
    ).toBeInTheDocument();
    expect(screen.getByText('Fresh organic sourdough bread baked daily.')).toBeInTheDocument();
    expect(screen.getByText('feinschmecker')).toBeInTheDocument();
    expect(screen.getByText('auszeit')).toBeInTheDocument();
  });

  it('generates OpenGraph metadata correctly', async () => {
    vi.mocked(api.fetchExperienceBySlug).mockResolvedValue(mockDetail);

    const metadata = await generateMetadata({
      params: Promise.resolve({ slug: 'bio-brotgarten-berlin' }),
    });

    expect(metadata.title).toBe('Bio Brotgarten Berlin | echtgut.de');
    expect(metadata.openGraph?.title).toBe('Bio Brotgarten Berlin');
  });
});
