'use client';

import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { PlaceDto } from '@/lib/api';
import { Star } from 'lucide-react';

interface LocationMapProps {
  places?: PlaceDto[];
  lat?: number;
  lng?: number;
  height?: string;
}

const customIcon = L.divIcon({
  className: 'custom-map-marker',
  html: `
    <div style="
      width: 24px;
      height: 24px;
      background-color: #10b981;
      border: 3px solid white;
      border-radius: 50%;
      box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
      transform: translate(-12px, -12px);
    "></div>
  `,
});

export default function LocationMap({
  places = [],
  lat = 48.7758,
  lng = 9.1829,
  height = 'h-48',
}: Readonly<LocationMapProps>) {
  // Center is Stuttgart by default, or the first place's coordinates, or the provided lat/lng
  const centerLat = places.length > 0 ? places[0].lat : lat;
  const centerLng = places.length > 0 ? places[0].lon : lng;

  return (
    <div
      className={`${height} relative z-0 w-full overflow-hidden rounded-xl border border-border shadow-inner`}
    >
      <MapContainer
        center={[centerLat, centerLng]}
        zoom={places.length > 0 ? 12 : 15}
        scrollWheelZoom={true}
        className="h-full w-full"
      >
        <TileLayer
          attribution="&copy; OpenStreetMap"
          url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
        />

        {places.map((place) => (
          <Marker key={place.id} position={[place.lat, place.lon]} icon={customIcon}>
            <Popup className="custom-popup">
              <div className="w-48 overflow-hidden rounded-lg">
                <div className="relative h-24 w-full">
                  <img
                    src={place.imageUrl}
                    alt={place.name}
                    className="h-full w-full object-cover"
                  />
                </div>
                <div className="bg-bg p-3 text-fg">
                  <h4 className="mb-1 text-sm font-bold">{place.name}</h4>
                  <p className="mb-2 line-clamp-2 text-xs text-fg-muted">{place.category}</p>
                  {place.rating !== undefined && (
                    <div className="flex items-center gap-1 text-xs">
                      <Star className="h-3 w-3 fill-yellow-500 text-yellow-500" />
                      <span>{place.rating.toFixed(1)}</span>
                      {place.reviewCount !== undefined && (
                        <span className="text-fg-muted">({place.reviewCount})</span>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </Popup>
          </Marker>
        ))}

        {places.length === 0 && <Marker position={[centerLat, centerLng]} icon={customIcon} />}
      </MapContainer>
    </div>
  );
}
