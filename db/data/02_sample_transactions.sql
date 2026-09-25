-- =============================================================================
-- DATA: a small transaction history so GET /v1/api/accounts/{accountNumber}/statement
-- returns something on a freshly seeded database, plus the matching withdrawal_history rows.
--
-- Requires 01_seed_customers_accounts.sql first (looks accounts up by account_number).
-- Each account's last balance_after equals its seeded balance, so the ledger is consistent
-- with what the app would have written:
--   CH-0000088291  balance 2450.75 : +1000.00 (check) -> 2551.50, -100.75 -> 2450.75
--   SV-0000044102  balance 12800.00: -500.00 -> 10500.00, +2300.00 (check) -> 12800.00
--   CH-0000010001  balance 3200.50 : +400.00 (cash)  -> 3200.50
-- Dates are relative to CURDATE() and all fall after each account's created_date, inside the
-- default 18-month statement window. deposit_type is only set on deposits (cash|check).
-- Run once; a second run would duplicate these rows (there is no natural unique key).
--
-- Usage:   mysql -u <user> -p < db/data/02_sample_transactions.sql
-- =============================================================================

USE db_example;

START TRANSACTION;

INSERT INTO account_transactions (account_id, transaction_type, amount, balance_after, transaction_date, deposit_type) VALUES
  ((SELECT id FROM accounts WHERE account_number = 'CH-0000088291'), 'DEPOSIT',    1000.00,  2551.50, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'check'),
  ((SELECT id FROM accounts WHERE account_number = 'CH-0000088291'), 'WITHDRAWAL',  100.75,  2450.75, DATE_SUB(CURDATE(), INTERVAL 10 DAY), NULL),
  ((SELECT id FROM accounts WHERE account_number = 'SV-0000044102'), 'WITHDRAWAL',  500.00, 10500.00, DATE_SUB(CURDATE(), INTERVAL 60 DAY), NULL),
  ((SELECT id FROM accounts WHERE account_number = 'SV-0000044102'), 'DEPOSIT',    2300.00, 12800.00, DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'check'),
  ((SELECT id FROM accounts WHERE account_number = 'CH-0000010001'), 'DEPOSIT',     400.00,  3200.50, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  'cash');

-- One withdrawal_history row per WITHDRAWAL above (status is always COMPLETED for a
-- withdrawal that went through; address is the customer's address at the time).
INSERT INTO withdrawal_history (account_id, withdrawal_date, amount, status, first_name, last_name,
                                street, city, state, zip, country, address_line1, address_line2) VALUES
  ((SELECT id FROM accounts WHERE account_number = 'CH-0000088291'), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 100.75, 'COMPLETED', 'Alice', 'Smith',
   '123 Main St', 'Austin', 'TX', '78701', 'USA', 'Apt 4B', NULL),
  ((SELECT id FROM accounts WHERE account_number = 'SV-0000044102'), DATE_SUB(CURDATE(), INTERVAL 60 DAY), 500.00, 'COMPLETED', 'Bob', 'Jones',
   '456 Oak Ln', 'Dallas', 'TX', '75201', 'USA', '456 Oak Ln', NULL);

COMMIT;
