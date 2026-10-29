import React, { useState, useEffect } from 'react';
import { TopBar } from './components/TopBar';
import { BottomBar, TabType } from './components/BottomBar';
import { OfflineBanner } from './components/OfflineBanner';
import { DispatchLogModal } from './components/DispatchLogModal';
import { EmailHtmlModal } from './components/EmailHtmlModal';
import { DisburseModal } from './components/DisburseModal';
import { MoneyReceivedModal } from './components/MoneyReceivedModal';
import { AgentModal } from './components/AgentModal';
import { ExportModal } from './components/ExportModal';
import { PermanentDeleteModal } from './components/PermanentDeleteModal';
import { LoginScreen } from './screens/LoginScreen';
import { InquiriesScreen } from './screens/InquiriesScreen';
import { NewInquiryScreen } from './screens/NewInquiryScreen';
import { LedgerScreen } from './screens/LedgerScreen';
import { AgentsScreen } from './screens/AgentsScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { Inquiry, LedgerRecord, Agent, EmailSettings, DispatchItem, LedgerSend } from './types';
import { buildDispatchEmail } from './utils/emailTemplate';
import { exportLedgerPdf } from './utils/pdfExport';

// Initial seed data exactly matching screenshots 01 through 09
const INITIAL_AGENTS: Agent[] = [
  {
    id: 'agent-1',
    name: 'Sunrise Tours',
    email: 'quotes@sunrise.example',
    phone: '+91 98250 11122',
    isActive: true,
  },
  {
    id: 'agent-2',
    name: 'Himalaya Holidays',
    email: 'book@himalaya.example',
    phone: '+91 98790 22233',
    isActive: true,
  },
  {
    id: 'agent-3',
    name: 'Coastal Escapes',
    email: 'hello@coastal.example',
    phone: '+91 99090 33344',
    isActive: true,
  },
  {
    id: 'agent-4',
    name: 'SkyWay Travels',
    email: 'deals@skyway.example',
    phone: '+91 97120 44455',
    isActive: true,
  },
];

const INITIAL_INQUIRIES: Inquiry[] = [
  {
    id: 'inq-1043',
    inquiryNumber: 1043,
    category: 'package',
    clientName: 'Rahul Shah',
    clientPhone: '+91 90000 00000',
    destination: 'Goa',
    pickupPoint: 'Surat',
    dropPoint: 'Surat',
    dateType: 'flexible',
    startDate: '2027-01-18',
    endDate: '2027-01-31',
    adults: 3,
    children: 1,
    status: 'inquiry',
    createdAt: '2026-10-24T10:00:00Z',
    stops: [
      { id: 'stop-1', stopOrder: 1, placeName: 'North Goa', nights: 2 },
      { id: 'stop-2', stopOrder: 2, placeName: 'South Goa', nights: 3 },
    ],
    dispatches: [
      {
        id: 'disp-1',
        inquiryId: 'inq-1043',
        agentId: 'agent-1',
        agentName: 'Sunrise Tours',
        agentEmail: 'quotes@sunrise.example',
        status: 'sent',
        sentAt: '2026-10-24T10:05:00Z',
      },
      {
        id: 'disp-2',
        inquiryId: 'inq-1043',
        agentId: 'agent-2',
        agentName: 'Himalaya Holidays',
        agentEmail: 'book@himalaya.example',
        status: 'sent',
        sentAt: '2026-10-24T10:05:01Z',
      },
      {
        id: 'disp-3',
        inquiryId: 'inq-1043',
        agentId: 'agent-3',
        agentName: 'Coastal Escapes',
        agentEmail: 'hello@coastal.example',
        status: 'failed',
        errorMessage: 'Connection timed out to agent mail exchange',
      },
    ],
  },
  {
    id: 'inq-1042',
    inquiryNumber: 1042,
    category: 'ticket',
    clientName: 'Neha Patel',
    clientPhone: '+91 90000 00000',
    destination: 'Dubai',
    pickupPoint: 'Surat',
    dropPoint: 'Dubai',
    dateType: 'fixed',
    startDate: '2027-02-10',
    endDate: '2027-02-17',
    adults: 2,
    children: 0,
    flightTripType: 'round_trip',
    flightClass: 'Economy',
    status: 'confirmed',
    createdAt: '2026-10-23T08:30:00Z',
    stops: [],
    dispatches: [
      {
        id: 'disp-4',
        inquiryId: 'inq-1042',
        agentId: 'agent-1',
        agentName: 'Sunrise Tours',
        agentEmail: 'quotes@sunrise.example',
        status: 'sent',
        sentAt: '2026-10-23T08:32:00Z',
      },
      {
        id: 'disp-5',
        inquiryId: 'inq-1042',
        agentId: 'agent-4',
        agentName: 'SkyWay Travels',
        agentEmail: 'deals@skyway.example',
        status: 'sent',
        sentAt: '2026-10-23T08:32:01Z',
      },
    ],
  },
  {
    id: 'inq-1041',
    inquiryNumber: 1041,
    category: 'package',
    clientName: 'Amit Joshi',
    clientPhone: '+91 90000 00000',
    destination: 'Manali',
    pickupPoint: 'Ahmedabad',
    dropPoint: 'Ahmedabad',
    dateType: 'fixed',
    startDate: '2026-12-20',
    endDate: '2026-12-26',
    adults: 4,
    children: 2,
    status: 'cancelled',
    createdAt: '2026-10-20T14:15:00Z',
    stops: [
      { id: 'stop-3', stopOrder: 1, placeName: 'Manali', nights: 4 },
      { id: 'stop-4', stopOrder: 2, placeName: 'Kullu', nights: 2 },
    ],
    dispatches: [
      {
        id: 'disp-6',
        inquiryId: 'inq-1041',
        agentId: 'agent-2',
        agentName: 'Himalaya Holidays',
        agentEmail: 'book@himalaya.example',
        status: 'sent',
        sentAt: '2026-10-20T14:18:00Z',
      },
    ],
  },
];

