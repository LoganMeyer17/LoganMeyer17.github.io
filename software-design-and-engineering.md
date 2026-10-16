---
title: Software Design and Engineering
---

# Artifact and Selection

For this enhancement, I chose the Inventory Tracking Android application I originally built in CS 360 Mobile Architecture and Programming. The application uses Java, XML layouts, and a local SQLite database. Users can create an account, log in, manage inventory items, and choose whether to allow SMS alerts.

I selected this application because its basic features worked, but there were several areas that needed improvement. The Activity classes handled too many responsibilities, and changing one inventory item caused the entire displayed list to be rebuilt. Validation and account creation were also handled directly within the screens. Including this project in my ePortfolio gives me a chance to show how I can review earlier work, recognize design problems, and improve the organization and maintainability of an existing application.

# Software Design Improvements

My main focus was organizing the code without completely changing how the application works. I created an InputValidator class to keep input rules in one place and an InventoryItem class to represent each inventory record. InventoryAdapter uses RecyclerView to display inventory items and reuse row views instead of rebuilding the full list after every change. DatabaseHelper now returns InventoryItem objects instead of requiring the Activity to work directly with database Cursors. These changes reduced the amount of work handled inside the inventory screen and gave each class a clearer purpose.

I separated account registration from login and added password confirmation during registration. The input requirements and error messages are clearer, making it easier for users to understand what needs to be corrected. Adding an inventory item now opens a separate dialog, and users must confirm before deleting an item. SmsHelper was also added to manage the SMS preference and notification logic outside the main Activity.

I made additional interface improvements while finalizing the application. Inventory items now display a simple status of “In stock,” “Low stock,” or “Out of stock.” The Add Item button becomes active only after all required information is valid, and missing fields display an error. I also added an inventory image to the login screen. I created the image myself in Microsoft Paint, so it is my own original work.

The cumulative application also addresses the security and ownership concerns mentioned in my instructor’s feedback. Passwords are salted and hashed instead of stored as readable text, and inventory records are connected to individual users so one account cannot view or change another user’s inventory. These changes are discussed in detail in the Database enhancement narrative. I kept them in the cumulative application because they make the completed project safer and more complete.

# Course Outcomes and Plan Updates

This enhancement mainly supports Course Outcome Four, which focuses on using appropriate computing techniques and tools to develop useful solutions. Separating responsibilities, creating model and helper classes, improving the inventory display, and testing the application all show progress toward this outcome.

The enhancement also supports Course Outcome Two because the code review and written narrative explain the design decisions and changes made to the application. Course Outcome Five is supported through input validation, permission handling, password protection, and user-owned inventory records. These improvements reduce security risks and provide better protection for user information.

I considered replacing SQLiteOpenHelper with Room, but I decided to improve the existing SQLite implementation instead. This allowed me to build on the original project while keeping the changes manageable. The later Database enhancement added password protection, inventory ownership, migration support, database constraints, and inventory history. Those changes are discussed separately in the Database narrative.

Search, sorting, SKU lookup, and restocking priorities were originally planned for the Algorithms and Data Structures enhancement. Those features were completed during a later stage of the project and remain included in the final application. The ePortfolio presents the final cumulative application under the Databases enhancement, while this narrative focuses on the Software Design and Engineering work.

# Reflection and Testing

One challenge was deciding how much to change at once. My first revision felt too different from the original application, so I chose a design that improved the structure while remaining easy for me to understand. This helped me think more carefully about why each class was needed and how the different parts of the application should work together.

Moving validation into its own class made the rules easier to understand and test separately from the screens. Separating the inventory model, adapter, database operations, account registration, and SMS logic also showed me how smaller classes can make an application easier to maintain.

My instructor’s feedback emphasized testing, password security, and user ownership. I addressed those concerns as the application developed through the later enhancements. The project passed 18 local tests covering validation and inventory organization. I also ran the application in the Android emulator to confirm that account creation, login, inventory management, searching, sorting, validation, and interface updates worked correctly. The final database and SMS instrumented tests are documented separately with the Database enhancement.

This enhancement taught me that software design is not only about adding more features. It also involves organizing responsibilities, protecting user information, testing changes, and making the application easier to understand and maintain. The completed artifact shows how I improved an earlier project through several stages while keeping its original purpose.

[Return to the ePortfolio Home Page](https://loganmeyer17.github.io/)
