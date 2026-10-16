# Algorithms and Data Structures Testing Verification

**Logan Meyer**  
**October 10, 2026**  
**CS 499**  
**Project:** Enhanced Inventory Tracking Android Application

## Overview

This file summarizes the automated testing used to verify the Algorithms and Data Structures enhancement of the Inventory Tracking application. The evidence is the local unit-test suite covering input validation, inventory searching, filtering, sorting, indexed SKU lookup, restocking priorities, and synchronization of the in-memory data structures.

Database-specific testing is documented separately with the final Database enhancement so that each portfolio category can be reviewed independently.

## Test Environment

The local unit tests were run through Android Studio using JUnit. Gradle 9.3.1 generated the detailed HTML report.

## Test Results

- The input-validation suite passed 12 of 12 tests.
- The Algorithms and Data Structures suite passed 6 of 6 tests.
- Together, the local unit-test suites passed 18 of 18 tests.
- No local unit tests failed or were skipped.

## Algorithms and Data Structures Coverage

The tests verified exact SKU lookup through a `HashMap`, case-insensitive partial searching by item name or SKU, low-stock and out-of-stock filtering, and sorting by name, SKU, quantity, and restocking priority. They also verified the `PriorityQueue` used to organize restocking needs and confirmed that the list, SKU index, and priority queue remain synchronized after inventory records are replaced or removed.

The validation tests covered username, password, SKU, and quantity rules used by the application. These tests support the reliability of the values supplied to the inventory-management algorithms.

## Testing Evidence

This folder contains:

- `01 Local Unit Tests 18 Passed.png`
- `02 Local Unit Tests Build Successful.png`
- A complete Gradle HTML report for the local unit-test suites

Open `Generated Test Reports/Local Unit Tests/index.html` to view the detailed results.

## Conclusion

The results demonstrate that the Algorithms and Data Structures enhancement performs its search, lookup, filtering, sorting, and restocking operations correctly while keeping its supporting structures synchronized. All 18 focused local tests passed with no failures or skipped tests.
