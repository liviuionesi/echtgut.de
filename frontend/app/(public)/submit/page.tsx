'use client';

import React, { useState } from 'react';
import { submitLocalGem } from '@/lib/api';

export default function SubmitLocalGemPage() {
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<'IDLE' | 'SUBMITTING' | 'SUCCESS' | 'ERROR'>('IDLE');
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatus('SUBMITTING');
    setErrorMessage('');

    try {
      await submitLocalGem({ name, address, description });
      setStatus('SUCCESS');
      setName('');
      setAddress('');
      setDescription('');
    } catch (err: any) {
      setStatus('ERROR');
      setErrorMessage(err.message || 'Ein Fehler ist aufgetreten.');
    }
  };

  return (
    <main className="min-h-screen bg-bg py-16 text-fg">
      <div className="container mx-auto max-w-2xl px-4">
        <h1 className="mb-8 font-display text-4xl font-semibold text-fg">Geheimtipp einreichen</h1>
        <p className="mb-8 text-lg text-fg-muted">
          Kennst du einen Ort, der auf echtgut.de nicht fehlen darf? Teile ihn mit unserer
          Redaktion!
        </p>

        {status === 'SUCCESS' && (
          <div className="mb-8 rounded-lg border border-emerald-500/20 bg-emerald-500/10 p-4 text-emerald-500">
            Vielen Dank! Dein Geheimtipp wurde erfolgreich eingereicht und wird von unserer
            Redaktion geprüft.
          </div>
        )}

        {status === 'ERROR' && (
          <div className="mb-8 rounded-lg border border-red-500/20 bg-red-500/10 p-4 text-red-500">
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="name" className="mb-2 block text-sm font-medium">
              Name des Ortes
            </label>
            <input
              type="text"
              id="name"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded-md border border-border bg-bg-elevated px-4 py-2 text-fg focus:border-accent focus:outline-none"
              placeholder="z.B. Bio-Bäckerei Brotgarten"
            />
          </div>

          <div>
            <label htmlFor="address" className="mb-2 block text-sm font-medium">
              Adresse / Ort
            </label>
            <input
              type="text"
              id="address"
              required
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              className="w-full rounded-md border border-border bg-bg-elevated px-4 py-2 text-fg focus:border-accent focus:outline-none"
              placeholder="z.B. Kastanienallee 12, Berlin"
            />
          </div>

          <div>
            <label htmlFor="description" className="mb-2 block text-sm font-medium">
              Warum ist das ein Geheimtipp?
            </label>
            <textarea
              id="description"
              required
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full rounded-md border border-border bg-bg-elevated px-4 py-2 text-fg focus:border-accent focus:outline-none"
              placeholder="Beschreibe kurz, was diesen Ort besonders macht..."
            />
          </div>

          <button
            type="submit"
            disabled={status === 'SUBMITTING'}
            className="rounded-md bg-accent px-6 py-3 font-semibold text-white transition-colors hover:bg-accent/90 disabled:opacity-50"
          >
            {status === 'SUBMITTING' ? 'Wird eingereicht...' : 'Geheimtipp einreichen'}
          </button>
        </form>
      </div>
    </main>
  );
}
