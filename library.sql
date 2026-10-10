-- Online Library Management System : MySQL schema + sample data
-- Run once:  mysql -u root -p < library.sql
CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

DROP TABLE IF EXISTS search_history;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS librarians;

CREATE TABLE librarians (
    librarian_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL          -- SHA-256 hash (hex)
);

CREATE TABLE members (
    member_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    membership_id VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,         -- SHA-256 hash (hex)
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE books (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(150) NOT NULL,
    isbn VARCHAR(30) UNIQUE NOT NULL,
    genre VARCHAR(100) NOT NULL DEFAULT '',
    available BOOLEAN NOT NULL DEFAULT TRUE
);

-- Deleting a book keeps the history (book_id becomes NULL).
-- Deleting a member removes that member's own records.
CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NULL,
    member_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE SET NULL,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

CREATE TABLE notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    member_id INT NOT NULL,
    message VARCHAR(500) NOT NULL,
    notification_date DATETIME NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

CREATE TABLE search_history (
    search_id INT PRIMARY KEY AUTO_INCREMENT,
    member_id INT NOT NULL,
    search_text VARCHAR(200) NOT NULL,
    searched_at DATETIME NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

-- Passwords are stored as SHA-256 hashes (same algorithm as the Java code).
INSERT INTO librarians (username, password)
VALUES ('admin', SHA2('admin123', 256));

INSERT INTO members (name, email, membership_id, password)
VALUES ('Rahul Sharma', 'rahul@example.com', 'MEM001', SHA2('1234', 256));

INSERT INTO books (title, author, isbn, genre) VALUES
('Java Programming', 'Herbert Schildt', '9780071808552', 'Programming'),
('Python Crash Course', 'Eric Matthes', '9781593279288', 'Programming'),
('Clean Code', 'Robert C. Martin', '9780132350884', 'Programming');
