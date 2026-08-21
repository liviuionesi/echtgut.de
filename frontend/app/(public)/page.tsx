import React from 'react';
import { ThemeToggle } from '@/components/theme-toggle';

export default function PublicHomePage() {
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
    </main>
  );
}
