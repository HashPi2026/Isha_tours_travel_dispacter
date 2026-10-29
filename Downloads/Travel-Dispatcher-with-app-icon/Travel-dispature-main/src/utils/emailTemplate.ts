import { Inquiry } from '../types';

export function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

export function formatHumanDate(rawDate: string): string {
  try {
    const parts = rawDate.split('-');
    if (parts.length === 3) {
      const year = parts[0];
      const monthIdx = parseInt(parts[1], 10) - 1;
      const day = parts[2];
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      return `${parseInt(day, 10)} ${months[monthIdx]} ${year}`;
    }
    return rawDate;
  } catch {
    return rawDate;
  }
}

/**
 * Builds email strictly without client phone.
 */
export function buildDispatchEmail(inquiry: Inquiry) {
  const safeClientName = escapeHtml(inquiry.clientName);
  const safeDestination = escapeHtml(inquiry.destination);
  const safePickup = inquiry.pickupPoint ? escapeHtml(inquiry.pickupPoint) : '';
  const safeDrop = inquiry.dropPoint ? escapeHtml(inquiry.dropPoint) : '';
  const isPackage = inquiry.category === 'package';
  const categoryLabel = isPackage ? 'Package' : 'Flight Ticket';
  const subject = `New ${categoryLabel} Inquiry #${inquiry.inquiryNumber} - ${inquiry.destination}`;

  let formattedDates = '';
  if (inquiry.dateType === 'flexible') {
    if (inquiry.flexibleDateRange && inquiry.flexibleDateRange.trim()) {
      formattedDates = escapeHtml(inquiry.flexibleDateRange.trim());
    } else if (inquiry.startDate && inquiry.endDate) {
      formattedDates = `Anytime between ${formatHumanDate(inquiry.startDate)} and ${formatHumanDate(inquiry.endDate)}`;
    } else {
      formattedDates = 'Flexible (Anytime Between)';
    }
  } else {
    if (inquiry.startDate && inquiry.endDate) {
      formattedDates = `${formatHumanDate(inquiry.startDate)} to ${formatHumanDate(inquiry.endDate)}`;
    } else if (inquiry.startDate) {
      formattedDates = formatHumanDate(inquiry.startDate);
    } else {
      formattedDates = 'Dates to be decided';
    }
  }

  const passengersStr = `${inquiry.adults} Adult${inquiry.adults > 1 ? 's' : ''}${inquiry.children > 0 ? `, ${inquiry.children} Child${inquiry.children > 1 ? 'ren' : ''}` : ''}`;

  const stopsHtml = isPackage && inquiry.stops.length > 0
    ? inquiry.stops.map(s => `${escapeHtml(s.placeName)} (${s.nights}N)`).join(' &rarr; ')
    : '';

  const stopsText = isPackage && inquiry.stops.length > 0
    ? inquiry.stops.map(s => `${s.placeName} (${s.nights}N)`).join(' -> ')
    : '';

  const tripTypeLabel = inquiry.flightTripType === 'round_trip' ? 'Round-Trip (Going & Return)' : 'One-Way (Single)';

  const plainText = [
    `NEW TRAVEL INQUIRY #${inquiry.inquiryNumber}`,
    '----------------------------------------',
    `Category: ${categoryLabel}`,
    `Client Name: ${inquiry.clientName}`,
    `Destination: ${inquiry.destination}`,
    inquiry.pickupPoint ? `Pickup Point: ${inquiry.pickupPoint}` : '',
    inquiry.dropPoint ? `Drop Point: ${inquiry.dropPoint}` : '',
    `Travel Dates: ${formattedDates}`,
    `Passengers: ${passengersStr}`,
    isPackage && stopsText ? `Itinerary Stops: ${stopsText}` : '',
    !isPackage ? `Trip Type: ${tripTypeLabel}` : '',
    !isPackage && inquiry.flightClass ? `Class: ${inquiry.flightClass}` : '',
    !isPackage && inquiry.preferredAirline ? `Preferred Airline: ${inquiry.preferredAirline}` : '',
    inquiry.specialNotes ? `Special Notes: ${inquiry.specialNotes}` : '',
    '----------------------------------------',
    'Please reply with your best quote.'
  ].filter(Boolean).join('\n');

  const htmlBody = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1e293b; background: #f8fafc; margin: 0; padding: 24px; }
    .card { max-width: 600px; margin: 0 auto; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; }
    .header { background: #0f172a; color: #ffffff; padding: 20px 24px; }
    .header h1 { margin: 0; font-size: 18px; font-weight: 700; }
    .header p { margin: 4px 0 0; font-size: 13px; color: #94a3b8; }
    .content { padding: 24px; }
    .row { display: flex; margin-bottom: 12px; font-size: 14px; }
    .label { font-weight: 600; color: #64748b; width: 140px; flex-shrink: 0; }
    .value { color: #0f172a; flex: 1; }
    .stops-badge { background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 6px; padding: 8px 12px; margin: 12px 0; font-size: 13px; color: #334155; }
    .footer { background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 16px 24px; text-align: center; font-size: 14px; font-weight: 600; color: #0f172a; }
  </style>
</head>
<body>
  <div class="card">
    <div class="header">
      <h1>New ${categoryLabel} Inquiry #${inquiry.inquiryNumber}</h1>
      <p>Partner Quotation Request &middot; ${safeDestination}</p>
    </div>
    <div class="content">
      <div class="row"><div class="label">Client Name</div><div class="value"><strong>${safeClientName}</strong></div></div>
      <div class="row"><div class="label">Destination</div><div class="value">${safeDestination}</div></div>
      ${safePickup ? `<div class="row"><div class="label">Pickup Point</div><div class="value">${safePickup}</div></div>` : ''}
      ${safeDrop ? `<div class="row"><div class="label">Drop Point</div><div class="value">${safeDrop}</div></div>` : ''}
      <div class="row"><div class="label">Travel Dates</div><div class="value">${formattedDates}</div></div>
      <div class="row"><div class="label">Passengers</div><div class="value">${passengersStr}</div></div>
      ${isPackage && stopsHtml ? `<div class="stops-badge"><strong>Itinerary:</strong> ${stopsHtml}</div>` : ''}
      ${!isPackage ? `<div class="row"><div class="label">Trip Type</div><div class="value">${tripTypeLabel}</div></div>` : ''}
      ${!isPackage && inquiry.flightClass ? `<div class="row"><div class="label">Flight Class</div><div class="value">${escapeHtml(inquiry.flightClass)}</div></div>` : ''}
      ${!isPackage && inquiry.preferredAirline ? `<div class="row"><div class="label">Preferred Airline</div><div class="value">${escapeHtml(inquiry.preferredAirline)}</div></div>` : ''}
      ${inquiry.specialNotes ? `<div class="row"><div class="label">Special Notes</div><div class="value">${escapeHtml(inquiry.specialNotes)}</div></div>` : ''}
    </div>
    </div>
    <div class="footer">
      Please reply with your best quote.
    </div>
  </div>
</body>
</html>
  `.trim();

  return { subject, plainText, htmlBody };
}

