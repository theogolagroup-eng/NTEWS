import AppShell from '@/components/layout/AppShell';
import { ThemeProvider } from '@/contexts/ThemeContext';
import { ActionPointsProvider } from '@/contexts/ActionPointsContext';

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <head>
        <title>NTEWS - National Threat Early Warning System</title>
        <meta name="description" content="AI-powered threat intelligence command dashboard" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.ico" />
      </head>
      <body>
        <ThemeProvider>
          <ActionPointsProvider>
            <AppShell>
              {children}
            </AppShell>
          </ActionPointsProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
