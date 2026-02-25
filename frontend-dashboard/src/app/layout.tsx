import AppShell from "@/components/layout/AppShell";
import { ThemeProvider } from "@/contexts/ThemeContext";
import { ActionPointsProvider } from "@/contexts/ActionPointsContext";
import "./globals.css";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <head>
        <title>
          NTEWS | Republic of Kenya — National Threat Early Warning System
        </title>
        <meta
          name="description"
          content="RESTRICTED — AI-Powered Threat Intelligence & Early Warning Platform, Republic of Kenya"
        />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <meta name="robots" content="noindex,nofollow" />
        <link rel="icon" href="/favicon.ico" />
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link
          rel="preconnect"
          href="https://fonts.gstatic.com"
          crossOrigin="anonymous"
        />
      </head>
      <body>
        <ThemeProvider>
          <ActionPointsProvider>
            <AppShell>{children}</AppShell>
          </ActionPointsProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
