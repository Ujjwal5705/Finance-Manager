<h1 align="center">Personal Finance Manager</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java" alt="Java">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.14-green?style=for-the-badge&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Maven-Project-blue?style=for-the-badge&logo=apachemaven" alt="Maven">
</p>

<p align="center">
  <strong>A robust, secure, and production-ready Spring Boot backend for personal financial tracking and analytics.</strong>
</p>

---

A production-ready backend application built using **Java 17**, **Spring Boot 3**, and **Spring Security** for managing personal finances, transactions, savings goals, and financial reports.

This project was developed as a complete backend engineering assignment with a focus on:

- RESTful API design
- Authentication & session management
- Validation & exception handling
- Financial analytics
- Dockerized deployment
- Automated testing

---

# Live Deployment

## Render Deployment
https://finance-manager-9q8z.onrender.com

---

# GitHub Repository

## Repository URL
https://github.com/Ujjwal5705/Finance-Manager

---

# Features

## Authentication & Session Management
- User registration
- Session-based login/logout
- Secure password hashing using BCrypt
- Session validation for protected APIs

---

## Category Management
- Create custom categories
- Delete categories
- Default expense/income categories
- Category validation
- Prevent deletion of categories currently in use

---

## Transaction Management
- Create transactions
- Update transactions
- Delete transactions
- Filter transactions by:
  - date range
  - category
- Future date validation
- Immutable transaction date updates

---

## Savings Goals
- Create financial goals
- Update target amount
- Track savings progress
- Remaining amount calculation
- Progress percentage calculation
- Goal validation rules

---

## Financial Reports
- Monthly financial reports
- Yearly financial reports
- Expense analytics
- Income analytics
- Net savings calculation

---

## Validation & Error Handling
- Request validation using Jakarta Validation
- Global exception handling
- Proper HTTP status codes
- Invalid enum/date handling
- Input sanitization

---

## Testing
- JUnit 5
- Mockito
- JaCoCo coverage reporting
- Controller unit tests
- Session utility tests
- Full integration test suite

---

# Tech Stack

| Technology | Usage |
|---|---|
| Java 17 | Core language |
| Spring Boot 3 | Backend framework |
| Spring Security | Authentication |
| Spring Data JPA | ORM |
| Hibernate | Persistence |
| H2 Database | In-memory database |
| Maven | Dependency management |
| Docker | Containerization |
| Render | Cloud deployment |
| JUnit 5 | Unit testing |
| Mockito | Mocking framework |
| JaCoCo | Coverage reporting |

---

# Project Structure

```text
src/main/java/com/finance/finance_manager

├── config
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## API Endpoints

### Authentication

| Method | Endpoint            | Description       |
|--------|---------------------|-------------------|
| POST   | `/api/auth/register` | Register new user |
| POST   | `/api/auth/login`    | Login user        |
| POST   | `/api/auth/logout`   | Logout user       |

### Categories

| Method | Endpoint                   | Description          |
|--------|----------------------------|----------------------|
| GET    | `/api/categories`          | List user categories |
| POST   | `/api/categories`          | Create a category    |
| DELETE | `/api/categories/{name}`   | Delete a category    |

### Transactions

| Method | Endpoint                  | Description            |
|--------|---------------------------|------------------------|
| POST   | `/api/transactions`       | Create a transaction   |
| GET    | `/api/transactions`       | List transactions      |
| PUT    | `/api/transactions/{id}`  | Update a transaction   |
| DELETE | `/api/transactions/{id}`  | Delete a transaction   |

**Filters**

- Date range: `GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31`
- By category: `GET /api/transactions?category=Food`

### Goals

| Method | Endpoint              | Description       |
|--------|-----------------------|-------------------|
| POST   | `/api/goals`          | Create a goal     |
| GET    | `/api/goals`          | List all goals    |
| GET    | `/api/goals/{id}`     | Get a goal by ID  |
| PUT    | `/api/goals/{id}`     | Update a goal     |
| DELETE | `/api/goals/{id}`     | Delete a goal     |

### Reports

| Method | Endpoint                          | Description              |
|--------|-----------------------------------|--------------------------|
| GET    | `/api/reports/monthly/{year}/{month}` | Monthly financial report |
| GET    | `/api/reports/yearly/{year}`         | Yearly financial report  |

---

## Authentication

The API uses **session-based authentication**.  
After a successful login, a session cookie is created and required for accessing protected endpoints.

---

## Running Locally

1. **Clone the repository**
   ```bash
   git clone https://github.com/Ujjwal5705/Finance-Manager.git
   ```

2. **Navigate into the project**
   ```bash
   cd Finance-Manager
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   
The application will start on http://localhost:8080.

---

## Running Tests
   ```bash
   # Execute unit tests
   mvn test

   # Generate JaCoCo coverage report
   mvn clean test
   ```

#### Open the coverage report:
   ```text
   target/site/jacoco/index.html
   ```

---

## Docker Support
   ```bash
   # Build Docker image
   docker build -t finance-manager.
   
   # Run container
   docker run -p 8080:8080 finance-manager
   ```

---

## Deployment
The application is deployed on Render using Docker.
Deployment includes:
- Automatic GitHub integration
- Containerized Spring Boot deployment
- Cloud-hosted backend APIs

---

## Test Results
Integration Test Suite
- Total Tests Executed: 86
- Tests Passed: 86
- Tests Failed: 0

#### Success Rate: 100%
