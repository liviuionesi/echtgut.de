import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import AdminDashboardPage from '../app/admin/page';

vi.mock('../lib/api', () => ({
  fetchNextPendingDeal: vi.fn().mockResolvedValue(null),
  promoteDeal: vi.fn(),
  rejectDeal: vi.fn(),
  fetchAdminTags: vi.fn().mockResolvedValue([]),
  createAdminTag: vi.fn(),
}));

/**
 * Test suite for the curator admin portal dashboard (`AdminDashboardPage`).
 *
 * Verifies that administrative portal headers, badges, and review status messages render.
 */
describe('AdminDashboardPage', () => {
  /**
   * Verifies that the curator portal title and airlock status badge render properly.
   */
  it('renders curator admin portal title and airlock badge', () => {
    // 1. Given the curator admin page is rendered
    render(<AdminDashboardPage />);

    // 2. When inspecting the portal DOM
    // 3. Then the admin header and airlock status badge must be present
    expect(
      screen.getByRole('heading', { level: 1, name: 'Curator Admin Portal' }),
    ).toBeInTheDocument();
    expect(screen.getByText('Curator Airlock Active')).toBeInTheDocument();
  });
});
