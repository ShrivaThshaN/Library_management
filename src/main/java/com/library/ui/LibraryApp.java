package com.library.ui;

import com.library.services.BookService;
import com.library.services.TransactionService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class LibraryApp {

    private static final BookService bookService = new BookService();
    private static final TransactionService transactionService = new TransactionService();

    public static void main(String[] args) {
        com.library.util.DBUtil.initSchema();

        String portEnv = System.getenv("PORT");
        int port = (portEnv != null && !portEnv.isEmpty()) ? Integer.parseInt(portEnv) : 5000;

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/static", Location.CLASSPATH);
        }).start("0.0.0.0", port);

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  📚 Library Management System is running!");
        System.out.println("  🌐 Listening on 0.0.0.0:" + port);
        System.out.println("═══════════════════════════════════════════════");

        // ─── Book APIs ───────────────────────────────────────

        // Add a new book
        app.post("/add-book", ctx -> {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            String title = body.has("title") ? body.get("title").getAsString() : "";
            String author = body.has("author") ? body.get("author").getAsString() : "";
            JsonObject result = bookService.addBook(title, author);
            ctx.json(result.toString());
        });

        // Get all books
        app.get("/books", ctx -> {
            ctx.json(bookService.getAllBooks().toString());
        });

        // Delete a book
        app.get("/delete-book", ctx -> {
            String idParam = ctx.queryParam("id");
            if (idParam == null || idParam.isEmpty()) {
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("message", "Book ID is required");
                ctx.json(error.toString());
                return;
            }
            try {
                int id = Integer.parseInt(idParam);
                JsonObject result = bookService.deleteBook(id);
                ctx.json(result.toString());
            } catch (NumberFormatException e) {
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("message", "Invalid Book ID");
                ctx.json(error.toString());
            }
        });

        // Search books
        app.get("/search", ctx -> {
            String query = ctx.queryParam("q");
            ctx.json(bookService.searchBooks(query).toString());
        });

        // ─── Transaction APIs ────────────────────────────────

        // Issue a book
        app.post("/issue-book", ctx -> {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            int userId = body.has("userId") ? body.get("userId").getAsInt() : 0;
            int bookId = body.has("bookId") ? body.get("bookId").getAsInt() : 0;

            if (userId <= 0 || bookId <= 0) {
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("message", "Valid User ID and Book ID are required");
                ctx.json(error.toString());
                return;
            }

            JsonObject result = transactionService.issueBook(userId, bookId);
            ctx.json(result.toString());
        });

        // Return a book
        app.post("/return-book", ctx -> {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            int userId = body.has("userId") ? body.get("userId").getAsInt() : 0;
            int bookId = body.has("bookId") ? body.get("bookId").getAsInt() : 0;

            if (userId <= 0 || bookId <= 0) {
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("message", "Valid User ID and Book ID are required");
                ctx.json(error.toString());
                return;
            }

            JsonObject result = transactionService.returnBook(userId, bookId);
            ctx.json(result.toString());
        });

        // Get all transactions
        app.get("/transactions", ctx -> {
            ctx.json(transactionService.getAllTransactions().toString());
        });

        // Get all users (helper)
        app.get("/users", ctx -> {
            ctx.json(transactionService.getAllUsers().toString());
        });

        // Add member
        app.post("/add-user", ctx -> {
            JsonObject body = JsonParser.parseString(ctx.body()).getAsJsonObject();
            String name = body.has("name") ? body.get("name").getAsString() : "";
            String email = body.has("email") ? body.get("email").getAsString() : "";
            ctx.json(transactionService.addUser(name, email).toString());
        });

        // Delete member
        app.get("/delete-user", ctx -> {
            String idParam = ctx.queryParam("id");
            JsonObject error = new JsonObject();
            if (idParam == null || idParam.isEmpty()) {
                error.addProperty("success", false);
                error.addProperty("message", "Member ID is required");
                ctx.json(error.toString());
                return;
            }
            try {
                int id = Integer.parseInt(idParam);
                ctx.json(transactionService.deleteUser(id).toString());
            } catch (NumberFormatException e) {
                error.addProperty("success", false);
                error.addProperty("message", "Invalid Member ID");
                ctx.json(error.toString());
            }
        });
    }
}
