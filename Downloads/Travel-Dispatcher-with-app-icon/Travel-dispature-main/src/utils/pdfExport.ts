import jsPDF from 'jspdf';
import { Inquiry, LedgerRecord } from '../types';

export function exportInquiriesPdf(inquiries: Inquiry[], includePhone: boolean, periodName: string = 'All Records') {
  const doc = new jsPDF();

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(18);
  doc.setTextColor(15, 23, 42); // slate-900
  doc.text('Travel Inquiries Report', 14, 20);

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);
  doc.setTextColor(100, 116, 139); // slate-500
  const dateStr = new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
  doc.text(`Period: ${periodName} | Generated: ${dateStr} | Total Records: ${inquiries.length}`, 14, 27);
  doc.text(`Phone Privacy: ${includePhone ? 'Included (Confidential)' : 'Masked (Privacy Mode Active)'}`, 14, 33);

  doc.setDrawColor(226, 232, 240);
  doc.line(14, 36, 196, 36);

  let y = 44;
  inquiries.forEach((inq, index) => {
    if (y > 270) {
      doc.addPage();
      y = 20;
    }

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(11);
    doc.setTextColor(15, 23, 42);
    const categoryLabel = inq.category === 'ticket' ? 'Flight Ticket' : 'Tour Package';
    doc.text(`#${inq.inquiryNumber}  ${inq.destination} (${categoryLabel})`, 14, y);

    // Status pill text
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(9);
    if (inq.status === 'confirmed') {
      doc.setTextColor(22, 163, 74);
      doc.text('CONFIRMED', 160, y);
    } else if (inq.status === 'cancelled') {
      doc.setTextColor(220, 38, 38);
      doc.text('CANCELLED', 160, y);
    } else {
      doc.setTextColor(217, 119, 6);
      doc.text('PENDING', 160, y);
    }

    y += 6;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(51, 65, 85);
    const phoneDisplay = includePhone ? inq.clientPhone : '[Hidden for Privacy]';
    doc.text(`Client: ${inq.clientName} · Phone: ${phoneDisplay}`, 14, y);

    y += 5;
    const dateDisplay = inq.dateType === 'flexible'
      ? (inq.flexibleDateRange || (inq.startDate && inq.endDate ? `Anytime: ${inq.startDate} to ${inq.endDate}` : 'Flexible dates'))
      : (inq.startDate && inq.endDate ? `${inq.startDate} to ${inq.endDate}` : (inq.startDate || 'Dates to be decided'));
    const routeDisplay = inq.pickupPoint && inq.dropPoint ? `${inq.pickupPoint} -> ${inq.dropPoint}` : (inq.pickupPoint || inq.dropPoint || 'Direct');
    doc.text(`Route: ${routeDisplay} | Dates: ${dateDisplay} | Pax: ${inq.adults}A, ${inq.children}C`, 14, y);

    if (inq.category === 'package' && inq.stops.length > 0) {
      y += 5;
      const stopsStr = inq.stops.map(s => `${s.placeName} (${s.nights}N)`).join(' -> ');
      doc.setTextColor(71, 85, 105);
      doc.text(`Stops: ${stopsStr}`, 14, y);
    }

    y += 5;
    doc.setTextColor(100, 116, 139);
    doc.text(`Dispatched to ${inq.dispatches.length} agent(s)`, 14, y);

    y += 4;
    doc.setDrawColor(241, 245, 249);
    doc.line(14, y, 196, y);
    y += 8;
  });

  doc.save(`travel_inquiries_${Date.now()}.pdf`);
}

