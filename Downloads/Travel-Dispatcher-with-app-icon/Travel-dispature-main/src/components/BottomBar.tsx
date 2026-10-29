import React from 'react';
import { FileText, Plus, BookOpen, Users, Settings } from 'lucide-react';

export type TabType = 'inquiries' | 'new' | 'ledger' | 'agents' | 'settings';

interface BottomBarProps {
  currentTab: TabType;
  onTabChange: (tab: TabType) => void;
}

export const BottomBar: React.FC<BottomBarProps> = ({ currentTab, onTabChange }) => {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30 flex h-16 w-full items-center justify-around border-t border-slate-200/80 bg-white/95 px-2 backdrop-blur-md shadow-lg shadow-slate-900/5">
      {/* Inquiries */}
      <button
        onClick={() => onTabChange('inquiries')}
        className={`flex flex-col items-center justify-center gap-1 px-3 py-1 transition-colors ${
          currentTab === 'inquiries' ? 'text-slate-900 font-bold' : 'text-slate-400 hover:text-slate-600'
        }`}
      >
        <FileText className={`h-5 w-5 ${currentTab === 'inquiries' ? 'stroke-[2.5]' : 'stroke-[1.8]'}`} />
        <span className="text-[11px] font-medium leading-none">Inquiries</span>
      </button>

      {/* New (Center Highlighted Circle) */}
      <button
        onClick={() => onTabChange('new')}
        className="group -mt-4 flex flex-col items-center justify-center gap-1 focus:outline-none"
      >
        <div className={`flex h-11 w-11 items-center justify-center rounded-full shadow-md transition-transform active:scale-95 ${
          currentTab === 'new' ? 'bg-slate-900 text-white ring-4 ring-slate-100' : 'bg-slate-900 text-white'
        }`}>
          <Plus className="h-6 w-6 stroke-[2.5]" />
        </div>
        <span className={`text-[11px] font-medium leading-none ${currentTab === 'new' ? 'text-slate-900 font-bold' : 'text-slate-500'}`}>
          New
        </span>
      </button>

      {/* Ledger */}
      <button
        onClick={() => onTabChange('ledger')}
        className={`flex flex-col items-center justify-center gap-1 px-3 py-1 transition-colors ${
          currentTab === 'ledger' ? 'text-slate-900 font-bold' : 'text-slate-400 hover:text-slate-600'
        }`}
      >
        <BookOpen className={`h-5 w-5 ${currentTab === 'ledger' ? 'stroke-[2.5]' : 'stroke-[1.8]'}`} />
        <span className="text-[11px] font-medium leading-none">Ledger</span>
      </button>

      {/* Agents */}
      <button
        onClick={() => onTabChange('agents')}
        className={`flex flex-col items-center justify-center gap-1 px-3 py-1 transition-colors ${
          currentTab === 'agents' ? 'text-slate-900 font-bold' : 'text-slate-400 hover:text-slate-600'
        }`}
      >
        <Users className={`h-5 w-5 ${currentTab === 'agents' ? 'stroke-[2.5]' : 'stroke-[1.8]'}`} />
        <span className="text-[11px] font-medium leading-none">Agents</span>
      </button>

      {/* Settings */}
      <button
        onClick={() => onTabChange('settings')}
        className={`flex flex-col items-center justify-center gap-1 px-3 py-1 transition-colors ${
          currentTab === 'settings' ? 'text-slate-900 font-bold' : 'text-slate-400 hover:text-slate-600'
        }`}
      >
        <Settings className={`h-5 w-5 ${currentTab === 'settings' ? 'stroke-[2.5]' : 'stroke-[1.8]'}`} />
        <span className="text-[11px] font-medium leading-none">Settings</span>
      </button>
    </nav>
  );
};
