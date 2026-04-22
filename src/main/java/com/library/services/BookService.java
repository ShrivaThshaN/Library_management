package com.library.services;

import com.library.util.DBUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;

public class BookService {

    // Add a new book
    public JsonObject addBook(String title, String author) {
        JsonObject result = new JsonObject();
        if (title == null || title.trim().isEmpty() || author == null || author.trim().isEmpty()) {
            result.addProperty("success", false);
            result.addProperty("message", "Title and Author are required fields");
            return result;
        }

        String sql = "INSERT INTO books (title, author, available) VALUES (?, ?, true)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, title.trim());
            stmt.setString(2, author.trim());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            int id = 0;
            if (keys.next()) {
                id = keys.getInt(1);
            }

            result.addProperty("success", true);
            result.addProperty("message", "Book added successfully with ID: " + id);
            result.addProperty("id", id);

        } catch (SQLException e) {
            result.addProperty("success", false);
            result.addProperty("message", "Database error: " + e.getMessage());
        }
        return result;
    }

    // Get all books
    public JsonArray getAllBooks() {
        JsonArray books = new JsonArray();
        String sql = "SELECT * FROM books ORDER BY id DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JsonObject book = new JsonObject();
                book.addProperty("id", rs.getInt("id"));
                book.addProperty("title", rs.getString("title"));
                book.addProperty("author", rs.getString("author"));
                book.addProperty("available", rs.getBoolean("available"));
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books: " + e.getMessage());
        }
        return books;
    }

    // Delete a book by ID
    public JsonObject deleteBook(int id) {
        JsonObject result = new JsonObject();

        // Check if book has active transactions
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND status = 'ISSUED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                result.addProperty("success", false);
                result.addProperty("message", "Cannot delete book — it is currently issued");
                return result;
            }
        } catch (SQLException e) {
            result.addProperty("success", false);
            result.addProperty("message", "Database error: " + e.getMessage());
            return result;
        }

        // Delete related transactions first, then the book
        String deleteTxnSql = "DELETE FROM transactions WHERE book_id = ?";
        String deleteBookSql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement txnStmt = conn.prepareStatement(deleteTxnSql);
                 PreparedStatement bookStmt = conn.prepareStatement(deleteBookSql)) {

                txnStmt.setInt(1, id);
                txnStmt.executeUpdate();

                bookStmt.setInt(1, id);
                int rows = bookStmt.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    result.addProperty("success", true);
                    result.addProperty("message", "Book deleted successfully");
                } else {
                    conn.rollback();
                    result.addProperty("success", false);
                    result.addProperty("message", "Book not found with ID: " + id);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            result.addProperty("success", false);
            result.addProperty("message", "Database error: " + e.getMessage());
        }
        return result;
    }

    // Search books by title or author
    public JsonArray searchBooks(String keyword) {
        JsonArray books = new JsonArray();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }

        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + keyword.trim() + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JsonObject book = new JsonObject();
                book.addProperty("id", rs.getInt("id"));
                book.addProperty("title", rs.getString("title"));
                book.addProperty("author", rs.getString("author"));
                book.addProperty("available", rs.getBoolean("available"));
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error searching books: " + e.getMessage());
        }
        return books;
    }
}
