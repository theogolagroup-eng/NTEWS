"use client";

import React, { useEffect, useRef, useState } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Circle,
  useMap,
} from "react-leaflet";
import { Icon, LatLngBounds } from "leaflet";
import { Badge, Tooltip, Button, Space } from "antd";
import {
  ExclamationCircleOutlined,
  FireOutlined,
  EyeOutlined,
} from "@ant-design/icons";
import "leaflet/dist/leaflet.css";

// Fix Leaflet default icon issue
delete (Icon.Default.prototype as any)._getIconUrl;
Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.0/images/marker-icon-2x.png",
  iconUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.0/images/marker-icon.png",
  shadowUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.0/images/marker-shadow.png",
});

export interface ThreatPoint {
  id: string;
  latitude: number;
  longitude: number;
  title: string;
  threatLevel: "critical" | "high" | "medium" | "low";
  threatScore: number;
  timestamp: string;
  location: string;
  category: string;
}

interface Hotspot {
  id: string;
  latitude: number;
  longitude: number;
  locationName: string;
  probability: number;
  severity: "critical" | "high" | "medium" | "low";
  threatType: string;
  radius: number;
}

interface ThreatMapProps {
  threats?: ThreatPoint[];
  hotspots?: Hotspot[];
  onThreatClick?: (threat: ThreatPoint) => void;
  onHotspotClick?: (hotspot: Hotspot) => void;
}

