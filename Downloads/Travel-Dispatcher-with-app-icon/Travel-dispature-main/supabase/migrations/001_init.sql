-- ============================================================================
-- TRAVEL DISPATCHER - SUPABASE MIGRATION 001_init.sql
-- ============================================================================
-- Architecture: Single-owner private travel dispatcher system.
-- Only the owner UID registered in app_owner table has read/write privileges.
-- Anon access is completely blocked.
-- ============================================================================

-- Enable pgcrypto for UUIDs if not already enabled
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. APP OWNER TABLE (Whitelisted user)
CREATE TABLE IF NOT EXISTS public.app_owner (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.app_owner ENABLE ROW LEVEL SECURITY;

-- 2. PARTNER AGENTS DIRECTORY
CREATE TABLE IF NOT EXISTS public.agents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.agents ENABLE ROW LEVEL SECURITY;

-- 3. INQUIRIES TABLE
-- Number sequence starting at 1001
CREATE SEQUENCE IF NOT EXISTS public.inquiry_number_seq START WITH 1001;

CREATE TABLE IF NOT EXISTS public.inquiries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_number INTEGER NOT NULL DEFAULT nextval('public.inquiry_number_seq') UNIQUE,
    category TEXT NOT NULL CHECK (category IN ('package', 'ticket')),
    client_name TEXT NOT NULL,
    client_phone TEXT NOT NULL,
    destination TEXT NOT NULL,
    pickup_point TEXT DEFAULT '',
    drop_point TEXT DEFAULT '',
    date_type TEXT NOT NULL CHECK (date_type IN ('fixed', 'flexible')),
    start_date DATE,
    end_date DATE,
    flexible_date_range TEXT,
    adults INTEGER NOT NULL DEFAULT 1 CHECK (adults >= 1),
    children INTEGER NOT NULL DEFAULT 0 CHECK (children >= 0),
    flight_trip_type TEXT CHECK (flight_trip_type IN ('one_way', 'round_trip')),
    flight_class TEXT,
    preferred_airline TEXT,
    special_notes TEXT,
    status TEXT NOT NULL DEFAULT 'inquiry' CHECK (status IN ('inquiry', 'confirmed', 'cancelled')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.inquiries ENABLE ROW LEVEL SECURITY;

-- 4. INQUIRY STOPS (Only for Tour Packages)
CREATE TABLE IF NOT EXISTS public.inquiry_stops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL REFERENCES public.inquiries(id) ON DELETE CASCADE,
    stop_order INTEGER NOT NULL DEFAULT 1,
    place_name TEXT NOT NULL,
    nights INTEGER NOT NULL DEFAULT 1 CHECK (nights >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.inquiry_stops ENABLE ROW LEVEL SECURITY;

-- 5. DISPATCHES LOG
CREATE TABLE IF NOT EXISTS public.dispatches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL REFERENCES public.inquiries(id) ON DELETE CASCADE,
    agent_id UUID REFERENCES public.agents(id) ON DELETE SET NULL,
    agent_name TEXT NOT NULL,
    agent_email TEXT NOT NULL,
    agent_phone TEXT,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'sent', 'failed')),
    sent_at TIMESTAMPTZ,
    error_message TEXT,
    email_html TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.dispatches ENABLE ROW LEVEL SECURITY;

-- 6. KHATABOOK MIDDLEMAN LEDGER RECORDS
CREATE TABLE IF NOT EXISTS public.ledger_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID REFERENCES public.inquiries(id) ON DELETE SET NULL,
    client_name TEXT NOT NULL,
    received_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (received_amount >= 0),
    received_date DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.ledger_records ENABLE ROW LEVEL SECURITY;

-- 7. LEDGER DISBURSEMENTS / SENDS (Inside the same record)
CREATE TABLE IF NOT EXISTS public.ledger_sends (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ledger_record_id UUID NOT NULL REFERENCES public.ledger_records(id) ON DELETE CASCADE,
    agent_id UUID REFERENCES public.agents(id) ON DELETE SET NULL,
    agent_name TEXT NOT NULL,
    amount_sent NUMERIC(12, 2) NOT NULL CHECK (amount_sent > 0),
    disbursement_date DATE NOT NULL DEFAULT CURRENT_DATE,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.ledger_sends ENABLE ROW LEVEL SECURITY;

-- ============================================================================
-- DATABASE TRIGGERS & CONSTRAINTS (OVER-SENDING PROTECTION)
-- ============================================================================

-- Function to validate that total sent amounts never exceed received amount
CREATE OR REPLACE FUNCTION public.check_ledger_send_limit()
RETURNS TRIGGER AS $$
DECLARE
    v_received_amount NUMERIC(12, 2);
    v_total_sent NUMERIC(12, 2);
    v_target_record_id UUID;
BEGIN
    v_target_record_id := NEW.ledger_record_id;

    -- Row lock FOR UPDATE on the ledger_record to prevent concurrent over-spend race condition
    SELECT received_amount INTO v_received_amount
    FROM public.ledger_records
    WHERE id = v_target_record_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ledger record % does not exist', v_target_record_id;
    END IF;

    -- Calculate sum of all other sends plus this new/updated send
    SELECT COALESCE(SUM(amount_sent), 0) INTO v_total_sent
    FROM public.ledger_sends
    WHERE ledger_record_id = v_target_record_id
      AND (TG_OP = 'INSERT' OR id <> NEW.id);

    v_total_sent := v_total_sent + NEW.amount_sent;

    IF v_total_sent > v_received_amount THEN
        RAISE EXCEPTION 'Sent total exceeds received amount (Max allowed: %, Attempted total: %)',
            v_received_amount, v_total_sent;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_check_ledger_send_limit
BEFORE INSERT OR UPDATE ON public.ledger_sends
FOR EACH ROW
EXECUTE FUNCTION public.check_ledger_send_limit();

-- Function to prevent reducing received_amount below what has already been disbursed
CREATE OR REPLACE FUNCTION public.check_ledger_record_reduction()
RETURNS TRIGGER AS $$
DECLARE
    v_current_total_sent NUMERIC(12, 2);
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.received_amount < OLD.received_amount THEN
        SELECT COALESCE(SUM(amount_sent), 0) INTO v_current_total_sent
        FROM public.ledger_sends
        WHERE ledger_record_id = NEW.id;

        IF NEW.received_amount < v_current_total_sent THEN
            RAISE EXCEPTION 'Cannot reduce received amount to % because % has already been disbursed to agents',
                NEW.received_amount, v_current_total_sent;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_check_ledger_record_reduction
BEFORE UPDATE ON public.ledger_records
FOR EACH ROW
EXECUTE FUNCTION public.check_ledger_record_reduction();

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================
-- Helper security definer function to check if user is registered owner
CREATE OR REPLACE FUNCTION public.is_app_owner()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.app_owner
        WHERE id = auth.uid()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 1. app_owner: Owner can read their own profile, NO client can insert/update/delete (managed manually in dashboard)
CREATE POLICY "Owner read own row" ON public.app_owner
    FOR SELECT TO authenticated
    USING (auth.uid() = id);

-- 2. agents: Owner full access
CREATE POLICY "Owner all agents" ON public.agents
    FOR ALL TO authenticated
    USING (public.is_app_owner())
    WITH CHECK (public.is_app_owner());

-- 3. inquiries: Owner full access
CREATE POLICY "Owner all inquiries" ON public.inquiries
    FOR ALL TO authenticated
    USING (public.is_app_owner())
    WITH CHECK (public.is_app_owner());

-- 4. inquiry_stops: Owner full access
CREATE POLICY "Owner all stops" ON public.inquiry_stops
    FOR ALL TO authenticated
    USING (public.is_app_owner())
    WITH CHECK (public.is_app_owner());

-- 5. dispatches: Owner full access (including updating status from Android app)
CREATE POLICY "Owner all dispatches" ON public.dispatches
    FOR ALL TO authenticated
    USING (public.is_app_owner())
    WITH CHECK (public.is_app_owner());

-- 6. ledger_records: Owner full access
CREATE POLICY "Owner all ledger_records" ON public.ledger_records
    FOR ALL TO authenticated
    USING (public.is_app_owner())
    WITH CHECK (public.is_app_owner());

-- 7. ledger_sends: Owner full access
CREATE POLICY "Owner all ledger_sends" ON public.ledger_sends
    FOR ALL TO authenticated
    USING (public.is_app_owner())
    WITH CHECK (public.is_app_owner());

-- ============================================================================
-- INITIAL SEED SCRIPT (Optional sample data matching user screenshots)
-- ============================================================================
-- To seed sample agents:
-- INSERT INTO public.agents (name, email, phone, is_active) VALUES
--   ('Sunrise Tours', 'quotes@sunrise.example', '+91 98250 11122', true),
--   ('Himalaya Holidays', 'book@himalaya.example', '+91 98790 22233', true),
--   ('Coastal Escapes', 'hello@coastal.example', '+91 99090 33344', true),
--   ('SkyWay Travels', 'deals@skyway.example', '+91 97120 44455', true);
