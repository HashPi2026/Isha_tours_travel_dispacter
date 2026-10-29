export interface InquiryStop {
  id: string;
  inquiryId?: string;
  stopOrder: number;
  placeName: string;
  nights: number;
}

export interface DispatchItem {
  id: string;
  inquiryId: string;
  agentId?: string;
  agentName: string;
  agentEmail: string;
  agentPhone?: string;
  status: 'pending' | 'sent' | 'failed';
  sentAt?: string;
  errorMessage?: string;
  emailHtml?: string;
}

export interface Inquiry {
  id: string;
  inquiryNumber: number;
  category: 'package' | 'ticket';
  clientName: string;
  clientPhone: string;
  destination: string;
  pickupPoint?: string;
  dropPoint?: string;
  dateType: 'fixed' | 'flexible';
  startDate?: string;
  endDate?: string;
  flexibleDateRange?: string;
  adults: number;
  children: number;
  flightTripType?: 'one_way' | 'round_trip';
  flightClass?: string;
  preferredAirline?: string;
  specialNotes?: string;
  status: 'inquiry' | 'confirmed' | 'cancelled';
  createdAt: string;
  stops: InquiryStop[];
  dispatches: DispatchItem[];
}

export interface LedgerSend {
  id: string;
  ledgerRecordId: string;
  agentId?: string;
  agentName: string;
  amountSent: number;
  disbursementDate: string;
  note?: string;
}

export interface LedgerRecord {
  id: string;
  inquiryId?: string;
  clientName: string;
  receivedAmount: number;
  receivedDate: string;
  notes?: string;
  createdAt: string;
  sends: LedgerSend[];
}

export interface Agent {
  id: string;
  name: string;
  email: string;
  phone: string;
  isActive: boolean;
  createdAt?: string;
}

export interface EmailSettings {
  gmailAddress: string;
  appPassword?: string;
  hasAppPasswordSaved?: boolean;
  senderDisplayName: string;
  replyToEmail: string;
}
