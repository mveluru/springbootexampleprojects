-- =============================================================================
-- DDL: drops every table created by 01_create_tables.sql. DESTRUCTIVE - deletes all
-- banking and event data. Tables are dropped children-first so foreign keys don't block it.
-- After this, starting the app recreates the tables (ddl-auto: update) and re-seeds the
-- demo accounts (AccountDataSeeder runs when the accounts table is empty).
--
-- Usage:   mysql -u <user> -p < db/ddl/02_drop_tables.sql
-- =============================================================================

USE db_example;

DROP TABLE IF EXISTS withdrawal_history;
DROP TABLE IF EXISTS account_transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS events;
