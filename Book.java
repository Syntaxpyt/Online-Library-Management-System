public class Book {
    private final int bookId;
    private final String title;
    private final String author;
    private final String isbn;
    private final String genre;
    private final boolean available;

    public Book(int bookId, String title, String author, String isbn,
                String genre, boolean available) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre == null ? "" : genre;
        this.available = available;
    }

    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getGenre() { return genre; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() {
        return String.format("%-5d %-25s %-20s %-15s %-15s %-10s",
                bookId, cut(title, 25), cut(author, 20), isbn, cut(genre, 15),
                available ? "Available" : "Borrowed");
    }

    static String cut(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n - 1) + "~";
    }
}
