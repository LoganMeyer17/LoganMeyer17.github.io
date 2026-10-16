# Database Testing Verification

**Logan Meyer**  
**October 16, 2026**  
**CS 499**  
**Project:** Final Enhanced Inventory Tracking Android Application

## Overview

This file summarizes the automated testing completed for the final Database enhancement of the Inventory Tracking application. The emulator-based tests verify authentication security, account-owned inventory, database constraints and transactions, versioned migration, inventory audit history, user-scoped history retrieval, and persistence after the database is reopened.

## Test Environment

The instrumented tests were run through Android Studio on the Medium Phone API 36.1 emulator. Gradle 9.3.1 generated the complete HTML test report.

## Test Results

- The database instrumented suite passed 9 of 9 tests.
- The SMS-preference instrumented suite passed 2 of 2 tests.
- The complete Android instrumented suite passed 11 of 11 tests.
- No tests failed or were skipped.

## Database Test Coverage

### Authentication and credential protection

The tests verified that passwords are salted and hashed instead of stored as plaintext. They also confirmed that duplicate usernames are rejected and that SQL-style characters cannot bypass authentication.

### Account ownership and data isolation

The tests confirmed that inventory records belong to individual accounts. A user cannot read, change, or delete another user’s inventory. They also verified that a valid first login can claim legacy inventory without exposing those records across accounts.

### Constraints and transactions

The tests verified that database constraints reject invalid data and that transactions prevent partial writes. Quantity changes cannot produce a value below zero or above the supported integer limit.

### Migration and persistence

Migration testing confirmed that records from the original database schema are preserved while plaintext passwords are removed. Authentication, inventory records, and history data remain available after the database is closed and reopened.

### Inventory audit history

The tests verified that item creation, quantity changes, and deletion produce complete audit-history records. The final test suite also confirmed that history queries are restricted to the signed-in user, return the correct stored values, and persist after reopening the database.

## Testing Evidence

This folder contains:

- `01 Database Instrumented Tests 9 Passed.png`, showing all database-specific tests passing
- `02 Complete Instrumented Test Suite 11 Passed.png`, showing the complete final instrumented suite passing
- `Generated Test Reports`, containing the complete Gradle HTML report

Open `Generated Test Reports/index.html` to view the full results. The report includes separate pages for the database and SMS-preference test classes as well as the combined 11-test summary.

## Conclusion

The final instrumented test results provide direct evidence that the Database enhancement protects credentials, enforces account ownership, preserves data during migration, rejects invalid or incomplete writes, records inventory history, and maintains user-scoped data after the database is reopened. All 11 instrumented tests passed with no failures or skipped tests.
