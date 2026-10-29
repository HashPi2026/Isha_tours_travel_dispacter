import React from 'react';
import { X, CheckCircle2, XCircle, Eye, RefreshCw } from 'lucide-react';
import { Inquiry, DispatchItem } from '../types';

interface DispatchLogModalProps {
  inquiry: Inquiry | null;
  onClose: () => void;
  onRetryDispatch: (dispatch: DispatchItem) => void;
  onViewEmailHtml: (dispatch: DispatchItem) => void;
}

export const DispatchLogModal: React.FC<DispatchLogModalProps> = ({
  inquiry,
  onClose,
  onRetryDispatch,
  onViewEmailHtml,
}) => {
  if (!inquiry) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/50 backdrop-blur-xs transition-opacity">
      {/* Bottom Sheet on Mobile, Centered Modal on Desktop */}
      <div className="w-full max-w-lg rounded-t-3xl sm:rounded-2xl border border-slate-200 bg-white p-5 shadow-2xl animate-in slide-in-from-bottom duration-200 max-h-[90vh] flex flex-col">
        {/* Top Handle Bar for Bottom Sheet */}
        <div className="mx-auto -mt-1 mb-3 h-1 w-12 rounded-full bg-slate-300 sm:hidden" />

        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <h2 className="text-base font-bold text-slate-900">
            Dispatch Status · Inquiry #{inquiry.inquiryNumber}
          </h2>
          <button
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 hover:bg-slate-100 hover:text-slate-700"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Scrollable Content */}
        <div className="mt-4 overflow-y-auto space-y-3.5 pr-1 flex-1">
          {/* Summary Box */}
          <div className="rounded-xl border border-slate-200 bg-white p-4">
            <h3 className="text-base font-bold text-slate-900">{inquiry.destination}</h3>
            <p className="mt-1 text-xs text-slate-500">
              Client: {inquiry.clientName} · Dispatched to {inquiry.dispatches.length} agent(s)
            </p>
          </div>

          {/* Agents Dispatch List */}
          {inquiry.dispatches.length === 0 ? (
            <div className="rounded-xl border border-dashed border-slate-200 p-6 text-center text-xs text-slate-400">
              No dispatches recorded for this inquiry.
            </div>
          ) : (
            inquiry.dispatches.map((dispatch) => {
              const isSent = dispatch.status === 'sent';
              const isFailed = dispatch.status === 'failed';

              return (
                <div
                  key={dispatch.id}
                  className="rounded-xl border border-slate-200 bg-white p-4 transition-all"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <h4 className="text-sm font-bold text-slate-900">{dispatch.agentName}</h4>
                      <p className="text-xs text-slate-500">{dispatch.agentEmail}</p>
                    </div>

                    {/* Status Badge */}
                    <div>
                      {isSent && (
                        <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-600">
                          <CheckCircle2 className="h-4 w-4" />
                          Sent
                        </span>
                      )}
                      {isFailed && (
                        <span className="inline-flex items-center gap-1 text-xs font-semibold text-rose-600">
                          <XCircle className="h-4 w-4" />
                          Failed
                        </span>
                      )}
                      {!isSent && !isFailed && (
                        <span className="inline-flex items-center text-xs font-medium text-slate-500">
                          Pending
                        </span>
                      )}
                    </div>
                  </div>

                  {dispatch.errorMessage && (
                    <p className="mt-2 text-[11px] text-rose-600 bg-rose-50 border border-rose-100 rounded-md p-1.5">
                      {dispatch.errorMessage}
                    </p>
                  )}

                  <hr className="my-3 border-slate-100" />

                  {/* Actions Row */}
                  <div className="flex items-center justify-between">
                    <button
                      type="button"
                      onClick={() => onViewEmailHtml(dispatch)}
                      className="inline-flex items-center gap-1.5 text-xs font-medium text-slate-600 hover:text-slate-900"
                    >
                      <Eye className="h-4 w-4 text-slate-500" />
                      <span>View Email HTML</span>
                    </button>

                    {isFailed && (
                      <button
                        type="button"
                        onClick={() => onRetryDispatch(dispatch)}
                        className="inline-flex items-center gap-1.5 rounded-lg border border-rose-200 bg-rose-50 px-3 py-1.5 text-xs font-semibold text-rose-600 hover:bg-rose-100 transition-colors"
                      >
                        <RefreshCw className="h-3.5 w-3.5" />
                        <span>Retry</span>
                      </button>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Footer Done Button */}
        <div className="mt-5 pt-3 border-t border-slate-100 flex justify-end">
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl bg-slate-900 px-6 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-slate-800 transition-colors"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};
