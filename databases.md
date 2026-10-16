---
title: Databases
---

# Artifact and Selection

For this enhancement, I used the Inventory Tracking Android application that I originally created in CS 360 Mobile Architecture and Programming before beginning CS 499. The application uses Java, XML layouts, and a local SQLite database. Users can create accounts, log in, and manage inventory records. For this part of the capstone, I focused on improving credential storage, inventory ownership, data integrity, migration, history, and persistence.

I selected this artifact because the database stores information that must remain accurate and private. The earlier version stored readable passwords, treated inventory as a shared collection, and did not provide a safe path for major schema changes. These limitations created security and ownership problems even though the application's basic features appeared to work.

This enhancement builds on the earlier Software Design and Engineering and Algorithms and Data Structures work. The cumulative application still contains the improved interface, validation, searching, filtering, sorting, and restocking features. Those changes are discussed in their respective narratives. This narrative focuses on the database design and the evidence used to verify it.

# Database Improvements

The first major improvement was replacing plaintext passwords with salted password hashes. When a user creates an account, the application stores a separate hash and salt instead of the original password. Authentication verifies the submitted password against those stored values. The current users table no longer contains the original plaintext password column.

Using a different salt for each account means that two users who select the same password do not receive the same stored values. Authentication queries also use selection arguments instead of constructing SQL statements from user input. This prevents special characters or SQL-style input from changing the intended query.

The second improvement was assigning inventory records to individual accounts. The items table now includes an owner_username value connected to the users table through a foreign key. Inventory reads, quantity changes, and deletions require the signed-in username. A user therefore cannot view, modify, or delete another account's inventory simply by knowing an item ID.

The database also uses a combined uniqueness constraint for the owner and SKU. One user cannot create the same SKU twice, but different users may use the same SKU in their separate inventories. Additional constraints prevent negative quantities and low-stock thresholds, null required values, unsupported history actions, and inventory records associated with nonexistent accounts. Indexes support the account-specific inventory and history queries.

Inventory writes now use transactions. Adding an item, changing its quantity, or deleting it also creates an audit-history record. Both operations must succeed before the transaction is committed. If the history write fails, the inventory change is rolled back so the current inventory and its history cannot become inconsistent.

The inventory_history table records item creation, quantity changes, deletion, and the assignment of migrated legacy records. Each entry stores the associated username, item information, action, quantity values, and time. Keeping the item name and SKU in the history allows a deleted item's activity to remain understandable after the original inventory row is gone.

For the final polish, I added the InventoryHistoryEntry model and a user-scoped history query. History results are limited to the signed-in user and returned from newest to oldest. I also added a View Inventory History option that displays the recorded activity through the application. This made the audit information available to the user instead of leaving it accessible only through direct database queries.

The database uses explicit migration steps rather than deleting the existing tables and starting over. The migration preserves users and inventory while introducing password hashing, ownership, constraints, history, and indexes. Existing plaintext passwords are converted into salted hashes before the old users table is removed.

Legacy inventory is preserved without immediately assigning it to an arbitrary account. After the first successful login following migration, the application assigns the preserved records to that account within a transaction and records the assignment in the history table. An unsuccessful login cannot claim those records. This approach preserves existing data while introducing account ownership in a controlled way.

I retained SQLiteOpenHelper instead of replacing the storage layer with Room. SQLiteOpenHelper already supported the application, and using it allowed me to focus directly on the schema, constraints, transactions, queries, and migration sequence. Replacing the storage framework would have increased the project's scope without removing the need to handle the existing data carefully.

# Course Outcomes and Final Integration

This enhancement primarily addresses Course Outcome Five: Develop a security mindset that anticipates adversarial exploits in software architecture and designs to expose potential vulnerabilities, mitigate design flaws, and ensure privacy and enhanced security of data and resources. Salted password hashing, parameterized queries, account ownership, ownership checks, database constraints, and unauthorized-access tests provide evidence of this outcome.

The enhancement also supports Course Outcome Three. I designed a versioned schema, selected account-specific constraints and indexes, used transactions to keep related writes consistent, and created a migration process that preserved existing data. These decisions required me to balance security, data integrity, compatibility, and implementation complexity.

Course Outcome Four is demonstrated through my use of Java, SQLiteOpenHelper, Android Studio, database transactions, schema migrations, and Android instrumented testing. I used these tools to implement and verify the database behavior with a real SQLite database on an Android emulator.

My response to instructor feedback contributes to Course Outcome One. The instructor identified Android-device verification of migration, ownership, and persistence as the remaining gap. I converted that feedback into focused tests and organized the screenshots, generated report, and README so that another reviewer could trace the database claims to evidence.

The narrative, code documentation, testing evidence, and ePortfolio presentation support Course Outcome Two. These materials explain the purpose of the enhancement and its technical results for readers with different levels of familiarity with Android development.

# Reflection, Testing, and Feedback

One of the main challenges was adding ownership without losing inventory created under the earlier shared schema. Assigning preserved records during migration would have required the database to guess which user owned them. Leaving the records temporarily unassigned and claiming them only after successful authentication provided a safer transition for this application.

Another challenge was keeping audit history consistent with the current inventory. Saving an inventory change and its history separately could produce incomplete records if one write failed. Placing both operations in the same transaction allowed the database to roll back the inventory change when the history insert failed.

The database instrumented suite contains nine tests. These tests cover password hashing, duplicate-user protection, SQL-style authentication input, ownership, unauthorized access, constraints, transaction rollback, audit history, quantity boundaries, migration, legacy inventory, user-scoped history, and persistence after reopening the database.

All nine database tests passed on the Medium Phone API 36.1 emulator. Two SMS-preference tests also passed, producing a complete result of 11 out of 11 instrumented tests with no failures or skipped tests. The testing-evidence folder contains the database and complete-suite screenshots, the generated Gradle report, and a README describing the results.

The final testing directly addresses the instructor's request for emulator-based evidence of migration, ownership, and persistence. It also verifies the history query added during final polishing. Completing this enhancement improved my understanding of how schemas, queries, constraints, transactions, migrations, ownership rules, and testing must work together to protect stored information.

[Return to the ePortfolio Home Page](https://loganmeyer17.github.io/)
