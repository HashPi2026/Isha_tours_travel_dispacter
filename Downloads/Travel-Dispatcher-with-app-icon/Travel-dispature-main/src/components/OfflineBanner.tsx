import React from 'react';
import { WifiOff } from 'lucide-react';

interface OfflineBannerProps {
  isOffline: boolean;
}

export const OfflineBanner: React.FC<OfflineBannerProps> = ({ isOffline }) => {
  if (!isOffline) return null;

  return (
    <div className="flex w-full items-center justify-center gap-2 bg-rose-600 px-4 py-2 text-xs font-semibold text-white shadow-sm transition-all animate-in slide-in-from-top">
      <WifiOff className="h-4 w-4" />
      <span>No Internet Connection · Offline Mode Active (Supabase sync paused)</span>
    </div>
  );
};
