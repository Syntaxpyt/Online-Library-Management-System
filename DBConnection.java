import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Creates JDBC connections to MySQL using settings from db.properties. */
public class DBConnection {
    private static final Properties props = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("Warning: db.properties not found, using defaults.");
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = props.getProperty("db.url",
                "jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        String user = props.getProperty("db.user", "root");
        String pass = props.getProperty("db.password", "");
        return DriverManager.getConnection(url, user, pass);
    }

    /** SHA-256 hex hash, so passwords are never stored as plain text. */
    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest(text.getBytes("UTF-8"))) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
