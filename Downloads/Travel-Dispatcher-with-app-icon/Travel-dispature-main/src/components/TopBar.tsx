import React from 'react';
import { Settings, LogOut } from 'lucide-react';

interface TopBarProps {
  onSettingsClick: () => Unit;
  onLogoutClick: () => Unit;
}

type Unit = void;

export const TopBar: React.FC<TopBarProps> = ({ onSettingsClick, onLogoutClick }) => {
  return (
    <header className="sticky top-0 z-30 flex h-14 w-full items-center justify-between border-b border-slate-200/80 bg-white/95 px-4 backdrop-blur-md">
      <div className="flex items-center gap-2.5">
        <span className="text-lg font-bold tracking-tight text-slate-900">
          Travel Dispatcher
        </span>
        <span className="inline-flex items-center rounded-full border border-slate-200 bg-slate-100 px-2.5 py-0.5 text-[11px] font-medium text-slate-600">
          Preview
        </span>
      </div>

      <div className="flex items-center gap-1">
        <button
          onClick={onSettingsClick}
          title="Settings"
          className="flex h-9 w-9 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 hover:text-slate-900 transition-colors"
        >
          <Settings className="h-5 w-5" />
        </button>
        <button
          onClick={onLogoutClick}
          title="Sign Out"
          className="flex h-9 w-9 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 hover:text-slate-900 transition-colors"
        >
          <LogOut className="h-5 w-5" />
        </button>
      </div>
    </header>
  );
};
