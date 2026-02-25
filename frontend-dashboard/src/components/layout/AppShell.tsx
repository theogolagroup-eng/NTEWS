"use client";

import React, { useState, useEffect, useCallback } from "react";
import { Dropdown } from "antd";
import {
  DashboardOutlined,
  AlertOutlined,
  ThunderboltOutlined,
  DatabaseOutlined,
  UserOutlined,
  BellOutlined,
  LogoutOutlined,
  SettingOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ReloadOutlined,
  CaretUpOutlined,
  CaretDownOutlined,
  MinusOutlined,
} from "@ant-design/icons";
import { useRouter, usePathname } from "next/navigation";

/* ── Types ─────────────────────────────────────────────────── */
interface ThreatState {
  level: "critical" | "high" | "medium" | "low";
  tpi: number;
  activeAlerts: number;
}

const NAV = [
  {
    href: "/dashboard",
    icon: <DashboardOutlined />,
    label: "Operations Center",
  },
  { href: "/alerts", icon: <AlertOutlined />, label: "Active Alerts" },
  {
    href: "/forecast",
    icon: <ThunderboltOutlined />,
    label: "Predictive Intel",
  },
  { href: "/sources", icon: <DatabaseOutlined />, label: "Data Sources" },
];

const LEVEL_COLOR: Record<string, string> = {
  critical: "var(--red)",
  high: "var(--orange)",
  medium: "var(--yellow)",
  low: "var(--green)",
};

const LEVEL_DIM: Record<string, string> = {
  critical: "var(--red-dim)",
  high: "var(--orange-dim)",
  medium: "var(--yellow-dim)",
  low: "var(--green-dim)",
};

const LEVEL_BORDER: Record<string, string> = {
  critical: "var(--red-border)",
  high: "var(--orange-border)",
  medium: "var(--yellow-border)",
  low: "var(--green-border)",
};

