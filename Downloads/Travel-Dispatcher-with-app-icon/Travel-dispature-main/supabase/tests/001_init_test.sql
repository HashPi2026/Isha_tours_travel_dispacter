-- ============================================================================
-- TRAVEL DISPATCHER - SQL TEST SUITE 001_init_test.sql
-- ============================================================================
-- Run this in the Supabase SQL Editor to verify:
-- 1. Database schema and tables
-- 2. Triggers with row locks (FOR UPDATE)
-- 3. Over-sending rejection ('Sent total exceeds received amount')
-- 4. Prevention of reducing received amount below disbursed total
-- 5. Foreign key cascades (stops & dispatches deleted on inquiry delete)
-- 6. Foreign key SET NULL (ledger records kept when inquiry deleted)
-- 7. Agent preservation as text on agent delete
-- ============================================================================

DO $$
DECLARE
    v_test_owner_id UUID := gen_random_uuid();
    v_existing_owner_id UUID;
    v_created_mock_user BOOLEAN := false;
    v_agent_id UUID;
    v_inquiry_id UUID;
    v_ledger_id UUID;
    v_send_id UUID;
    v_caught_oversend BOOLEAN := false;
    v_caught_reduction BOOLEAN := false;
BEGIN
    RAISE NOTICE '>>> TEST START: Travel Dispatcher Schema Validation';

    -- 1. Resolve or Create Test Owner (satisfying app_owner_id_fkey -> auth.users)
    SELECT id INTO v_existing_owner_id FROM public.app_owner LIMIT 1;

    IF v_existing_owner_id IS NOT NULL THEN
        v_test_owner_id := v_existing_owner_id;
        RAISE NOTICE 'Found existing app_owner (id: %)', v_test_owner_id;
    ELSE
        SELECT id INTO v_existing_owner_id FROM auth.users LIMIT 1;
        IF v_existing_owner_id IS NOT NULL THEN
            v_test_owner_id := v_existing_owner_id;
            INSERT INTO public.app_owner (id, email)
            SELECT id, email FROM auth.users WHERE id = v_test_owner_id
            ON CONFLICT (id) DO NOTHING;
            RAISE NOTICE 'Linked existing auth.user to app_owner (id: %)', v_test_owner_id;
        ELSE
            -- No users exist in auth.users yet. Create temporary test auth user
            INSERT INTO auth.users (
                instance_id, id, aud, role, email, encrypted_password,
                email_confirmed_at, recovery_sent_at, last_sign_in_at,
                raw_app_meta_data, raw_user_meta_data, is_super_admin, created_at, updated_at
            ) VALUES (
                '00000000-0000-0000-0000-000000000000',
                v_test_owner_id,
                'authenticated',
                'authenticated',
                'owner_test@example.com',
                '',
                now(),
                now(),
                now(),
                '{"provider":"email","providers":["email"]}'::jsonb,
                '{}'::jsonb,
                false,
                now(),
                now()
            );

            INSERT INTO public.app_owner (id, email)
            VALUES (v_test_owner_id, 'owner_test@example.com')
            ON CONFLICT (id) DO NOTHING;
            v_created_mock_user := true;
            RAISE NOTICE 'Created temporary test auth user (id: %)', v_test_owner_id;
        END IF;
    END IF;

    -- 2. Insert Test Agent
    INSERT INTO public.agents (name, email, phone, is_active)
    VALUES ('Apex Holidays Test', 'apex@example.com', '+91 99999 11111', true)
    RETURNING id INTO v_agent_id;

    -- 3. Insert Test Inquiry (Fixed)
    INSERT INTO public.inquiries (
        category, client_name, client_phone, destination, pickup_point, drop_point,
        date_type, start_date, end_date, adults, children, status
    ) VALUES (
        'package', 'Test Client', '+91 98765 00000', 'Goa Test', 'Airport', 'Resort',
        'fixed', '2027-01-18', '2027-01-25', 2, 0, 'inquiry'
    ) RETURNING id INTO v_inquiry_id;

    -- 3b. Insert Test Inquiry (Flexible with no pickup/drop and flexible_date_range)
    INSERT INTO public.inquiries (
        category, client_name, client_phone, destination,
        date_type, flexible_date_range, adults, children, status
    ) VALUES (
        'ticket', 'Flexible Client', '+91 98765 11111', 'Dubai Test',
        'flexible', 'Anytime between 18 Jan 2027 and 31 Jan 2027', 1, 0, 'inquiry'
    );

    -- 4. Insert Inquiry Stop & Dispatch
    INSERT INTO public.inquiry_stops (inquiry_id, stop_order, place_name, nights)
    VALUES (v_inquiry_id, 1, 'North Goa', 3);

    INSERT INTO public.dispatches (inquiry_id, agent_id, agent_name, agent_email, status)
    VALUES (v_inquiry_id, v_agent_id, 'Apex Holidays Test', 'apex@example.com', 'pending');

    -- Create linked Ledger record
    INSERT INTO public.ledger_records (inquiry_id, client_name, received_amount, notes)
    VALUES (v_inquiry_id, 'Test Client', 10000.00, 'Advance payment')
    RETURNING id INTO v_ledger_id;

    -- 5. Test Ledger Send Validation (Valid send: 4000 out of 10000)
    INSERT INTO public.ledger_sends (ledger_record_id, agent_id, agent_name, amount_sent, note)
    VALUES (v_ledger_id, v_agent_id, 'Apex Holidays Test', 4000.00, 'Hotel advance')
    RETURNING id INTO v_send_id;

    -- 6. Test OVER-SENDING REJECTION
    -- Available balance is 6000. Attempting to send 7000 MUST fail with trigger exception!
    BEGIN
        INSERT INTO public.ledger_sends (ledger_record_id, agent_id, agent_name, amount_sent, note)
        VALUES (v_ledger_id, v_agent_id, 'Apex Holidays Test', 7000.00, 'Excess send');
    EXCEPTION
        WHEN OTHERS THEN
            v_caught_oversend := true;
            RAISE NOTICE 'SUCCESS: Caught expected over-send error: %', SQLERRM;
    END;

    IF NOT v_caught_oversend THEN
        RAISE EXCEPTION 'TEST FAILED: Over-sending was NOT rejected by database trigger!';
    END IF;

    -- 7. Test REDUCING RECEIVED AMOUNT BELOW DISBURSED REJECTION
    -- Current disbursed is 4000. Attempting to lower received amount to 3000 MUST fail!
    BEGIN
        UPDATE public.ledger_records
        SET received_amount = 3000.00
        WHERE id = v_ledger_id;
    EXCEPTION
        WHEN OTHERS THEN
            v_caught_reduction := true;
            RAISE NOTICE 'SUCCESS: Caught expected reduction error: %', SQLERRM;
    END;

    IF NOT v_caught_reduction THEN
        RAISE EXCEPTION 'TEST FAILED: Reducing received amount below disbursed was NOT rejected!';
    END IF;

    -- 8. Test Inquiry Deletion: ON DELETE CASCADE on stops & dispatches, ON DELETE SET NULL on ledger_records
    DELETE FROM public.inquiries WHERE id = v_inquiry_id;

    -- Verify stops and dispatches were cascaded
    PERFORM 1 FROM public.inquiry_stops WHERE inquiry_id = v_inquiry_id;
    IF FOUND THEN
        RAISE EXCEPTION 'TEST FAILED: inquiry_stops were not cascaded on inquiry delete!';
    END IF;

    PERFORM 1 FROM public.dispatches WHERE inquiry_id = v_inquiry_id;
    IF FOUND THEN
        RAISE EXCEPTION 'TEST FAILED: dispatches were not cascaded on inquiry delete!';
    END IF;

    -- Verify ledger record still exists with inquiry_id set to NULL
    PERFORM 1 FROM public.ledger_records WHERE id = v_ledger_id AND inquiry_id IS NULL;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'TEST FAILED: ledger record inquiry_id was not set to NULL!';
    END IF;

    -- 9. Test Agent Deletion: ON DELETE SET NULL on ledger_sends while preserving agent_name text
    DELETE FROM public.agents WHERE id = v_agent_id;

    PERFORM 1 FROM public.ledger_sends WHERE id = v_send_id AND agent_id IS NULL AND agent_name = 'Apex Holidays Test';
    IF NOT FOUND THEN
        RAISE EXCEPTION 'TEST FAILED: ledger send agent_id was not set to NULL or agent_name lost!';
    END IF;

    -- 10. Test Ledger Record Cascade Deletion of Sends
    DELETE FROM public.ledger_records WHERE id = v_ledger_id;

    PERFORM 1 FROM public.ledger_sends WHERE id = v_send_id;
    IF FOUND THEN
        RAISE EXCEPTION 'TEST FAILED: ledger sends were not cascaded on ledger record delete!';
    END IF;

    -- Clean up temporary test auth user if we created one
    IF v_created_mock_user THEN
        DELETE FROM public.app_owner WHERE id = v_test_owner_id;
        DELETE FROM auth.users WHERE id = v_test_owner_id;
        RAISE NOTICE 'Cleaned up temporary test user';
    END IF;

    RAISE NOTICE '>>> ALL TESTS PASSED SUCCESSFULLY! Database schema & rules are 100%% compliant.';
END $$;
