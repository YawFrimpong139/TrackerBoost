# 🛠️ ProjectTracker

A robust Java Spring Boot web application for managing software development projects, tasks, and developers.

## 🚀 Features

- CRUD operations for Projects, Tasks, and Developers
- Assign multiple developers to tasks
- Track task status and due dates
- Paginated & sortable APIs for better performance
- Audit logging and user activity tracking
- MongoDB & Redis integration (mocked in tests)

---

## 🧰 Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **Hibernate Validator**
- **MongoDB (for audit logs)**
- **Redis (for caching)**
- **MySQL / PostgreSQL (for main DB)**
- **Lombok**
- **JUnit & Mockito (for testing)**
- **Maven**

---

## 🖥️ Local Setup

### ✅ Prerequisites

- Java 17+
- Maven 3.6+
- Docker (for Redis and MongoDB if needed)
- MySQL/PostgreSQL

### 🔧 Clone & Build

```bash
git clone https://github.com/yourusername/project-tracker.git
cd project-tracker
mvn clean install
```

### ⚙️ Setup .env or application.properties\
```
spring.datasource.url=jdbc:mysql://localhost:3306/projecttracker
spring.datasource.username=root
spring.datasource.password=your_password

spring.data.mongodb.uri=mongodb://localhost:27017/projecttracker
spring.redis.host=localhost
spring.redis.port=6379
```

### 🐳 Start Redis & MongoDB (Optional)
```
docker run -d -p 6379:6379 redis
docker run -d -p 27017:27017 mongo
```

### Screenshots
![img_1.png](img_1.png)
![img.png](img.png)



### 📝 License
MIT License – free to use and modify.
