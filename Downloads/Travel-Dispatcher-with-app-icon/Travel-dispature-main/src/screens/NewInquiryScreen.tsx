import React, { useState } from 'react';
import {
  Luggage,
  Plane,
  ShieldCheck,
  Plus,
  X,
  Search,
  Check,
  Send,
} from 'lucide-react';
import { Agent, Inquiry, InquiryStop } from '../types';

interface NewInquiryScreenProps {
  agents: Agent[];
  onSaveInquiry: (
    inquiryData: Omit<Inquiry, 'id' | 'inquiryNumber' | 'createdAt' | 'dispatches'>,
    stops: InquiryStop[],
    selectedAgentIds: string[]
  ) => void;
}

export const NewInquiryScreen: React.FC<NewInquiryScreenProps> = ({
  agents,
  onSaveInquiry,
}) => {
  const [category, setCategory] = useState<'package' | 'ticket'>('package');
  const [flightTripType, setFlightTripType] = useState<'one_way' | 'round_trip'>('one_way');

  // Client Details
  const [clientName, setClientName] = useState('');
  const [clientPhone, setClientPhone] = useState('');
  const [destination, setDestination] = useState('');
  const [pickupPoint, setPickupPoint] = useState('');
  const [dropPoint, setDropPoint] = useState('');

  // Stops for Tour Package
  const [stops, setStops] = useState<InquiryStop[]>([
    { id: '1', stopOrder: 1, placeName: '', nights: 1 },
  ]);

  // Dates
  const [dateType, setDateType] = useState<'fixed' | 'flexible'>('fixed');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [flexibleDateRange, setFlexibleDateRange] = useState('');

  // Passengers & Preferences
  const [adults, setAdults] = useState('2');
  const [children, setChildren] = useState('0');
  const [flightClass, setFlightClass] = useState('Economy');
  const [preferredAirline, setPreferredAirline] = useState('');
  const [specialNotes, setSpecialNotes] = useState('');

  // Agent Dispatch Selection - unchecked by default
  const [selectedAgentIds, setSelectedAgentIds] = useState<string[]>([]);
  const [agentSearch, setAgentSearch] = useState('');

  const [validationError, setValidationError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isPackage = category === 'package';

  const addStop = () => {
    setStops((prev) => [
      ...prev,
      { id: Date.now().toString(), stopOrder: prev.length + 1, placeName: '', nights: 1 },
    ]);
  };

  const updateStop = (index: number, field: 'placeName' | 'nights', val: any) => {
    setStops((prev) => {
      const copy = [...prev];
      copy[index] = { ...copy[index], [field]: val };
      return copy;
    });
  };

  const removeStop = (index: number) => {
    setStops((prev) => prev.filter((_, i) => i !== index));
  };

  const toggleAgent = (agentId: string) => {
    setSelectedAgentIds((prev) =>
      prev.includes(agentId) ? prev.filter((id) => id !== agentId) : [...prev, agentId]
    );
  };

  const handleSelectAllAgents = () => {
    if (selectedAgentIds.length === agents.length) {
      setSelectedAgentIds([]);
    } else {
      setSelectedAgentIds(agents.map((a) => a.id));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!clientName.trim() || !clientPhone.trim() || !destination.trim()) {
      setValidationError('Please fill in Client Name, Client Phone, and Destination.');
      return;
    }

    if (selectedAgentIds.length === 0) {
      setValidationError('Please select at least one partner agent to dispatch quotation requests.');
      return;
    }

    setValidationError(null);
    setIsSubmitting(true);

    const validStops = isPackage ? stops.filter((s) => s.placeName.trim().length > 0) : [];

    onSaveInquiry(
      {
        category,
        clientName: clientName.trim(),
        clientPhone: clientPhone.trim(),
        destination: destination.trim(),
        pickupPoint: pickupPoint.trim() || undefined,
        dropPoint: dropPoint.trim() || undefined,
        dateType,
        startDate: dateType === 'fixed' ? startDate : '',
        endDate: dateType === 'fixed' ? endDate : '',
        flexibleDateRange: dateType === 'flexible' ? flexibleDateRange.trim() : undefined,
        adults: parseInt(adults, 10) || 1,
        children: parseInt(children, 10) || 0,
        flightTripType: !isPackage ? flightTripType : undefined,
        flightClass: !isPackage && flightClass ? flightClass : undefined,
        preferredAirline: !isPackage && preferredAirline.trim() ? preferredAirline.trim() : undefined,
        specialNotes: specialNotes.trim() || undefined,
        status: 'inquiry',
        stops: validStops,
      },
      validStops,
      selectedAgentIds
    );
  };

  const filteredAgents = agents.filter(
    (a) =>
      a.isActive &&
      (!agentSearch ||
        a.name.toLowerCase().includes(agentSearch.toLowerCase()) ||
        a.email.toLowerCase().includes(agentSearch.toLowerCase()) ||
        a.phone.includes(agentSearch))
  );

  return (
    <div className="mx-auto max-w-xl pb-32 pt-3">
      {/* Title */}
      <div className="px-4">
        <h1 className="text-xl font-bold tracking-tight text-slate-900">
          New Travel Inquiry
        </h1>
        <p className="mt-0.5 text-xs text-slate-500">
          Fast intake & one-click quotation dispatching to partner agents
        </p>
      </div>

      <form onSubmit={handleSubmit} className="mt-4 space-y-4 px-4">
        {/* 1. INQUIRY CATEGORY */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs">
          <label className="text-[11px] font-bold tracking-wider text-slate-500 block mb-3">
            INQUIRY CATEGORY
          </label>

          <div className="grid grid-cols-2 gap-2.5">
            <button
              type="button"
              onClick={() => setCategory('package')}
              className={`flex items-center justify-center gap-2 rounded-xl py-3 text-sm font-semibold transition-all ${
                isPackage
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'border border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
              }`}
            >
              <Luggage className="h-4 w-4" />
              <span>Tour Package</span>
            </button>

            <button
              type="button"
              onClick={() => setCategory('ticket')}
              className={`flex items-center justify-center gap-2 rounded-xl py-3 text-sm font-semibold transition-all ${
                !isPackage
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'border border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
              }`}
            >
              <Plane className="h-4 w-4" />
              <span>Flight Ticket</span>
            </button>
          </div>

          {!isPackage && (
            <div className="mt-3.5 flex items-center gap-6 pt-1">
              <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-800">
                <input
                  type="radio"
                  name="tripType"
                  checked={flightTripType === 'one_way'}
                  onChange={() => setFlightTripType('one_way')}
                  className="h-4 w-4 text-slate-900 focus:ring-slate-900"
                />
                <span>One-Way (Single)</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-800">
                <input
                  type="radio"
                  name="tripType"
                  checked={flightTripType === 'round_trip'}
                  onChange={() => setFlightTripType('round_trip')}
                  className="h-4 w-4 text-slate-900 focus:ring-slate-900"
                />
                <span>Round-Trip (Going & Return)</span>
              </label>
            </div>
          )}
        </div>

        {/* 2. CLIENT & DESTINATION DETAILS */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs space-y-3.5">
          <label className="text-[11px] font-bold tracking-wider text-slate-500 block">
            CLIENT & DESTINATION DETAILS
          </label>

          <div>
            <label className="block text-xs font-semibold text-slate-800">
              Client Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Ramesh Patel"
              value={clientName}
              onChange={(e) => setClientName(e.target.value)}
              className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
            />
          </div>

          <div>
            <div className="flex items-center justify-between">
              <label className="text-xs font-semibold text-slate-800">
                Client Phone <span className="text-rose-500">*</span>
              </label>
              <div className="flex items-center gap-1 text-[11px] font-semibold text-emerald-600">
                <ShieldCheck className="h-3.5 w-3.5" />
                <span>Never shared</span>
              </div>
            </div>
            <input
              type="text"
              required
              placeholder="+91 98765 43210"
              value={clientPhone}
              onChange={(e) => setClientPhone(e.target.value)}
              className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-800">
              Destination <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Goa / Dubai / Kashmir"
              value={destination}
              onChange={(e) => setDestination(e.target.value)}
              className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-800">
              Pickup Point
            </label>
            <input
              type="text"
              placeholder="e.g. Mumbai Airport / Ahmedabad"
              value={pickupPoint}
              onChange={(e) => setPickupPoint(e.target.value)}
              className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-800">
              Drop Point
            </label>
            <input
              type="text"
              placeholder="e.g. Hotel Resort / Airport"
              value={dropPoint}
              onChange={(e) => setDropPoint(e.target.value)}
              className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
            />
          </div>
        </div>

        {/* 3. STOPS & NIGHT STAYS (Tour Package) */}
        {isPackage && (
          <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <label className="text-[11px] font-bold tracking-wider text-slate-500 block">
                  STOPS & NIGHT STAYS
                </label>
                <p className="text-[11px] text-slate-400">Add places and nights for this tour</p>
              </div>

              <button
                type="button"
                onClick={addStop}
                className="flex items-center gap-1 rounded-lg border border-slate-200 bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700 hover:bg-slate-200"
              >
                <Plus className="h-3.5 w-3.5" />
                <span>Add Stop</span>
              </button>
            </div>

            <div className="space-y-2 pt-1">
              {stops.map((stop, idx) => (
                <div key={stop.id || idx} className="flex items-center gap-2">
                  <span className="w-5 text-xs font-bold text-slate-400">{idx + 1}.</span>
                  <input
                    type="text"
                    placeholder="Place / City (e.g. North Goa)"
                    value={stop.placeName}
                    onChange={(e) => updateStop(idx, 'placeName', e.target.value)}
                    className="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                  />
                  <div className="flex items-center gap-1.5">
                    <input
                      type="number"
                      min="0"
                      value={stop.nights}
                      onChange={(e) => updateStop(idx, 'nights', parseInt(e.target.value, 10) || 1)}
                      className="w-14 rounded-xl border border-slate-200 px-2.5 py-2 text-xs text-center font-semibold text-slate-900 focus:border-slate-900 focus:outline-none"
                    />
                    <span className="text-xs text-slate-500 font-medium">Nts</span>
                  </div>
                  {stops.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeStop(idx)}
                      className="flex h-7 w-7 items-center justify-center rounded-lg text-slate-400 hover:text-rose-600"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 4. DATES */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs space-y-3.5">
          <label className="text-[11px] font-bold tracking-wider text-slate-500 block">
            DATES
          </label>

          <div className="flex items-center gap-6">
            <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-800">
              <input
                type="radio"
                name="dateType"
                checked={dateType === 'fixed'}
                onChange={() => setDateType('fixed')}
                className="h-4 w-4 text-slate-900 focus:ring-slate-900"
              />
              <span>Fixed Dates</span>
            </label>

            <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-800">
              <input
                type="radio"
                name="dateType"
                checked={dateType === 'flexible'}
                onChange={() => setDateType('flexible')}
                className="h-4 w-4 text-slate-900 focus:ring-slate-900"
              />
              <span>Anytime Between (Flexible)</span>
            </label>
          </div>

          {dateType === 'fixed' ? (
            <div className="grid grid-cols-2 gap-3 pt-1">
              <div>
                <label className="block text-xs font-semibold text-slate-800">
                  Start Date
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-800">
                  End Date
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                />
              </div>
            </div>
          ) : (
            <div className="pt-1">
              <label className="block text-xs font-semibold text-slate-800">
                Travel Date Range / Details
              </label>
              <input
                type="text"
                placeholder="e.g. Anytime between 18 Jan 2027 and 31 Jan 2027, or 1st week of March"
                value={flexibleDateRange}
                onChange={(e) => setFlexibleDateRange(e.target.value)}
                className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
              />
            </div>
          )}
        </div>

        {/* 5. PASSENGERS & PREFERENCES */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs space-y-3.5">
          <label className="text-[11px] font-bold tracking-wider text-slate-500 block">
            PASSENGERS & PREFERENCES
          </label>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-800">
                Adults (12+ yrs) <span className="text-rose-500">*</span>
              </label>
              <input
                type="number"
                min="1"
                required
                value={adults}
                onChange={(e) => setAdults(e.target.value)}
                className="mt-1 w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-800">Children</label>
              <input
                type="number"
                min="0"
                value={children}
                onChange={(e) => setChildren(e.target.value)}
                className="mt-1 w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
              />
            </div>
          </div>

          {!isPackage && (
            <>
              <div>
                <label className="block text-xs font-semibold text-slate-800">Flight Class (Optional)</label>
                <select
                  value={flightClass}
                  onChange={(e) => setFlightClass(e.target.value)}
                  className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                >
                  <option value="Economy">Economy</option>
                  <option value="Premium Economy">Premium Economy</option>
                  <option value="Business">Business</option>
                  <option value="First Class">First Class</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-800">Preferred Airline (Optional)</label>
                <input
                  type="text"
                  placeholder="e.g. IndiGo, Air India, Emirates"
                  value={preferredAirline}
                  onChange={(e) => setPreferredAirline(e.target.value)}
                  className="mt-1 w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                />
              </div>
            </>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-800">
              Special Notes / Requests (Optional)
            </label>
            <textarea
              rows={3}
              placeholder="e.g. 4-star beach resort preferred, vegetarian meals, airport transfer required"
              value={specialNotes}
              onChange={(e) => setSpecialNotes(e.target.value)}
              className="mt-1 w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-900 focus:border-slate-900 focus:outline-none resize-none"
            />
          </div>
        </div>

        {/* 6. DISPATCH TO AGENTS */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <label className="text-[11px] font-bold tracking-wider text-slate-500 block">
              DISPATCH TO AGENTS ({selectedAgentIds.length} SELECTED)
            </label>

            <button
              type="button"
              onClick={handleSelectAllAgents}
              className="text-xs font-semibold text-blue-600 hover:text-blue-700"
            >
              {selectedAgentIds.length === agents.length ? 'Deselect All' : 'Select All'}
            </button>
          </div>
          <p className="text-[11px] text-slate-400">
            Separate email sent to each agent via Gmail SMTP (port 465 SSL)
          </p>

          <div className="relative mt-2">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
              <Search className="h-3.5 w-3.5" />
            </div>
            <input
              type="text"
              placeholder="Search active agents..."
              value={agentSearch}
              onChange={(e) => setAgentSearch(e.target.value)}
              className="w-full rounded-xl border border-slate-200 bg-white py-2 pl-9 pr-3 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
            />
          </div>

          <div className="divide-y divide-slate-100 pt-1">
            {filteredAgents.map((agent) => {
              const isChecked = selectedAgentIds.includes(agent.id);

              return (
                <div
                  key={agent.id}
                  onClick={() => toggleAgent(agent.id)}
                  className="flex items-center justify-between py-2.5 cursor-pointer select-none"
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`flex h-5 w-5 items-center justify-center rounded-md border transition-all ${
                        isChecked
                          ? 'border-slate-900 bg-slate-900 text-white'
                          : 'border-slate-300 bg-white'
                      }`}
                    >
                      {isChecked && <Check className="h-3.5 w-3.5 stroke-[2.5]" />}
                    </div>
                    <div>
                      <h4 className="text-xs font-bold text-slate-900">{agent.name}</h4>
                      <p className="text-[11px] text-slate-500">{agent.email}</p>
                    </div>
                  </div>

                  <span className="text-[11px] text-slate-400 font-medium">{agent.phone}</span>
                </div>
              );
            })}
          </div>
        </div>

        {validationError && (
          <p className="text-xs font-medium text-rose-600 bg-rose-50 border border-rose-100 rounded-xl p-3">
            {validationError}
          </p>
        )}

        {/* Sticky Action Button: Save & Send to Agents */}
        <div className="sticky bottom-18 left-0 right-0 pt-2 z-20">
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-slate-900 py-3.5 text-sm font-bold text-white shadow-lg shadow-slate-900/10 hover:bg-slate-800 active:scale-[0.99] transition-all disabled:opacity-60"
          >
            {isSubmitting ? (
              <span className="flex items-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                Dispatching Emails...
              </span>
            ) : (
              <>
                <Send className="h-4 w-4 stroke-[2.2]" />
                <span>Save & Send to Agents</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
