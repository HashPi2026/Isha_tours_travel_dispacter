import express from 'express';
import nodemailer from 'nodemailer';
import path from 'path';
import { fileURLToPath } from 'url';
import dotenv from 'dotenv';
import pg from 'pg';

dotenv.config();

const { Pool } = pg;
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000;

app.use(express.json());

// Initialize PostgreSQL Connection Pool to Supabase
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false },
  max: 10,
  idleTimeoutMillis: 30000,
});

pool.on('error', (err) => {
  console.error('[DB] Unexpected error on idle client:', err);
});

// Helper to format date as YYYY-MM-DD
function formatDate(d: any): string | undefined {
  if (!d) return undefined;
  if (typeof d === 'string') return d.slice(0, 10);
  if (d instanceof Date) return d.toISOString().slice(0, 10);
  return undefined;
}

// ============================================================================
// DATABASE API ENDPOINTS (Option 2: Live Supabase Persistence)
// ============================================================================

// 1. GET ALL DATA: Inquiries, Agents, Ledger Records
app.get('/api/db/all', async (_req, res) => {
  try {
    // 1. Fetch Agents
    const agentsRes = await pool.query(`
      SELECT id, name, email, phone, is_active AS "isActive", created_at AS "createdAt"
      FROM public.agents
      ORDER BY name ASC
    `);

    // 2. Fetch Inquiries
    const inquiriesRes = await pool.query(`
      SELECT 
        id,
        inquiry_number AS "inquiryNumber",
        category,
        client_name AS "clientName",
        client_phone AS "clientPhone",
        destination,
        pickup_point AS "pickupPoint",
        drop_point AS "dropPoint",
        date_type AS "dateType",
        start_date AS "startDate",
        end_date AS "endDate",
        flexible_date_range AS "flexibleDateRange",
        adults,
        children,
        flight_trip_type AS "flightTripType",
        flight_class AS "flightClass",
        preferred_airline AS "preferredAirline",
        special_notes AS "specialNotes",
        status,
        created_at AS "createdAt"
      FROM public.inquiries
      ORDER BY inquiry_number DESC
    `);

    // 3. Fetch Stops
    const stopsRes = await pool.query(`
      SELECT id, inquiry_id AS "inquiryId", stop_order AS "stopOrder", place_name AS "placeName", nights
      FROM public.inquiry_stops
      ORDER BY stop_order ASC
    `);

    // 4. Fetch Dispatches
    const dispatchesRes = await pool.query(`
      SELECT 
        id,
        inquiry_id AS "inquiryId",
        agent_id AS "agentId",
        agent_name AS "agentName",
        agent_email AS "agentEmail",
        agent_phone AS "agentPhone",
        status,
        sent_at AS "sentAt",
        error_message AS "errorMessage",
        email_html AS "emailHtml",
        created_at AS "createdAt"
      FROM public.dispatches
      ORDER BY created_at ASC
    `);

    // 5. Fetch Ledger Records
    const ledgerRes = await pool.query(`
      SELECT 
        id,
        inquiry_id AS "inquiryId",
        client_name AS "clientName",
        received_amount AS "receivedAmount",
        received_date AS "receivedDate",
        notes,
        created_at AS "createdAt"
      FROM public.ledger_records
      ORDER BY created_at DESC
    `);

    // 6. Fetch Ledger Sends
    const sendsRes = await pool.query(`
      SELECT 
        id,
        ledger_record_id AS "ledgerRecordId",
        agent_id AS "agentId",
        agent_name AS "agentName",
        amount_sent AS "amountSent",
        disbursement_date AS "disbursementDate",
        note,
        created_at AS "createdAt"
      FROM public.ledger_sends
      ORDER BY disbursement_date DESC, created_at DESC
    `);

    // Assemble Inquiries with stops and dispatches
    const stopsByInquiry = new Map<string, any[]>();
    for (const stop of stopsRes.rows) {
      const list = stopsByInquiry.get(stop.inquiryId) || [];
      list.push(stop);
      stopsByInquiry.set(stop.inquiryId, list);
    }

    const dispatchesByInquiry = new Map<string, any[]>();
    for (const disp of dispatchesRes.rows) {
      const list = dispatchesByInquiry.get(disp.inquiryId) || [];
      list.push({
        ...disp,
        sentAt: disp.sentAt ? new Date(disp.sentAt).toISOString() : undefined,
      });
      dispatchesByInquiry.set(disp.inquiryId, list);
    }

    const inquiries = inquiriesRes.rows.map((row) => ({
      ...row,
      startDate: formatDate(row.startDate),
      endDate: formatDate(row.endDate),
      createdAt: row.createdAt ? new Date(row.createdAt).toISOString() : new Date().toISOString(),
      stops: stopsByInquiry.get(row.id) || [],
      dispatches: dispatchesByInquiry.get(row.id) || [],
    }));

    // Assemble Ledger Records with sends
    const sendsByLedger = new Map<string, any[]>();
    for (const send of sendsRes.rows) {
      const list = sendsByLedger.get(send.ledgerRecordId) || [];
      list.push({
        ...send,
        amountSent: parseFloat(send.amountSent) || 0,
        disbursementDate: formatDate(send.disbursementDate) || '',
      });
      sendsByLedger.set(send.ledgerRecordId, list);
    }

    const ledgerRecords = ledgerRes.rows.map((row) => ({
      ...row,
      receivedAmount: parseFloat(row.receivedAmount) || 0,
      receivedDate: formatDate(row.receivedDate) || '',
      createdAt: row.createdAt ? new Date(row.createdAt).toISOString() : new Date().toISOString(),
      sends: sendsByLedger.get(row.id) || [],
    }));

    const agents = agentsRes.rows.map((row) => ({
      ...row,
      isActive: Boolean(row.isActive),
    }));

    return res.json({
      success: true,
      inquiries,
      agents,
      ledgerRecords,
      totalInquiries: inquiries.length,
      totalAgents: agents.length,
      totalLedgerRecords: ledgerRecords.length,
    });
  } catch (err: any) {
    console.error('[DB] Error fetching all data:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// 2. CREATE INQUIRY (with stops and dispatches)
app.post('/api/db/inquiries', async (req, res) => {
  const client = await pool.connect();
  try {
    const { inquiryData, stops, dispatches } = req.body;

    await client.query('BEGIN');

    // Insert Inquiry
    const inqInsert = await client.query(
      `
      INSERT INTO public.inquiries (
        category, client_name, client_phone, destination, pickup_point, drop_point,
        date_type, start_date, end_date, flexible_date_range, adults, children,
        flight_trip_type, flight_class, preferred_airline, special_notes, status
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17)
      RETURNING id, inquiry_number AS "inquiryNumber", created_at AS "createdAt"
      `,
      [
        inquiryData.category,
        inquiryData.clientName,
        inquiryData.clientPhone,
        inquiryData.destination,
        inquiryData.pickupPoint || '',
        inquiryData.dropPoint || '',
        inquiryData.dateType,
        inquiryData.startDate || null,
        inquiryData.endDate || null,
        inquiryData.flexibleDateRange || null,
        inquiryData.adults || 1,
        inquiryData.children || 0,
        inquiryData.flightTripType || null,
        inquiryData.flightClass || null,
        inquiryData.preferredAirline || null,
        inquiryData.specialNotes || null,
        inquiryData.status || 'inquiry',
      ]
    );

    const newInquiryId = inqInsert.rows[0].id;
    const inquiryNumber = inqInsert.rows[0].inquiryNumber;
    const createdAt = inqInsert.rows[0].createdAt;

    // Insert Stops
    const createdStops: any[] = [];
    if (Array.isArray(stops) && stops.length > 0) {
      for (let i = 0; i < stops.length; i++) {
        const s = stops[i];
        const stopRes = await client.query(
          `
          INSERT INTO public.inquiry_stops (inquiry_id, stop_order, place_name, nights)
          VALUES ($1, $2, $3, $4)
          RETURNING id, stop_order AS "stopOrder", place_name AS "placeName", nights
          `,
          [newInquiryId, i + 1, s.placeName, s.nights || 1]
        );
        createdStops.push(stopRes.rows[0]);
      }
    }

    // Insert Dispatches
    const createdDispatches: any[] = [];
    if (Array.isArray(dispatches) && dispatches.length > 0) {
      for (const d of dispatches) {
        // Resolve valid agent_id if exists in agents table, else null
        let validAgentId = null;
        if (d.agentId && d.agentId.match(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i)) {
          validAgentId = d.agentId;
        }

        const dispRes = await client.query(
          `
          INSERT INTO public.dispatches (inquiry_id, agent_id, agent_name, agent_email, status, email_html)
          VALUES ($1, $2, $3, $4, $5, $6)
          RETURNING id, agent_id AS "agentId", agent_name AS "agentName", agent_email AS "agentEmail", status, email_html AS "emailHtml"
          `,
          [newInquiryId, validAgentId, d.agentName, d.agentEmail, d.status || 'pending', d.emailHtml || null]
        );
        createdDispatches.push({
          ...dispRes.rows[0],
          inquiryId: newInquiryId,
        });
      }
    }

    await client.query('COMMIT');

    return res.json({
      success: true,
      inquiry: {
        ...inquiryData,
        id: newInquiryId,
        inquiryNumber,
        createdAt: new Date(createdAt).toISOString(),
        stops: createdStops,
        dispatches: createdDispatches,
      },
    });
  } catch (err: any) {
    await client.query('ROLLBACK');
    console.error('[DB] Error creating inquiry:', err);
    return res.status(500).json({ success: false, error: err.message });
  } finally {
    client.release();
  }
});

// 3. UPDATE INQUIRY STATUS
app.patch('/api/db/inquiries/:id/status', async (req, res) => {
  const { id } = req.params;
  const { status } = req.body;
  try {
    await pool.query('UPDATE public.inquiries SET status = $1, updated_at = now() WHERE id = $2', [status, id]);
    return res.json({ success: true });
  } catch (err: any) {
    console.error('[DB] Error updating inquiry status:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// 4. DELETE INQUIRY
app.delete('/api/db/inquiries/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query('DELETE FROM public.inquiries WHERE id = $1', [id]);
    return res.json({ success: true });
  } catch (err: any) {
    console.error('[DB] Error deleting inquiry:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// 5. UPDATE DISPATCH STATUS
app.patch('/api/db/dispatches/:id', async (req, res) => {
  const { id } = req.params;
  const { status, sentAt, errorMessage } = req.body;
  try {
    await pool.query(
      `
      UPDATE public.dispatches
      SET status = $1, sent_at = $2, error_message = $3
      WHERE id = $4
      `,
      [status, sentAt || (status === 'sent' ? new Date() : null), errorMessage || null, id]
    );
    return res.json({ success: true });
  } catch (err: any) {
    console.error('[DB] Error updating dispatch:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// 6. SAVE OR UPDATE AGENT
app.post('/api/db/agents', async (req, res) => {
  const { id, name, email, phone, isActive } = req.body;
  try {
    if (id && id.match(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i)) {
      // Update
      const updateRes = await pool.query(
        `
        UPDATE public.agents
        SET name = $1, email = $2, phone = $3, is_active = $4, updated_at = now()
        WHERE id = $5
        RETURNING id, name, email, phone, is_active AS "isActive", created_at AS "createdAt"
        `,
        [name, email, phone, isActive ?? true, id]
      );
      return res.json({ success: true, agent: updateRes.rows[0] });
    } else {
      // Insert
      const insertRes = await pool.query(
        `
        INSERT INTO public.agents (name, email, phone, is_active)
        VALUES ($1, $2, $3, $4)
        RETURNING id, name, email, phone, is_active AS "isActive", created_at AS "createdAt"
        `,
        [name, email, phone, isActive ?? true]
      );
      return res.json({ success: true, agent: insertRes.rows[0] });
    }
  } catch (err: any) {
    console.error('[DB] Error saving agent:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// 7. DELETE AGENT
app.delete('/api/db/agents/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query('DELETE FROM public.agents WHERE id = $1', [id]);
    return res.json({ success: true });
  } catch (err: any) {
    console.error('[DB] Error deleting agent:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// 8. ADD MONEY RECEIVED (LEDGER RECORD)
app.post('/api/db/ledger', async (req, res) => {
  const { clientName, receivedAmount, receivedDate, notes } = req.body;
  try {
    const insertRes = await pool.query(
      `
      INSERT INTO public.ledger_records (client_name, received_amount, received_date, notes)
      VALUES ($1, $2, $3, $4)
      RETURNING id, client_name AS "clientName", received_amount AS "receivedAmount", received_date AS "receivedDate", notes, created_at AS "createdAt"
      `,
      [clientName, receivedAmount, receivedDate || new Date(), notes || null]
    );

    return res.json({
      success: true,
      record: {
        ...insertRes.rows[0],
        receivedAmount: parseFloat(insertRes.rows[0].receivedAmount),
        receivedDate: formatDate(insertRes.rows[0].receivedDate),
        createdAt: new Date(insertRes.rows[0].createdAt).toISOString(),
        sends: [],
      },
    });
  } catch (err: any) {
    console.error('[DB] Error creating ledger record:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// 9. ADD DISBURSEMENT (LEDGER SEND)
app.post('/api/db/ledger/disburse', async (req, res) => {
  const { ledgerRecordId, agentId, agentName, amountSent, disbursementDate, note } = req.body;
  try {
    let validAgentId = null;
    if (agentId && agentId.match(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i)) {
      validAgentId = agentId;
    }

    const insertRes = await pool.query(
      `
      INSERT INTO public.ledger_sends (ledger_record_id, agent_id, agent_name, amount_sent, disbursement_date, note)
      VALUES ($1, $2, $3, $4, $5, $6)
      RETURNING id, ledger_record_id AS "ledgerRecordId", agent_id AS "agentId", agent_name AS "agentName", amount_sent AS "amountSent", disbursement_date AS "disbursementDate", note, created_at AS "createdAt"
      `,
      [ledgerRecordId, validAgentId, agentName, amountSent, disbursementDate || new Date(), note || null]
    );

    return res.json({
      success: true,
      send: {
        ...insertRes.rows[0],
        amountSent: parseFloat(insertRes.rows[0].amountSent),
        disbursementDate: formatDate(insertRes.rows[0].disbursementDate),
      },
    });
  } catch (err: any) {
    console.error('[DB] Error adding disbursement:', err);
    // Returns trigger error message if over-sent (e.g. Sent total exceeds received amount)
    return res.status(400).json({ success: false, error: err.message });
  }
});

// 10. DELETE LEDGER RECORD
app.delete('/api/db/ledger/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query('DELETE FROM public.ledger_records WHERE id = $1', [id]);
    return res.json({ success: true });
  } catch (err: any) {
    console.error('[DB] Error deleting ledger record:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// 11. DELETE DISBURSEMENT SEND
app.delete('/api/db/ledger/sends/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query('DELETE FROM public.ledger_sends WHERE id = $1', [id]);
    return res.json({ success: true });
  } catch (err: any) {
    console.error('[DB] Error deleting disbursement send:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// ============================================================================
// EMAIL API ENDPOINTS (Gmail SMTP Direct SSL)
// ============================================================================

// API: Send Quotation Email via Gmail SMTP (port 465 SSL)
app.post('/api/send-email', async (req, res) => {
  const {
    to,
    subject,
    text,
    html,
    gmailAddress,
    appPassword,
    senderDisplayName,
    replyTo,
  } = req.body;

  const authUser = (gmailAddress || process.env.GMAIL_USER || '').trim();
  const authPass = (appPassword || process.env.GMAIL_APP_PASSWORD || '').replace(/\s+/g, '').trim();

  if (!authUser || !authPass) {
    return res.status(400).json({
      success: false,
      error: 'Gmail Address and 16-character App Password are required. Please configure them in Settings.',
    });
  }

  if (!to || !subject) {
    return res.status(400).json({
      success: false,
      error: 'Recipient email address ("to") and subject are required.',
    });
  }

  try {
    const transporter = nodemailer.createTransport({
      host: 'smtp.gmail.com',
      port: 465,
      secure: true, // SSL
      auth: {
        user: authUser,
        pass: authPass,
      },
      connectionTimeout: 15000,
      greetingTimeout: 10000,
      socketTimeout: 20000,
    });

    const displayName = senderDisplayName?.trim() || 'Travel Dispatcher';
    const fromAddress = `"${displayName}" <${authUser}>`;

    const info = await transporter.sendMail({
      from: fromAddress,
      to: to.trim(),
      replyTo: replyTo?.trim() || authUser,
      subject,
      text,
      html,
    });

    console.log(`[SMTP] Successfully sent email to ${to}: messageId = ${info.messageId}`);
    return res.json({
      success: true,
      messageId: info.messageId,
      accepted: info.accepted,
    });
  } catch (err: any) {
    console.error(`[SMTP] Error sending email to ${to}:`, err);
    let readableError = err.message || 'Failed to send email';

    if (err.code === 'EAUTH' || err.responseCode === 535) {
      readableError = 'Gmail SMTP Authentication failed. Please verify your Gmail address and 16-character Google App Password (ensure 2-Step Verification is active).';
    } else if (err.code === 'ESOCKET' || err.code === 'ETIMEDOUT') {
      readableError = 'Network timeout connecting to smtp.gmail.com:465. Please retry.';
    }

    return res.status(500).json({
      success: false,
      error: readableError,
    });
  }
});

// API: Verify SMTP credentials & Send Test Email
app.post('/api/test-email', async (req, res) => {
  const { gmailAddress, appPassword, senderDisplayName } = req.body;

  const authUser = (gmailAddress || process.env.GMAIL_USER || '').trim();
  const authPass = (appPassword || process.env.GMAIL_APP_PASSWORD || '').replace(/\s+/g, '').trim();

  if (!authUser || !authPass) {
    return res.status(400).json({
      success: false,
      error: 'Please enter your Gmail address and 16-character App Password first.',
    });
  }

  try {
    const transporter = nodemailer.createTransport({
      host: 'smtp.gmail.com',
      port: 465,
      secure: true,
      auth: {
        user: authUser,
        pass: authPass,
      },
      connectionTimeout: 15000,
    });

    // Verify SMTP connection
    await transporter.verify();

    const displayName = senderDisplayName?.trim() || 'Travel Dispatcher';
    const info = await transporter.sendMail({
      from: `"${displayName}" <${authUser}>`,
      to: authUser,
      subject: 'Travel Dispatcher · SMTP Connection Test',
      text: `Hello,\n\nYour Gmail SMTP setup with App Password is functioning correctly on smtp.gmail.com:465 SSL.\nYou can now dispatch travel inquiries directly to partner agents from Travel Dispatcher.\n\nTime: ${new Date().toLocaleString()}`,
      html: `
        <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 520px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;">
          <div style="background: #0f172a; color: #ffffff; padding: 16px 20px; border-radius: 8px; margin-bottom: 20px;">
            <h2 style="margin: 0; font-size: 17px; font-weight: 700;">Travel Dispatcher · SMTP Verified</h2>
          </div>
          <p style="color: #334155; font-size: 14px; line-height: 1.6;">
            Success! Your Gmail SMTP configuration with Google App Password is authenticated and active on <strong>smtp.gmail.com:465 SSL</strong>.
          </p>
          <div style="background: #f1f5f9; border-radius: 8px; padding: 12px 16px; margin: 16px 0; font-size: 13px; color: #475569;">
            <div><strong>Sender:</strong> ${authUser}</div>
            <div><strong>Display Name:</strong> ${displayName}</div>
            <div><strong>Port:</strong> 465 (Direct SSL)</div>
            <div><strong>Timestamp:</strong> ${new Date().toLocaleString()}</div>
          </div>
          <p style="color: #64748b; font-size: 12px;">
            Partner quotation emails dispatched from Travel Dispatcher will now be delivered to recipient agent inboxes and visible in your Gmail Sent folder.
          </p>
        </div>
      `,
    });

    console.log(`[SMTP] Test email successfully delivered to ${authUser}: ${info.messageId}`);
    return res.json({
      success: true,
      message: `Test email sent successfully to ${authUser}! Please check your Inbox and Sent folder.`,
      messageId: info.messageId,
    });
  } catch (err: any) {
    console.error('[SMTP] Test email failed:', err);
    let readableError = err.message || 'SMTP connection failed';

    if (err.code === 'EAUTH' || err.responseCode === 535) {
      readableError = 'Gmail SMTP Authentication Failed (535). Please ensure you are using a 16-character App Password (not your normal Google account password) and that 2-Step Verification is enabled.';
    }

    return res.status(500).json({
      success: false,
      error: readableError,
    });
  }
});

// API: Download complete Android Studio project as ZIP
app.get('/api/download-android-zip', async (_req, res) => {
  try {
    const JSZip = (await import('jszip')).default;
    const fs = await import('fs');
    const zip = new JSZip();

    function addDirToZip(dirPath: string, zipFolder: any) {
      if (!fs.existsSync(dirPath)) return;
      const items = fs.readdirSync(dirPath);
      for (const item of items) {
        if (item === '.gradle' || item === 'build' || item === '.idea' || item === '.git') continue;
        const fullPath = path.join(dirPath, item);
        const stat = fs.statSync(fullPath);
        if (stat.isDirectory()) {
          addDirToZip(fullPath, zipFolder.folder(item));
        } else {
          zipFolder.file(item, fs.readFileSync(fullPath));
        }
      }
    }

    const androidRoot = path.join(__dirname, 'android');
    if (fs.existsSync(androidRoot)) {
      addDirToZip(androidRoot, zip);
    }

    const supabaseRoot = path.join(__dirname, 'supabase');
    if (fs.existsSync(supabaseRoot)) {
      addDirToZip(supabaseRoot, zip.folder('supabase'));
    }

    const buffer = await zip.generateAsync({ type: 'nodebuffer', compression: 'DEFLATE' });
    res.setHeader('Content-Type', 'application/zip');
    res.setHeader('Content-Disposition', 'attachment; filename="travel-dispatcher-android-project.zip"');
    return res.send(buffer);
  } catch (err: any) {
    console.error('[ZIP] Error generating android zip:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// Mount Vite or serve static files
async function startServer() {
  if (process.env.NODE_ENV !== 'production') {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: { middlewareMode: true, port: PORT, host: '0.0.0.0' },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    app.use(express.static(path.resolve(__dirname, 'dist')));
    app.get('*', (_req, res) => {
      res.sendFile(path.resolve(__dirname, 'dist', 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Travel Dispatcher server running on http://0.0.0.0:${PORT}`);
  });
}

startServer();
