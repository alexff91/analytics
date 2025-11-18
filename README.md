# Analytics Platform

> A modern, powerful SPSS-like statistical analysis platform built with Spring Boot 3 and React

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green)
![React](https://img.shields.io/badge/React-18-blue)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## Features

### Statistical Analysis
- **Descriptive Statistics**: Mean, median, standard deviation, variance, skewness, kurtosis, quartiles, and more
- **Frequency Distributions**: Detailed frequency tables with percentages
- **Correlation Analysis**: Pearson correlation matrices
- **Regression Analysis**:
  - Simple linear regression
  - Multiple linear regression (OLS)
- **Hypothesis Testing**:
  - One-sample t-test
  - Two-sample t-test
  - One-way ANOVA
  - Chi-square test of independence
- **Covariance Analysis**: Covariance matrices

### Data Management
- **File Upload**: Support for Excel (.xlsx, .xls) and CSV files
- **Drag-and-drop interface** (when using React frontend)
- **Data parsing and validation**
- **Column extraction and preview**
- **File metadata tracking**

### Modern Architecture
- **RESTful API**: Clean, well-documented API endpoints
- **JWT Authentication**: Secure token-based authentication
- **Role-based Access Control**: Admin, Manager, and User roles
- **OpenAPI/Swagger Documentation**: Interactive API documentation at `/swagger-ui.html`
- **H2 Database**: Embedded database for easy setup (PostgreSQL ready for production)
- **Docker Support**: Containerized deployment with docker-compose

### User Features
- **User Registration and Login**
- **Secure password encryption** (BCrypt)
- **Token refresh mechanism**
- **File ownership and management**
- **Audit trails** (created/modified timestamps)

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Security** with JWT
- **Spring Data JPA**
- **H2 Database** (development) / **PostgreSQL** (production)
- **Apache POI** for Excel processing
- **Apache Commons Math3** for statistical analysis
- **WEKA** for machine learning capabilities
- **Lombok** for reducing boilerplate
- **MapStruct** for object mapping

### Frontend (Optional)
- **React 18**
- **Material-UI (MUI)**
- **Axios** for API calls
- **Chart.js** for visualizations

### DevOps
- **Docker** and **Docker Compose**
- **Maven** for build management

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker (optional, for containerized deployment)

### Option 1: Run Locally (Development Mode)

1. **Clone the repository**
```bash
git clone <repository-url>
cd analytics
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:file:./data/analytics`
     - Username: `sa`
     - Password: (leave blank)

### Option 2: Run with Docker

1. **Build and start containers**
```bash
docker-compose up -d
```

2. **Check logs**
```bash
docker-compose logs -f analytics-platform
```

3. **Stop containers**
```bash
docker-compose down
```

### Option 3: Run with Docker (production-ready PostgreSQL)

The `docker-compose.yml` includes PostgreSQL for production use:

```bash
docker-compose up -d
```

This will start:
- Analytics Platform on http://localhost:8080
- PostgreSQL on port 5432

## Default Credentials

The application creates a default admin user on first startup:

- **Username**: `admin`
- **Password**: `admin123`

⚠️ **Change these credentials immediately in production!**

## API Documentation

### Authentication Endpoints

#### Register
```bash
POST /api/auth/signup
Content-Type: application/json

{
  "username": "johndoe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "admin",
  "password": "admin123"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "email": "admin@analytics.com",
  "fullName": "Admin User",
  "roles": ["ROLE_ADMIN"]
}
```

### File Upload Endpoints

#### Upload File
```bash
POST /api/files/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <your-file.xlsx>
description: "Sales data Q1 2024"
```

#### Get File Data
```bash
GET /api/files/{fileId}/data
Authorization: Bearer <token>
```

#### Get File Columns
```bash
GET /api/files/{fileId}/columns
Authorization: Bearer <token>
```

### Analytics Endpoints

All analytics endpoints require authentication and use this request format:

```json
{
  "fileId": 1,
  "variables": ["column1", "column2"],
  "parameters": {
    // Analysis-specific parameters
  }
}
```

#### Descriptive Statistics
```bash
POST /api/analytics/descriptive-statistics
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileId": 1,
  "variables": ["age", "salary", "experience"]
}
```

#### Frequency Distribution
```bash
POST /api/analytics/frequency-distribution
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileId": 1,
  "variables": ["department", "gender"]
}
```

#### Correlation Analysis
```bash
POST /api/analytics/correlation
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileId": 1,
  "variables": ["age", "salary", "experience"]
}
```

#### Simple Regression
```bash
POST /api/analytics/simple-regression
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileId": 1,
  "parameters": {
    "dependentVariable": "salary",
    "independentVariable": "experience"
  }
}
```

#### Multiple Regression
```bash
POST /api/analytics/multiple-regression
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileId": 1,
  "parameters": {
    "dependentVariable": "salary",
    "independentVariables": ["age", "experience", "education"]
  }
}
```

#### T-Test
```bash
POST /api/analytics/t-test
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileId": 1,
  "variables": ["salary"],
  "parameters": {
    "mu": 50000
  }
}
```

#### ANOVA
```bash
POST /api/analytics/anova
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileId": 1,
  "variables": ["group1_salary", "group2_salary", "group3_salary"]
}
```

## Configuration

### Application Properties

Edit `src/main/resources/application.yml`:

```yaml
app:
  jwt:
    secret: <base64-encoded-secret-key>  # Change this!
    expiration: 86400000  # 24 hours
    refresh-expiration: 604800000  # 7 days

  file-storage:
    upload-dir: ./uploads
    temp-dir: ./temp

  cors:
    allowed-origins: http://localhost:3000,http://localhost:8080
```

### Database Configuration

#### H2 (Development)
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/analytics
    driver-class-name: org.h2.Driver
    username: sa
    password:
```

#### PostgreSQL (Production)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/analytics
    driver-class-name: org.postgresql.Driver
    username: analytics
    password: your-password
```

## Project Structure

```
analytics/
├── src/
│   ├── main/
│   │   ├── java/com/analytics/
│   │   │   ├── AnalyticsPlatformApplication.java
│   │   │   ├── api/
│   │   │   │   ├── controller/       # REST Controllers
│   │   │   │   └── dto/              # Data Transfer Objects
│   │   │   ├── config/               # Configuration classes
│   │   │   ├── domain/
│   │   │   │   ├── entity/           # JPA Entities
│   │   │   │   └── repository/       # Spring Data Repositories
│   │   │   ├── security/             # Security & JWT
│   │   │   └── service/              # Business Logic
│   │   └── resources/
│   │       ├── application.yml       # Configuration
│   │       └── application-prod.yml  # Production config
│   └── test/                          # Tests
├── uploads/                           # Uploaded files
├── data/                              # H2 database
├── logs/                              # Application logs
├── Dockerfile                         # Docker configuration
├── docker-compose.yml                 # Docker Compose
├── pom.xml                            # Maven dependencies
└── README.md                          # This file
```

## Development

### Run in Development Mode
```bash
mvn spring-boot:run
```

### Build JAR
```bash
mvn clean package
```

### Run JAR
```bash
java -jar target/analytics-platform-2.0.0.jar
```

### Run Tests
```bash
mvn test
```

## Production Deployment

### Using Docker

1. **Build the image**
```bash
docker build -t analytics-platform:latest .
```

2. **Run with docker-compose**
```bash
docker-compose -f docker-compose.yml up -d
```

### Environment Variables

For production, override these environment variables:

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/analytics
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-password
APP_JWT_SECRET=your-base64-secret-key
```

## Security Considerations

1. **Change default admin password** immediately
2. **Use strong JWT secret** (minimum 256 bits)
3. **Enable HTTPS** in production
4. **Configure CORS** appropriately
5. **Use environment variables** for sensitive data
6. **Regular security updates** for dependencies
7. **Implement rate limiting** for API endpoints

## API Testing with cURL

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}' \
  | jq -r '.accessToken')

# Upload file
FILE_ID=$(curl -s -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@data.xlsx" \
  -F "description=Test data" \
  | jq -r '.fileId')

# Run descriptive statistics
curl -X POST http://localhost:8080/api/analytics/descriptive-statistics \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"fileId\":$FILE_ID,\"variables\":[\"age\",\"salary\"]}"
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database Issues
```bash
# Reset H2 database
rm -rf data/

# Check database with H2 Console
# Visit: http://localhost:8080/h2-console
```

### Build Issues
```bash
# Clean Maven cache
mvn clean

# Rebuild
mvn clean install -U
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create an issue on GitHub
- Contact the development team

## Roadmap

- [ ] React frontend with Material-UI
- [ ] Real-time data visualization with Chart.js/Plotly
- [ ] Export to SPSS format
- [ ] R/Python integration for advanced analytics
- [ ] Scheduled reports
- [ ] Email notifications
- [ ] Multi-language support
- [ ] Cloud storage integration (S3, Azure Blob)
- [ ] Real-time collaboration features

## Acknowledgments

- Apache Commons Math for statistical functions
- WEKA for machine learning capabilities
- Spring Boot team for the excellent framework
- All contributors and users of this platform
