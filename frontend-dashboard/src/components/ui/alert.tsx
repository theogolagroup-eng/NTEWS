import React from 'react';
import { cn } from '@/utils/performance';

export interface AlertProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
}

export const Alert: React.FC<AlertProps> = ({ children, className, ...props }) => {
  return (
    <div
      className={cn(
        "relative w-full rounded-lg border p-4",
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export const AlertDescription: React.FC<AlertProps> = ({ children, className, ...props }) => {
  return (
    <div
      className={cn(
        "text-sm text-muted-foreground",
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};
