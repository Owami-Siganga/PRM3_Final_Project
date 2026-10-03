# Community Store

A Spring Boot + Java + REST API + JavaScript marketplace based on the Group 6 Project Management requirements.

## Technology stack

- Java 17
- Spring Boot 3.3.2
- Spring Web REST API
- Spring Data JPA
- H2 database
- BCrypt password hashing
- HTML
- CSS
- Vanilla JavaScript

## Implemented MVP features

- Student/university email registration
- Login with password hashing
- Temporary account lock after repeated failed logins
- Product listings
- Product search
- Category, location and price filtering
- Cart
- Demo checkout REST endpoint
- Notifications
- Community bulletin board
- Seller review backend endpoints
- Responsive web interface

## Important note about payments

The assignment mentions PayFast or SnapScan. This starter project uses a demo checkout endpoint so that the website can run without exposing payment credentials.

For final production submission, connect the checkout controller to a real PayFast or SnapScan sandbox account and never hard-code API secrets in JavaScript.

## Run the project

1. Install Java 17 or newer.
2. Install Maven.
3. Open the project folder in IntelliJ IDEA or NetBeans.
4. Run:

```bash
mvn spring-boot:run
```

5. Open:

```text
http://localhost:8080
```

## H2 database console

```text
http://localhost:8080/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:file:./data/community-store
User: sa
Password: leave blank
```

## Main REST API routes

```text
POST   /api/auth/register
POST   /api/auth/login

GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}?sellerId={sellerId}

GET    /api/reviews/seller/{sellerId}
POST   /api/reviews

GET    /api/bulletin
POST   /api/bulletin

GET    /api/notifications/user/{userId}
PUT    /api/notifications/{id}/read

POST   /api/checkout
```

## Next recommended upgrades

- Replace H2 with MySQL
- Add JWT or Spring Security session authentication
- Add role-based admin dashboard
- Add image upload instead of image URL
- Add real PayFast/SnapScan sandbox integration
- Add private buyer/seller messaging
- Add reporting for fraudulent listings
- Add automated tests
- Add seller average rating on product cards
