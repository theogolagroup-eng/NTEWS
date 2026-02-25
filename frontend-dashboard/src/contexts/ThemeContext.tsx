"use client";

import React, { createContext, useContext } from "react";

/* ────────────────────────────────────────────────────────────
   NTEWS — Kenya Security Ops-Center Theme
   Always dark — mission-critical ops systems don't use light.
   All values reference the CSS variables in globals.css.
   ──────────────────────────────────────────────────────────── */

const STYLES = {
  background: "var(--surface-0)",
  contentBackground: "var(--surface-0)",
  sidebarBackground: "var(--surface-1)",
  headerColor: "var(--surface-1)",
  textColor: "var(--text-primary)",
  secondaryTextColor: "var(--text-secondary)",
  mutedTextColor: "var(--text-muted)",
  cardBackground: "var(--surface-1)",
  cardBorder: "1px solid var(--border-subtle)",
  glassBackground: "var(--surface-2)",
  headerBackground: "var(--surface-1)",
  borderDim: "var(--border-subtle)",
  borderNormal: "var(--border-default)",
  borderActive: "var(--border-active)",
  hoverBackground: "var(--surface-3)",
  dividerColor: "var(--border-subtle)",
  textShadow: "none",
  placeholderBackground: "var(--surface-2)",
  successColor: "var(--low)",
  warningColor: "var(--medium)",
  errorColor: "var(--critical)",
  infoColor: "#3b82f6",
  sidebarTextColor: "var(--text-secondary)",
} as const;

interface ThemeContextType {
  isDarkMode: true;
  toggleTheme: () => void;
  themeStyles: typeof STYLES;
}

const ThemeContext = createContext<ThemeContextType>({
  isDarkMode: true,
  toggleTheme: () => {},
  themeStyles: STYLES,
});

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <ThemeContext.Provider
    value={{ isDarkMode: true, toggleTheme: () => {}, themeStyles: STYLES }}
  >
    {children}
  </ThemeContext.Provider>
);

export const useTheme = () => useContext(ThemeContext);

export default ThemeContext;
