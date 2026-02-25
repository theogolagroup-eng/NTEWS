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

    const bounds = new LatLngBounds();

    threats.forEach((threat) => {
      bounds.extend([threat.latitude, threat.longitude]);
    });

    hotspots.forEach((hotspot) => {
      bounds.extend([hotspot.latitude, hotspot.longitude]);
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
    -1.2921, 36.8219,
  ]); // Nairobi
  const [mapZoom, setMapZoom] = useState(11);

  // Mock data for demonstration
  const mockThreats: ThreatPoint[] = [
    {
      id: "1",
      latitude: -1.2921,
      longitude: 36.8219,
      title: "Suspicious Activity - Nairobi CBD",
      threatLevel: "high",
      threatScore: 0.75,
      timestamp: new Date().toISOString(),
      location: "Nairobi CBD, Kenya",
      category: "social_unrest",
    },
    {
      id: "2",
      latitude: -1.2864,
      longitude: 36.8172,
      title: "Cyber Threat Alert - Banking Sector",
      threatLevel: "medium",
      threatScore: 0.65,
      timestamp: new Date().toISOString(),
      location: "Westlands, Nairobi",
      category: "cyber",
    },
    {
      id: "3",
      latitude: -1.3032,
      longitude: 36.8225,
      title: "CCTV Anomaly Detection",
      threatLevel: "medium",
      threatScore: 0.58,
      timestamp: new Date().toISOString(),
      location: "Industrial Area, Nairobi",
      category: "criminal",
    },
  ];

  const mockHotspots: Hotspot[] = [
    {
      id: "h1",
      latitude: -1.2921,
      longitude: 36.8219,
      locationName: "Nairobi CBD",
      probability: 0.75,
      severity: "high",
      threatType: "social_unrest",
      radius: 2000,
    },
    {
      id: "h2",
      latitude: -1.2864,
      longitude: 36.8172,
      locationName: "Westlands",
      probability: 0.45,
      severity: "medium",
      threatType: "criminal",
      radius: 1500,
    },
  ];

  const displayThreats = threats.length > 0 ? threats : mockThreats;
  const displayHotspots = hotspots.length > 0 ? hotspots : mockHotspots;

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
              <div style={{ minWidth: "250px" }}>
                <h4 style={{ margin: "0 0 8px 0" }}>{threat.title}</h4>
                <Space
                  direction="vertical"
                  size="small"
                  style={{ width: "100%" }}
                >
                  <div>
                    <Badge
                      color={getSeverityBadgeColor(threat.threatLevel)}
                      text={threat.threatLevel.toUpperCase()}
                    />
                    <span style={{ marginLeft: "8px" }}>
                      Score: {(threat.threatScore * 100).toFixed(1)}%
                    </span>
                  </div>
                  <div>
                    <strong>Location:</strong> {threat.location}
                  </div>
                  <div>
                    <strong>Category:</strong> {threat.category}
                  </div>
                  <div>
                    <strong>Time:</strong>{" "}
                    {new Date(threat.timestamp).toLocaleString()}
                  </div>
                  <Button
                    type="primary"
                    size="small"
                    icon={<EyeOutlined />}
                    onClick={() => onThreatClick?.(threat)}
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
              <div style={{ minWidth: "250px" }}>
                <h4 style={{ margin: "0 0 8px 0" }}>
                  <FireOutlined
                    style={{
                      color: getHotspotColor(hotspot.severity),
                      marginRight: "8px",
                    }}
                  />
                  {hotspot.locationName}
                </h4>
                <Space
                  direction="vertical"
                  size="small"
                  style={{ width: "100%" }}
                >
                  <div>
                    <Badge
                      color={getSeverityBadgeColor(hotspot.severity)}
                      text={hotspot.severity.toUpperCase()}
                    />
                    <span style={{ marginLeft: "8px" }}>
                      Risk: {(hotspot.probability * 100).toFixed(1)}%
                    </span>
                  </div>
                  <div>
                    <strong>Threat Type:</strong> {hotspot.threatType}
                  </div>
                  <div>
                    <strong>Radius:</strong> {hotspot.radius}m
                  </div>
                  <Button
                    type="primary"
                    size="small"
                    icon={<EyeOutlined />}
                    onClick={() => onHotspotClick?.(hotspot)}
                  >
                    View Hotspot
                  </Button>
                </Space>
              </div>
            </Popup>
          </Circle>
        ))}
      </MapContainer>

      {/* Map Legend */}
      <div
        style={{
          position: "absolute",
          bottom: "20px",
          right: "20px",
          background: "#161b22",
          border: "1px solid #30363d",
          padding: "12px 14px",
          borderRadius: "8px",
          boxShadow: "0 4px 12px rgba(0,0,0,0.5)",
          zIndex: 1001,
          fontSize: "12px",
          color: "#e6edf3",
          minWidth: "150px",
        }}
      >
        <div
          style={{
            fontWeight: 600,
            marginBottom: "8px",
            color: "#e6edf3",
            letterSpacing: "0.02em",
          }}
        >
          Threat Levels
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
          {[
            { color: "#f85149", label: "Critical" },
            { color: "#f0883e", label: "High" },
            { color: "#e3b341", label: "Medium" },
            { color: "#3fb950", label: "Low" },
          ].map(({ color, label }) => (
            <div
              key={label}
              style={{ display: "flex", alignItems: "center", gap: "8px" }}
            >
              <div
                style={{
                  width: "12px",
                  height: "12px",
                  borderRadius: "50%",
                  backgroundColor: color,
                  flexShrink: 0,
                }}
              />
              <span style={{ color: "#c9d1d9" }}>{label}</span>
            </div>
          ))}
        </div>
        <div
          style={{
            fontWeight: 600,
            marginTop: "12px",
            marginBottom: "8px",
            color: "#e6edf3",
            letterSpacing: "0.02em",
          }}
        >
          Hotspots
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <div
            style={{
              width: "16px",
              height: "16px",
              borderRadius: "50%",
              backgroundColor: "#f0883e",
              opacity: 0.4,
              flexShrink: 0,
              border: "1px solid #f0883e",
            }}
          />
          <span style={{ color: "#c9d1d9" }}>Predicted Risk Areas</span>
        </div>
      </div>
    </div>
  );
}
