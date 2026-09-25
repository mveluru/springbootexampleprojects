-- =============================================================================
-- DATA: sample rows for the events table (normally filled by POST /api/events after the
-- payload passes JSON-schema validation). event_id is the primary key - a second run fails
-- on the duplicate key rather than inserting twice.
--
-- Usage:   mysql -u <user> -p < db/data/03_sample_events.sql
-- =============================================================================

USE db_example;

INSERT INTO events (event_id, version, timestamp, user_id, email) VALUES
  ('3f2b8c1e-6a1d-4c55-9a7e-0d1c2b3a4f01', 'v1', NOW(6), 'user-1001', 'alice.smith@example.com'),
  ('3f2b8c1e-6a1d-4c55-9a7e-0d1c2b3a4f02', 'v1', NOW(6), 'user-1002', 'bob.jones@example.com'),
  ('3f2b8c1e-6a1d-4c55-9a7e-0d1c2b3a4f03', 'v1', NOW(6), 'user-1003', 'carol.davis@example.com');
