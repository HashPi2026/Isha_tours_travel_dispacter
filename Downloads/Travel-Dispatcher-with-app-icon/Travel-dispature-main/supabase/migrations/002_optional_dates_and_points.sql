-- ============================================================================
-- 002_optional_dates_and_points.sql
-- Run this in Supabase SQL Editor if tables are already created
-- ============================================================================

-- 1. Allow optional Pickup Point and Drop Point
ALTER TABLE public.inquiries ALTER COLUMN pickup_point DROP NOT NULL;
ALTER TABLE public.inquiries ALTER COLUMN pickup_point SET DEFAULT '';

ALTER TABLE public.inquiries ALTER COLUMN drop_point DROP NOT NULL;
ALTER TABLE public.inquiries ALTER COLUMN drop_point SET DEFAULT '';

-- 2. Allow optional Start Date and End Date (for flexible dates)
ALTER TABLE public.inquiries ALTER COLUMN start_date DROP NOT NULL;
ALTER TABLE public.inquiries ALTER COLUMN end_date DROP NOT NULL;

-- 3. Add column for flexible date text (e.g. "Anytime between 18 Jan and 31 Jan")
ALTER TABLE public.inquiries ADD COLUMN IF NOT EXISTS flexible_date_range TEXT;
