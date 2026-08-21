'use client';

import React from 'react';

export default function AdminDashboardPage() {
  return (
    <div className="p-8 max-w-7xl mx-auto">
      <header className="flex justify-between items-center mb-8 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-bold text-white">Curator Admin Portal</h1>
          <p className="text-slate-400 text-sm">Review, refine, and promote incoming deal submissions</p>
        </div>
        <div className="px-3 py-1 bg-amber-500/10 text-amber-400 border border-amber-500/20 text-xs font-semibold rounded">
          Curator Airlock Active
        </div>
      </header>

      <div className="bg-slate-800/50 border border-slate-700 rounded-lg p-6 text-center">
        <p className="text-slate-300">Airlock queue initialized. Pending raw deal reviews ready.</p>
      </div>
    </div>
  );
}
