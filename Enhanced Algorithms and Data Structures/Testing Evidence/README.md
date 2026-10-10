AUTOMATED TESTING VERIFICATION

Logan Meyer
10/10/2026
CS 499
Project: Enhanced Inventory Tracking Android



OVERVIEW

This file summarizes the automated testing completed for the enhanced Inventory Tracking application. To address my instructor's request for additional verification, I ran the complete local unit-test and Android instrumented-test suites. The emulator-based tests specifically verified database migration, account ownership, and persistence.



TEST ENVIRONMENT

The tests were run through Android Studio on the Medium Phone API 36.1 emulator. Gradle 9.3.1 generated the detailed test reports.



TEST RESULTS

The input validation suite passed 12 of 12 tests. The algorithms and data structures suite passed 6 of 6 tests. Together, the local unit-test suites passed 18 of 18 tests.

The database instrumented suite passed 8 of 8 tests. The SMS preference instrumented suite passed 2 of 2 tests. Together, the Android instrumented-test suites passed 10 of 10 tests.

A total of 28 automated tests passed. No tests failed or were skipped.



LOCAL UNIT-TEST COVERAGE

The local unit tests verified username, password, SKU, and quantity validation. They also verified exact SKU lookup using a HashMap, partial inventory searching, low-stock and out-of-stock filtering, inventory sorting, PriorityQueue restocking order, and data-structure synchronization after inventory records were updated or deleted.



ANDROID INSTRUMENTED-TEST COVERAGE

The Android instrumented tests verified salted password hashing without plaintext password storage, duplicate-user protections, and resistance to SQL-style authentication input. They confirmed that inventory records belong to individual accounts and that users cannot view, change, or delete another account's inventory.

The tests also verified database constraints, transaction rollback, inventory audit history, quantity boundaries, and separate SMS preferences for each user. Migration tests confirmed that records from earlier database versions were preserved without retaining plaintext passwords. Persistence was verified by closing and reopening the database and confirming that authentication and updated inventory information remained available.



TESTING EVIDENCE

This folder contains three screenshots showing the successful local and Android test runs. It also contains complete Gradle HTML reports for the local unit tests and Android instrumented tests. The detailed results can be viewed by opening the index.html file inside either generated-report folder.



CONCLUSION

The test run addressed the requested Android verification of migration, ownership, and persistence. The application passed all 28 automated tests with no failures or skipped tests. These results support the claims made about the completed algorithms, validation, security, database, and persistence enhancements.

