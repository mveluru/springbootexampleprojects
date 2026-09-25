-- =============================================================================
-- DATA: the 20 demo bank locations (8 OFFICE, 6 ATM, 6 BOTH) and the operations each one
-- serves, exactly as BankLocationDataSeeder creates them - generated from that class.
--
-- * Run ONLY against EMPTY bank_locations / bank_location_services tables (ids 1..20 are
--   explicit, so a second run fails on the primary key and the transaction rolls back).
-- * If you load this before starting the app, BankLocationDataSeeder sees a non-empty table
--   and skips its own seeding - same result either way.
-- * Office hours are 08:00-16:00 in America/Chicago (Central time, follows daylight saving);
--   ATM-only locations have NULL hours and NULL phone.
--
-- Usage:   mysql -u <user> -p < db/data/04_seed_bank_locations.sql
-- =============================================================================

USE db_example;

START TRANSACTION;

INSERT INTO bank_locations (id, name, location_type, address_line1, address_line2, city, state, zip, country, opens_at, closes_at, time_zone, phone_number) VALUES
  (1, 'Austin Downtown Branch', 'OFFICE', '300 Congress Ave', NULL, 'Austin', 'TX', '78701', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(512) 555-0101'),
  (2, 'Dallas Main Street Branch', 'OFFICE', '1500 Main St', 'Suite 100', 'Dallas', 'TX', '75201', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(214) 555-0102'),
  (3, 'Houston Galleria Branch', 'BOTH', '5085 Westheimer Rd', NULL, 'Houston', 'TX', '77056', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(713) 555-0103'),
  (4, 'San Antonio Riverwalk ATM', 'ATM', '100 E Commerce St', NULL, 'San Antonio', 'TX', '78205', 'USA', NULL, NULL, 'America/Chicago', NULL),
  (5, 'Fort Worth Sundance Square Branch', 'OFFICE', '420 Main St', NULL, 'Fort Worth', 'TX', '76102', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(817) 555-0105'),
  (6, 'Chicago Loop Branch', 'BOTH', '10 S LaSalle St', NULL, 'Chicago', 'IL', '60603', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(312) 555-0106'),
  (7, 'Chicago O''Hare Airport ATM', 'ATM', '10000 W O''Hare Ave', 'Terminal 1', 'Chicago', 'IL', '60666', 'USA', NULL, NULL, 'America/Chicago', NULL),
  (8, 'Milwaukee Wisconsin Ave Branch', 'OFFICE', '500 W Wisconsin Ave', NULL, 'Milwaukee', 'WI', '53203', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(414) 555-0108'),
  (9, 'Madison Capitol Square ATM', 'ATM', '1 E Main St', NULL, 'Madison', 'WI', '53703', 'USA', NULL, NULL, 'America/Chicago', NULL),
  (10, 'Minneapolis Nicollet Branch', 'BOTH', '800 Nicollet Mall', NULL, 'Minneapolis', 'MN', '55402', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(612) 555-0110'),
  (11, 'Kansas City Plaza Branch', 'OFFICE', '4600 J C Nichols Pkwy', NULL, 'Kansas City', 'MO', '64112', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(816) 555-0111'),
  (12, 'St. Louis Gateway Branch', 'BOTH', '1 N Broadway', NULL, 'St. Louis', 'MO', '63102', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(314) 555-0112'),
  (13, 'Nashville Music Row Branch', 'OFFICE', '1200 Demonbreun St', NULL, 'Nashville', 'TN', '37203', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(615) 555-0113'),
  (14, 'Memphis Beale Street ATM', 'ATM', '150 Beale St', NULL, 'Memphis', 'TN', '38103', 'USA', NULL, NULL, 'America/Chicago', NULL),
  (15, 'New Orleans Canal Street Branch', 'BOTH', '365 Canal St', NULL, 'New Orleans', 'LA', '70130', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(504) 555-0115'),
  (16, 'Oklahoma City Bricktown Branch', 'OFFICE', '100 E Sheridan Ave', NULL, 'Oklahoma City', 'OK', '73104', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(405) 555-0116'),
  (17, 'Omaha Old Market ATM', 'ATM', '1100 Howard St', NULL, 'Omaha', 'NE', '68102', 'USA', NULL, NULL, 'America/Chicago', NULL),
  (18, 'Des Moines Ingersoll Branch', 'BOTH', '2900 Ingersoll Ave', NULL, 'Des Moines', 'IA', '50312', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(515) 555-0118'),
  (19, 'Birmingham Five Points Branch', 'OFFICE', '2000 Highland Ave', NULL, 'Birmingham', 'AL', '35205', 'USA', '08:00:00', '16:00:00', 'America/Chicago', '(205) 555-0119'),
  (20, 'Little Rock River Market ATM', 'ATM', '400 President Clinton Ave', NULL, 'Little Rock', 'AR', '72201', 'USA', NULL, NULL, 'America/Chicago', NULL);

INSERT INTO bank_location_services (bank_location_id, service) VALUES
  (1, 'BANKING'),
  (1, 'LOANS_MORTGAGES'),
  (1, 'NOTARY'),
  (2, 'BANKING'),
  (2, 'SAFE_DEPOSIT_LOCKER'),
  (2, 'WIRE_TRANSFER'),
  (3, 'BANKING'),
  (3, 'SAFE_DEPOSIT_LOCKER'),
  (3, 'FOREIGN_EXCHANGE'),
  (3, 'ATM_CASH_WITHDRAWAL'),
  (3, 'ATM_DEPOSIT'),
  (4, 'ATM_CASH_WITHDRAWAL'),
  (4, 'ATM_DEPOSIT'),
  (5, 'BANKING'),
  (5, 'LOANS_MORTGAGES'),
  (6, 'BANKING'),
  (6, 'SAFE_DEPOSIT_LOCKER'),
  (6, 'WIRE_TRANSFER'),
  (6, 'FOREIGN_EXCHANGE'),
  (6, 'ATM_CASH_WITHDRAWAL'),
  (6, 'ATM_DEPOSIT'),
  (7, 'ATM_CASH_WITHDRAWAL'),
  (8, 'BANKING'),
  (8, 'NOTARY'),
  (8, 'LOANS_MORTGAGES'),
  (9, 'ATM_CASH_WITHDRAWAL'),
  (9, 'ATM_DEPOSIT'),
  (10, 'BANKING'),
  (10, 'SAFE_DEPOSIT_LOCKER'),
  (10, 'ATM_CASH_WITHDRAWAL'),
  (10, 'ATM_DEPOSIT'),
  (11, 'BANKING'),
  (11, 'SAFE_DEPOSIT_LOCKER'),
  (11, 'NOTARY'),
  (12, 'BANKING'),
  (12, 'WIRE_TRANSFER'),
  (12, 'ATM_CASH_WITHDRAWAL'),
  (12, 'ATM_DEPOSIT'),
  (13, 'BANKING'),
  (13, 'LOANS_MORTGAGES'),
  (13, 'NOTARY'),
  (14, 'ATM_CASH_WITHDRAWAL'),
  (14, 'ATM_DEPOSIT'),
  (15, 'BANKING'),
  (15, 'SAFE_DEPOSIT_LOCKER'),
  (15, 'FOREIGN_EXCHANGE'),
  (15, 'ATM_CASH_WITHDRAWAL'),
  (16, 'BANKING'),
  (16, 'LOANS_MORTGAGES'),
  (17, 'ATM_CASH_WITHDRAWAL'),
  (17, 'ATM_DEPOSIT'),
  (18, 'BANKING'),
  (18, 'NOTARY'),
  (18, 'ATM_CASH_WITHDRAWAL'),
  (18, 'ATM_DEPOSIT'),
  (19, 'BANKING'),
  (19, 'SAFE_DEPOSIT_LOCKER'),
  (19, 'LOANS_MORTGAGES'),
  (20, 'ATM_CASH_WITHDRAWAL'),
  (20, 'ATM_DEPOSIT');

COMMIT;
