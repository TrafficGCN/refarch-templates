-- Drop the existing check constraint
ALTER TABLE links DROP CONSTRAINT IF EXISTS links_scope_check;

-- Add the new check constraint with uppercase values
ALTER TABLE links ADD CONSTRAINT links_scope_check CHECK (scope IN ('INTERNAL', 'EXTERNAL')); 