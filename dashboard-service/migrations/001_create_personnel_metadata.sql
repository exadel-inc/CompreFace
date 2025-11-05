-- Migration: Create personnel_metadata table
-- Purpose: Store military personnel metadata (department, sub_department, rank)
-- Why: CompreFace doesn't provide API to retrieve metadata, so we store it separately

CREATE TABLE IF NOT EXISTS personnel_metadata (
    subject_name VARCHAR(255) PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    sub_department VARCHAR(100),
    rank VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_personnel_department ON personnel_metadata(department);
CREATE INDEX IF NOT EXISTS idx_personnel_sub_department ON personnel_metadata(sub_department);

-- Comments for documentation
COMMENT ON TABLE personnel_metadata IS '1BIP Personnel metadata - stores department, rank, etc. Synced with CompreFace subjects';
COMMENT ON COLUMN personnel_metadata.subject_name IS 'Subject name from CompreFace (PRIMARY KEY)';
COMMENT ON COLUMN personnel_metadata.department IS 'Bataillon / Unité (e.g., 10BPAG, 1BCAS)';
COMMENT ON COLUMN personnel_metadata.sub_department IS 'Compagnie / Section (manually entered)';
COMMENT ON COLUMN personnel_metadata.rank IS 'Grade militaire (e.g., Lieutenant, Sergent)';
