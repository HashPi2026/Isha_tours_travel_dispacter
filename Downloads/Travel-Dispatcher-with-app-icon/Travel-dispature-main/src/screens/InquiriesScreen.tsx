import React, { useState } from 'react';
import {
  Download,
  Plus,
  Layers,
  Clock,
  CheckCircle2,
  XCircle,
  Search,
  Luggage,
  Plane,
  Eye,
  Trash2,
} from 'lucide-react';
import { Inquiry } from '../types';

interface InquiriesScreenProps {
  inquiries: Inquiry[];
  onNewInquiryClick: () => void;
  onOpenExportModal: () => void;
  onViewDispatchLog: (inquiry: Inquiry) => void;
  onStatusChange: (inquiryId: string, newStatus: 'inquiry' | 'confirmed' | 'cancelled') => void;
  onRequestDelete: (inquiry: Inquiry) => void;
}

export const InquiriesScreen: React.FC<InquiriesScreenProps> = ({
  inquiries,
  onNewInquiryClick,
  onOpenExportModal,
  onViewDispatchLog,
  onStatusChange,
  onRequestDelete,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'inquiry' | 'confirmed' | 'cancelled'>('all');
  const [typeFilter, setTypeFilter] = useState<'all' | 'package' | 'ticket'>('all');

  const totalCount = inquiries.length;
  const pendingCount = inquiries.filter((i) => i.status === 'inquiry').length;
  const confirmedCount = inquiries.filter((i) => i.status === 'confirmed').length;
  const cancelledCount = inquiries.filter((i) => i.status === 'cancelled').length;

  const filteredInquiries = inquiries.filter((inq) => {
    const matchesSearch =
      !searchQuery ||
      inq.clientName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      inq.destination.toLowerCase().includes(searchQuery.toLowerCase()) ||
      inq.inquiryNumber.toString().includes(searchQuery) ||
      inq.clientPhone.includes(searchQuery);

    const matchesStatus = statusFilter === 'all' || inq.status === statusFilter;
    const matchesType = typeFilter === 'all' || inq.category === typeFilter;

    return matchesSearch && matchesStatus && matchesType;
  });

  return (
    <div className="mx-auto max-w-xl pb-24 pt-3">
      {/* Title & Subtitle */}
      <div className="px-4">
        <h1 className="text-xl font-bold tracking-tight text-slate-900">
          Travel Inquiries
        </h1>
        <p className="mt-0.5 text-xs text-slate-500">
          Search inquiries, filter by status, and track dispatch logs
        </p>

        {/* Top Action Buttons: Export PDF & New Inquiry */}
        <div className="mt-4 flex items-center gap-2.5">
          <button
            onClick={onOpenExportModal}
            className="flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs font-bold text-slate-800 shadow-xs hover:bg-slate-50 active:scale-[0.99] transition-all"
          >
            <Download className="h-4 w-4" />
            <span>Export PDF ({totalCount})</span>
          </button>

          <button
            onClick={onNewInquiryClick}
            className="flex flex-1 items-center justify-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2.5 text-xs font-bold text-white shadow-sm hover:bg-slate-800 active:scale-[0.99] transition-all"
          >
            <Plus className="h-4 w-4 stroke-[2.5]" />
            <span>+ New Inquiry</span>
          </button>
        </div>

        {/* 2x2 Metric Summary Grid */}
        <div className="mt-4 grid grid-cols-2 gap-2.5">
          {/* ALL INQUIRIES (Dark) */}
          <div className="rounded-2xl border border-slate-900 bg-slate-900 p-4 text-white shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold tracking-wider text-slate-300">
                ALL INQUIRIES
              </span>
              <Layers className="h-4 w-4 text-slate-300" />
            </div>
            <div className="mt-2 text-2xl font-bold">{totalCount}</div>
          </div>

          {/* PENDING (I) */}
          <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold tracking-wider text-slate-500">
                PENDING (I)
              </span>
              <Clock className="h-4 w-4 text-slate-400" />
            </div>
            <div className="mt-2 text-2xl font-bold text-slate-900">{pendingCount}</div>
          </div>

          {/* CONFIRMED (C) */}
          <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold tracking-wider text-slate-500">
                CONFIRMED (C)
              </span>
              <CheckCircle2 className="h-4 w-4 text-slate-400" />
            </div>
            <div className="mt-2 text-2xl font-bold text-slate-900">{confirmedCount}</div>
          </div>

          {/* CANCELLED (X) */}
          <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold tracking-wider text-slate-500">
                CANCELLED (X)
              </span>
              <XCircle className="h-4 w-4 text-slate-400" />
            </div>
            <div className="mt-2 text-2xl font-bold text-slate-900">{cancelledCount}</div>
          </div>
        </div>

        {/* Search Bar */}
        <div className="relative mt-4">
          <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-400">
            <Search className="h-4 w-4" />
          </div>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by client name, destination, inquiry #, ph..."
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-xs font-medium text-slate-900 focus:border-slate-900 focus:outline-none focus:ring-1 focus:ring-slate-900 placeholder:text-slate-400 shadow-xs"
          />
        </div>

        {/* Status Filter Chips */}
        <div className="mt-3 flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
          <button
            onClick={() => setStatusFilter('all')}
            className={`shrink-0 rounded-lg px-3 py-1.5 text-xs font-bold transition-all ${
              statusFilter === 'all'
                ? 'bg-slate-900 text-white'
                : 'bg-slate-200 text-slate-700 hover:bg-slate-300'
            }`}
          >
            All Statuses
          </button>
          <button
            onClick={() => setStatusFilter('inquiry')}
            className={`shrink-0 rounded-lg px-3 py-1.5 text-xs font-bold transition-all ${
              statusFilter === 'inquiry'
                ? 'bg-slate-900 text-white'
                : 'bg-slate-200 text-slate-700 hover:bg-slate-300'
            }`}
          >
            Pending / Inquiry ({pendingCount})
          </button>
          <button
            onClick={() => setStatusFilter('confirmed')}
            className={`shrink-0 rounded-lg px-3 py-1.5 text-xs font-bold transition-all ${
              statusFilter === 'confirmed'
                ? 'bg-slate-900 text-white'
                : 'bg-slate-200 text-slate-700 hover:bg-slate-300'
            }`}
          >
            Confirmed ({confirmedCount})
          </button>
          <button
            onClick={() => setStatusFilter('cancelled')}
            className={`shrink-0 rounded-lg px-3 py-1.5 text-xs font-bold transition-all ${
              statusFilter === 'cancelled'
                ? 'bg-slate-900 text-white'
                : 'bg-slate-200 text-slate-700 hover:bg-slate-300'
            }`}
          >
            Cancelled ({cancelledCount})
          </button>
        </div>

        {/* Category Sub-Filters */}
        <div className="mt-2.5 flex items-center text-xs font-medium text-slate-500">
          <button
            onClick={() => setTypeFilter('all')}
            className={`hover:text-slate-900 ${typeFilter === 'all' ? 'font-bold text-slate-900' : ''}`}
          >
            All Types
          </button>
          <span className="mx-2 text-slate-300">&middot;</span>
          <button
            onClick={() => setTypeFilter('package')}
            className={`hover:text-slate-900 ${typeFilter === 'package' ? 'font-bold text-slate-900' : ''}`}
          >
            Packages
          </button>
          <span className="mx-2 text-slate-300">&middot;</span>
          <button
            onClick={() => setTypeFilter('ticket')}
            className={`hover:text-slate-900 ${typeFilter === 'ticket' ? 'font-bold text-slate-900' : ''}`}
          >
            Tickets
          </button>
        </div>
      </div>

      {/* Inquiry Cards List */}
      <div className="mt-4 space-y-3.5 px-4">
        {filteredInquiries.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-200 bg-white p-8 text-center text-xs text-slate-400">
            No inquiries match your current filters.
          </div>
        ) : (
          filteredInquiries.map((inquiry) => {
            const isPackage = inquiry.category === 'package';
            const isPending = inquiry.status === 'inquiry';
            const isConfirmed = inquiry.status === 'confirmed';
            const isCancelled = inquiry.status === 'cancelled';

            const datesDisplay =
              inquiry.dateType === 'flexible'
                ? inquiry.flexibleDateRange || (inquiry.startDate && inquiry.endDate ? `Anytime: ${inquiry.startDate} to ${inquiry.endDate}` : 'Flexible dates')
                : (inquiry.startDate && inquiry.endDate ? `${inquiry.startDate} to ${inquiry.endDate}` : (inquiry.startDate || 'Dates to be decided'));

            const paxDisplay = `${inquiry.adults} Adult${inquiry.adults > 1 ? 's' : ''}${
              inquiry.children > 0 ? `, ${inquiry.children} Child${inquiry.children > 1 ? 'ren' : ''}` : ''
            }`;

            const routeText = inquiry.pickupPoint && inquiry.dropPoint
              ? `${inquiry.pickupPoint} → ${inquiry.dropPoint}`
              : (inquiry.pickupPoint ? `From: ${inquiry.pickupPoint}` : (inquiry.dropPoint ? `To: ${inquiry.dropPoint}` : null));

            return (
              <div
                key={inquiry.id}
                className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs transition-shadow hover:shadow-sm"
              >
                {/* Header line: Category Icon + #Number Destination + Status Dot */}
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-slate-100 text-slate-600">
                    {isPackage ? <Luggage className="h-5 w-5" /> : <Plane className="h-5 w-5" />}
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs font-semibold text-slate-400">
                        #{inquiry.inquiryNumber}
                      </span>
                      <h3 className="truncate text-base font-bold text-slate-900">
                        {inquiry.destination}
                      </h3>
                    </div>
                  </div>

                  {/* Status Indicator */}
                  <div>
                    {isPending && (
                      <span className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-600">
                        <span className="h-2 w-2 rounded-full bg-amber-500" />
                        Inquiry (I)
                      </span>
                    )}
                    {isConfirmed && (
                      <span className="inline-flex items-center gap-1.5 text-xs font-bold text-emerald-600">
                        <span className="h-2 w-2 rounded-full bg-emerald-500" />
                        Confirmed (C)
                      </span>
                    )}
                    {isCancelled && (
                      <span className="inline-flex items-center gap-1.5 text-xs font-bold text-rose-600">
                        <span className="h-2 w-2 rounded-full bg-rose-500" />
                        Cancelled (x)
                      </span>
                    )}
                  </div>
                </div>

                {/* Client & Phone */}
                <div className="mt-2.5 text-xs text-slate-600">
                  Client: <strong className="font-semibold text-slate-800">{inquiry.clientName}</strong> &middot; Phone: {inquiry.clientPhone}
                </div>

                {/* Route, Dates, Pax, Flight Class */}
                <div className="mt-1 text-xs text-slate-500 leading-relaxed">
                  {[routeText, datesDisplay, paxDisplay, !isPackage && inquiry.flightClass ? inquiry.flightClass : null].filter(Boolean).join(' · ')}
                </div>

                {/* Stops container (if package and stops exist) */}
                {isPackage && inquiry.stops.length > 0 && (
                  <div className="mt-2.5 rounded-lg border border-slate-200/80 bg-slate-50 px-3 py-1.5 text-xs text-slate-700 font-medium">
                    <span className="text-slate-500 font-semibold">Stops:</span>{' '}
                    {inquiry.stops.map((s) => `${s.placeName} (${s.nights}N)`).join(' → ')}
                  </div>
                )}

                {/* Dispatched Count & View Dispatch Log & Preview Link */}
                <div className="mt-3 flex items-center gap-2 text-xs text-slate-600">
                  <span>Dispatched to {inquiry.dispatches.length} agent(s)</span>
                  <button
                    onClick={() => onViewDispatchLog(inquiry)}
                    className="inline-flex items-center gap-1 font-semibold text-blue-600 hover:text-blue-700"
                  >
                    <Eye className="h-3.5 w-3.5" />
                    <span>View Dispatch Log & Preview</span>
                  </button>
                </div>

                {/* Bottom Status Buttons & Delete */}
                <div className="mt-3.5 flex items-center justify-between border-t border-slate-100 pt-3">
                  <div className="flex items-center gap-1.5">
                    <span className="text-xs font-medium text-slate-400 mr-1">Status:</span>

                    {/* I (Pending) */}
                    <button
                      onClick={() => onStatusChange(inquiry.id, 'inquiry')}
                      className={`rounded-lg px-2.5 py-1 text-xs font-bold transition-all ${
                        isPending
                          ? 'border border-amber-300 bg-amber-50 text-amber-700 shadow-xs'
                          : 'border border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-100'
                      }`}
                    >
                      I (Pending)
                    </button>

                    {/* C (Confirmed) */}
                    <button
                      onClick={() => onStatusChange(inquiry.id, 'confirmed')}
                      className={`rounded-lg px-2.5 py-1 text-xs font-bold transition-all ${
                        isConfirmed
                          ? 'border border-emerald-300 bg-emerald-50 text-emerald-700 shadow-xs'
                          : 'border border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-100'
                      }`}
                    >
                      C (Confirmed)
                    </button>

                    {/* x (Cancelled) */}
                    <button
                      onClick={() => onStatusChange(inquiry.id, 'cancelled')}
                      className={`rounded-lg px-2.5 py-1 text-xs font-bold transition-all ${
                        isCancelled
                          ? 'border border-rose-300 bg-rose-50 text-rose-700 shadow-xs'
                          : 'border border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-100'
                      }`}
                    >
                      x (Cancelled)
                    </button>
                  </div>

                  {/* Delete Trash Icon */}
                  <button
                    onClick={() => onRequestDelete(inquiry)}
                    title="Delete Inquiry permanently"
                    className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 hover:bg-rose-50 hover:text-rose-600 transition-colors"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
