"use client";

import React, { createContext, useContext } from "react";

/* ────────────────────────────────────────────────────────────
   NTEWS — Kenya Security Ops-Center Theme
   Always dark — mission-critical ops systems don't use light.
   All values reference the CSS variables in globals.css.
   ──────────────────────────────────────────────────────────── */

const STYLES = {
  background: "var(--bg)",
  contentBackground: "var(--surface-1)",
  sidebarBackground: "var(--surface-2)",
  headerColor: "var(--surface-1)",
  textColor: "var(--text-1)",
  secondaryTextColor: "var(--text-2)",
  mutedTextColor: "var(--text-3)",
  cardBackground: "var(--surface-2)",
  cardBorder: "1px solid var(--border)",
  glassBackground: "var(--surface-3)",
  headerBackground: "var(--surface-2)",
  borderDim: "var(--border-muted)",
  borderNormal: "var(--border)",
  borderActive: "var(--border-emphasis)",
  hoverBackground: "var(--surface-3)",
  dividerColor: "var(--border)",
  textShadow: "none",
  placeholderBackground: "var(--surface-3)",
  successColor: "var(--green)",
  warningColor: "var(--orange)",
  errorColor: "var(--red)",
  infoColor: "var(--blue)",
  sidebarTextColor: "var(--text-2)",
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