const INITIAL_LEDGER: LedgerRecord[] = [
  {
    id: 'led-1',
    clientName: 'Amit Joshi',
    receivedAmount: 5000,
    receivedDate: '2026-10-15',
    notes: 'Advance, Manali',
    createdAt: '2026-10-15T09:00:00Z',
    sends: [],
  },
  {
    id: 'led-2',
    clientName: 'Neha Patel',
    receivedAmount: 15000,
    receivedDate: '2026-10-12',
    notes: 'Advance, Dubai tickets',
    createdAt: '2026-10-12T11:00:00Z',
    sends: [
      {
        id: 'send-1',
        ledgerRecordId: 'led-2',
        agentId: 'agent-1',
        agentName: 'Sunrise Tours',
        amountSent: 9000,
        disbursementDate: '2026-10-14',
      },
      {
        id: 'send-2',
        ledgerRecordId: 'led-2',
        agentId: 'agent-4',
        agentName: 'SkyWay Travels',
        amountSent: 4000,
        disbursementDate: '2026-10-15',
      },
    ],
  },
];

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(true);
  const [currentOwnerEmail, setCurrentOwnerEmail] = useState('owner@traveldispatcher.local');
  const [currentTab, setCurrentTab] = useState<TabType>('inquiries');
  const [isOffline, setIsOffline] = useState(!navigator.onLine);

  // Data States
  const [inquiries, setInquiries] = useState<Inquiry[]>(() => {
    const saved = localStorage.getItem('td_inquiries');
    return saved ? JSON.parse(saved) : INITIAL_INQUIRIES;
  });

  const [ledgerRecords, setLedgerRecords] = useState<LedgerRecord[]>(() => {
    const saved = localStorage.getItem('td_ledger');
    return saved ? JSON.parse(saved) : INITIAL_LEDGER;
  });

  const [agents, setAgents] = useState<Agent[]>(() => {
    const saved = localStorage.getItem('td_agents');
    return saved ? JSON.parse(saved) : INITIAL_AGENTS;
  });

  const [emailSettings, setEmailSettings] = useState<EmailSettings>(() => {
    const saved = localStorage.getItem('td_email_settings');
    return saved
      ? JSON.parse(saved)
      : {
          gmailAddress: 'mistryjay1804p@gmail.com',
          senderDisplayName: 'Travel Dispatcher',
          replyToEmail: '',
          hasAppPasswordSaved: true,
        };
  });

  // Modal States
  const [selectedInquiryForLog, setSelectedInquiryForLog] = useState<Inquiry | null>(null);
  const [selectedDispatchForHtml, setSelectedDispatchForHtml] = useState<DispatchItem | null>(null);
  const [activeRecordForDisburse, setActiveRecordForDisburse] = useState<LedgerRecord | null>(null);
  const [isMoneyReceivedOpen, setIsMoneyReceivedOpen] = useState(false);
  const [agentModalData, setAgentModalData] = useState<{ isOpen: boolean; agent: Agent | null }>({
    isOpen: false,
    agent: null,
  });
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);

  // Permanent Delete Modal State
  const [deleteModalState, setDeleteModalState] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    action: () => void;
  }>({
    isOpen: false,
    title: '',
    message: '',
    action: () => {},
  });

  // Sync with localStorage
  useEffect(() => {
    localStorage.setItem('td_inquiries', JSON.stringify(inquiries));
  }, [inquiries]);

  useEffect(() => {
    localStorage.setItem('td_ledger', JSON.stringify(ledgerRecords));
  }, [ledgerRecords]);

  useEffect(() => {
    localStorage.setItem('td_agents', JSON.stringify(agents));
  }, [agents]);

  useEffect(() => {
    localStorage.setItem('td_email_settings', JSON.stringify(emailSettings));
  }, [emailSettings]);

  // Online / Offline monitor
  useEffect(() => {
    const handleOnline = () => setIsOffline(false);
    const handleOffline = () => setIsOffline(true);
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Fetch initial data from Supabase DB on startup
  useEffect(() => {
    async function loadData() {
      try {
        const res = await fetch('/api/db/all');
        if (res.ok) {
          const data = await res.json();
          if (data.success) {
            if (Array.isArray(data.agents) && data.agents.length > 0) {
              setAgents(data.agents);
            }
            if (Array.isArray(data.inquiries) && data.inquiries.length > 0) {
              setInquiries(data.inquiries);
            }
            if (Array.isArray(data.ledgerRecords) && data.ledgerRecords.length > 0) {
              setLedgerRecords(data.ledgerRecords);
            }
          }
        }
      } catch (err) {
        console.warn('[DB] Supabase live fetch failed, using local cache:', err);
      }
    }
    loadData();
  }, []);

  // Update Inquiry Status in Supabase & State
  const handleStatusChange = async (inquiryId: string, newStatus: 'inquiry' | 'confirmed' | 'cancelled') => {
    setInquiries((prev) =>
      prev.map((inq) => (inq.id === inquiryId ? { ...inq, status: newStatus } : inq))
    );
    try {
      await fetch(`/api/db/inquiries/${inquiryId}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus }),
      });
    } catch (err) {
      console.error('[DB] Failed to update inquiry status in Supabase:', err);
    }
  };

  // Delete Inquiry permanently from Supabase & State
  const handleRequestDeleteInquiry = (inquiry: Inquiry) => {
    setDeleteModalState({
      isOpen: true,
      title: `Delete Inquiry #${inquiry.inquiryNumber}?`,
      message:
        'This will permanently delete this inquiry, its stops, and all dispatch records from Supabase. Any linked middleman ledger records will remain preserved with the inquiry link cleared (ON DELETE SET NULL).',
      action: async () => {
        setInquiries((prev) => prev.filter((i) => i.id !== inquiry.id));
        setDeleteModalState((s) => ({ ...s, isOpen: false }));
        try {
          await fetch(`/api/db/inquiries/${inquiry.id}`, { method: 'DELETE' });
        } catch (err) {
          console.error('[DB] Failed to delete inquiry from Supabase:', err);
        }
      },
    });
  };

  // Save new inquiry to Supabase and trigger dispatches via real SMTP
  const handleSaveInquiry = async (
    inquiryData: Omit<Inquiry, 'id' | 'inquiryNumber' | 'createdAt' | 'dispatches'>,
    stops: any[],
    selectedAgentIds: string[]
  ) => {
    const nextNumber =
      inquiries.length > 0 ? Math.max(...inquiries.map((i) => i.inquiryNumber)) + 1 : 1001;

    const tempInquiryId = `inq-${Date.now()}`;

    // Build quotation email content (never contains client phone!)
    const built = buildDispatchEmail({
      ...inquiryData,
      id: tempInquiryId,
      inquiryNumber: nextNumber,
      createdAt: new Date().toISOString(),
      dispatches: [],
      stops,
    });

    // Initialize dispatches
    const initialDispatches: DispatchItem[] = selectedAgentIds.map((agentId) => {
      const agent = agents.find((a) => a.id === agentId);
      return {
        id: `disp-${Date.now()}-${agentId}`,
        inquiryId: tempInquiryId,
        agentId,
        agentName: agent ? agent.name : 'Partner Agent',
        agentEmail: agent ? agent.email : 'quotes@example.com',
        status: 'pending',
        emailHtml: built.htmlBody,
      };
    });

    let savedInquiry: Inquiry = {
      ...inquiryData,
      id: tempInquiryId,
      inquiryNumber: nextNumber,
      createdAt: new Date().toISOString(),
      stops,
      dispatches: initialDispatches,
    };

    // 1. Save directly into Supabase database
    try {
      const dbRes = await fetch('/api/db/inquiries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          inquiryData,
          stops,
          dispatches: initialDispatches,
        }),
      });
      if (dbRes.ok) {
        const dbData = await dbRes.json();
        if (dbData.success && dbData.inquiry) {
          savedInquiry = dbData.inquiry;
        }
      }
    } catch (err) {
      console.warn('[DB] Offline saving inquiry:', err);
    }

    // Add to list and switch view
    setInquiries((prev) => [savedInquiry, ...prev]);
    setCurrentTab('inquiries');

    // 2. Asynchronously dispatch real emails to agents one by one with 600ms gap
    for (let i = 0; i < savedInquiry.dispatches.length; i++) {
      const item = savedInquiry.dispatches[i];

      try {
        const response = await fetch('/api/send-email', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            to: item.agentEmail,
            subject: built.subject,
            text: built.plainText,
            html: built.htmlBody,
            gmailAddress: emailSettings.gmailAddress,
            appPassword: emailSettings.appPassword,
            senderDisplayName: emailSettings.senderDisplayName,
            replyTo: emailSettings.replyToEmail,
          }),
        });

        const data = await response.json();
        const isSuccess = response.ok && data.success;
        const status = isSuccess ? 'sent' : 'failed';
        const errorMessage = isSuccess ? undefined : (data.error || 'Failed to deliver quotation email');
        const sentAt = isSuccess ? new Date().toISOString() : undefined;

        // Update in Supabase
        if (item.id && !item.id.startsWith('disp-')) {
          fetch(`/api/db/dispatches/${item.id}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status, sentAt, errorMessage }),
          }).catch(console.error);
        }

        // Update in state
        setInquiries((prev) =>
          prev.map((inq) => {
            if (inq.id === savedInquiry.id) {
              return {
                ...inq,
                dispatches: inq.dispatches.map((d) =>
                  d.id === item.id
                    ? { ...d, status, sentAt, errorMessage }
                    : d
                ),
              };
            }
            return inq;
          })
        );
      } catch (err: any) {
        setInquiries((prev) =>
          prev.map((inq) => {
            if (inq.id === savedInquiry.id) {
              return {
                ...inq,
                dispatches: inq.dispatches.map((d) =>
                  d.id === item.id
                    ? { ...d, status: 'failed', errorMessage: err.message || 'Network error connecting to email service' }
                    : d
                ),
              };
            }
            return inq;
          })
        );
      }

      // 600 ms throttle between dispatches
      if (i < savedInquiry.dispatches.length - 1) {
        await new Promise((resolve) => setTimeout(resolve, 600));
      }
    }
  };

  // Retry failed dispatch via real SMTP
  const handleRetryDispatch = async (dispatch: DispatchItem) => {
    const inq = inquiries.find((i) => i.id === dispatch.inquiryId);
    if (!inq) return;

    const built = buildDispatchEmail(inq);

    // Set to pending while sending
    setInquiries((prev) =>
      prev.map((i) => {
        if (i.id === dispatch.inquiryId) {
          return {
            ...i,
            dispatches: i.dispatches.map((d) => (d.id === dispatch.id ? { ...d, status: 'pending' as const } : d)),
          };
        }
        return i;
      })
    );

    try {
      const response = await fetch('/api/send-email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          to: dispatch.agentEmail,
          subject: built.subject,
          text: built.plainText,
          html: built.htmlBody,
          gmailAddress: emailSettings.gmailAddress,
          appPassword: emailSettings.appPassword,
          senderDisplayName: emailSettings.senderDisplayName,
          replyTo: emailSettings.replyToEmail,
        }),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        setInquiries((prev) =>
          prev.map((i) => {
            if (i.id === dispatch.inquiryId) {
              return {
                ...i,
                dispatches: i.dispatches.map((d) =>
                  d.id === dispatch.id
                    ? { ...d, status: 'sent', sentAt: new Date().toISOString(), errorMessage: undefined }
                    : d
                ),
              };
            }
            return i;
          })
        );
      } else {
        setInquiries((prev) =>
          prev.map((i) => {
            if (i.id === dispatch.inquiryId) {
              return {
                ...i,
                dispatches: i.dispatches.map((d) =>
                  d.id === dispatch.id
                    ? { ...d, status: 'failed', errorMessage: data.error || 'Retry delivery failed' }
                    : d
                ),
              };
            }
            return i;
          })
        );
      }
    } catch (err: any) {
      setInquiries((prev) =>
        prev.map((i) => {
          if (i.id === dispatch.inquiryId) {
            return {
              ...i,
              dispatches: i.dispatches.map((d) =>
                d.id === dispatch.id
                  ? { ...d, status: 'failed', errorMessage: err.message || 'Network error on retry' }
                  : d
              ),
            };
          }
          return i;
        })
      );
    }
  };

  // Add Sent Amount in Middleman Ledger (saved to Supabase)
  const handleAddSentAmount = async (
    recordId: string,
    agentId: string | undefined,
    agentName: string,
    amountSent: number,
    disbursementDate: string,
    note?: string
  ) => {
    let newSend: LedgerSend = {
      id: `send-${Date.now()}`,
      ledgerRecordId: recordId,
      agentId,
      agentName,
      amountSent,
      disbursementDate,
      note,
    };

    try {
      const res = await fetch('/api/db/ledger/disburse', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ledgerRecordId: recordId,
          agentId,
          agentName,
          amountSent,
          disbursementDate,
          note,
        }),
      });
      const data = await res.json();
      if (res.ok && data.success && data.send) {
        newSend = data.send;
      } else if (!res.ok) {
        alert(data.error || 'Failed to record disbursement in Supabase');
        return;
      }
    } catch (err) {
      console.warn('[DB] Offline ledger send:', err);
    }

    setLedgerRecords((prev) =>
      prev.map((rec) =>
        rec.id === recordId ? { ...rec, sends: [newSend, ...rec.sends] } : rec
      )
    );
  };

  // Delete Ledger Record permanently from Supabase & State
  const handleDeleteLedgerRecord = (record: LedgerRecord) => {
    setDeleteModalState({
      isOpen: true,
      title: `Delete Ledger Record for ${record.clientName}?`,
      message: `Warning: This will permanently delete this client's booking record and ALL ${record.sends.length} associated agent disbursement records from Supabase.`,
      action: async () => {
        setLedgerRecords((prev) => prev.filter((r) => r.id !== record.id));
        setDeleteModalState((s) => ({ ...s, isOpen: false }));
        try {
          await fetch(`/api/db/ledger/${record.id}`, { method: 'DELETE' });
        } catch (err) {
          console.error('[DB] Failed to delete ledger record from Supabase:', err);
        }
      },
    });
  };

  // Delete single sent disbursement from Supabase & State
  const handleDeleteSend = (record: LedgerRecord, send: LedgerSend) => {
    setDeleteModalState({
      isOpen: true,
      title: `Delete Disbursement to ${send.agentName}?`,
      message: `This will permanently delete the Rs. ${send.amountSent.toLocaleString()} disbursement and restore the held client balance.`,
      action: async () => {
        setLedgerRecords((prev) =>
          prev.map((r) =>
            r.id === record.id ? { ...r, sends: r.sends.filter((s) => s.id !== send.id) } : r
          )
        );
        setDeleteModalState((s) => ({ ...s, isOpen: false }));
        try {
          await fetch(`/api/db/ledger/sends/${send.id}`, { method: 'DELETE' });
        } catch (err) {
          console.error('[DB] Failed to delete disbursement send from Supabase:', err);
        }
      },
    });
  };

  // Save new client funds into ledger (saved to Supabase)
  const handleSaveMoneyReceived = async (
    clientName: string,
    amount: number,
    date: string,
    notes?: string
  ) => {
    let newRecord: LedgerRecord = {
      id: `led-${Date.now()}`,
      clientName,
      receivedAmount: amount,
      receivedDate: date,
      notes,
      createdAt: new Date().toISOString(),
      sends: [],
    };

    try {
      const res = await fetch('/api/db/ledger', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          clientName,
          receivedAmount: amount,
          receivedDate: date,
          notes,
        }),
      });
      if (res.ok) {
        const data = await res.json();
        if (data.success && data.record) {
          newRecord = data.record;
        }
      }
    } catch (err) {
      console.warn('[DB] Offline ledger record:', err);
    }

    setLedgerRecords((prev) => [newRecord, ...prev]);
  };

  // Agent CRUD (saved to Supabase)
  const handleSaveAgent = async (agentData: Omit<Agent, 'id' | 'createdAt'>, agentId?: string) => {
    try {
      const res = await fetch('/api/db/agents', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...agentData,
          id: agentId,
        }),
      });
      if (res.ok) {
        const data = await res.json();
        if (data.success && data.agent) {
          if (agentId) {
            setAgents((prev) => prev.map((a) => (a.id === agentId ? { ...a, ...data.agent } : a)));
          } else {
            setAgents((prev) => [...prev, data.agent]);
          }
          return;
        }
      }
    } catch (err) {
      console.warn('[DB] Offline agent save:', err);
    }

    if (agentId) {
      setAgents((prev) =>
        prev.map((a) => (a.id === agentId ? { ...a, ...agentData } : a))
      );
    } else {
      const newAgent: Agent = {
        ...agentData,
        id: `agent-${Date.now()}`,
        createdAt: new Date().toISOString(),
      };
      setAgents((prev) => [...prev, newAgent]);
    }
  };

  const handleDeleteAgent = (agent: Agent) => {
    setDeleteModalState({
      isOpen: true,
      title: `Delete Agent ${agent.name}?`,
      message: `This will permanently delete ${agent.name} from your partner directory. All past dispatches and middleman ledger disbursements will preserve "${agent.name}" as historical text.`,
      action: async () => {
        setAgents((prev) => prev.filter((a) => a.id !== agent.id));
        setDeleteModalState((s) => ({ ...s, isOpen: false }));
        try {
          await fetch(`/api/db/agents/${agent.id}`, { method: 'DELETE' });
        } catch (err) {
          console.error('[DB] Failed to delete agent from Supabase:', err);
        }
      },
    });
  };

  // Export JSON Backup
  const handleExportBackup = () => {
    const backupData = {
      exported_at: new Date().toISOString(),
      owner_email: currentOwnerEmail,
      inquiries_count: inquiries.length,
      ledger_records_count: ledgerRecords.length,
      agents_count: agents.length,
      inquiries,
      ledger_records: ledgerRecords,
      agents,
    };

    const blob = new Blob([JSON.stringify(backupData, null, 2)], {
      type: 'application/json;charset=utf-8;',
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `travel_dispatcher_backup_${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  if (!isAuthenticated) {
    return (
      <LoginScreen
        onLoginSuccess={(email) => {
          setCurrentOwnerEmail(email);
          setIsAuthenticated(true);
        }}
      />
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-sans">
      {/* Sticky Top Bar matching screenshot */}
      <TopBar
        onSettingsClick={() => setCurrentTab('settings')}
        onLogoutClick={() => setIsAuthenticated(false)}
      />

      {/* Offline Banner */}
      <OfflineBanner isOffline={isOffline} />

      {/* Main Screen Content */}
      <main className="flex-1">
        {currentTab === 'inquiries' && (
          <InquiriesScreen
            inquiries={inquiries}
            onNewInquiryClick={() => setCurrentTab('new')}
            onOpenExportModal={() => setIsExportModalOpen(true)}
            onViewDispatchLog={(inq) => setSelectedInquiryForLog(inq)}
            onStatusChange={handleStatusChange}
            onRequestDelete={handleRequestDeleteInquiry}
          />
        )}

        {currentTab === 'new' && (
          <NewInquiryScreen
            agents={agents}
            onSaveInquiry={handleSaveInquiry}
          />
        )}

        {currentTab === 'ledger' && (
          <LedgerScreen
            records={ledgerRecords}
            onOpenMoneyReceived={() => setIsMoneyReceivedOpen(true)}
            onOpenDisburseModal={(rec) => setActiveRecordForDisburse(rec)}
            onDeleteRecord={handleDeleteLedgerRecord}
            onDeleteSend={handleDeleteSend}
            onExportPdf={() => exportLedgerPdf(ledgerRecords)}
          />
        )}

        {currentTab === 'agents' && (
          <AgentsScreen
            agents={agents}
            onOpenAddAgent={() => setAgentModalData({ isOpen: true, agent: null })}
            onEditAgent={(agent) => setAgentModalData({ isOpen: true, agent })}
            onDeleteAgent={handleDeleteAgent}
          />
        )}

        {currentTab === 'settings' && (
          <SettingsScreen
            currentOwnerEmail={currentOwnerEmail}
            emailSettings={emailSettings}
            onSaveEmailSettings={(settings) => setEmailSettings(settings)}
            onSendTestEmail={async (callback) => {
              try {
                const response = await fetch('/api/test-email', {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/json' },
                  body: JSON.stringify({
                    gmailAddress: emailSettings.gmailAddress,
                    appPassword: emailSettings.appPassword,
                    senderDisplayName: emailSettings.senderDisplayName,
                  }),
                });
                const data = await response.json();
                if (response.ok && data.success) {
                  callback(true, data.message || `Test email successfully sent to ${emailSettings.gmailAddress}!`);
                } else {
                  callback(false, data.error || 'SMTP verification failed.');
                }
              } catch (err: any) {
                callback(false, err.message || 'Network error connecting to test email server.');
              }
            }}
            onExportBackupJson={handleExportBackup}
            onSignOut={() => setIsAuthenticated(false)}
          />
        )}
      </main>

      {/* Bottom Navigation Bar */}
      <BottomBar currentTab={currentTab} onTabChange={(t) => setCurrentTab(t)} />

      {/* MODALS */}
      {/* 1. Dispatch Log Modal (Screenshot 03) */}
      <DispatchLogModal
        inquiry={selectedInquiryForLog}
        onClose={() => setSelectedInquiryForLog(null)}
        onRetryDispatch={handleRetryDispatch}
        onViewEmailHtml={(dispatch) => {
          if (!dispatch.emailHtml && selectedInquiryForLog) {
            const built = buildDispatchEmail(selectedInquiryForLog);
            setSelectedDispatchForHtml({ ...dispatch, emailHtml: built.htmlBody });
          } else {
            setSelectedDispatchForHtml(dispatch);
          }
        }}
      />

      {/* 2. Email HTML Preview Modal */}
      <EmailHtmlModal
        dispatch={selectedDispatchForHtml}
        onClose={() => setSelectedDispatchForHtml(null)}
      />

      {/* 3. Disburse to Agent Modal (Screenshot 07) */}
      <DisburseModal
        record={activeRecordForDisburse}
        agents={agents}
        isOpen={!!activeRecordForDisburse}
        onClose={() => setActiveRecordForDisburse(null)}
        onAddSentAmount={handleAddSentAmount}
      />

      {/* 4. Money Received Modal */}
      <MoneyReceivedModal
        isOpen={isMoneyReceivedOpen}
        onClose={() => setIsMoneyReceivedOpen(false)}
        onSave={handleSaveMoneyReceived}
      />

      {/* 5. Agent Add/Edit Modal */}
      <AgentModal
        isOpen={agentModalData.isOpen}
        agent={agentModalData.agent}
        onClose={() => setAgentModalData({ isOpen: false, agent: null })}
        onSave={handleSaveAgent}
      />

      {/* 6. Export Modal with Phone Privacy Guard */}
      <ExportModal
        isOpen={isExportModalOpen}
        inquiries={inquiries}
        onClose={() => setIsExportModalOpen(false)}
      />

      {/* 7. Permanent Delete Modal */}
      <PermanentDeleteModal
        isOpen={deleteModalState.isOpen}
        title={deleteModalState.title}
        message={deleteModalState.message}
        onConfirm={deleteModalState.action}
        onClose={() => setDeleteModalState((s) => ({ ...s, isOpen: false }))}
      />
    </div>
  );
}
