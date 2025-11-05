-- ============================================
-- MIGRATION: Add join_date to customer table
-- Date: 02/11/2025
-- ============================================
-- Instructions:
-- 1. Open pgAdmin
-- 2. Connect to your database
-- 3. Open Query Tool
-- 4. Copy ALL this file and paste
-- 5. Press F5 to execute
-- ============================================

DO $$ 
BEGIN
    -- Check if column already exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer' AND column_name = 'join_date'
    ) THEN
        -- Add column with default value
        ALTER TABLE customer 
        ADD COLUMN join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        
        -- Update existing rows
        UPDATE customer 
        SET join_date = CURRENT_TIMESTAMP 
        WHERE join_date IS NULL;
        
        RAISE NOTICE '✓ SUCCESS: Column join_date has been added!';
    ELSE
        RAISE NOTICE '⚠ SKIP: Column join_date already exists!';
    END IF;
END $$;

-- Verify the result
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ MIGRATION SUCCESSFUL!'
        ELSE '✗ NO DATA FOUND'
    END as status,
    COUNT(*) as total_customers
FROM customer;

-- Show customer data with join_date
SELECT customer_id, name, phone_number, join_date 
FROM customer 
ORDER BY customer_id;

-- ============================================
-- DONE! Now run: .\build.bat
-- Then run: java -jar PTTK.jar
-- ============================================
