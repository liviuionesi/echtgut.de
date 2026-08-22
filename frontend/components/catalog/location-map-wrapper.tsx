'use client';

import React from 'react';
import dynamic from 'next/dynamic';
import { PlaceDto } from '@/lib/api';

const DynamicLocationMap = dynamic(() => import('@/components/catalog/location-map'), {
  ssr: false,
  loading: () => (
    <div
      data-testid="mock-location-map"
      className="flex h-48 w-full items-center justify-center rounded-xl border border-border bg-stone-900/50"
    >
      <div className="h-6 w-6 animate-spin rounded-full border-2 border-emerald-500 border-t-transparent" />
    </div>
  ),
});

interface LocationMapWrapperProps {
  places?: PlaceDto[];
  lat?: number;
  lng?: number;
  height?: string;
}

export function LocationMapWrapper(props: Readonly<LocationMapWrapperProps>) {
  return <DynamicLocationMap {...props} />;
}
