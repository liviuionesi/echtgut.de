'use client';

import React from 'react';
import dynamic from 'next/dynamic';

const DynamicLocationMap = dynamic(() => import('@/components/catalog/location-map'), {
  ssr: false,
  loading: () => (
    <div
      data-testid="mock-location-map"
      className="flex h-48 w-full items-center justify-center rounded-lg border border-border bg-stone-900/50"
    >
      <div className="h-6 w-6 animate-spin rounded-full border-2 border-emerald-500 border-t-transparent" />
    </div>
  ),
});

interface LocationMapWrapperProps {
  lat: number;
  lng: number;
}

export function LocationMapWrapper({ lat, lng }: Readonly<LocationMapWrapperProps>) {
  return <DynamicLocationMap lat={lat} lng={lng} />;
}
