# BloodNet - Smart Emergency Blood Donor Network

A comprehensive web application that connects blood donors, patients, and hospitals in emergency situations. Built with Java Servlets, JSP, JDBC, and MySQL.

## 🩸 Features

- **Donor Registration & Management**: Secure donor registration with blood type and location tracking
- **Blood Request System**: Patients and hospitals can request blood with urgency levels
- **Smart Donor Matching**: AI-powered matching based on blood type compatibility, location, and eligibility
- **Real-time Communication**: Chat system between donors and hospitals
- **Admin Dashboard**: Comprehensive monitoring of all users, requests, and inventory
- **Security**: Password hashing, session management, and input validation
- **Responsive Design**: Mobile-friendly interface with modern UI/UX

## 🏗️ Architecture

### Backend Technologies
- **Java Servlets**: Request handling and business logic
- **JSP**: Dynamic web page rendering
- **JDBC**: Database connectivity with connection pooling
- **MySQL**: Relational database for data persistence
- **Maven**: Dependency management and build automation

### Frontend Technologies
- **HTML5/CSS3**: Modern web standards
- **JavaScript**: Interactive user interface
- **Tailwind CSS**: Utility-first CSS framework
- **Responsive Design**: Mobile-first approach

## 📋 Prerequisites

- **Java 11+**: JDK 11 or higher
- **Maven 3.6+**: Build tool
- **MySQL 8.0+**: Database server
- **Apache Tomcat 9.0+**: Application server
- **Git**: Version control

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/bloodnet.git
cd bloodnet
```

### 2. Database Setup

#### Create MySQL Database
```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database and user
CREATE DATABASE bloodnet_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bloodnet_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON bloodnet_db.* TO 'bloodnet_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Import Database Schema
```bash
mysql -u bloodnet_user -p bloodnet_db < database_schema.sql
```

### 3. Configuration

#### Update Database Connection
Edit `src/main/java/com/bloodnet/util/DBConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/bloodnet_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String DB_USERNAME = "bloodnet_user";
private static final String DB_PASSWORD = "your_secure_password";
```

#### Update Context Parameters
Edit `src/main/webapp/WEB-INF/web.xml`:
```xml
<context-param>
    <param-name>db.password</param-name>
    <param-value>your_secure_password</param-value>
</context-param>
```

### 4. Build the Application
```bash
mvn clean compile
mvn package
```

### 5. Deploy to Tomcat

#### Option A: Copy WAR file
```bash
cp target/bloodnet.war $TOMCAT_HOME/webapps/
```

#### Option B: Use Maven Tomcat Plugin
```bash
mvn tomcat7:deploy
```

### 6. Start the Application
```bash
# Start Tomcat
$TOMCAT_HOME/bin/startup.sh  # Linux/Mac
# or
$TOMCAT_HOME/bin/startup.bat  # Windows

# Access the application
http://localhost:8080/bloodnet
```

## 📁 Project Structure

```
bloodnet/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── bloodnet/
│       │           ├── servlets/          # Servlet classes
│       │           ├── model/             # Data models
│       │           ├── dao/               # Data Access Objects
│       │           └── util/              # Utility classes
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── web.xml               # Web configuration
│           │   └── lib/                  # JAR dependencies
│           ├── static/                   # Static resources
│           │   ├── css/
│           │   └── js/
│           └── *.jsp                     # JSP pages
├── database_schema.sql                   # Database schema
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```

## 🔧 API Endpoints

### Authentication
- `POST /bloodnet/register` - Donor registration
- `POST /bloodnet/login` - User login
- `DELETE /bloodnet/login` - User logout

### Blood Requests
- `GET /bloodnet/requestBlood` - Blood request form
- `POST /bloodnet/requestBlood` - Submit blood request

### Donor Matching
- `GET /bloodnet/matchDonors` - Find compatible donors
- `POST /bloodnet/matchDonors` - Search donors with criteria

### Communication
- `GET /bloodnet/chat` - Chat interface
- `POST /bloodnet/chat` - Send message
- `GET /bloodnet/chat?action=getMessages` - Retrieve messages

## 🗄️ Database Schema

### Core Tables
- **donors**: Donor information and eligibility
- **hospitals**: Hospital registration and verification
- **patients**: Patient information
- **blood_requests**: Blood request details
- **chat_messages**: Communication between users
- **donor_matches**: Matching results and responses
- **blood_inventory**: Hospital blood stock
- **notifications**: System notifications
- **admin_users**: Administrative users
- **audit_log**: System audit trail

### Key Features
- **Blood Type Compatibility**: Automatic matching based on medical compatibility
- **Geographic Matching**: Location-based donor search
- **Eligibility Tracking**: Donation frequency and age validation
- **Real-time Updates**: Live inventory and request status

## 🔒 Security Features

- **Password Hashing**: SHA-256 with salt
- **Session Management**: Secure session handling
- **Input Validation**: Comprehensive form validation
- **SQL Injection Prevention**: Prepared statements
- **XSS Protection**: Output encoding
- **CORS Configuration**: Cross-origin request handling

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

## 📊 Monitoring & Logging

- **Request Logging**: All HTTP requests logged
- **Error Tracking**: Comprehensive error handling
- **Performance Monitoring**: Response time tracking
- **Audit Trail**: User action logging

## 🚀 Deployment

### Production Deployment
1. **Environment Setup**: Configure production database
2. **Security Configuration**: Update passwords and keys
3. **SSL Certificate**: Enable HTTPS
4. **Load Balancing**: Configure for high availability
5. **Monitoring**: Set up application monitoring

### Docker Deployment
```bash
# Build Docker image
docker build -t bloodnet:latest .

# Run container
docker run -p 8080:8080 -e DB_HOST=mysql-server bloodnet:latest
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: [Wiki](https://github.com/your-username/bloodnet/wiki)
- **Issues**: [GitHub Issues](https://github.com/your-username/bloodnet/issues)
- **Email**: support@bloodnet.com

## 🙏 Acknowledgments

- Medical professionals for blood type compatibility guidelines
- Open source community for excellent tools and libraries
- Blood donation organizations for domain expertise

## 📈 Roadmap

- [ ] Mobile application (React Native)
- [ ] Advanced geolocation services
- [ ] Machine learning for better matching
- [ ] Integration with hospital management systems
- [ ] Multi-language support
- [ ] Advanced analytics dashboard

---

**BloodNet** - Making a difference, one donation at a time. 🩸❤️