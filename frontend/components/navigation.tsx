'use client';

import React from 'react';
import Link from 'next/link';
import { Compass, Map, Heart, Search } from 'lucide-react';
import { usePathname } from 'next/navigation';

export function Navigation() {
  const pathname = usePathname();

  const navItems = [
    { label: 'Entdecken', href: '/', icon: Compass },
    { label: 'Karte', href: '/map', icon: Map },
    { label: 'Top Bewertet', href: '/top', icon: Heart },
  ];

  return (
    <nav className="fixed left-0 right-0 top-0 z-50 flex items-center justify-between border-b border-border bg-bg/80 px-6 py-4 backdrop-blur-xl transition-all">
      <Link href="/" className="flex items-center gap-2">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent">
          <Compass className="h-5 w-5 text-white" />
        </div>
        <span className="font-display text-xl font-bold tracking-tight text-fg">echtgut.de</span>
      </Link>

      <div className="hidden items-center gap-8 md:flex">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-2 text-sm font-medium transition-colors ${
                isActive ? 'text-accent' : 'text-fg-muted hover:text-fg'
              }`}
            >
              <Icon className="h-4 w-4" />
              {item.label}
            </Link>
          );
        })}
      </div>

      <div className="flex items-center gap-4">
        <button className="rounded-full p-2 text-fg-muted transition-colors hover:bg-bg-elevated hover:text-fg">
          <Search className="h-5 w-5" />
        </button>
      </div>
    </nav>
  );
}
