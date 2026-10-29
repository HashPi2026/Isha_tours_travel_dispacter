-- ============================================================================
-- TRAVEL DISPATCHER - SEED SAMPLE DATA (003_seed_data.sql)
-- ============================================================================
-- Run this in your Supabase SQL Editor if you want to populate your empty tables
-- with sample partner agents, inquiries, stops, dispatches, and ledger records.
-- ============================================================================

DO $$
DECLARE
    v_agent1_id UUID;
    v_agent2_id UUID;
    v_agent3_id UUID;
    v_agent4_id UUID;
    v_inq1_id UUID;
    v_inq2_id UUID;
    v_inq3_id UUID;
    v_led1_id UUID;
    v_led2_id UUID;
BEGIN
    RAISE NOTICE 'Seeding sample data into Travel Dispatcher tables...';

    -- 1. Insert Partner Agents
    INSERT INTO public.agents (name, email, phone, is_active)
    VALUES ('Sunrise Tours', 'quotes@sunrise.example', '+91 98250 11122', true)
    RETURNING id INTO v_agent1_id;

    INSERT INTO public.agents (name, email, phone, is_active)
    VALUES ('Himalaya Holidays', 'book@himalaya.example', '+91 98790 22233', true)
    RETURNING id INTO v_agent2_id;

    INSERT INTO public.agents (name, email, phone, is_active)
    VALUES ('Coastal Escapes', 'hello@coastal.example', '+91 99090 33344', true)
    RETURNING id INTO v_agent3_id;

    INSERT INTO public.agents (name, email, phone, is_active)
    VALUES ('SkyWay Travels', 'deals@skyway.example', '+91 97120 44455', true)
    RETURNING id INTO v_agent4_id;

    -- 2. Insert Sample Inquiries
    -- Inquiry #1043: Goa Package
    INSERT INTO public.inquiries (
        category, client_name, client_phone, destination, pickup_point, drop_point,
        date_type, start_date, end_date, adults, children, status
    ) VALUES (
        'package', 'Rahul Shah', '+91 90000 00000', 'Goa', 'Surat', 'Surat',
        'flexible', '2027-01-18', '2027-01-31', 3, 1, 'inquiry'
    ) RETURNING id INTO v_inq1_id;

    -- Stops for Goa
    INSERT INTO public.inquiry_stops (inquiry_id, stop_order, place_name, nights)
    VALUES 
        (v_inq1_id, 1, 'North Goa', 2),
        (v_inq1_id, 2, 'South Goa', 3);

    -- Dispatches for #1043
    INSERT INTO public.dispatches (inquiry_id, agent_id, agent_name, agent_email, status, sent_at)
    VALUES
        (v_inq1_id, v_agent1_id, 'Sunrise Tours', 'quotes@sunrise.example', 'sent', now()),
        (v_inq1_id, v_agent2_id, 'Himalaya Holidays', 'book@himalaya.example', 'sent', now()),
        (v_inq1_id, v_agent3_id, 'Coastal Escapes', 'hello@coastal.example', 'failed', null);

    -- Inquiry #1042: Dubai Flight Ticket
    INSERT INTO public.inquiries (
        category, client_name, client_phone, destination, pickup_point, drop_point,
        date_type, start_date, end_date, adults, children, flight_trip_type, flight_class, status
    ) VALUES (
        'ticket', 'Neha Patel', '+91 90000 00000', 'Dubai', 'Surat', 'Dubai',
        'fixed', '2027-02-10', '2027-02-17', 2, 0, 'round_trip', 'Economy', 'confirmed'
    ) RETURNING id INTO v_inq2_id;

    -- Dispatches for #1042
    INSERT INTO public.dispatches (inquiry_id, agent_id, agent_name, agent_email, status, sent_at)
    VALUES
        (v_inq2_id, v_agent1_id, 'Sunrise Tours', 'quotes@sunrise.example', 'sent', now()),
        (v_inq2_id, v_agent4_id, 'SkyWay Travels', 'deals@skyway.example', 'sent', now());

    -- Inquiry #1041: Manali Package
    INSERT INTO public.inquiries (
        category, client_name, client_phone, destination, pickup_point, drop_point,
        date_type, start_date, end_date, adults, children, status
    ) VALUES (
        'package', 'Amit Joshi', '+91 90000 00000', 'Manali', 'Ahmedabad', 'Ahmedabad',
        'fixed', '2026-12-20', '2026-12-26', 4, 2, 'cancelled'
    ) RETURNING id INTO v_inq3_id;

    INSERT INTO public.inquiry_stops (inquiry_id, stop_order, place_name, nights)
    VALUES
        (v_inq3_id, 1, 'Manali', 4),
        (v_inq3_id, 2, 'Kullu', 2);

    INSERT INTO public.dispatches (inquiry_id, agent_id, agent_name, agent_email, status, sent_at)
    VALUES
        (v_inq3_id, v_agent2_id, 'Himalaya Holidays', 'book@himalaya.example', 'sent', now());

    -- 3. Insert Middleman Khatabook Ledger Records
    INSERT INTO public.ledger_records (inquiry_id, client_name, received_amount, received_date, notes)
    VALUES (v_inq3_id, 'Amit Joshi', 5000.00, CURRENT_DATE - INTERVAL '10 days', 'Advance, Manali')
    RETURNING id INTO v_led1_id;

    INSERT INTO public.ledger_records (inquiry_id, client_name, received_amount, received_date, notes)
    VALUES (v_inq2_id, 'Neha Patel', 15000.00, CURRENT_DATE - INTERVAL '13 days', 'Advance, Dubai tickets')
    RETURNING id INTO v_led2_id;

    -- Ledger Sends / Disbursements
    INSERT INTO public.ledger_sends (ledger_record_id, agent_id, agent_name, amount_sent, disbursement_date)
    VALUES
        (v_led2_id, v_agent1_id, 'Sunrise Tours', 9000.00, CURRENT_DATE - INTERVAL '11 days'),
        (v_led2_id, v_agent4_id, 'SkyWay Travels', 4000.00, CURRENT_DATE - INTERVAL '10 days');

    RAISE NOTICE '>>> Seed complete! All tables now have sample records.';
END $$;
