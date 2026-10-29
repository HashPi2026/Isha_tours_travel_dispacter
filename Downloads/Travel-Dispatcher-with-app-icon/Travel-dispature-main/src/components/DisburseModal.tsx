import React, { useState, useEffect } from 'react';
import { X, Calendar } from 'lucide-react';
import { LedgerRecord, Agent } from '../types';

interface DisburseModalProps {
  record: LedgerRecord | null;
  agents: Agent[];
  isOpen: boolean;
  onClose: () => void;
  onAddSentAmount: (
    recordId: string,
    agentId: string | undefined,
    agentName: string,
    amountSent: number,
    disbursementDate: string,
    note?: string
  ) => void;
}

export const DisburseModal: React.FC<DisburseModalProps> = ({
  record,
  agents,
  isOpen,
  onClose,
  onAddSentAmount,
}) => {
  if (!isOpen || !record) return null;

  const currentTotalSent = record.sends.reduce((sum, s) => sum + s.amountSent, 0);
  const maxAvailable = Math.max(0, record.receivedAmount - currentTotalSent);

  const [selectedAgentId, setSelectedAgentId] = useState<string>('');
  const [agentNameInput, setAgentNameInput] = useState<string>('');
  const [amountSent, setAmountSent] = useState<string>(maxAvailable > 0 ? maxAvailable.toString() : '0');
  const [disbursementDate, setDisbursementDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );
  const [note, setNote] = useState<string>('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (record) {
      const remaining = Math.max(0, record.receivedAmount - record.sends.reduce((sum, s) => sum + s.amountSent, 0));
      setAmountSent(remaining > 0 ? remaining.toString() : '0');
      setError(null);
    }
  }, [record]);

  const handleAgentSelect = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const agentId = e.target.value;
    setSelectedAgentId(agentId);
    if (agentId) {
      const found = agents.find((a) => a.id === agentId);
      if (found) setAgentNameInput(found.name);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const amount = parseFloat(amountSent);

    if (!agentNameInput.trim()) {
      setError('Please select an agent or enter a supplier name.');
      return;
    }
    if (isNaN(amount) || amount <= 0) {
      setError('Please enter a valid disbursement amount greater than 0.');
      return;
    }
    if (amount > maxAvailable) {
      setError(`Sent total exceeds received amount (Max allowed: Rs. ${maxAvailable.toLocaleString()})`);
      return;
    }

    onAddSentAmount(
      record.id,
      selectedAgentId || undefined,
      agentNameInput.trim(),
      amount,
      disbursementDate,
      note.trim() || undefined
    );

    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/50 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-t-3xl sm:rounded-2xl border border-slate-200 bg-white p-5 shadow-2xl animate-in slide-in-from-bottom sm:zoom-in-95 duration-200 flex flex-col">
        {/* Top Handle Bar for Mobile */}
        <div className="mx-auto -mt-1 mb-3 h-1 w-12 rounded-full bg-slate-300 sm:hidden" />

        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <h2 className="text-base font-bold text-slate-900">
            Disburse to Agent · {record.clientName}
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 hover:bg-slate-100 hover:text-slate-700"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          {/* Client (Locked) */}
          <div>
            <label className="block text-xs font-semibold text-slate-700">Client (Locked)</label>
            <input
              type="text"
              readOnly
              value={record.clientName}
              className="mt-1.5 w-full rounded-xl border border-slate-200 bg-slate-50 px-3.5 py-2.5 text-sm font-medium text-slate-700 cursor-not-allowed"
            />
          </div>

          {/* Select Partner Agent or Type Name */}
          <div>
            <label className="block text-xs font-semibold text-slate-700">
              Select Partner Agent or Type Name <span className="text-rose-500">*</span>
            </label>

            <select
              value={selectedAgentId}
              onChange={handleAgentSelect}
              className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-800 focus:border-slate-900 focus:outline-none focus:ring-1 focus:ring-slate-900"
            >
              <option value="">-- Choose from saved directory --</option>
              {agents
                .filter((a) => a.isActive)
                .map((agent) => (
                  <option key={agent.id} value={agent.id}>
                    {agent.name} ({agent.email})
                  </option>
                ))}
            </select>

            <input
              type="text"
              placeholder="Agent or Supplier Name (e.g. Apex Holidays)"
              value={agentNameInput}
              onChange={(e) => {
                setAgentNameInput(e.target.value);
                const match = agents.find((a) => a.name.toLowerCase() === e.target.value.toLowerCase());
                if (match) setSelectedAgentId(match.id);
                else setSelectedAgentId('');
              }}
              className="mt-2 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-800 focus:border-slate-900 focus:outline-none focus:ring-1 focus:ring-slate-900"
            />
          </div>

          {/* Amount Sent (Rs.) with Prefilled max available */}
          <div>
            <div className="flex items-center justify-between">
              <label className="text-xs font-semibold text-slate-700">
                Amount Sent (Rs.) <span className="text-rose-500">*</span>
              </label>
              <button
                type="button"
                onClick={() => {
                  setAmountSent(maxAvailable.toString());
                  setError(null);
                }}
                className="text-xs font-medium text-emerald-600 hover:text-emerald-700"
              >
                Prefilled: Max available Rs. {maxAvailable.toLocaleString()}
              </button>
            </div>
            <input
              type="number"
              step="any"
              value={amountSent}
              onChange={(e) => {
                setAmountSent(e.target.value);
                const val = parseFloat(e.target.value);
                if (val > maxAvailable) {
                  setError(`Sent total exceeds received amount (Max allowed: Rs. ${maxAvailable.toLocaleString()})`);
                } else {
                  setError(null);
                }
              }}
              className={`mt-1.5 w-full rounded-xl border px-3.5 py-2.5 text-sm font-semibold focus:outline-none focus:ring-1 ${
                error
                  ? 'border-rose-400 bg-rose-50/30 text-rose-900 focus:border-rose-500 focus:ring-rose-500'
                  : 'border-slate-200 bg-white text-slate-900 focus:border-slate-900 focus:ring-slate-900'
              }`}
            />
            {error && <p className="mt-1 text-xs font-medium text-rose-600">{error}</p>}
          </div>

          {/* Disbursement Date */}
          <div>
            <label className="block text-xs font-semibold text-slate-700">Disbursement Date</label>
            <div className="relative mt-1.5">
              <input
                type="date"
                value={disbursementDate}
                onChange={(e) => setDisbursementDate(e.target.value)}
                className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-800 focus:border-slate-900 focus:outline-none focus:ring-1 focus:ring-slate-900"
              />
            </div>
          </div>

          {/* Note */}
          <div>
            <label className="block text-xs font-semibold text-slate-700">Note</label>
            <input
              type="text"
              placeholder="e.g. Hotel advance payment"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-800 focus:border-slate-900 focus:outline-none focus:ring-1 focus:ring-slate-900"
            />
          </div>

          {/* Footer Actions */}
          <div className="mt-6 flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="rounded-xl bg-slate-900 px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-slate-800 transition-colors"
            >
              Add Sent Amount
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
