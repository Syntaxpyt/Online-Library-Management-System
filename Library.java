import java.sql.*;
import java.util.ArrayList;

/** All database operations (JDBC + MySQL). */
public class Library {

    // ---------- helpers ----------
    private static boolean isDuplicate(SQLException e) {
        return e.getErrorCode() == 1062 || "23000".equals(e.getSQLState());
    }

    private static void error(String action, SQLException e) {
        System.out.println("Database error while " + action + ": " + e.getMessage());
    }

    private static Book readBook(ResultSet rs) throws SQLException {
        return new Book(rs.getInt("book_id"), rs.getString("title"),
                rs.getString("author"), rs.getString("isbn"),
                rs.getString("genre"), rs.getBoolean("available"));
    }

    private static Member readMember(ResultSet rs) throws SQLException {
        return new Member(rs.getInt("member_id"), rs.getString("name"),
                rs.getString("email"), rs.getString("membership_id"),
                rs.getBoolean("notifications_enabled"));
    }

    /** Checks that the database can be reached. */
    public boolean testConnection() {
        try (Connection c = DBConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            System.out.println("Cannot connect to MySQL: " + e.getMessage());
            return false;
        }
    }

    // ---------- login ----------
    public boolean loginLibrarian(String username, String password) {
        String sql = "SELECT 1 FROM librarians WHERE username=? AND password=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, DBConnection.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            error("logging in", e);
            return false;
        }
    }

    public Member loginMember(String membershipId, String password) {
        String sql = "SELECT * FROM members WHERE membership_id=? AND password=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, membershipId);
            ps.setString(2, DBConnection.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readMember(rs) : null;
            }
        } catch (SQLException e) {
            error("logging in", e);
            return null;
        }
    }

    // ---------- books ----------
    public void addBook(String title, String author, String isbn, String genre) {
        String sql = "INSERT INTO books (title, author, isbn, genre) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.setString(4, genre);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("Book added successfully. Book ID: " + keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (isDuplicate(e)) System.out.println("A book with this ISBN already exists.");
            else error("adding book", e);
        }
    }

    /** @return null on success, otherwise an error message */
    public String updateBook(int id, String title, String author, String isbn, String genre) {
        String sql = "UPDATE books SET title=?, author=?, isbn=?, genre=? WHERE book_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.setString(4, genre);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0 ? null : "Book not found.";
        } catch (SQLException e) {
            return isDuplicate(e) ? "Another book already has this ISBN."
                    : "Database error: " + e.getMessage();
        }
    }

    /** @return null on success, otherwise an error message */
    public String deleteBook(int id) {
        String sql = "DELETE FROM books WHERE book_id=? AND available=TRUE";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0) return null;
            return findBook(id) == null ? "Book not found." : "Book is currently borrowed.";
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    public Book findBook(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM books WHERE book_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readBook(rs) : null;
            }
        } catch (SQLException e) {
            error("finding book", e);
            return null;
        }
    }

    public void showBooks() {
        ArrayList<Book> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM books ORDER BY book_id")) {
            while (rs.next()) list.add(readBook(rs));
        } catch (SQLException e) {
            error("loading books", e);
            return;
        }
        if (list.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.println("\nBOOK INVENTORY");
        printBookHeader();
        for (Book b : list) System.out.println(b);
    }

    static void printBookHeader() {
        System.out.printf("%-5s %-25s %-20s %-15s %-15s %-10s%n",
                "ID", "TITLE", "AUTHOR", "ISBN", "GENRE", "STATUS");
        System.out.println("------------------------------------------------------------------------------------------");
    }

    public ArrayList<Book> searchBooks(String keyword, int memberId) {
        ArrayList<Book> result = new ArrayList<>();
        String like = "%" + keyword + "%";
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? "
                + "OR genre LIKE ? OR isbn LIKE ? ORDER BY book_id";
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 1; i <= 4; i++) ps.setString(i, like);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) result.add(readBook(rs));
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO search_history (member_id, search_text, searched_at) VALUES (?,?,NOW())")) {
                ps.setInt(1, memberId);
                ps.setString(2, keyword.length() > 200 ? keyword.substring(0, 200) : keyword);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            error("searching books", e);
        }
        return result;
    }

    // ---------- members ----------
    public void addMember(String name, String email, String membershipId, String password) {
        String sql = "INSERT INTO members (name, email, membership_id, password) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, membershipId);
            ps.setString(4, DBConnection.hash(password));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("Member created successfully. Member ID: " + keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (isDuplicate(e)) System.out.println("This Membership ID already exists.");
            else error("adding member", e);
        }
    }

    /** A blank password keeps the existing one. @return null on success */
    public String updateMember(int id, String name, String email,
                               String membershipId, String password) {
        boolean changePw = !password.isEmpty();
        String sql = "UPDATE members SET name=?, email=?, membership_id=?"
                + (changePw ? ", password=?" : "") + " WHERE member_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, name);
            ps.setString(i++, email);
            ps.setString(i++, membershipId);
            if (changePw) ps.setString(i++, DBConnection.hash(password));
            ps.setInt(i, id);
            return ps.executeUpdate() > 0 ? null : "Member not found.";
        } catch (SQLException e) {
            return isDuplicate(e) ? "This Membership ID already exists."
                    : "Database error: " + e.getMessage();
        }
    }

    /** @return null on success, otherwise an error message */
    public String deleteMember(int id) {
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM transactions WHERE member_id=? AND status='BORROWED' LIMIT 1")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return "Member has an active borrowed book.";
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM members WHERE member_id=?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0 ? null : "Member not found.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    public void showMembers() {
        ArrayList<Member> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM members ORDER BY member_id")) {
            while (rs.next()) list.add(readMember(rs));
        } catch (SQLException e) {
            error("loading members", e);
            return;
        }
        if (list.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.println("\nMEMBER LIST");
        System.out.printf("%-5s %-20s %-30s %-15s %-12s%n",
                "ID", "NAME", "EMAIL", "MEMBERSHIP", "NOTIFY");
        System.out.println("--------------------------------------------------------------------------------");
        for (Member m : list) System.out.println(m);
    }

    public Member findMember(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM members WHERE member_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readMember(rs) : null;
            }
        } catch (SQLException e) {
            error("finding member", e);
            return null;
        }
    }

    public boolean updateProfile(int memberId, String name, String email, String password) {
        boolean changePw = !password.isEmpty();
        String sql = "UPDATE members SET name=?, email=?" + (changePw ? ", password=?" : "")
                + " WHERE member_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, name);
            ps.setString(i++, email);
            if (changePw) ps.setString(i++, DBConnection.hash(password));
            ps.setInt(i, memberId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            error("updating profile", e);
            return false;
        }
    }

    public boolean setNotificationPreference(int memberId, boolean enabled) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE members SET notifications_enabled=? WHERE member_id=?")) {
            ps.setBoolean(1, enabled);
            ps.setInt(2, memberId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            error("updating preference", e);
            return false;
        }
    }

    // ---------- borrow / return (done as one DB transaction) ----------
    /** @return null on success, otherwise the reason it failed */
    public String borrowBook(int bookId, int memberId) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // atomic: only succeeds if the book is still available
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET available=FALSE WHERE book_id=? AND available=TRUE")) {
                    ps.setInt(1, bookId);
                    if (ps.executeUpdate() == 0) {
                        c.rollback();
                        return findBook(bookId) == null ? "Book not found."
                                : "Book is already borrowed.";
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO transactions (book_id, member_id, borrow_date, status) "
                                + "VALUES (?,?,CURDATE(),'BORROWED')")) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, memberId);
                    ps.executeUpdate();
                }
                c.commit();
                return null;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    /** @return null on success, otherwise the reason it failed */
    public String returnBook(int bookId, int memberId) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE transactions SET status='RETURNED', return_date=CURDATE() "
                                + "WHERE book_id=? AND member_id=? AND status='BORROWED'")) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, memberId);
                    if (ps.executeUpdate() == 0) {
                        c.rollback();
                        return "You have not borrowed this book.";
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET available=TRUE WHERE book_id=?")) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                c.commit();
                return null;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    // ---------- history / transactions ----------
    public void showMemberHistory(int memberId) {
        String sql = "SELECT t.transaction_id, b.title, t.borrow_date, t.return_date, t.status "
                + "FROM transactions t LEFT JOIN books b ON t.book_id=b.book_id "
                + "WHERE t.member_id=? ORDER BY t.transaction_id";
        System.out.println("\nBORROWING HISTORY");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    String title = rs.getString("title");
                    System.out.println("Transaction ID : " + rs.getInt("transaction_id"));
                    System.out.println("Book           : " + (title == null ? "Deleted Book" : title));
                    System.out.println("Borrow Date    : " + rs.getDate("borrow_date"));
                    Date ret = rs.getDate("return_date");
                    System.out.println("Return Date    : " + (ret == null ? "-" : ret));
                    System.out.println("Status         : " + rs.getString("status"));
                    System.out.println("-------------------------------------------");
                }
                if (!found) System.out.println("No borrowing history.");
            }
        } catch (SQLException e) {
            error("loading history", e);
        }
    }

    public void showAllTransactions() {
        String sql = "SELECT t.transaction_id, t.book_id, b.title, t.member_id, m.name, "
                + "t.borrow_date, t.return_date, t.status FROM transactions t "
                + "LEFT JOIN books b ON t.book_id=b.book_id "
                + "JOIN members m ON t.member_id=m.member_id ORDER BY t.transaction_id";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            boolean found = false;
            System.out.println("\nTRANSACTION LIST");
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                System.out.println("Transaction ID : " + rs.getInt("transaction_id"));
                System.out.println("Book           : " + (title == null ? "Deleted Book" : title)
                        + (rs.getObject("book_id") == null ? "" : " (ID " + rs.getInt("book_id") + ")"));
                System.out.println("Member         : " + rs.getString("name")
                        + " (ID " + rs.getInt("member_id") + ")");
                System.out.println("Borrow Date    : " + rs.getDate("borrow_date"));
                Date ret = rs.getDate("return_date");
                System.out.println("Return Date    : " + (ret == null ? "-" : ret));
                System.out.println("Status         : " + rs.getString("status"));
                System.out.println("-------------------------------------------");
            }
            if (!found) System.out.println("No transactions found.");
        } catch (SQLException e) {
            error("loading transactions", e);
        }
    }

    // ---------- notifications ----------
    public void sendNotification(int memberId, String message) {
        Member member = findMember(memberId);
        if (member == null) {
            System.out.println("Member not found.");
            return;
        }
        if (!member.isNotificationsEnabled()) {
            System.out.println("Member has disabled notifications.");
            return;
        }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO notifications (member_id, message, notification_date) VALUES (?,?,NOW())")) {
            ps.setInt(1, memberId);
            ps.setString(2, message.length() > 500 ? message.substring(0, 500) : message);
            ps.executeUpdate();
            System.out.println("Notification sent successfully.");
        } catch (SQLException e) {
            error("sending notification", e);
        }
    }

    /** Shows the member's notifications and marks them as read. */
    public void showNotifications(int memberId) {
        System.out.println("\nNOTIFICATIONS");
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT message, notification_date, is_read FROM notifications "
                            + "WHERE member_id=? ORDER BY notification_date DESC, notification_id DESC")) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        System.out.println((rs.getBoolean("is_read") ? "   " : "[NEW] ")
                                + rs.getTimestamp("notification_date") + " - " + rs.getString("message"));
                    }
                    if (!found) System.out.println("No notifications.");
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE notifications SET is_read=TRUE WHERE member_id=?")) {
                ps.setInt(1, memberId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            error("loading notifications", e);
        }
    }

    public void showSearchHistory(int memberId) {
        System.out.println("\nBOOK SEARCH HISTORY");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT search_text, searched_at FROM search_history WHERE member_id=? "
                             + "ORDER BY search_id DESC LIMIT 20")) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("- " + rs.getString("search_text")
                            + "  (" + rs.getTimestamp("searched_at") + ")");
                }
                if (!found) System.out.println("No searches yet.");
            }
        } catch (SQLException e) {
            error("loading search history", e);
        }
    }

    // ---------- reports ----------
    private int count(Statement st, String sql) throws SQLException {
        try (ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public void inventoryReport() {
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement()) {
            System.out.println("\n========== INVENTORY REPORT ==========");
            System.out.println("Total Books       : " + count(st, "SELECT COUNT(*) FROM books"));
            System.out.println("Available Books   : " + count(st, "SELECT COUNT(*) FROM books WHERE available=TRUE"));
            System.out.println("Borrowed Books    : " + count(st, "SELECT COUNT(*) FROM books WHERE available=FALSE"));
            System.out.println("Total Members     : " + count(st, "SELECT COUNT(*) FROM members"));
            System.out.println("Total Transactions: " + count(st, "SELECT COUNT(*) FROM transactions"));
            System.out.println("======================================");
        } catch (SQLException e) {
            error("creating report", e);
        }
    }

    public void borrowingTrends() {
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement()) {
            int total = count(st, "SELECT COUNT(*) FROM transactions");
            int returned = count(st, "SELECT COUNT(*) FROM transactions WHERE status='RETURNED'");
            System.out.println("\n========== BORROWING TRENDS ==========");
            System.out.println("Total Borrowings : " + total);
            System.out.println("Total Returns    : " + returned);
            System.out.println("Currently Out    : " + (total - returned));
            System.out.println("--- Most borrowed books ---");
            try (ResultSet rs = st.executeQuery(
                    "SELECT b.title, COUNT(*) AS n FROM transactions t JOIN books b "
                            + "ON t.book_id=b.book_id GROUP BY b.book_id, b.title "
                            + "ORDER BY n DESC LIMIT 5")) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.println(rs.getString("title") + " - " + rs.getInt("n") + " time(s)");
                }
                if (!any) System.out.println("(none yet)");
            }
            System.out.println("======================================");
        } catch (SQLException e) {
            error("creating report", e);
        }
    }
}
