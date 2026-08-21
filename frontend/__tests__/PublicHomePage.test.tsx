import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import PublicHomePage from '../app/(public)/page';

describe('PublicHomePage', () => {
  it('renders the headline and description', () => {
    render(<PublicHomePage />);
    expect(screen.getByText('echtgut.de')).toBeInTheDocument();
    expect(
      screen.getByText('Handverlesene, geprüfte lokale Geheimtipps und Erlebnisse.')
    ).toBeInTheDocument();
  });
});
