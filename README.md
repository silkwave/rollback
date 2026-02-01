# Spring Boot Banking System with Transaction Rollback

A comprehensive banking system demonstration built with Spring Boot, showcasing transaction management with rollback capabilities, event-driven architecture, and enterprise Java patterns.

## Overview

This project is a **banking system simulation** that demonstrates:
- Transaction management with rollback handling
- Event-driven architecture for failure notifications
- Retry mechanisms with configurable strategies
- Request context tracking using ThreadLocal and GUIDs
- Async processing with proper context propagation
- Clean separation of concerns (MVC pattern)

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.2.0 | Core framework |
| **Java** | 21 | Programming language |
| **Spring Web** | 3.2.0 | REST API development |
| **Spring JDBC** | 3.2.0 | Database access |
| **MyBatis** | 3.0.3 | ORM framework |
| **H2 Database** | Latest | In-memory database for development |
| **Lombok** | Latest | Boilerplate code reduction |
| **Validation** | 3.2.0 | Bean validation |

## Features

### 1. Banking Operations

- **Account Management**
  - Create checking, savings, credit, and business accounts
  - Account status management (active, frozen, closed)
  - Currency support (KRW, USD, EUR)
  - Overdraft limits

- **Transaction Processing**
  - Deposit processing with external payment gateway simulation
  - Account-to-account transfers
  - Transaction history tracking
  - Reference number generation

- **Customer Management**
  - Individual and business customer types
  - Risk level assessment (LOW, MEDIUM, HIGH)
  - Customer status tracking

### 2. Transaction Rollback & Event Handling

- **@Transactional** boundaries for ACID compliance
- **TransactionFailed** events published on failures
- **@TransactionalEventListener** triggers after rollback
- **Async notification** processing with context preservation

### 3. Retry Mechanism

- **LockRetryTemplate** for configurable retry logic
- **LinearBackoffRetryStrategy** with exponential-like backoff
  - 5 maximum attempts
  - 1000ms base delay
  - 500ms increment per attempt
- Applied to external payment processing

### 4. Request Context Tracking

- **GUID-based** request correlation across the system
- **ThreadLocal** context storage per request
- **MDC (Mapped Diagnostic Context)** for structured logging
- Client information tracking:
  - IP address
  - User-Agent
  - Session ID

### 5. Async Processing

- **@EnableAsync** with custom thread pool
- Thread pool: 2-5 threads
- **AsyncUncaughtExceptionHandler** for error handling
- Context propagation to async threads

## Project Structure

```
src/main/java/com/example/rollback/
├── RollbackApplication.java          # Main application
├── controller/
│   ├── BankingController.java        # Banking REST API
│   └── CustomerController.java       # Customer REST API
├── service/
│   ├── AccountService.java           # Account business logic
│   ├── CustomerService.java          # Customer business logic
│   ├── PaymentClient.java            # Payment gateway simulation
│   └── ...                           # Other services
├── repository/
│   ├── AccountRepository.java        # Data access layer
│   ├── CustomerRepository.java
│   └── ...
├── domain/
│   ├── Account.java                  # Account entity
│   ├── Customer.java                 # Customer entity
│   ├── Transaction.java              # Transaction entity
│   └── ...                           # DTOs and enums
├── event/
│   ├── TransactionFailed.java        # Failure event
│   ├── TransactionFailureHandler.java # Rollback handler
│   └── ...
├── retry/
│   ├── LockRetryTemplate.java        # Retry template
│   ├── LinearBackoffRetryStrategy.java
│   └── ...
├── util/
│   ├── ContextHolder.java            # ThreadLocal context
│   ├── GuidQueueUtil.java            # GUID generation
│   └── ...
├── config/
│   ├── AsyncConfig.java              # Async configuration
│   ├── RetryConfig.java              # Retry configuration
│   └── ...
└── exception/
    ├── PaymentException.java         # Payment exception
    └── OrderException.java           # Order exception
```

## API Endpoints

