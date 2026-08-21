import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import AdminDashboardPage from '../app/admin/page';

/**
 * Test suite for the curator admin portal dashboard (`AdminDashboardPage`).
 *
 * Verifies that administrative portal headers, badges, and review status messages render.
 */
describe('AdminDashboardPage', () => {
  /**
   * Verifies that the curator portal title and initial queue state render properly.
   */
  it('renders curator admin portal title', () => {
    // 1. Given the curator admin page is rendered
    render(<AdminDashboardPage />);

    // 2. When inspecting the portal DOM
    // 3. Then the admin header and airlock status text must be present
    expect(
      screen.getByRole('heading', { level: 1, name: 'Curator Admin Portal' }),
    ).toBeInTheDocument();
    expect(
      screen.getByText('Airlock queue initialized. Pending raw deal reviews ready.'),
    ).toBeInTheDocument();
  });
});
