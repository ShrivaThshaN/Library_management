package com.library.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    private static final String JDBC_URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        String url = System.getenv("DATABASE_URL");
        String user = System.getenv("PGUSER");
        String pass = System.getenv("PGPASSWORD");
        String host = System.getenv("PGHOST");
        String port = System.getenv("PGPORT");
        String db = System.getenv("PGDATABASE");

        if (url != null && !url.isEmpty()) {
            try {
                URI u = new URI(url.replaceFirst("^postgres(ql)?://", "http://"));
                String userInfo = u.getUserInfo();
                if (userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    if (user == null) user = parts[0];
                    if (pass == null && parts.length > 1) pass = parts[1];
                }
                if (host == null) host = u.getHost();
                if (port == null && u.getPort() > 0) port = String.valueOf(u.getPort());
                if (db == null) {
                    String path = u.getPath();
                    if (path != null && path.startsWith("/")) db = path.substring(1);
                }
            } catch (URISyntaxException ignored) {}
        }

        if (host == null) host = "localhost";
        if (port == null) port = "5432";
        if (db == null) db = "postgres";
        if (user == null) user = "postgres";
        if (pass == null) pass = "";

        JDBC_URL = "jdbc:postgresql://" + host + ":" + port + "/" + db + "?sslmode=prefer";
        USER = user;
        PASSWORD = pass;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static void initSchema() {
        String[] stmts = new String[] {
            "CREATE TABLE IF NOT EXISTS books (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "author VARCHAR(255) NOT NULL, " +
                "available BOOLEAN NOT NULL DEFAULT TRUE)",
            "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255))",
            "CREATE TABLE IF NOT EXISTS transactions (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INTEGER NOT NULL, " +
                "book_id INTEGER NOT NULL, " +
                "issue_date DATE, " +
                "return_date DATE, " +
                "fine DOUBLE PRECISION DEFAULT 0, " +
                "status VARCHAR(20))"
        };
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            for (String s : stmts) st.execute(s);

            try (java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    st.execute("INSERT INTO users (name, email) VALUES " +
                        "('Shrivathshan','shrivathshan@example.com')," +
                        "('Ananya Krishnan','ananya@example.com')," +
                        "('Vikram Saxena','vikram@example.com')," +
                        "('Meera Banerjee','meera@example.com')");
                }
            }
            try (java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    st.execute("INSERT INTO books (title, author, available) VALUES " +
                        "('Shadows of the Forgotten','Aarav Mehta',TRUE)," +
                        "('Echoes in the Wind','Lakshmi Iyer',TRUE)," +
                        "('The Silent Cipher','Daniel Brooks',TRUE)," +
                        "('Beneath the Mango Tree','Kavya Nair',TRUE)," +
                        "('Crimson Skies Over Madras','Rohan Pillai',TRUE)");
                }
            }
        } catch (SQLException e) {
            System.err.println("Schema init error: " + e.getMessage());
        }
    }
}
