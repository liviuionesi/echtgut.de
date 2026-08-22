'use client';

import React from 'react';
import { MapContainer, TileLayer, Marker } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

interface LocationMapProps {
  lat: number;
  lng: number;
}

// Create a custom SVG divIcon for a clean, brand-aligned pin.
// Prevents standard Leaflet PNG-loading issues in Next.js.
const customIcon = L.divIcon({
  className: 'custom-map-marker',
  html: `
    <div style="
      width: 24px;
      height: 24px;
      background-color: #10b981; /* emerald-500 */
      border: 3px solid white;
      border-radius: 50%;
      box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
      transform: translate(-12px, -12px);
    "></div>
  `,
});

/**
 * A free-tier OpenStreetMap Leaflet map.
 * Must be dynamically imported with `ssr: false` since Leaflet relies on the DOM.
 */
export default function LocationMap({ lat, lng }: Readonly<LocationMapProps>) {
  return (
    <div className="h-48 w-full overflow-hidden rounded-lg border border-border">
      <MapContainer center={[lat, lng]} zoom={15} scrollWheelZoom={false} className="h-full w-full">
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Marker position={[lat, lng]} icon={customIcon} />
      </MapContainer>
    </div>
  );
}
