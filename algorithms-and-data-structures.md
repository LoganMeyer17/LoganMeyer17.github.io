---
title: Algorithms and Data Structures
---


# Artifact and Selection

For this enhancement, I used the Inventory Tracking Android application that I originally created in CS 360 Mobile Architecture and Programming before beginning CS 499. The application uses Java, XML layouts, and a local SQLite database. It allows users to create an account, log in, manage inventory records, and choose whether to allow SMS alerts. I completed the algorithms and data structures enhancement and incorporated it into the final cumulative artifact.

I selected this artifact because inventory information becomes harder to manage as the number of records increases. The earlier version could display inventory, but it did not give users useful ways to search, filter, sort, or prioritize the records. Improving these areas allowed me to demonstrate how algorithms and data structures can solve practical problems within an existing application.

# Algorithms and Data Structures Improvements

The main addition for this enhancement is the InventoryOrganizer class. This class uses a list to maintain the inventory records, a HashMap to associate each SKU with an InventoryItem, and a PriorityQueue to organize items that need to be restocked. Separating this logic from the inventory screen makes the algorithms easier to understand, maintain, and test.

The enhanced application allows users to search by an item name or SKU. An exact SKU search uses the HashMap for an average O(1) lookup. Partial searches require an O(n) scan because the application must compare the search text with each item. Users can also filter the inventory to show all items, low-stock items, or out-of-stock items. The sorting options organize records by name, SKU, quantity, or restocking priority. These comparison-based sorts generally require O(n log n) time.

Each inventory item includes a low-stock threshold. When an item’s quantity falls below that threshold, the application calculates its shortage by subtracting the current quantity from the threshold. The PriorityQueue uses this value to place the item with the greatest shortage first. This gives the user a clearer way to identify which inventory items need attention.

The SQLite database stores the low-stock threshold, and a migration adds the value to existing inventory records. I kept SQLiteOpenHelper instead of changing the application to Room because replacing the entire database system was not necessary for this enhancement. The database change remained focused on what the restocking algorithm needed while preserving compatibility with the rest of the application.

# Course Outcomes and Final Integration

I chose each structure based on the operation it needed to perform. The list supports displaying, filtering, and sorting records. The HashMap supports direct SKU lookup, and the PriorityQueue organizes items by restocking urgency.

These structures require additional memory and must remain consistent when an item changes. I decided to rebuild the HashMap and PriorityQueue from the list after an item is added, updated, or deleted. This requires another pass through the inventory, but it keeps the update logic in one place and reduces the chance of outdated information remaining in one of the structures. For the expected size of this application, I determined that the simpler and more reliable approach was appropriate.

The final cumulative version also demonstrates the use of well-founded testing and security practices. It includes salted password hashing, account-owned inventory records, database constraints, transaction-backed history, and migrations that preserve existing data. Integrating these later improvements without breaking the search, sorting, filtering, or restocking behavior shows that the algorithms enhancement works as part of a complete application rather than only as isolated code.
This enhancement fully addresses Course Outcome Three: Design and evaluate computing solutions that solve a given problem using algorithmic principles and computer science practices and standards appropriate to its solution, while managing the trade-offs involved in design choices. I addressed this outcome by selecting data structures based on the operations they needed to perform and evaluating their effects on performance, memory use, reliability, and code complexity.

The enhancement also supports Course Outcome Four: Demonstrate an ability to use well-founded and innovative techniques, skills, and tools in computing practices for the purpose of implementing computer solutions that deliver value and accomplish industry-specific goals. I demonstrated this outcome through Android development tools, automated unit and instrumented testing, database migrations, and the use of appropriate data structures. The cumulative security improvements support Course Outcome Five: Develop a security mindset that anticipates adversarial exploits in software architecture and designs to expose potential vulnerabilities, mitigate design flaws, and ensure privacy and enhanced security of data and resources. Password hashing, account-owned inventory, input validation, database constraints, and tests for unauthorized access provide evidence of this security mindset.

The process of responding to instructor feedback supports Course Outcome One: Employ strategies for building collaborative environments that enable diverse audiences to support organizational decision-making in the field of computer science. My instructor identified Android-device verification as the remaining gap, so I converted that feedback into specific migration, ownership, and persistence tests. I then organized the screenshots, generated reports, and README so that my instructor and other reviewers could independently evaluate the results and determine whether the application met its requirements.

The written materials support Course Outcome Two: Design, develop, and deliver professional-quality oral, written, and visual communications that are coherent, technically sound, and appropriately adapted to specific audiences and contexts. The enhancement narrative, testing documentation, code-review presentation, screenshots, and ePortfolio explain the project and its results for both technical and nontechnical audiences. My professional self-assessment and completed ePortfolio will provide additional evidence for Course Outcomes One and Two.

# Reflection, Testing, and Feedback

One of the main challenges was keeping the list, HashMap, and PriorityQueue synchronized. A quantity change can affect the displayed record, the item stored under an SKU, and its position in the restocking order. I learned that a Java PriorityQueue does not automatically rearrange an existing item when one of its values changes. Rebuilding the supporting structures after a change provided a manageable solution and made the behavior easier to test.

I also learned that one data structure is not the best choice for every task. The HashMap improves exact SKU searches, but partial searches and filters still need to examine the inventory records. Sorting requires additional processing, but it gives the user a more useful view of the information. This enhancement helped me better understand how performance, memory use, correctness, and code complexity affect design decisions.

Six algorithms and data structures tests cover exact SKU lookup, partial searches, stock filters, sorting, restocking priority, and synchronization after records are changed or removed. These tests passed along with twelve input-validation tests, for a total of 18 passing local unit tests. I also ran 10 Android instrumented tests on the Medium Phone API 36.1 emulator. Eight database tests and two SMS preference tests passed, bringing the complete result to 28 of 28 tests passed with no failures or skipped tests.

My instructor identified Android-device verification as the remaining issue after the database enhancement and specifically requested evidence for migration, ownership, and persistence. The instrumented tests confirmed that earlier records migrated without data loss or plaintext password storage, inventory remained separated by account, and authentication and updated inventory data remained available after the database was closed and reopened.

The final artifact includes a Testing Evidence folder with screenshots, generated Gradle reports, and a README that summarizes the results. This verification addresses the instructor’s feedback and supports the claims made about the completed algorithms, validation, security, database, and persistence enhancements.

[Return to the ePortfolio Home Page](https://loganmeyer17.github.io/)