/* ── Live Clock ───────────────────────────────────────────── */
function Clock() {
  const [time, setTime] = useState("");
  const [date, setDate] = useState("");

  useEffect(() => {
    const tick = () => {
      const now = new Date();
      const tz = { timeZone: "Africa/Nairobi" };
      setTime(now.toLocaleTimeString("en-KE", { ...tz, hour12: false }));
      setDate(
        now.toLocaleDateString("en-KE", {
          ...tz,
          weekday: "short",
          day: "2-digit",
          month: "short",
        }),
      );
    };
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, []);

  return (
    <div style={{ textAlign: "right" }}>
      <div className="clock-time" suppressHydrationWarning>
        {time || "--:--:--"}
      </div>
      <div className="clock-date" suppressHydrationWarning>
        {date || ""} · EAT
      </div>
    </div>
  );
}

/* ── AppShell ─────────────────────────────────────────────── */
export default function AppShell({ children }: { children: React.ReactNode }) {
  const [collapsed, setCollapsed] = useState(false);
  const [threat, setThreat] = useState<ThreatState>({
    level: "medium",
    tpi: 65,
    activeAlerts: 12,
  });

  const router = useRouter();
  const pathname = usePathname();

  /* Fetch threat state from backend */
  const fetchThreat = useCallback(async () => {
    try {
      const res = await fetch(
        "http://localhost:8080/api/intelligence/dashboard/summary",
      );
      const data = await res.json();
      const total =
        (data.criticalThreats ?? 0) +
        (data.highThreats ?? 0) +
        (data.mediumThreats ?? 0);
      let level: ThreatState["level"] = "low";
      if (data.criticalThreats > 0) level = "critical";
      else if (data.highThreats > 5) level = "high";
      else if (total > 10) level = "medium";
      setThreat({
        level,
        tpi: Math.min(95, Math.max(5, total * 5 + data.criticalThreats * 20)),
        activeAlerts: data.activeThreats ?? threat.activeAlerts,
      });
    } catch {
      /* offline — keep last state */
    }
  }, []);

  useEffect(() => {
    fetchThreat();
    const id = setInterval(fetchThreat, 30_000);
    return () => clearInterval(id);
  }, [fetchThreat]);

  const levelColor = LEVEL_COLOR[threat.level];
  const levelDim = LEVEL_DIM[threat.level];
  const levelBorder = LEVEL_BORDER[threat.level];

  /* Page title from route */
  const pageTitle =
    NAV.find((n) => pathname?.startsWith(n.href))?.label ?? "Dashboard";

  const userMenu = {
    items: [
      { key: "profile", icon: <UserOutlined />, label: "Profile" },
      { key: "settings", icon: <SettingOutlined />, label: "Settings" },
      { type: "divider" as const },
      {
        key: "logout",
        icon: <LogoutOutlined />,
        label: "Sign out",
        danger: true,
      },
    ],
    onClick: ({ key }: { key: string }) => {
      if (key === "logout") router.push("/login");
    },
  };

  return (
    /* .shell — full viewport, no overflow */
    <div className="shell">
      {/* Restricted banner */}
      <div className="restricted-banner">
        RESTRICTED — AUTHORIZED PERSONNEL ONLY — REPUBLIC OF KENYA
      </div>

      {/* Shell body: sidebar + main column, side by side */}
      <div className="shell-inner">
        {/* ──────────────── SIDEBAR ──────────────────────── */}
        <aside className={`sidebar${collapsed ? " collapsed" : ""}`}>
          {/* Logo */}
          <div className="sb-logo">
            <div className="sb-logo-mark">KE</div>
            {!collapsed && (
              <div className="sb-logo-text">
                <div className="sb-logo-name">NTEWS</div>
                <div className="sb-logo-sub">Republic of Kenya</div>
              </div>
            )}
          </div>

          {/* Threat level strip */}
          {!collapsed && (
            <div
              className="sb-threat"
              style={{ background: levelDim, borderColor: levelBorder }}
            >
              <div
                className={`dot ${threat.level === "critical" ? "dot-red" : threat.level === "low" ? "dot-green" : "dot-yellow"}`}
              />
              <div>
                <div className="sb-threat-level" style={{ color: levelColor }}>
                  {threat.level} threat
                </div>
                <div className="sb-threat-tpi">TPI {threat.tpi} / 100</div>
              </div>
            </div>
          )}

          {/* Navigation */}
          <nav className="sb-nav">
            {NAV.map(({ href, icon, label }) => {
              const active = !!pathname?.startsWith(href);
              return (
                <button
                  key={href}
                  className={`sb-nav-item${active ? " active" : ""}`}
                  onClick={() => router.push(href)}
                  title={collapsed ? label : undefined}
                  style={{
                    justifyContent: collapsed ? "center" : "flex-start",
                  }}
                >
                  <span className="icon">{icon}</span>
                  {!collapsed && label}
                </button>
              );
            })}
          </nav>

          {/* Online indicator */}
          <div
            className="sb-footer"
            style={{ justifyContent: collapsed ? "center" : "flex-start" }}
          >
            <div className="dot dot-green" />
            {!collapsed && (
              <span style={{ fontSize: 11, color: "var(--text-3)" }}>
                All systems live
              </span>
            )}
          </div>
        </aside>

        {/* ──────────────── MAIN COLUMN ──────────────────── */}
        <div className="main-col">
          {/* ── Topbar ─────────────────────────────────── */}
          <header className="topbar">
            {/* Left */}
            <div className="topbar-left">
              {/* Collapse toggle */}
              <button
                className="ibtn"
                onClick={() => setCollapsed((c) => !c)}
                title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
              >
                {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              </button>

              {/* Page title */}
              <div
                style={{
                  fontSize: 15,
                  fontWeight: 600,
                  color: "var(--text-1)",
                  whiteSpace: "nowrap",
                }}
              >
                {pageTitle}
              </div>
            </div>

            {/* Right */}
            <div className="topbar-right">
              {/* Threat level chip */}
              <div
                className="threat-pill"
                style={{
                  color: levelColor,
                  background: levelDim,
                  borderColor: levelBorder,
                }}
              >
                <div
                  className={`dot ${threat.level === "critical" ? "dot-red" : threat.level === "low" ? "dot-green" : "dot-yellow"}`}
                />
                {threat.level.toUpperCase()}
                <span
                  style={{
                    color: "var(--text-3)",
                    fontWeight: 400,
                    fontSize: 10,
                  }}
                >
                  &middot; TPI {threat.tpi}
                </span>
              </div>

              {/* Alert count */}
              {threat.activeAlerts > 0 && (
                <div
                  className="threat-pill"
                  style={{
                    color: "var(--red)",
                    background: "var(--red-dim)",
                    borderColor: "var(--red-border)",
                  }}
                >
                  {threat.activeAlerts} alerts
                </div>
              )}

              <div className="topbar-sep" />

              <button
                className="ibtn"
                onClick={() => window.location.reload()}
                title="Refresh data"
              >
                <ReloadOutlined />
              </button>

              <button
                className="ibtn"
                title="Notifications"
                style={{ position: "relative" }}
              >
                <BellOutlined />
                {threat.activeAlerts > 0 && (
                  <span
                    style={{
                      position: "absolute",
                      top: 6,
                      right: 6,
                      width: 5,
                      height: 5,
                      background: "var(--red)",
                      borderRadius: "50%",
                    }}
                  />
                )}
              </button>

              <div className="topbar-sep" />

              <Clock />

              <div className="topbar-sep" />

              {/* User menu */}
              <Dropdown
                menu={userMenu}
                placement="bottomRight"
                trigger={["click"]}
              >
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 8,
                    cursor: "pointer",
                    padding: "4px 6px",
                    borderRadius: "var(--r-md)",
                    transition: "background 0.12s",
                  }}
                  onMouseEnter={(e) =>
                    ((e.currentTarget as HTMLElement).style.background =
                      "var(--surface-3)")
                  }
                  onMouseLeave={(e) =>
                    ((e.currentTarget as HTMLElement).style.background =
                      "transparent")
                  }
                >
                  <div
                    style={{
                      width: 28,
                      height: 28,
                      background: "var(--green-dim)",
                      border: "1px solid var(--green-border)",
                      borderRadius: "50%",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      color: "var(--green)",
                      fontSize: 12,
                    }}
                  >
                    <UserOutlined />
                  </div>
                  <div style={{ display: "flex", flexDirection: "column" }}>
                    <span
                      style={{
                        fontSize: 12,
                        fontWeight: 600,
                        color: "var(--text-1)",
                        lineHeight: 1,
                      }}
                    >
                      Analyst
                    </span>
                    <span
                      style={{
                        fontSize: 10,
                        color: "var(--text-3)",
                        marginTop: 1,
                      }}
                    >
                      Intel Ops
                    </span>
                  </div>
                </div>
              </Dropdown>
            </div>
          </header>

          {/* ── Scrollable page content ─────────────────── */}
          <main className="page-scroll">{children}</main>

          {/* ── Footer ─────────────────────────────────── */}
          <footer className="shell-footer">
            <span>NTEWS v2.0 · Republic of Kenya · NIS</span>
            <span>RESTRICTED // OFFICIAL USE ONLY</span>
          </footer>
        </div>
      </div>
    </div>
  );
}
