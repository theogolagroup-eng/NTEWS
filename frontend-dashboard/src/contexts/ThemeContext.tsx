"use client";



import React, { createContext, useContext } from "react";



/* ────────────────────────────────────────────────────────────

   NTEWS — Kenya Security Ops-Center Theme

   Always dark — mission-critical ops systems don't use light.

   All values reference the CSS variables in globals.css.

   ──────────────────────────────────────────────────────────── */



const STYLES = {

  background: "#000000",

  contentBackground: "#000000",

  sidebarBackground: "#1a1a1a",

  headerColor: "#1a1a1a",

  textColor: "#ffffff",

  secondaryTextColor: "#e0e0e0",

  mutedTextColor: "#b0b0b0",

  cardBackground: "#1a1a1a",

  cardBorder: "1px solid #333333",

  glassBackground: "#2d2d2d",

  headerBackground: "#1a1a1a",

  borderDim: "#333333",

  borderNormal: "#555555",

  borderActive: "#ffffff",

  hoverBackground: "#2d2d2d",

  dividerColor: "#333333",

  textShadow: "none",

  placeholderBackground: "#2d2d2d",

  successColor: "#006600",

  warningColor: "#ff6600",

  errorColor: "#cc0000",

  infoColor: "#3366cc",

  sidebarTextColor: "#e0e0e0",

  kenyanBlack: "#000000",

  kenyanRed: "#cc0000",

  kenyanGreen: "#006600",

  kenyanWhite: "#ffffff",

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

