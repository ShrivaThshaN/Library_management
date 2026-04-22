package com.library.services;

import com.library.util.DBUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TransactionService {

    // Issue a book to a user
    public JsonObject issueBook(int userId, int bookId) {
        JsonObject result = new JsonObject();

        try (Connection conn = DBUtil.getConnection()) {
            // Verify user exists
            String userCheck = "SELECT id FROM users WHERE id = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userCheck)) {
                userStmt.setInt(1, userId);
                ResultSet userRs = userStmt.executeQuery();
                if (!userRs.next()) {
                    result.addProperty("success", false);
                    result.addProperty("message", "User not found with ID: " + userId);
                    return result;
                }
            }

            // Check if book exists and is available
            String bookCheck = "SELECT available FROM books WHERE id = ?";
            try (PreparedStatement bookStmt = conn.prepareStatement(bookCheck)) {
                bookStmt.setInt(1, bookId);
                ResultSet bookRs = bookStmt.executeQuery();
                if (!bookRs.next()) {
                    result.addProperty("success", false);
                    result.addProperty("message", "Book not found with ID: " + bookId);
                    return result;
                }
                if (!bookRs.getBoolean("available")) {
                    result.addProperty("success", false);
                    result.addProperty("message", "Book is currently unavailable (already issued)");
                    return result;
                }
            }

            // Check if user already has this book issued
            String dupCheck = "SELECT id FROM transactions WHERE user_id = ? AND book_id = ? AND status = 'ISSUED'";
            try (PreparedStatement dupStmt = conn.prepareStatement(dupCheck)) {
                dupStmt.setInt(1, userId);
                dupStmt.setInt(2, bookId);
                ResultSet dupRs = dupStmt.executeQuery();
                if (dupRs.next()) {
                    result.addProperty("success", false);
                    result.addProperty("message", "This user already has this book issued");
                    return result;
                }
            }

            conn.setAutoCommit(false);
            try {
                // Insert transaction
                String insertSql = "INSERT INTO transactions (user_id, book_id, issue_date, status) VALUES (?, ?, ?, 'ISSUED')";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, bookId);
                    insertStmt.setDate(3, Date.valueOf(LocalDate.now()));
                    insertStmt.executeUpdate();
                }

                // Update book availability
                String updateSql = "UPDATE books SET available = false WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();
                }

                conn.commit();
                result.addProperty("success", true);
                result.addProperty("message", "Book issued successfully to User ID: " + userId);
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

    // Return a book
    public JsonObject returnBook(int userId, int bookId) {
        JsonObject result = new JsonObject();

        try (Connection conn = DBUtil.getConnection()) {
            // Find the latest ISSUED transaction for this user and book
            String findSql = "SELECT id, issue_date FROM transactions WHERE user_id = ? AND book_id = ? AND status = 'ISSUED' ORDER BY id DESC LIMIT 1";
            int transactionId = -1;
            LocalDate issueDate = null;

            try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                findStmt.setInt(1, userId);
                findStmt.setInt(2, bookId);
                ResultSet rs = findStmt.executeQuery();

                if (!rs.next()) {
                    result.addProperty("success", false);
                    result.addProperty("message", "No active issue found for User " + userId + " and Book " + bookId);
                    return result;
                }

                transactionId = rs.getInt("id");
                issueDate = rs.getDate("issue_date").toLocalDate();
            }

            // Calculate fine
            LocalDate returnDate = LocalDate.now();
            long daysBorrowed = ChronoUnit.DAYS.between(issueDate, returnDate);
            double fine = 0.0;
            if (daysBorrowed > 7) {
                fine = (daysBorrowed - 7) * 10.0;
            }

            conn.setAutoCommit(false);
            try {
                // Update transaction
                String updateTxnSql = "UPDATE transactions SET return_date = ?, fine = ?, status = 'RETURNED' WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateTxnSql)) {
                    updateStmt.setDate(1, Date.valueOf(returnDate));
                    updateStmt.setDouble(2, fine);
                    updateStmt.setInt(3, transactionId);
                    updateStmt.executeUpdate();
                }

                // Update book availability
                String updateBookSql = "UPDATE books SET available = true WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateBookSql)) {
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();
                }

                conn.commit();

                String message = "Book returned successfully.";
                if (fine > 0) {
                    message += " Fine charged: ₹" + String.format("%.2f", fine) + " (" + (daysBorrowed - 7) + " overdue days)";
                } else {
                    message += " No fine applicable.";
                }

                result.addProperty("success", true);
                result.addProperty("message", message);
                result.addProperty("fine", fine);
                result.addProperty("daysBorrowed", daysBorrowed);

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

    // Get all transactions
    public JsonArray getAllTransactions() {
        JsonArray transactions = new JsonArray();
        String sql = """
            SELECT t.id, t.user_id, u.name AS user_name, t.book_id, b.title AS book_title,
                   t.issue_date, t.return_date, t.fine, t.status
            FROM transactions t
            LEFT JOIN users u ON t.user_id = u.id
            LEFT JOIN books b ON t.book_id = b.id
            ORDER BY t.id DESC
        """;

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JsonObject txn = new JsonObject();
                txn.addProperty("id", rs.getInt("id"));
                txn.addProperty("userId", rs.getInt("user_id"));
                txn.addProperty("userName", rs.getString("user_name") != null ? rs.getString("user_name") : "Unknown");
                txn.addProperty("bookId", rs.getInt("book_id"));
                txn.addProperty("bookTitle", rs.getString("book_title") != null ? rs.getString("book_title") : "Deleted");
                txn.addProperty("issueDate", rs.getDate("issue_date") != null ? rs.getDate("issue_date").toString() : "");
                txn.addProperty("returnDate", rs.getDate("return_date") != null ? rs.getDate("return_date").toString() : "—");
                txn.addProperty("fine", rs.getDouble("fine"));
                txn.addProperty("status", rs.getString("status"));
                transactions.add(txn);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return transactions;
    }

    // Add a new user (member)
    public JsonObject addUser(String name, String email) {
        JsonObject result = new JsonObject();
        if (name == null || name.trim().isEmpty()) {
            result.addProperty("success", false);
            result.addProperty("message", "Name is required");
            return result;
        }
        String sql = "INSERT INTO users (name, email) VALUES (?, ?) RETURNING id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            stmt.setString(2, email == null ? "" : email.trim());
            ResultSet rs = stmt.executeQuery();
            int id = 0;
            if (rs.next()) id = rs.getInt(1);
            result.addProperty("success", true);
            result.addProperty("message", "Member registered with ID: " + id);
            result.addProperty("id", id);
        } catch (SQLException e) {
            result.addProperty("success", false);
            result.addProperty("message", "Database error: " + e.getMessage());
        }
        return result;
    }

    // Delete a user (member)
    public JsonObject deleteUser(int id) {
        JsonObject result = new JsonObject();
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE user_id = ? AND status = 'ISSUED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement chk = conn.prepareStatement(checkSql)) {
            chk.setInt(1, id);
            ResultSet rs = chk.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                result.addProperty("success", false);
                result.addProperty("message", "Cannot delete — member has active issued books");
                return result;
            }
        } catch (SQLException e) {
            result.addProperty("success", false);
            result.addProperty("message", "Database error: " + e.getMessage());
            return result;
        }
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement t = conn.prepareStatement("DELETE FROM transactions WHERE user_id = ?");
                 PreparedStatement u = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                t.setInt(1, id); t.executeUpdate();
                u.setInt(1, id);
                int rows = u.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    result.addProperty("success", true);
                    result.addProperty("message", "Member deleted successfully");
                } else {
                    conn.rollback();
                    result.addProperty("success", false);
                    result.addProperty("message", "Member not found with ID: " + id);
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

    // Get all users (helper for frontend)
    public JsonArray getAllUsers() {
        JsonArray users = new JsonArray();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JsonObject user = new JsonObject();
                user.addProperty("id", rs.getInt("id"));
                user.addProperty("name", rs.getString("name"));
                user.addProperty("email", rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }
}
