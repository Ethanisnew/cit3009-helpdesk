# University Help Desk & Ticketing System

A distributed, multi-tiered enterprise application built for the University of Technology, Jamaica. It enables students/staff to report technical issues, technicians to resolve them, and administrators to manage the entire support workflow.

## Tech Stack
*   **Frontend:** JavaFX 21 (MVC Architecture)
*   **Backend:** Spring Boot 4.1.2 (REST API)
*   **Database:** MySQL 8.0 with Hibernate/JPA
*   **Networking:** TCP/IP Socket Server (Port 9090)
*   **Logging:** Log4j2 (Rolling files)
*   **Build Tool:** Maven

## Prerequisites
Before running the application, ensure you have:
1.  **Java 21** installed (`java -version`)
2.  **MySQL Server** installed and running on `localhost:3306`
3.  Maven installed (or use the included `mvnw` wrapper)

## Configuration
1.  Open `helpdesk-backend/src/main/resources/application.properties`.
2.  Update the database credentials to match your local MySQL setup:
    ```properties
    spring.datasource.username=root
    spring.datasource.password=YOUR_MYSQL_PASSWORD
