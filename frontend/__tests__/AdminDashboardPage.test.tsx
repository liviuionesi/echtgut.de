import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import AdminDashboardPage from '../app/(admin)/admin/page';

describe('AdminDashboardPage', () => {
  it('renders curator admin portal title', () => {
    render(<AdminDashboardPage />);
    expect(screen.getByText('Curator Admin Portal')).toBeInTheDocument();
    expect(
      screen.getByText('Airlock queue initialized. Pending raw deal reviews ready.')
    ).toBeInTheDocument();
  });
});
