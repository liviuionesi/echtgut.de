import React from 'react';

export default function PublicHomePage() {
  return (
    <main className="container mx-auto px-4 py-16 text-center">
      <div className="inline-block px-4 py-1 mb-6 rounded-full bg-emerald-500/10 text-emerald-400 text-sm font-medium border border-emerald-500/20">
        Echt & Kuratiert
      </div>
      <h1 className="text-4xl md:text-6xl font-bold tracking-tight text-white mb-6">
        echtgut.de
      </h1>
      <p className="text-lg md:text-xl text-slate-400 max-w-2xl mx-auto mb-8">
        Handverlesene, geprüfte lokale Geheimtipps und Erlebnisse.
      </p>
    </main>
  );
}
