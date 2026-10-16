---
title: Algorithms and Data Structures
---

# Artifact and Selection

For this enhancement, I used the Inventory Tracking Android application that I originally created in CS 360 Mobile Architecture and Programming before beginning CS 499. The application uses Java, XML layouts, and a local SQLite database. It allows users to create an account, log in, and manage inventory records. For this part of the capstone, I focused on improving how the application searches, filters, sorts, and prioritizes inventory information.

I selected this artifact because inventory becomes more difficult to manage as the number of records increases. The earlier application could display inventory, but it did not provide useful ways to locate a specific item, separate low-stock records, arrange the results, or identify restocking needs. Improving these areas allowed me to demonstrate how algorithms and data structures can solve practical problems within an existing application.

The application continued to change after I completed this enhancement. The cumulative version contains additional database and security improvements, but those changes are discussed in the separate Database enhancement narrative. This narrative focuses on the algorithms, data structures, design decisions, and testing completed for this category.

# Algorithms and Data Structures Improvements

The main addition for this enhancement was the InventoryOrganizer class. This class separates the inventory-management logic from the Android activity that displays the records. It uses a list to maintain the primary collection of InventoryItem objects, a HashMap to associate each SKU with its corresponding item, and a PriorityQueue to organize records that require restocking. Separating this logic from the user interface made the algorithms easier to understand, maintain, and test.

The enhanced application allows users to search by item name or SKU. An exact SKU search uses the HashMap, which provides an average lookup time of O(1). This avoids scanning the complete inventory when the user already knows the exact SKU. Partial searches require an O(n) scan because the application must compare the search text with each item's name and SKU.

Users can filter the inventory to display all items, low-stock items, or out-of-stock items. These filters also require an O(n) scan of the inventory list. The application can sort the visible results by name, SKU, quantity, or restocking priority. The comparison-based sorting operations generally require O(n log n) time.

Each InventoryItem includes a quantity and a low-stock threshold. The PriorityQueue uses those values to organize items that fall below their thresholds. This provides a consistent way to return the records that need restocking attention without repeatedly implementing restocking logic in the user-interface code.

I chose each structure according to the operation it needed to support. The list provides an ordered collection for displaying, filtering, and sorting records. The HashMap provides efficient exact SKU lookup. The PriorityQueue maintains the restocking order. Using multiple structures improved the application's functionality, but it also created a consistency requirement because the same inventory information could be represented in more than one structure.

To manage this trade-off, I made the list the primary source for the in-memory inventory. After an item is added, updated, or removed, the InventoryOrganizer rebuilds the SKU map and restocking queue from that list. Rebuilding these structures requires additional processing, but it keeps the update logic in one place and reduces the risk of stale information remaining in the map or queue. For the expected size of this application, I determined that this approach provided a reasonable balance between performance, reliability, and code complexity.

# Course Outcomes and Integration

This enhancement primarily addresses Course Outcome Three: Design and evaluate computing solutions that solve a given problem using algorithmic principles and computer science practices and standards appropriate to its solution, while managing the trade-offs involved in design choices. I addressed this outcome by selecting structures according to their required operations and evaluating their effects on lookup performance, sorting cost, memory use, consistency, and maintainability.

The enhancement also supports Course Outcome Four: Demonstrate an ability to use well-founded and innovative techniques, skills, and tools in computing practices for the purpose of implementing computer solutions that deliver value and accomplish industry-specific goals. I demonstrated this outcome by using Java collections, Android Studio, JUnit, object-oriented design, and automated testing to add practical inventory-management capabilities to an existing mobile application.

My response to feedback and my organization of the supporting evidence contribute to Course Outcome One. The instructor's feedback emphasized that the selected data structures needed to be visible in the implementation and supported by focused tests. I retained the InventoryOrganizer design, verified its behavior through a dedicated test suite, and organized the screenshots, generated report, and README so that another reviewer could follow the evidence.

The narrative, code documentation, testing screenshots, generated report, and ePortfolio presentation support Course Outcome Two. Together, these materials explain the enhancement for readers who want either a general description of the application or more detailed technical evidence.

The later database and security changes in the cumulative application provide separate evidence for Course Outcome Five. I discuss those changes, their implementation, and their Android instrumented tests in the Database enhancement narrative rather than repeating them here.

# Reflection, Testing, and Feedback

One of the main challenges was keeping the list, HashMap, and PriorityQueue synchronized. A quantity change can affect the displayed record, the item associated with an SKU, and the item's position in the restocking order. I learned that a Java PriorityQueue does not automatically rearrange an existing object when a value used by its comparator changes. Rebuilding the supporting structures after an update provided a manageable solution and made the expected behavior easier to test.

I also learned that one data structure is not the best choice for every task. The HashMap improves exact SKU searches, but partial searches and filters still need to examine the inventory list. Sorting requires additional processing, but it gives users more useful ways to view their records. The PriorityQueue supports restocking decisions but must remain synchronized with changes to the primary list. These trade-offs helped me better understand how performance, memory use, correctness, and code complexity influence software design.

Six InventoryOrganizer tests cover exact SKU lookup, partial searching, stock filtering, sorting, restocking priority, and synchronization after records are replaced or removed. These tests passed along with twelve input-validation tests, producing a total of 18 passing local unit tests with no failures or skipped tests.

The Algorithms testing-evidence folder contains a screenshot showing all 18 tests passing, a screenshot showing the successful build, a complete Gradle HTML report for the local test suites, and a README explaining the results. This evidence supports the claims made about the search, lookup, filtering, sorting, validation, and data-structure behavior without relying on the separate Database enhancement evidence.

Completing this enhancement improved both the application and my understanding of algorithm selection. The final design does not use one structure for every operation. Instead, it combines structures with different strengths and includes synchronization logic and automated tests to manage the resulting trade-offs.

[Return to the ePortfolio Home Page](https://loganmeyer17.github.io/)

[Return to the ePortfolio Home Page](https://loganmeyer17.github.io/)
