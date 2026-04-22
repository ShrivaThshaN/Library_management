# 📚 LibraryVault – Library Management System

LibraryVault is a full-stack web-based Library Management System built using **Java (Javalin)**, **MySQL**, and a modern **HTML/CSS/JavaScript frontend**.
It provides a real-world interface to manage books, users, and transactions with features like issuing, returning, and fine calculation.

---

## 🎥 Demo

Watch the working demo: https://drive.google.com/file/d/1mic6AD190Rmas4O49IwgHh61WsJitq33/view?usp=sharing

---

## 🚀 Features

### 📖 Book Management

* Add new books
* View all books in a structured table
* Delete books
* Search books by title or author

### 🔄 Issue & Return System

* Issue books to users
* Return issued books
* Track availability status

### 💰 Fine Calculation

* No fine for first 7 days
* ₹10 per day after due date

### 📊 Transactions Tracking

* View complete transaction history
* Includes:

  * User ID
  * Book ID
  * Issue Date
  * Return Date
  * Fine
  * Status (ISSUED / RETURNED)

---

## 🛠️ Tech Stack

* **Backend:** Java 21 + Javalin
* **Frontend:** HTML5, CSS3, JavaScript (SPA)
* **Database:** MySQL (phpMyAdmin)
* **Build Tool:** Maven
* **JSON Handling:** Gson
* **Logging:** SLF4J

---

## 📁 Project Structure

```id="projstruct"
Library/
├── pom.xml
├── schema.sql
│
└── src/main/
    ├── java/com/library/
    │   ├── ui/
    │   │   └── LibraryApp.java
    │   ├── services/
    │   │   ├── BookService.java
    │   │   └── TransactionService.java
    │   └── util/
    │       └── DBUtil.java
    │
    └── resources/static/
        ├── index.html
        └── styles.css
```

---

## 🗄️ Database Setup

Run the SQL script:

```sql id="dbsetup"
source schema.sql;
```

This will create:

* `library_db`
* Tables:

  * books
  * users
  * transactions

With sample data included.

---

## 🔌 API Endpoints

| Method | Endpoint             | Description       |
| ------ | -------------------- | ----------------- |
| POST   | `/add-book`          | Add new book      |
| GET    | `/books`             | Get all books     |
| GET    | `/delete-book?id=ID` | Delete book       |
| GET    | `/search?q=keyword`  | Search books      |
| POST   | `/issue-book`        | Issue book        |
| POST   | `/return-book`       | Return book       |
| GET    | `/transactions`      | View transactions |
| GET    | `/users`             | Get users         |

---

## ⚙️ How to Run

### 1️⃣ Setup Database

Run `schema.sql` in phpMyAdmin or MySQL CLI

---

### 2️⃣ Build Project

```bash id="buildcmd"
mvn clean package
```

---

### 3️⃣ Run Application

```bash id="runcmd"
java -jar target/library-management-system-1.0-SNAPSHOT.jar
```

---

### 4️⃣ Open in Browser

```id="url"
http://localhost:7000
```

---

## 🎨 Frontend Highlights

* Single Page Application (SPA)
* Navigation: Home | Books | Transactions
* Glassmorphism UI design
* Responsive layout
* Styled tables and forms
* Toast notifications for feedback

---

## 🧠 Core Concepts Used

* Java OOP
* REST API design
* JDBC (Database connectivity)
* Full-stack integration
* CRUD operations
* Transaction management
* UI/UX design principles

---

## 🧪 Testing

Manual testing includes:

* Add book → verify in UI
* Issue book → status changes
* Return book → fine calculation
* Search → results filter correctly
* Transactions → all logs visible

---

## ⚠️ Configuration Note

Default DB settings:

```id="dbconfig"
Host: localhost
Port: 3306
User: root
Password: (empty)
```

Update in `DBUtil.java` if needed.

---

## 👨‍💻 Author

Charan Reddy
---

## ⭐ Final Note

This project demonstrates a complete full-stack implementation of a Library Management System with backend logic, database integration, and a modern frontend UI, making it highly suitable for academic submissions and technical interviews.
