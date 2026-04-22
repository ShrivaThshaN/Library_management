# 📚 BookShelf — A Modern Library Management System
 
BookShelf is a clean, full-stack library management web app built with **Java + Javalin** on the backend and a lightweight **HTML / CSS / JavaScript** single-page frontend. It keeps track of your books, members, and lending activity with a calm green UI and an honest, no-nonsense workflow.
 
> Built and maintained by **Shrivathshan**.
 Video Demo : https://drive.google.com/file/d/1z9WA8biIeUI83J3WfJA0CedHDDEnKTQR/view?usp=sharing
---
 
## ✨ Why BookShelf?
 
Most academic library projects feel either too bare or unnecessarily heavy. BookShelf aims for the middle:
 
- A real REST backend — not a glorified script
- A polished UI that actually looks like something you'd ship
- Sensible defaults, sample data, and a one-command run
- Easy to read, easy to extend, easy to demo
 
---
 
## 🧩 Features
 
### 📖 Book Catalogue
- Add new titles with author info
- Browse the entire shelf in a clean table view
- Live search by title or author
- Delete books that are not currently checked out
 
### 👥 Member Directory
- Register new members with name and email
- Browse all registered members
- Remove inactive members (blocked if they still hold an issued book)
 
### 🔄 Issue & Return Workflow
- Issue a book to a member from a curated dropdown of available books
- Return a book in one click directly from the transaction list
- Automatic transaction logging with issue and return dates
 
### 💰 Smart Fine Calculation
- Grace period of **7 days**
- After that, **₹10 per overdue day** is automatically added to the transaction
- Fine appears next to the transaction in the history view
 
### 📊 Dashboard at a Glance
- Total books, total members, books currently issued, and books available
- Recent transaction feed with status badges (ISSUED / RETURNED)
 
---
 
## 🛠️ Tech Stack
 
| Layer       | Tool / Library              |
|-------------|-----------------------------|
| Language    | Java 17                     |
| Web server  | Javalin 6                   |
| JSON        | Gson                        |
| Logging     | SLF4J (simple)              |
| Database    | PostgreSQL (JDBC)           |
| Build       | Apache Maven (shade plugin) |
| Frontend    | Vanilla HTML, CSS, JS       |
 
No framework bloat. No bundler. Just code that runs.
 
---
 
## 📁 Project Layout
 
```
BookShelf/
├── pom.xml
└── src/main/
    ├── java/com/library/
    │   ├── ui/LibraryApp.java          # Javalin routes & app entrypoint
    │   ├── services/
    │   │   ├── BookService.java        # Book CRUD logic
    │   │   └── TransactionService.java # Members, issue, return, fines
    │   └── util/DBUtil.java            # JDBC connection + schema bootstrap
    └── resources/static/
        ├── index.html                  # Single-page UI shell
        └── styles.css                  # BookShelf green theme
```
 
---
 
## 🗄️ Database
 
BookShelf auto-creates its own schema on startup — there is **no separate `schema.sql` step** to remember. On first launch the app will:
 
1. Create three tables: `books`, `users`, `transactions`
2. Seed a few sample books and members so the UI isn't empty
3. Connect using standard PostgreSQL environment variables:
   - `DATABASE_URL` (preferred), or
   - `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`
 
Defaults fall back to `localhost:5432 / postgres / postgres / (empty)` if nothing is set.
 
---
 
## 🔌 REST API
 
| Method | Endpoint               | Purpose                     |
|--------|------------------------|-----------------------------|
| GET    | `/books`               | List all books              |
| POST   | `/add-book`            | Add a new book              |
| GET    | `/delete-book?id=`     | Delete a book               |
| GET    | `/search?q=`           | Search books                |
| GET    | `/users`               | List all members            |
| POST   | `/add-user`            | Register a new member       |
| GET    | `/delete-user?id=`     | Remove a member             |
| POST   | `/issue-book`          | Issue a book to a member    |
| POST   | `/return-book`         | Return an issued book       |
| GET    | `/transactions`        | Full transaction history    |
 
All POST endpoints accept JSON bodies. All responses are JSON.
 
---
 
## ⚙️ Getting Started
 
### 1. Set up PostgreSQL
 
Make sure a PostgreSQL instance is reachable. Either set `DATABASE_URL`:
 
```bash
export DATABASE_URL="postgresql://user:password@localhost:5432/bookshelf"
```
 
…or set the individual `PG*` variables. The schema is created for you.
 
### 2. Build
 
```bash
mvn clean package
```
 
This produces a fat (shaded) jar at `target/library-management-system-1.0-SNAPSHOT.jar`.
 
### 3. Run
 
```bash
java -jar target/library-management-system-1.0-SNAPSHOT.jar
```
 
The server starts on **port 5000** by default. Override with `PORT=8080 java -jar …` if you'd like.
 
### 4. Open
 
Visit **http://localhost:5000** and you're in.
 
---
 
## 🎨 UI Notes
 
- Calm green palette (`#1b5e20` primary, `#4caf50` accent)
- Card-based panels with soft shadows
- Status pills for ISSUED / RETURNED / Available / Issued
- Toast notifications for every action
- Responsive layout that collapses cleanly on small screens
 
---
 
## 🧠 Concepts Demonstrated
 
- REST API design with Javalin
- Plain JDBC with prepared statements (no ORM magic)
- Transactional updates for issue / return / delete flows
- Date math for due dates and overdue fines
- Separation of concerns: routing → services → persistence
- A frontend written without a build step, proving you don't always need one
 
---
 
## 🧪 Quick Manual Test
 
1. Add a book → it appears in the Books table immediately
2. Register a member → it appears in Members
3. Issue that book to that member → status flips to ISSUED, dashboard updates
4. Return the book → fine is calculated, status flips to RETURNED
5. Search a partial title → results filter live
 
---
 
## 🚀 Roadmap Ideas
 
- Per-book copy counts and genres
- Member phone numbers and join dates
- Authentication for librarian vs member roles
- Email reminders for overdue books
- Export transactions to CSV
 
Pull requests and suggestions are welcome.
 
---
 
## 👤 Author
 
**Shrivathshan**
Designed, built, and maintained with care.
 
---
 
## 📄 License
 
Released under the MIT License — free to use, modify, and learn from.