export function exportInquiriesCsv(inquiries: Inquiry[], includePhone: boolean) {
  const headers = [
    'Inquiry #',
    'Category',
    'Client Name',
    'Client Phone',
    'Destination',
    'Pickup Point',
    'Drop Point',
    'Date Type',
    'Start Date',
    'End Date',
    'Adults',
    'Children',
    'Status',
    'Dispatched Count'
  ];

  const rows = inquiries.map(inq => [
    inq.inquiryNumber,
    inq.category,
    `"${inq.clientName.replace(/"/g, '""')}"`,
    includePhone ? `"${inq.clientPhone.replace(/"/g, '""')}"` : '"[HIDDEN]"',
    `"${inq.destination.replace(/"/g, '""')}"`,
    `"${(inq.pickupPoint || '').replace(/"/g, '""')}"`,
    `"${(inq.dropPoint || '').replace(/"/g, '""')}"`,
    inq.dateType,
    inq.startDate || (inq.flexibleDateRange ? `"${inq.flexibleDateRange.replace(/"/g, '""')}"` : ''),
    inq.endDate || '',
    inq.adults,
    inq.children,
    inq.status,
    inq.dispatches.length
  ]);

  const csvContent = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', `travel_inquiries_${Date.now()}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

export function exportLedgerPdf(records: LedgerRecord[]) {
  const doc = new jsPDF();

  const totalReceived = records.reduce((sum, r) => sum + r.receivedAmount, 0);
  const totalSent = records.reduce((sum, r) => sum + r.sends.reduce((s2, snd) => s2 + snd.amountSent, 0), 0);
  const balanceWithMe = Math.max(0, totalReceived - totalSent);

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(18);
  doc.setTextColor(15, 23, 42);
  doc.text('Khatabook Middleman Ledger', 14, 20);

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);
  doc.setTextColor(100, 116, 139);
  const dateStr = new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
  doc.text(`Generated: ${dateStr} · Client funds & agent disbursement tracking`, 14, 27);

  // Summary Stat Box
  doc.setFillColor(248, 250, 252);
  doc.setDrawColor(226, 232, 240);
  doc.roundedRect(14, 32, 182, 22, 3, 3, 'FD');

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(9);
  doc.setTextColor(100, 116, 139);
  doc.text('TOTAL RECEIVED', 20, 40);
  doc.text('TOTAL SENT', 80, 40);
  doc.text('BALANCE WITH ME', 140, 40);

  doc.setFontSize(12);
  doc.setTextColor(15, 23, 42);
  doc.text(`Rs. ${totalReceived.toLocaleString()}`, 20, 48);
  doc.setTextColor(220, 38, 38);
  doc.text(`Rs. ${totalSent.toLocaleString()}`, 80, 48);
  doc.setTextColor(22, 163, 74);
  doc.text(`Rs. ${balanceWithMe.toLocaleString()}`, 140, 48);

  let y = 64;
  records.forEach((record) => {
    if (y > 260) {
      doc.addPage();
      y = 20;
    }

    const recSent = record.sends.reduce((sum, s) => sum + s.amountSent, 0);
    const recBalance = Math.max(0, record.receivedAmount - recSent);

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(12);
    doc.setTextColor(15, 23, 42);
    doc.text(record.clientName, 14, y);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(100, 116, 139);
    const noteText = record.notes ? ` · ${record.notes}` : '';
    doc.text(`Date: ${record.receivedDate}${noteText}`, 14, y + 5);

    // Mini breakdown
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.text(`Received: Rs. ${record.receivedAmount.toLocaleString()} | Sent Total: Rs. ${recSent.toLocaleString()} | Balance Held: Rs. ${recBalance.toLocaleString()}`, 14, y + 11);

    y += 16;

    if (record.sends.length > 0) {
      record.sends.forEach(send => {
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(8.5);
        doc.setTextColor(71, 85, 105);
        doc.text(`  • ${send.agentName} (${send.disbursementDate}): Rs. ${send.amountSent.toLocaleString()}${send.note ? ` - ${send.note}` : ''}`, 16, y);
        y += 5;
      });
    }

    doc.setDrawColor(241, 245, 249);
    doc.line(14, y + 2, 196, y + 2);
    y += 8;
  });

  doc.save(`travel_middleman_ledger_${Date.now()}.pdf`);
}
