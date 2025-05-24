# Train Ticket Booking System

A concurrent train ticket booking system that handles seat reservations with thread safety and proper error handling.

## Quick Start
**A Postman collection is included in the project:**
```diff
! Train_App.postman_collection.json
```

Import this collection into Postman to quickly test all API endpoints with pre-configured requests.

## System Architecture

### Initialization (AppConfig.java)
The system initializes with pre-configured users and seats:

1. **User Generation**:
   - Creates users based on `app.user-count` from application.yml
   - Each user gets:
     - Unique ID (auto-incremented)
     - First name and last name
     - Email (user_1@test.com, user_2@test.com, etc.)
     - Initial wallet balance (configured in application.yml)
     - <img width="1211" alt="image" src="https://github.com/user-attachments/assets/146e20ed-fa9d-4583-83db-e81b1cbf83bb" />


2. **Seat Generation**:
   - Creates seats based on `app.seat-count` from application.yml
   - Two sections: A and B
   - Each section gets seats numbered A1, A2... and B1, B2...
   - All seats are initially available
   - Uses ConcurrentHashMap for thread-safe operations
   - <img width="1211" alt="image" src="https://github.com/user-attachments/assets/0c1db9e2-a5b6-4c1b-9bfc-59d438827173" />


### Core Component: SeatManager
The heart of the application is the `SeatManager` class which handles all seat booking operations with thread safety:

1. **Thread Safety Mechanisms**:
   - Uses ReentrantLock for each seat
   - Implements timeout for lock acquisition (2 seconds)
   - Overall operation timeout (5 seconds)
   - Prevents double-booking through atomic operations

2. **Booking Process**:
   ```
   1. Validate seat exists
   2. Try to acquire seat lock (2s timeout)
   3. Check seat availability
   4. Process payment
   5. Reserve seat
   6. Handle errors and refunds if needed
   ```

## API Endpoints

### 1. Purchase Ticket
```http
POST /api/train/purchase
Content-Type: application/json

{
    "userId": "1",
    "seatNumber": "A1"
}
```
- Validates user existence
- Checks if user already has a ticket
- Uses SeatManager for thread-safe booking
- Returns booking confirmation or error

### 2. Modify Seat
```http
PUT /api/train/modify-seat
Content-Type: application/json

{
    "userId": "1",
    "seatNumber": "B1"
}
```
- Releases old seat
- Books new seat
- Prevents booking same seat again
- Thread-safe seat modification

### 3. View Receipt
```http
GET /api/train/receipt/{userId}
```
- Shows ticket details
- Includes user and seat information
- Validates ticket ownership

### 4. Delete User
```http
DELETE /api/train/user/{userId}
```
- Removes user from system
- Releases associated seat
- Cleans up reservations

## Configuration
Configure system parameters in `application.yml`:
```yaml
app:
  user-count: 10        # Number of pre-created users
  seat-count: 20        # Seats per section (A and B)
  ticket-price: 20      # Price per ticket
  wallet-balance: 40    # Initial user balance
```

## Thread Safety Features

1. **Seat Booking**:
   - Each seat has its own ReentrantLock
   - Lock timeout prevents deadlocks
   - Atomic operations for seat reservation

2. **User Operations**:
   - Thread-safe user map (ConcurrentHashMap)
   - Atomic user creation and deletion
   - Safe seat cleanup on user deletion

3. **Error Handling**:
   - Automatic refunds on failed bookings
   - Proper lock release in finally blocks
   - Comprehensive error logging

## Example Usage

1. **Book a Ticket**:
```bash
curl -X POST "http://localhost:8080/api/train/purchase" \
     -H "Content-Type: application/json" \
     -d '{"userId":"1","seatNumber":"A1"}'
```

2. **Modify Seat**:
```bash
curl -X PUT "http://localhost:8080/api/train/modify-seat" \
     -H "Content-Type: application/json" \
     -d '{"userId":"1","seatNumber":"B1"}'
```

3. **View Receipt**:
```bash
curl -X GET "http://localhost:8080/api/train/receipt/1"
```

4. **Delete User**:
```bash
curl -X DELETE "http://localhost:8080/api/train/user/1"
```
