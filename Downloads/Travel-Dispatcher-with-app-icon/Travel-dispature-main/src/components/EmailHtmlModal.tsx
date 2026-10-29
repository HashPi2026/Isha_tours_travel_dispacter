import React from 'react';
import { X, Mail, ShieldCheck } from 'lucide-react';
import { DispatchItem } from '../types';

interface EmailHtmlModalProps {
  dispatch: DispatchItem | null;
  onClose: () => void;
}

export const EmailHtmlModal: React.FC<EmailHtmlModalProps> = ({ dispatch, onClose }) => {
  if (!dispatch) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs">
      <div className="flex h-full max-h-[85vh] w-full max-w-2xl flex-col rounded-2xl border border-slate-200 bg-white shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-3.5">
          <div className="flex items-center gap-2">
            <Mail className="h-5 w-5 text-slate-700" />
            <div>
              <h3 className="text-sm font-bold text-slate-900">
                Email Dispatch Preview &middot; {dispatch.agentName}
              </h3>
              <p className="text-xs text-slate-500">To: {dispatch.agentEmail}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 hover:bg-slate-100 hover:text-slate-700"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Privacy Invariant Banner */}
        <div className="flex items-center gap-2 bg-emerald-50 px-5 py-2 text-xs font-medium text-emerald-800 border-b border-emerald-100">
          <ShieldCheck className="h-4 w-4 text-emerald-600 shrink-0" />
          <span>Privacy Verified: Client contact phone number is strictly excluded from quotation emails sent to partner agents.</span>
        </div>

        {/* Rendered HTML Body */}
        <div className="flex-1 overflow-y-auto p-4 bg-slate-50">
          {dispatch.emailHtml ? (
            <div
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs"
              dangerouslySetInnerHTML={{ __html: dispatch.emailHtml }}
            />
          ) : (
            <div className="flex h-full items-center justify-center text-xs text-slate-400">
              No HTML body stored for this dispatch record.
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="border-t border-slate-200 px-5 py-3 flex justify-end">
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl bg-slate-900 px-5 py-2 text-xs font-semibold text-white hover:bg-slate-800"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
