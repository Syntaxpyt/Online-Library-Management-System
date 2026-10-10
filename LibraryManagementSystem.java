import java.util.ArrayList;
import java.util.Scanner;

public class LibraryManagementSystem {
    static Scanner sc = new Scanner(System.in);
    static Library library = new Library();

    public static void main(String[] args) {
        if (!library.testConnection()) {
            System.out.println("Start MySQL, run library.sql, and check db.properties and the JDBC driver jar.");
            return;
        }
        System.out.println("Connected to MySQL database.");

        while (true) {
            System.out.println("\n======================================");
            System.out.println("     ONLINE LIBRARY MANAGEMENT");
            System.out.println("======================================");
            System.out.println("1. Librarian Login");
            System.out.println("2. Member Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    librarianLogin();
                    break;
                case 2:
                    memberLogin();
                    break;
                case 3:
                    System.out.println("Thank you for using the system.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void librarianLogin() {
        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        if (library.loginLibrarian(username, password)) {
            librarianDashboard();
        } else {
            System.out.println("Invalid librarian login.");
        }
    }

    static void librarianDashboard() {
        while (true) {
            System.out.println("\n========== LIBRARIAN DASHBOARD ==========");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. View Books");
            System.out.println("5. Add Member");
            System.out.println("6. Update Member");
            System.out.println("7. Delete Member");
            System.out.println("8. View Members");
            System.out.println("9. View Transactions");
            System.out.println("10. Send Notification");
            System.out.println("11. Inventory Report");
            System.out.println("12. Borrowing Trends");
            System.out.println("13. Logout");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    updateBook();
                    break;
                case 3:
                    deleteBook();
                    break;
                case 4:
                    library.showBooks();
                    break;
                case 5:
                    addMember();
                    break;
                case 6:
                    updateMember();
                    break;
                case 7:
                    deleteMember();
                    break;
                case 8:
                    library.showMembers();
                    break;
                case 9:
                    library.showAllTransactions();
                    break;
                case 10:
                    sendNotification();
                    break;
                case 11:
                    library.inventoryReport();
                    break;
                case 12:
                    library.borrowingTrends();
                    break;
                case 13:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void addBook() {
        String title = readRequired("Title: ");

        String author = readRequired("Author: ");

        String isbn = readRequired("ISBN: ");

        System.out.print("Genre: ");
        String genre = sc.nextLine().trim();

        library.addBook(title, author, isbn, genre);
    }

    static void updateBook() {
        System.out.print("Enter Book ID: ");
        int id = readInt();

        String title = readRequired("New Title: ");

        String author = readRequired("New Author: ");

        String isbn = readRequired("New ISBN: ");

        System.out.print("New Genre: ");
        String genre = sc.nextLine().trim();

        String err = library.updateBook(id, title, author, isbn, genre);
        System.out.println(err == null ? "Book updated successfully." : err);
    }

    static void deleteBook() {
        System.out.print("Enter Book ID: ");
        int id = readInt();

        String err = library.deleteBook(id);
        System.out.println(err == null ? "Book deleted successfully." : err);
    }

    static void addMember() {
        String name = readRequired("Name: ");

        String email = readRequired("Email: ");

        String membershipId = readRequired("Membership ID: ");

        String password = readRequired("Password: ");

        library.addMember(name, email, membershipId, password);
    }

    static void updateMember() {
        System.out.print("Enter Member ID: ");
        int id = readInt();

        String name = readRequired("New Name: ");

        String email = readRequired("New Email: ");

        String membershipId = readRequired("New Membership ID: ");

        System.out.print("New Password (leave blank to keep current): ");
        String password = sc.nextLine();

        String err = library.updateMember(id, name, email, membershipId, password);
        System.out.println(err == null ? "Member updated successfully." : err);
    }

    static void deleteMember() {
        System.out.print("Enter Member ID: ");
        int id = readInt();

        String err = library.deleteMember(id);
        System.out.println(err == null ? "Member deleted successfully." : err);
    }

    static void sendNotification() {
        System.out.print("Enter Member ID: ");
        int memberId = readInt();

        String message = readRequired("Enter notification message: ");

        library.sendNotification(memberId, message);
    }

    static void memberLogin() {
        String membershipId = readRequired("Membership ID: ");

        System.out.print("Password: ");
        String password = sc.nextLine();

        Member member = library.loginMember(membershipId, password);

        if (member == null) {
            System.out.println("Invalid membership ID or password.");
            return;
        }

        memberDashboard(member);
    }

    static void memberDashboard(Member member) {
        while (true) {
            System.out.println("\n========== MEMBER DASHBOARD ==========");
            System.out.println("Welcome, " + member.getName());
            System.out.println("1. Search Books");
            System.out.println("2. View All Books");
            System.out.println("3. Borrow Book");
            System.out.println("4. Return Book");
            System.out.println("5. Borrowing History");
            System.out.println("6. Search History");
            System.out.println("7. Update Profile");
            System.out.println("8. Notification Alerts");
            System.out.println("9. Notification Preference");
            System.out.println("10. Logout");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    searchBooks(member);
                    break;
                case 2:
                    library.showBooks();
                    break;
                case 3:
                    borrowBook(member);
                    break;
                case 4:
                    returnBook(member);
                    break;
                case 5:
                    library.showMemberHistory(member.getMemberId());
                    break;
                case 6:
                    library.showSearchHistory(member.getMemberId());
                    break;
                case 7:
                    updateProfile(member);
                    break;
                case 8:
                    library.showNotifications(member.getMemberId());
                    break;
                case 9:
                    notificationPreference(member);
                    break;
                case 10:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void searchBooks(Member member) {
        System.out.print("Enter title/author/genre/ISBN to search: ");
        String keyword = sc.nextLine();

        ArrayList<Book> result =
                library.searchBooks(keyword, member.getMemberId());

        if (result.isEmpty()) {
            System.out.println("No matching books found.");
            return;
        }

        System.out.println("\nSEARCH RESULTS");
        Library.printBookHeader();

        for (Book b : result) {
            System.out.println(b);
        }
    }

    static void borrowBook(Member member) {
        System.out.print("Enter Book ID: ");
        int bookId = readInt();

        String err = library.borrowBook(bookId, member.getMemberId());
        System.out.println(err == null ? "Book borrowed successfully." : "Unable to borrow: " + err);
    }

    static void returnBook(Member member) {
        System.out.print("Enter Book ID: ");
        int bookId = readInt();

        String err = library.returnBook(bookId, member.getMemberId());
        System.out.println(err == null ? "Book returned successfully." : "Unable to return: " + err);
    }

    static void updateProfile(Member member) {
        String name = readRequired("New Name: ");

        String email = readRequired("New Email: ");

        System.out.print("New Password (leave blank to keep current): ");
        String password = sc.nextLine();

        if (library.updateProfile(member.getMemberId(), name, email, password)) {
            member.setName(name);
            member.setEmail(email);
            System.out.println("Profile updated successfully.");
        } else {
            System.out.println("Could not update profile.");
        }
    }

    static void notificationPreference(Member member) {
        System.out.println("1. Enable Notifications");
        System.out.println("2. Disable Notifications");
        System.out.print("Enter choice: ");

        int choice = readInt();
        boolean enabled;

        if (choice == 1) {
            enabled = true;
        } else if (choice == 2) {
            enabled = false;
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        if (library.setNotificationPreference(member.getMemberId(), enabled)) {
            member.setNotificationsEnabled(enabled);
            System.out.println("Notification preference updated.");
        } else {
            System.out.println("Could not update preference.");
        }
    }

    /** Keeps asking until a non-empty value is entered. */
    static String readRequired(String label) {
        while (true) {
            System.out.print(label);
            String v = sc.nextLine().trim();
            if (!v.isEmpty()) return v;
            System.out.println("This field cannot be empty.");
        }
    }

    static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}
