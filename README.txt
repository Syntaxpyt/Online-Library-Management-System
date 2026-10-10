ONLINE LIBRARY MANAGEMENT SYSTEM  (Java + JDBC + MySQL)
=======================================================

Console application. Java connects to a MySQL database through JDBC.
All data (books, members, transactions, notifications, search history,
librarian accounts) is stored in MySQL - nothing is stored in local files.

FILES
-----
Book.java, Member.java       model classes
DBConnection.java            JDBC connection + SHA-256 password hashing
Library.java                 all database operations (PreparedStatements)
LibraryManagementSystem.java console menus (main class)
library.sql                  MySQL schema + sample data
db.properties                DB url / user / password (edit this)

REQUIREMENTS
------------
1. JDK 8 or later
2. MySQL Server 5.7 / 8.x running
3. MySQL JDBC driver jar: download "Connector/J" (platform independent)
   from https://dev.mysql.com/downloads/connector/j/ and put the jar
   (e.g. mysql-connector-j-8.4.0.jar) in this folder.

SETUP
-----
1. Create the database and sample data:
       mysql -u root -p < library.sql
2. Edit db.properties and set db.user / db.password for your MySQL.
3. Compile (Windows uses ; , Linux/Mac uses : in the classpath):
       javac -cp ".;mysql-connector-j-8.4.0.jar" *.java
4. Run:
       java -cp ".;mysql-connector-j-8.4.0.jar" LibraryManagementSystem
   (Linux/Mac:  java -cp ".:mysql-connector-j-8.4.0.jar" LibraryManagementSystem)

LOGINS (from library.sql)
-------------------------
Librarian : admin   / admin123
Member    : MEM001  / 1234
(Passwords are stored in the database as SHA-256 hashes.)

FEATURES
--------
Librarian: add/update/delete books, add/update/delete members, view books,
           members and transactions, send notifications, inventory report,
           borrowing trends (incl. most borrowed books).
Member:    search books (title/author/genre/ISBN), view all books,
           borrow / return, borrowing history, search history,
           update profile, notification preference, view notifications
           (new ones are marked [NEW] and become read once viewed).

NOTES
-----
- Re-running library.sql resets the database to the sample data.
- Deleting a book keeps its borrowing history ("Deleted Book").
- A book that is currently borrowed cannot be deleted; a member with an
  active borrowed book cannot be deleted.
- Borrow/return run as database transactions (commit/rollback).
