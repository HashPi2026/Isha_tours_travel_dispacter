import React from 'react';
import {
  Download,
  Plus,
  ArrowDownLeft,
  ArrowUpRight,
  Wallet,
  Send,
  Trash2,
} from 'lucide-react';
import { LedgerRecord, LedgerSend } from '../types';

interface LedgerScreenProps {
  records: LedgerRecord[];
  onOpenMoneyReceived: () => void;
  onOpenDisburseModal: (record: LedgerRecord) => void;
  onDeleteRecord: (record: LedgerRecord) => void;
  onDeleteSend: (record: LedgerRecord, send: LedgerSend) => void;
  onExportPdf: () => void;
}

export const LedgerScreen: React.FC<LedgerScreenProps> = ({
  records,
  onOpenMoneyReceived,
  onOpenDisburseModal,
  onDeleteRecord,
  onDeleteSend,
  onExportPdf,
}) => {
  const totalReceived = records.reduce((sum, r) => sum + r.receivedAmount, 0);
  const totalSent = records.reduce(
    (sum, r) => sum + r.sends.reduce((sSum, s) => sSum + s.amountSent, 0),
    0
  );
  const balanceWithMe = Math.max(0, totalReceived - totalSent);

  return (
    <div className="mx-auto max-w-xl pb-24 pt-3">
      {/* Title & Subtitle */}
      <div className="px-4">
        <h1 className="text-xl font-bold tracking-tight text-slate-900">
          Khatabook Middleman Ledger
        </h1>
        <p className="mt-0.5 text-xs text-slate-500">
          Track client money held, record agent disbursements, and prevent over-sending
        </p>

        {/* Action Buttons Row */}
        <div className="mt-4 flex items-center gap-2.5">
          <button
            onClick={onExportPdf}
            className="flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs font-bold text-slate-800 shadow-xs hover:bg-slate-50 active:scale-[0.99] transition-all"
          >
            <Download className="h-4 w-4" />
            <span>Export PDF</span>
          </button>

          <button
            onClick={onOpenMoneyReceived}
            className="flex flex-1 items-center justify-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2.5 text-xs font-bold text-white shadow-sm hover:bg-slate-800 active:scale-[0.99] transition-all"
          >
            <Plus className="h-4 w-4 stroke-[2.5]" />
            <span>+ Money Received</span>
          </button>
        </div>

        {/* 3 Summary Stat Cards (matching screenshot 06) */}
        <div className="mt-4 space-y-2.5">
          {/* TOTAL RECEIVED */}
          <div className="rounded-2xl border border-slate-200/90 bg-white p-4 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold tracking-wider text-slate-500">
                TOTAL RECEIVED
              </span>
              <ArrowDownLeft className="h-4 w-4 text-emerald-600" />
            </div>
            <div className="mt-1 text-2xl font-bold text-slate-900">
              Rs.  {totalReceived.toLocaleString()}
            </div>
            <p className="mt-0.5 text-[11px] text-slate-400">From all client bookings</p>
          </div>

          {/* TOTAL SENT */}
          <div className="rounded-2xl border border-slate-200/90 bg-white p-4 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold tracking-wider text-slate-500">
                TOTAL SENT
              </span>
              <ArrowUpRight className="h-4 w-4 text-rose-600" />
            </div>
            <div className="mt-1 text-2xl font-bold text-slate-900">
              Rs.  {totalSent.toLocaleString()}
            </div>
            <p className="mt-0.5 text-[11px] text-slate-400">Disbursed to travel agents</p>
          </div>

          {/* BALANCE WITH ME */}
          <div className="rounded-2xl border border-slate-200/90 bg-white p-4 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold tracking-wider text-slate-500">
                BALANCE WITH ME
              </span>
              <Wallet className="h-4 w-4 text-blue-600" />
            </div>
            <div className="mt-1 text-2xl font-bold text-emerald-600">
              Rs.  {balanceWithMe.toLocaleString()}
            </div>
            <p className="mt-0.5 text-[11px] text-slate-400">Net funds held</p>
          </div>
        </div>
      </div>

      {/* Ledger Records List */}
      <div className="mt-4 space-y-3.5 px-4">
        {records.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-200 bg-white p-8 text-center text-xs text-slate-400">
            No middleman ledger records yet. Click "+ Money Received" to record client booking payments.
          </div>
        ) : (
          records.map((record) => {
            const recordSent = record.sends.reduce((sum, s) => sum + s.amountSent, 0);
            const balanceHeld = Math.max(0, record.receivedAmount - recordSent);

            return (
              <div
                key={record.id}
                className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs transition-shadow"
              >
                {/* Header: Client Name + Delete */}
                <div className="flex items-center justify-between">
                  <h3 className="text-base font-bold text-slate-900">{record.clientName}</h3>

                  <button
                    onClick={() => onDeleteRecord(record)}
                    title="Delete Record"
                    className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 hover:bg-rose-50 hover:text-rose-600 transition-colors"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>

                {/* Subline: Date + Notes */}
                <div className="mt-0.5 text-xs text-slate-500">
                  Date: {record.receivedDate}
                  {record.notes && <span className="italic"> &middot; {record.notes}</span>}
                </div>

                {/* Stat Box: RECEIVED | SENT TOTAL | BALANCE HELD */}
                <div className="mt-3 grid grid-cols-3 divide-x divide-slate-200 rounded-xl border border-slate-200/80 bg-slate-50/70 p-3 text-center">
                  <div>
                    <span className="block text-[10px] font-bold tracking-wider text-slate-400">
                      RECEIVED
                    </span>
                    <span className="mt-0.5 block text-xs font-bold text-slate-900">
                      Rs.  {record.receivedAmount.toLocaleString()}
                    </span>
                  </div>

                  <div>
                    <span className="block text-[10px] font-bold tracking-wider text-slate-400">
                      SENT TOTAL
                    </span>
                    <span className="mt-0.5 block text-xs font-bold text-rose-600">
                      Rs.  {recordSent.toLocaleString()}
                    </span>
                  </div>

                  <div>
                    <span className="block text-[10px] font-bold tracking-wider text-slate-400">
                      BALANCE HELD
                    </span>
                    <span className="mt-0.5 block text-xs font-bold text-emerald-600">
                      Rs.  {balanceHeld.toLocaleString()}
                    </span>
                  </div>
                </div>

                {/* Sub-Section: DISBURSED TO AGENTS (X) + Send Button */}
                <div className="mt-4 flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-700">
                    DISBURSED TO AGENTS ({record.sends.length})
                  </span>

                  <button
                    onClick={() => onOpenDisburseModal(record)}
                    className="flex items-center gap-1 rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-bold text-white hover:bg-slate-800 transition-colors"
                  >
                    <Send className="h-3 w-3 stroke-[2.2]" />
                    <span>+ Send</span>
                  </button>
                </div>

                {/* Disbursed rows or Empty Dotted Box */}
                <div className="mt-2.5">
                  {record.sends.length === 0 ? (
                    <div className="rounded-xl border border-dashed border-slate-200/90 bg-slate-50/50 p-4 text-center text-xs text-slate-400">
                      No funds disbursed to agents yet. Click "+ Send" to pay an agent.
                    </div>
                  ) : (
                    <div className="space-y-2">
                      {record.sends.map((send) => (
                        <div
                          key={send.id}
                          className="flex items-center justify-between rounded-xl border border-slate-200/80 bg-slate-50/80 px-3.5 py-2.5"
                        >
                          <div>
                            <h4 className="text-xs font-bold text-slate-900">{send.agentName}</h4>
                            <p className="text-[11px] text-slate-500">
                              {send.disbursementDate}
                              {send.note && <span> &middot; {send.note}</span>}
                            </p>
                          </div>

                          <div className="flex items-center gap-3">
                            <span className="text-xs font-bold text-rose-600">
                              Rs.  {send.amountSent.toLocaleString()}
                            </span>

                            <button
                              onClick={() => onDeleteSend(record, send)}
                              title="Delete Send"
                              className="text-slate-400 hover:text-rose-600 p-1"
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
