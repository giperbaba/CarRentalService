-- Rename existing columns
ALTER TABLE bookings RENAME COLUMN modified_at TO updated_at;

-- Drop unused columns
ALTER TABLE bookings 
    DROP COLUMN IF EXISTS created_by,
    DROP COLUMN IF EXISTS modified_by; 