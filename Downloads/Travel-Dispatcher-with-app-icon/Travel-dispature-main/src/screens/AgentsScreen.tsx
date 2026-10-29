import React, { useState } from 'react';
import { UserPlus, Search, Mail, Phone, Edit, Trash2, CheckCircle2 } from 'lucide-react';
import { Agent } from '../types';

interface AgentsScreenProps {
  agents: Agent[];
  onOpenAddAgent: () => void;
  onEditAgent: (agent: Agent) => void;
  onDeleteAgent: (agent: Agent) => void;
}

export const AgentsScreen: React.FC<AgentsScreenProps> = ({
  agents,
  onOpenAddAgent,
  onEditAgent,
  onDeleteAgent,
}) => {
  const [searchQuery, setSearchQuery] = useState('');

  const filteredAgents = agents.filter(
    (a) =>
      !searchQuery ||
      a.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      a.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      a.phone.includes(searchQuery)
  );

  return (
    <div className="mx-auto max-w-xl pb-24 pt-3">
      {/* Title & Subtitle */}
      <div className="px-4">
        <h1 className="text-xl font-bold tracking-tight text-slate-900">
          Travel Agents Directory
        </h1>
        <p className="mt-0.5 text-xs text-slate-500">
          Manage partner travel agents and quotation contacts
        </p>

        {/* + Add New Agent Button */}
        <button
          onClick={onOpenAddAgent}
          className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl bg-slate-900 py-3 text-xs font-bold text-white shadow-sm hover:bg-slate-800 active:scale-[0.99] transition-all"
        >
          <UserPlus className="h-4 w-4" />
          <span>Add New Agent</span>
        </button>

        {/* Search Bar */}
        <div className="relative mt-3.5">
          <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-400">
            <Search className="h-4 w-4" />
          </div>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search agents by name, email or phone..."
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-xs font-medium text-slate-900 focus:border-slate-900 focus:outline-none focus:ring-1 focus:ring-slate-900 placeholder:text-slate-400 shadow-xs"
          />
        </div>
      </div>

      {/* Agents Cards List */}
      <div className="mt-4 space-y-3.5 px-4">
        {filteredAgents.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-200 bg-white p-8 text-center text-xs text-slate-400">
            No agents found matching &quot;{searchQuery}&quot;. Click &quot;Add New Agent&quot; to add one.
          </div>
        ) : (
          filteredAgents.map((agent) => (
            <div
              key={agent.id}
              className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs transition-shadow hover:shadow-sm"
            >
              <div className="flex items-start justify-between">
                <h3 className="text-base font-bold text-slate-900">{agent.name}</h3>

                {agent.isActive ? (
                  <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-600">
                    <CheckCircle2 className="h-4 w-4" />
                    Active
                  </span>
                ) : (
                  <span className="text-xs font-medium text-slate-400">Inactive</span>
                )}
              </div>

              {/* Email */}
              <div className="mt-2.5 flex items-center gap-2 text-xs text-slate-600">
                <Mail className="h-4 w-4 text-slate-400 shrink-0" />
                <span className="truncate">{agent.email}</span>
              </div>

              {/* Phone */}
              <div className="mt-1.5 flex items-center gap-2 text-xs text-slate-600">
                <Phone className="h-4 w-4 text-slate-400 shrink-0" />
                <span>{agent.phone}</span>
              </div>

              {/* Actions: Edit & Delete */}
              <div className="mt-3.5 flex items-center justify-end gap-5 border-t border-slate-100 pt-3">
                <button
                  type="button"
                  onClick={() => onEditAgent(agent)}
                  className="flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900"
                >
                  <Edit className="h-3.5 w-3.5" />
                  <span>Edit</span>
                </button>

                <button
                  type="button"
                  onClick={() => onDeleteAgent(agent)}
                  className="flex items-center gap-1.5 text-xs font-semibold text-rose-600 hover:text-rose-700"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                  <span>Delete</span>
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