// Component to auto-fit map to markers
const MapBoundsUpdater: React.FC<{
  threats: ThreatPoint[];
  hotspots: Hotspot[];
}> = ({ threats, hotspots }) => {
  const map = useMap();

  useEffect(() => {
    if (threats.length === 0 && hotspots.length === 0) return;

    const bounds = new LatLngBounds([]);

    threats.forEach((threat) => {
      const point = [threat.latitude, threat.longitude] as [number, number];
      bounds.extend(point);
    });

    hotspots.forEach((hotspot) => {
      const point = [hotspot.latitude, hotspot.longitude] as [number, number];
      bounds.extend(point);
    });

    if (bounds.isValid()) {
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [threats, hotspots, map]);

  return null;
};

export default function ThreatMap({
  threats = [],
  hotspots = [],
  onThreatClick,
  onHotspotClick,
}: ThreatMapProps) {
  const [mapCenter, setMapCenter] = useState<[number, number]>([
    0.0236, 37.9062,
  ]); // Center of Kenya
  const [mapZoom, setMapZoom] = useState(6); // Zoom level to show most of Kenya
  const [lastUpdate, setLastUpdate] = useState(new Date());

  // Use only real data - no mock data
  const displayThreats = threats;
  const displayHotspots = hotspots;

  const getThreatIcon = (threatLevel: string) => {
    const iconColors = {
      critical: "#ff4d4f",
      high: "#ff7a45",
      medium: "#ffa940",
      low: "#52c41a",
    };

    return new Icon({
      iconUrl: `data:image/svg+xml;base64,${btoa(`
        <svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
          <circle cx="16" cy="16" r="12" fill="${iconColors[threatLevel as keyof typeof iconColors]}" stroke="#fff" stroke-width="2"/>
          <text x="16" y="20" text-anchor="middle" fill="white" font-size="12" font-weight="bold">!</text>
        </svg>
      `)}`,
      iconSize: [32, 32],
      iconAnchor: [16, 32],
      popupAnchor: [0, -32],
    });
  };

  const getHotspotColor = (severity: string) => {
    const colors = {
      critical: "#ff4d4f",
      high: "#ff7a45",
      medium: "#ffa940",
      low: "#52c41a",
    };
    return colors[severity as keyof typeof colors] || "#1890ff";
  };

  const getSeverityBadgeColor = (severity: string) => {
    const colors = {
      critical: "red",
      high: "orange",
      medium: "gold",
      low: "green",
    };
    return colors[severity as keyof typeof colors] || "default";
  };

  return (
    <div style={{ height: "100%", width: "100%", position: "relative" }}>
      <MapContainer
        center={mapCenter}
        zoom={mapZoom}
        style={{ height: "100%", width: "100%" }}
        zoomControl={true}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />

        <MapBoundsUpdater threats={displayThreats} hotspots={displayHotspots} />

        {/* Render threat markers */}
        {displayThreats.map((threat) => (
          <Marker
            key={threat.id}
            position={[threat.latitude, threat.longitude]}
            icon={getThreatIcon(threat.threatLevel)}
            eventHandlers={{
              click: () => onThreatClick?.(threat),
            }}
          >
            <Popup>
              <div style={{ 
                minWidth: "280px",
                background: "#ffffff",
                borderRadius: "8px",
                padding: "16px",
                boxShadow: "0 4px 12px rgba(0,0,0,0.15)"
              }}>
                <h4 style={{ 
                  margin: "0 0 12px 0", 
                  fontSize: "16px",
                  fontWeight: "bold",
                  color: "#1a1a1a",
                  lineHeight: "1.3"
                }}>{threat.title}</h4>
                <Space
                  direction="vertical"
                  size="small"
                  style={{ width: "100%" }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    <Badge
                      color={getSeverityBadgeColor(threat.threatLevel)}
                      text={threat.threatLevel.toUpperCase()}
                      style={{ fontSize: "11px", fontWeight: "bold" }}
                    />
                    <span style={{ 
                      fontSize: "13px", 
                      fontWeight: "500",
                      color: "#333"
                    }}>
                      Score: {(threat.threatScore * 100).toFixed(1)}%
                    </span>
                  </div>
                  <div>
                    <strong style={{ color: "#1a1a1a", fontSize: "13px" }}>Location:</strong> 
                    <span style={{ color: "#555", fontSize: "13px", marginLeft: "4px" }}>
                      {threat.location}
                    </span>
                  </div>
                  <div>
                    <strong style={{ color: "#1a1a1a", fontSize: "13px" }}>Category:</strong> 
                    <span style={{ color: "#555", fontSize: "13px", marginLeft: "4px" }}>
                      {threat.category}
                    </span>
                  </div>
                  <div>
                    <strong style={{ color: "#1a1a1a", fontSize: "13px" }}>Time:</strong>{" "}
                    <span style={{ color: "#555", fontSize: "12px" }}>
                      {new Date(threat.timestamp).toLocaleString()}
                    </span>
                  </div>
                  <Button
                    type="primary"
                    size="small"
                    icon={<EyeOutlined />}
                    onClick={() => onThreatClick?.(threat)}
                    style={{ marginTop: "8px", fontWeight: "bold" }}
                  >
                    View Details
                  </Button>
                </Space>
              </div>
            </Popup>
          </Marker>
        ))}

        {/* Render hotspot circles */}
        {displayHotspots.map((hotspot) => (
          <Circle
            key={hotspot.id}
            center={[hotspot.latitude, hotspot.longitude]}
            radius={hotspot.radius}
            pathOptions={{
              color: getHotspotColor(hotspot.severity),
              fillColor: getHotspotColor(hotspot.severity),
              fillOpacity: 0.2,
              weight: 2,
            }}
            eventHandlers={{
              click: () => onHotspotClick?.(hotspot),
            }}
          >
            <Popup>
              <div style={{ 
                minWidth: "280px",
                background: "#ffffff",
                borderRadius: "8px",
                padding: "16px",
                boxShadow: "0 4px 12px rgba(0,0,0,0.15)"
              }}>
                <h4 style={{ 
                  margin: "0 0 12px 0",
                  fontSize: "16px",
                  fontWeight: "bold",
                  color: "#1a1a1a",
                  lineHeight: "1.3",
                  display: "flex",
                  alignItems: "center",
                  gap: "8px"
                }}>
                  <FireOutlined
                    style={{
                      color: getHotspotColor(hotspot.severity),
                      fontSize: "18px",
                    }}
                  />
                  {hotspot.locationName}
                </h4>
                <Space
                  direction="vertical"
                  size="small"
                  style={{ width: "100%" }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    <Badge
                      color={getSeverityBadgeColor(hotspot.severity)}
                      text={hotspot.severity.toUpperCase()}
                      style={{ fontSize: "11px", fontWeight: "bold" }}
                    />
                    <span style={{ 
                      fontSize: "13px", 
                      fontWeight: "500",
                      color: "#333"
                    }}>
                      Risk: {(hotspot.probability * 100).toFixed(1)}%
                    </span>
                  </div>
                  <div>
                    <strong style={{ color: "#1a1a1a", fontSize: "13px" }}>Threat Type:</strong> 
                    <span style={{ color: "#555", fontSize: "13px", marginLeft: "4px" }}>
                      {hotspot.threatType}
                    </span>
                  </div>
                  <div>
                    <strong style={{ color: "#1a1a1a", fontSize: "13px" }}>Radius:</strong> 
                    <span style={{ color: "#555", fontSize: "13px", marginLeft: "4px" }}>
                      {hotspot.radius}m
                    </span>
                  </div>
                  <Button
                    type="primary"
                    size="small"
                    icon={<EyeOutlined />}
                    onClick={() => onHotspotClick?.(hotspot)}
                    style={{ marginTop: "8px", fontWeight: "bold" }}
                  >
                    View Hotspot
                  </Button>
                </Space>
              </div>
            </Popup>
          </Circle>
        ))}
      </MapContainer>
    </div>
  );
}
