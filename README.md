\documentclass[12pt]{article}
\usepackage[a4paper,margin=1in]{geometry}
\usepackage{hyperref}
\usepackage{enumitem}
\usepackage{graphicx}
\usepackage{titlesec}
\titleformat{\section}{\large\bfseries}{\thesection}{1em}{}

\title{\textbf{Hotel Reservation System - README}}
\author{}
\date{}

\begin{document}

\maketitle

\section*{GitHub Repository Description}
JavaFX-based Hotel Reservation System with self-service kiosk and admin panel, built using MVC architecture, MySQL, and Java Logging.

\section*{📌 Project Overview}
This project replaces a manual hotel booking system with a modern, interactive desktop application. Guests can book rooms via a self-service kiosk, while admins can manage reservations, generate bills, and collect feedback through a secured backend interface.

\section*{🛠️ Technologies Used}
\begin{itemize}
    \item Java 17
    \item JavaFX (UI)
    \item MySQL (Database)
    \item JDBC (Database Connectivity)
    \item MVC Architecture
    \item Java Logging API
    \item XAMPP / Oracle SQL (Optional)
\end{itemize}

\section*{🚀 Key Features}

\subsection*{🧾 Kiosk Interface (Self-Service)}
\begin{itemize}
    \item Multi-step guest booking flow with validations
    \item Room suggestions based on guest count and rules
    \item Booking summary and confirmation
    \item Feedback form post-checkout
\end{itemize}

\subsection*{🔐 Admin Panel}
\begin{itemize}
    \item Admin login authentication
    \item Guest search by name or phone
    \item Reservation management (create, modify, cancel)
    \item Billing generation with optional discount
    \item Checkout process with feedback reminder
    \item Activity and exception logging
\end{itemize}

\section*{⚙️ Setup Instructions}
\subsection*{1. Clone the Repository}
\begin{verbatim}
git clone https://github.com/yourusername/hotel-reservation-system.git
cd hotel-reservation-system
\end{verbatim}

\subsection*{2. Configure the Database}
\begin{itemize}
    \item Install MySQL via XAMPP or standalone server.
    \item Create the database:
\begin{verbatim}
CREATE DATABASE IF NOT EXISTS HR;
USE HR;
\end{verbatim}
    \item Run the \texttt{schema.sql} file from \texttt{/databaseScript.sql} to create tables and insert test data.
\end{itemize}

\subsection*{3. Update Database Credentials}
Edit the file \texttt{\src\main\java\com\example\hotelreservation\database}:
\begin{verbatim}
private static final String URL = "jdbc:mysql://localhost:3306/hr";
private static final String USER = "root";
private static final String PASSWORD = ""; // Your MySQL password
\end{verbatim}

\subsection*{4. Run the Application}
Run \texttt{Main.java} from your IDE (IntelliJ, Eclipse, NetBeans).

\section*{💾 Database Structure}
\begin{itemize}
    \item \textbf{guests} – stores guest info and feedback
    \item \textbf{rooms} – room types, availability, pricing
    \item \textbf{reservations} – tracks booking info
    \item \textbf{billing} – reservation charges with tax \& discounts
    \item \textbf{admins} – login info for hotel staff
    \item \textbf{feedbacks} – ratings \& comments after stay
    \item \textbf{admin\_logs, exception\_logs} – activity \& error tracking
\end{itemize}

\section*{📸 Demo (Optional)}


\section*{📚 License}
This project is for academic and demonstration purposes. No commercial use is permitted without explicit permission.

\section*{🙋‍♂️ Author}
Developed by \textbf{[Vrunda Patel]} \\
\href{#}{LinkedIn} | \href{#https://github.com/ptlvrd}{GitHub} | \href{#}{Email}

\end{document}
