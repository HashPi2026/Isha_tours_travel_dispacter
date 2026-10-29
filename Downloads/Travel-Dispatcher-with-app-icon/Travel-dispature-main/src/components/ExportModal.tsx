import React, { useState } from 'react';
import { X, FileText, Download, ShieldAlert, Check } from 'lucide-react';
import { Inquiry } from '../types';
import { exportInquiriesPdf, exportInquiriesCsv } from '../utils/pdfExport';

interface ExportModalProps {
  isOpen: boolean;
  inquiries: Inquiry[];
  onClose: () => void;
}

export const ExportModal: React.FC<ExportModalProps> = ({ isOpen, inquiries, onClose }) => {
  const [periodPreset, setPeriodPreset] = useState<'today' | 'week' | 'month' | 'last_month' | 'all'>('all');
  const [statusFilter, setStatusFilter] = useState<'all' | 'inquiry' | 'confirmed' | 'cancelled'>('all');
  const [includePhone, setIncludePhone] = useState(false);

  if (!isOpen) return null;

  // Filter inquiries based on selection
  const now = new Date();
  const filtered = inquiries.filter((inq) => {
    if (statusFilter !== 'all' && inq.status !== statusFilter) return false;

    if (periodPreset === 'all') return true;

    const dateVal = inq.startDate || inq.createdAt;
    const inqDate = dateVal ? new Date(dateVal) : new Date();
    if (isNaN(inqDate.getTime())) return true;

    if (periodPreset === 'today') {
      return inqDate.toDateString() === now.toDateString();
    }
    if (periodPreset === 'week') {
      const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      return inqDate >= oneWeekAgo && inqDate <= now;
    }
    if (periodPreset === 'month') {
      return inqDate.getMonth() === now.getMonth() && inqDate.getFullYear() === now.getFullYear();
    }
    if (periodPreset === 'last_month') {
      const lastMonth = now.getMonth() === 0 ? 11 : now.getMonth() - 1;
      const lastMonthYear = now.getMonth() === 0 ? now.getFullYear() - 1 : now.getFullYear();
      return inqDate.getMonth() === lastMonth && inqDate.getFullYear() === lastMonthYear;
    }
    return true;
  });

  const periodLabels = {
    today: 'Today',
    week: 'This week',
    month: 'This month',
    last_month: 'Last month',
    all: 'All time',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-6 shadow-2xl animate-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center gap-2">
            <Download className="h-5 w-5 text-slate-800" />
            <h2 className="text-base font-bold text-slate-900">Export Travel Inquiries</h2>
          </div>
          <button
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 hover:bg-slate-100 hover:text-slate-700"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="mt-4 space-y-4">
          {/* Period Preset */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">Period Preset</label>
            <div className="grid grid-cols-3 gap-2">
              {(['all', 'today', 'week', 'month', 'last_month'] as const).map((preset) => (
                <button
                  key={preset}
                  type="button"
                  onClick={() => setPeriodPreset(preset)}
                  className={`rounded-xl border px-3 py-2 text-xs font-semibold transition-all ${
                    periodPreset === preset
                      ? 'border-slate-900 bg-slate-900 text-white'
                      : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  {periodLabels[preset]}
                </button>
              ))}
            </div>
          </div>

          {/* Status Filter */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">Status Filter</label>
            <div className="grid grid-cols-4 gap-2">
              {[
                { id: 'all', label: 'All' },
                { id: 'inquiry', label: 'Pending' },
                { id: 'confirmed', label: 'Confirmed' },
                { id: 'cancelled', label: 'Cancelled' },
              ].map((s) => (
                <button
                  key={s.id}
                  type="button"
                  onClick={() => setStatusFilter(s.id as any)}
                  className={`rounded-xl border px-2.5 py-1.5 text-xs font-semibold transition-all ${
                    statusFilter === s.id
                      ? 'border-slate-900 bg-slate-900 text-white'
                      : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  {s.label}
                </button>
              ))}
            </div>
          </div>

          {/* Client Phone Privacy Toggle */}
          <div className="rounded-xl border border-slate-200 bg-slate-50 p-3.5">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-xs font-bold text-slate-900">Include Client Phone Numbers</span>
                <p className="text-[11px] text-slate-500">Excluded by default for client contact privacy.</p>
              </div>
              <label className="relative inline-flex cursor-pointer items-center">
                <input
                  type="checkbox"
                  checked={includePhone}
                  onChange={(e) => setIncludePhone(e.target.checked)}
                  className="peer sr-only"
                />
                <div className="peer h-6 w-11 rounded-full bg-slate-200 after:absolute after:top-[2px] after:left-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-slate-300 after:bg-white after:transition-all after:content-[''] peer-checked:bg-slate-900 peer-checked:after:translate-x-full peer-checked:after:border-white"></div>
              </label>
            </div>

            {includePhone && (
              <div className="mt-2.5 flex items-start gap-2 rounded-lg bg-amber-50 border border-amber-200 p-2 text-[11px] text-amber-900">
                <ShieldAlert className="h-4 w-4 text-amber-600 shrink-0 mt-0.5" />
                <span>
                  <strong>Privacy Notice:</strong> Exported documents will contain raw unmasked client telephone numbers. Maintain confidentiality.
                </span>
              </div>
            )}
          </div>

          {/* Matched Records Count */}
          <div className="text-xs text-slate-500 font-medium">
            Matching Inquiries: <strong>{filtered.length}</strong> of {inquiries.length} records
          </div>

          {/* Action Buttons: Export PDF or CSV */}
          <div className="grid grid-cols-2 gap-3 pt-2">
            <button
              type="button"
              onClick={() => {
                exportInquiriesPdf(filtered, includePhone, periodLabels[periodPreset]);
                onClose();
              }}
              className="flex items-center justify-center gap-2 rounded-xl bg-slate-900 px-4 py-2.5 text-xs font-bold text-white shadow-sm hover:bg-slate-800 transition-colors"
            >
              <FileText className="h-4 w-4" />
              <span>Export PDF ({filtered.length})</span>
            </button>

            <button
              type="button"
              onClick={() => {
                exportInquiriesCsv(filtered, includePhone);
                onClose();
              }}
              className="flex items-center justify-center gap-2 rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-xs font-bold text-slate-800 hover:bg-slate-50 transition-colors"
            >
              <Download className="h-4 w-4" />
              <span>Export CSV</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
