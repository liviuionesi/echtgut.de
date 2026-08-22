import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { SingleCardReview } from '../components/admin/SingleCardReview';
import * as api from '../lib/api';

vi.mock('../lib/api', () => ({
  fetchNextPendingDeal: vi.fn(),
  promoteDeal: vi.fn(),
  rejectDeal: vi.fn(),
  fetchAdminTags: vi.fn(),
  createAdminTag: vi.fn(),
}));

const mockDeal1: api.RawDealResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  source: 'AFFILIATE_FEED',
  sourceRef: 'REF-001',
  rawTitle: 'Raw Thermal Bath Ticket',
  rawDescription: 'Raw description for thermal bath.',
  rawImageUrl: 'https://example.com/bath.jpg',
  locationText: 'Thermenallee 1, Berlin',
  lat: 52.52,
  lng: 13.405,
  priceHint: '€25',
  status: 'PENDING',
};

const mockDeal2: api.RawDealResponse = {
  id: '22222222-2222-2222-2222-222222222222',
  source: 'COMMUNITY_SUBMISSION',
  sourceRef: 'REF-002',
  rawTitle: 'Obscure Sauna Experience',
  rawDescription: 'Raw description for sauna.',
  rawImageUrl: 'https://example.com/sauna.jpg',
  locationText: 'Saunaweg 4, Hamburg',
  lat: 53.55,
  lng: 9.993,
  priceHint: '€18',
  status: 'PENDING',
};

const mockTags: api.TagResponse[] = [
  { id: 'tag-1', slug: 'auszeit', name: 'Auszeit', category: 'MOOD', isRetired: false },
  { id: 'tag-2', slug: 'romantik', name: 'Romantik', category: 'MOOD', isRetired: false },
];

describe('SingleCardReview', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(api.fetchAdminTags).mockResolvedValue(mockTags);
  });

  it('renders empty queue state when no pending deal is returned', async () => {
    vi.mocked(api.fetchNextPendingDeal).mockResolvedValueOnce(null);

    render(<SingleCardReview />);

    await waitFor(() => {
      expect(screen.getByText('Queue Empty')).toBeInTheDocument();
    });
  });

  it('renders raw deal candidate and pre-fills editorial refinement form', async () => {
    vi.mocked(api.fetchNextPendingDeal).mockResolvedValueOnce(mockDeal1);

    render(<SingleCardReview />);

    await waitFor(() => {
      expect(screen.getByText('Raw Thermal Bath Ticket')).toBeInTheDocument();
    });

    expect(screen.getByText('Source: AFFILIATE_FEED')).toBeInTheDocument();
    expect(screen.getAllByText('Raw description for thermal bath.')[0]).toBeInTheDocument();

    const titleInput = screen.getByDisplayValue('Raw Thermal Bath Ticket');
    const addressInput = screen.getByDisplayValue('Thermenallee 1, Berlin');

    expect(titleInput).toBeInTheDocument();
    expect(addressInput).toBeInTheDocument();
  });

  it('calls promoteDeal and advances queue on Promote button click', async () => {
    vi.mocked(api.fetchNextPendingDeal)
      .mockResolvedValueOnce(mockDeal1)
      .mockResolvedValueOnce(mockDeal2);
    vi.mocked(api.promoteDeal).mockResolvedValueOnce({
      id: 'exp-1',
      rawDealId: mockDeal1.id,
      slug: 'raw-thermal-bath-ticket',
      title: 'Polished Thermal Spa',
      description: 'Polished description',
      heroImageUrl: 'https://example.com/bath.jpg',
      address: 'Thermenallee 1, Berlin',
      lat: 52.52,
      lng: 13.405,
      isPublished: true,
    });

    render(<SingleCardReview />);

    await waitFor(() => {
      expect(screen.getByText('Raw Thermal Bath Ticket')).toBeInTheDocument();
    });

    const promoteButton = screen.getByRole('button', { name: /Promote to Public Catalog/i });
    fireEvent.click(promoteButton);

    await waitFor(() => {
      expect(api.promoteDeal).toHaveBeenCalledWith(
        mockDeal1.id,
        expect.objectContaining({
          editorialTitle: 'Raw Thermal Bath Ticket',
          editorialDescription: 'Raw description for thermal bath.',
          heroImageUrl: 'https://example.com/bath.jpg',
          address: 'Thermenallee 1, Berlin',
          lat: 52.52,
          lng: 13.405,
          isPublished: true,
          tags: ['auszeit'],
        }),
      );
    });

    await waitFor(() => {
      expect(screen.getByText('Obscure Sauna Experience')).toBeInTheDocument();
    });
  });

  it('calls rejectDeal and advances queue on Confirm Rejection click', async () => {
    vi.mocked(api.fetchNextPendingDeal)
      .mockResolvedValueOnce(mockDeal1)
      .mockResolvedValueOnce(null);
    vi.mocked(api.rejectDeal).mockResolvedValueOnce({
      ...mockDeal1,
      status: 'REJECTED',
      rejectionReason: 'Low quality listing',
    });

    render(<SingleCardReview />);

    await waitFor(() => {
      expect(screen.getByText('Raw Thermal Bath Ticket')).toBeInTheDocument();
    });

    const rejectFormButton = screen.getByRole('button', { name: /Reject Candidate/i });
    fireEvent.click(rejectFormButton);

    const reasonTextarea = screen.getByLabelText(/Reason for rejection/i);
    fireEvent.change(reasonTextarea, { target: { value: 'Low quality listing' } });

    const confirmRejectButton = screen.getByRole('button', { name: /Confirm Rejection/i });
    fireEvent.click(confirmRejectButton);

    await waitFor(() => {
      expect(api.rejectDeal).toHaveBeenCalledWith(mockDeal1.id, {
        reason: 'Low quality listing',
      });
    });

    await waitFor(() => {
      expect(screen.getByText('Queue Empty')).toBeInTheDocument();
    });
  });
});