### Banking API (`/api/banking`)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/banking/accounts` | POST | Create new bank account |
| `/api/banking/accounts` | GET | List all accounts |
| `/api/banking/accounts/{id}` | GET | Get account by ID |
| `/api/banking/accounts/customer/{customerId}` | GET | Get customer's accounts |
| `/api/banking/deposit` | POST | Deposit funds |
| `/api/banking/transfer` | POST | Transfer between accounts |
| `/api/banking/transactions` | GET | List all transactions |

### Customer API (`/api/banking/customers`)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/banking/customers` | POST | Create new customer |
| `/api/banking/customers` | GET | List all customers |
| `/api/banking/customers/{id}` | GET | Get customer by ID |
| `/api/banking/customers/{id}` | PUT | Update customer |

### Additional Endpoints

| Endpoint | Description |
|----------|-------------|
| `/h2-console` | H2 database console (dev only) |
| `/` | Static web UI (index.html) |

## Request Examples

### Create Account
```bash
curl -X POST http://localhost:8080/api/banking/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "accountType": "CHECKING",
    "currency": "KRW",
    "initialDeposit": 100000,
    "forceFailure": false
  }'
```

### Deposit
```bash
curl -X POST http://localhost:8080/api/banking/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "customerId": 1,
    "amount": 50000,
    "currency": "KRW",
    "description": "Salary deposit",
    "forceFailure": false
  }'
```

### Transfer
```bash
curl -X POST http://localhost:8080/api/banking/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "toAccountId": 2,
    "customerId": 1,
    "amount": 10000,
    "currency": "KRW",
    "description": "Monthly rent",
    "forceFailure": false
  }'
```

## Configuration

### application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  mybatis:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.example.rollback.domain

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## Key Design Patterns

### 1. Transaction Management

```java
@Transactional
public Account createAccount(AccountRequest request) {
    // Business logic
    transactionRepository.save(transaction);
    
    if (request.isForceFailure()) {
        throw new PaymentException("Simulated failure");
    }
    
    // If exception occurs, transaction automatically rolls back
}
```

### 2. Event-Driven Rollback Handling

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
@Async
public void handleTransactionFailed(TransactionFailed event) {
    // This runs only after transaction rollback completes
    notificationService.sendFailureNotification(event);
}
```

### 3. Retry with Backoff

```java
retryTemplate.execute(() -> {
    return paymentGateway.processPayment(request);
});
```

### 4. Context Tracking

```java
// Set context at request start
ContextHolder.initializeContext(guid);
MDC.put("guid", guid);

// Access anywhere in the same thread
String guid = ContextHolder.getContext().getGuid();
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.x or higher

### Build & Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Or run the JAR directly
java -jar build/libs/*.jar
```

### Access the Application

- **Web UI**: http://localhost:8080
- **API Base**: http://localhost:8080/api/banking
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (empty)

## Testing Rollback

Enable the "force failure" checkbox in the web UI or set `"forceFailure": true` in API requests to simulate transaction failures and observe rollback behavior.

## Frontend

The project includes a responsive web interface:
- **index.html**: Main banking dashboard
- **banking-style.css**: Modern blue-themed styling
- **script.js**: Interactive JavaScript with real-time logging

Features:
- Tab-based navigation (Accounts, Customers, Transactions)
- Real-time execution logs
- Form validation
- Account freeze/activate actions

## Development Notes

### Transaction Flow
1. Controller receives request
2. Service method begins (@Transactional)
3. Database operations execute
4. If success: commit transaction
5. If failure: rollback + publish TransactionFailed event
6. Event listener sends async notification

### Request Tracking
- Each request gets a unique GUID
- GUID propagates through logs via MDC
- Context automatically cleaned up after request

## File Statistics

- **Total Java Files**: 49
- **Lines of Code**: ~3,500+ (Java)
- **Frontend**: HTML, CSS, JavaScript
- **Build Tool**: Gradle

## License

This project is for educational and demonstration purposes.

## Author

Spring Boot Banking System Demo - Transaction Rollback Showcase

---

*Generated from comprehensive source analysis*
