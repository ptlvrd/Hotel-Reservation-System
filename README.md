# Hotel Reservation System

![Hotel Reservation System](https://img.shields.io/badge/JavaFX-Hotel%20Reservation%20System-blue)
![License](https://img.shields.io/badge/License-Academic-green)
![Java](https://img.shields.io/badge/Java-17-orange)

JavaFX-based Hotel Reservation System with self-service kiosk and admin panel, built using MVC architecture, MySQL, and Java Logging.

## 📌 Project Overview

This project replaces a manual hotel booking system with a modern, interactive desktop application. Guests can book rooms via a self-service kiosk, while admins can manage reservations, generate bills, and collect feedback through a secured backend interface.

## 🛠️ Technologies Used

- Java 17
- JavaFX (UI)
- MySQL (Database)
- JDBC (Database Connectivity)
- MVC Architecture
- Java Logging API
- XAMPP / Oracle SQL (Optional)

## 🚀 Key Features

### 🧾 Kiosk Interface (Self-Service)
- Multi-step guest booking flow with validations
- Room suggestions based on guest count and rules
- Booking summary and confirmation
- Feedback form post-checkout

### 🔐 Admin Panel
- Admin login authentication
- Guest search by name or phone
- Reservation management (create, modify, cancel)
- Billing generation with optional discount
- Checkout process with feedback reminder
- Activity and exception logging

## ⚙️ Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/hotel-reservation-system.git
cd hotel-reservation-system
```

### 2. Configure the Database
- Install MySQL via XAMPP or standalone server.
- Create the database:
```sql
CREATE DATABASE IF NOT EXISTS HR;
USE HR;
```
- Run the schema.sql file from `/databaseScript.sql` to create tables and insert test data.

### 3. Update Database Credentials
Open `/src/main/java/com/hotel/Database/Database.java` and edit:
```java
private static final String URL = "jdbc:mysql://localhost:3306/hr";
private static final String USER = "root";
private static final String PASSWORD = ""; // Your MySQL password
```

### 4. Run the Application
Run `Main.java` from your IDE (IntelliJ, Eclipse, NetBeans).

## 💾 Database Structure

- **guests** - stores guest info and feedback
- **rooms** - room types, availability, pricing
- **reservations** - tracks booking info
- **billing** - reservation charges with tax & discounts
- **admins** - login info for hotel staff
- **feedbacks** - ratings & comments after stay
- **admin_logs**, **exception_logs** - activity & error tracking

## 📚 License
This project is for academic and demonstration purposes. No commercial use is permitted without explicit permission.

## 🙋‍♂️ Author
Developed by [Vrunda Patel](https://github.com/ptlvrd)
- [LinkedIn](https://www.linkedin.com/in/vrundapatel027/)
