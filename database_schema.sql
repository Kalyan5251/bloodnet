-- BloodNet Database Schema
-- Smart Emergency Blood Donor Network
-- MySQL Database Creation Script

-- Create database
CREATE DATABASE IF NOT EXISTS bloodnet_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE bloodnet_db;

-- =============================================
-- DONORS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS donors (
    donor_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    blood_type ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    last_donation_date DATE NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    latitude DECIMAL(10, 8) DEFAULT 0.0,
    longitude DECIMAL(11, 8) DEFAULT 0.0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_blood_type (blood_type),
    INDEX idx_city_state (city, state),
    INDEX idx_location (latitude, longitude),
    INDEX idx_active (is_active),
    INDEX idx_last_donation (last_donation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- HOSPITALS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS hospitals (
    hospital_id INT AUTO_INCREMENT PRIMARY KEY,
    hospital_name VARCHAR(200) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    hospital_code VARCHAR(20) UNIQUE NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    latitude DECIMAL(10, 8) DEFAULT 0.0,
    longitude DECIMAL(11, 8) DEFAULT 0.0,
    contact_person VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_hospital_code (hospital_code),
    INDEX idx_license (license_number),
    INDEX idx_city_state (city, state),
    INDEX idx_verified (is_verified),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- PATIENTS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    blood_type ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    medical_condition TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(10),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_blood_type (blood_type),
    INDEX idx_city_state (city, state),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- BLOOD REQUESTS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS blood_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    requester_type ENUM('patient', 'hospital') NOT NULL,
    requester_id INT NOT NULL,
    blood_type ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    units_required INT NOT NULL CHECK (units_required > 0 AND units_required <= 10),
    urgency ENUM('low', 'medium', 'high', 'critical') DEFAULT 'medium',
    patient_name VARCHAR(100),
    patient_age VARCHAR(10),
    medical_condition TEXT,
    hospital_name VARCHAR(200),
    hospital_address TEXT,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    latitude DECIMAL(10, 8) DEFAULT 0.0,
    longitude DECIMAL(11, 8) DEFAULT 0.0,
    contact_person VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    contact_email VARCHAR(100) NOT NULL,
    additional_notes TEXT,
    status ENUM('pending', 'matched', 'fulfilled', 'cancelled') DEFAULT 'pending',
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    required_date TIMESTAMP NULL,
    fulfilled_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_requester (requester_type, requester_id),
    INDEX idx_blood_type (blood_type),
    INDEX idx_urgency (urgency),
    INDEX idx_status (status),
    INDEX idx_city_state (city, state),
    INDEX idx_request_date (request_date),
    INDEX idx_required_date (required_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- CHAT MESSAGES TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS chat_messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    sender_id INT NOT NULL,
    sender_type ENUM('donor', 'hospital', 'patient') NOT NULL,
    sender_name VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    message_type ENUM('text', 'image', 'file') DEFAULT 'text',
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    
    INDEX idx_request_id (request_id),
    INDEX idx_sender (sender_id, sender_type),
    INDEX idx_sent_at (sent_at),
    INDEX idx_is_read (is_read),
    
    FOREIGN KEY (request_id) REFERENCES blood_requests(request_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- DONOR MATCHES TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS donor_matches (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    donor_id INT NOT NULL,
    match_score INT DEFAULT 0,
    distance_km DECIMAL(8, 2) DEFAULT 0.0,
    is_contacted BOOLEAN DEFAULT FALSE,
    contacted_at TIMESTAMP NULL,
    response_status ENUM('pending', 'accepted', 'declined', 'no_response') DEFAULT 'pending',
    response_at TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_request_id (request_id),
    INDEX idx_donor_id (donor_id),
    INDEX idx_match_score (match_score),
    INDEX idx_response_status (response_status),
    INDEX idx_contacted (is_contacted),
    
    FOREIGN KEY (request_id) REFERENCES blood_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES donors(donor_id) ON DELETE CASCADE,
    UNIQUE KEY unique_request_donor (request_id, donor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- BLOOD INVENTORY TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS blood_inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    hospital_id INT NOT NULL,
    blood_type ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    units_available INT DEFAULT 0,
    units_reserved INT DEFAULT 0,
    expiry_date DATE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_hospital_id (hospital_id),
    INDEX idx_blood_type (blood_type),
    INDEX idx_units_available (units_available),
    INDEX idx_expiry_date (expiry_date),
    
    FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    UNIQUE KEY unique_hospital_blood_type (hospital_id, blood_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- NOTIFICATIONS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_type ENUM('donor', 'hospital', 'patient') NOT NULL,
    notification_type ENUM('blood_request', 'donor_match', 'message', 'system') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user (user_id, user_type),
    INDEX idx_notification_type (notification_type),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- ADMIN USERS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS admin_users (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('super_admin', 'admin', 'moderator') DEFAULT 'admin',
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- AUDIT LOG TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS audit_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    user_type ENUM('donor', 'hospital', 'patient', 'admin') NOT NULL,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50),
    record_id INT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user (user_id, user_type),
    INDEX idx_action (action),
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- SAMPLE DATA INSERTION
-- =============================================

-- Insert sample admin user
INSERT INTO admin_users (username, email, password_hash, salt, full_name, role) VALUES
('admin', 'admin@bloodnet.com', 'hashed_password_here', 'salt_here', 'System Administrator', 'super_admin');

-- Insert sample hospital
INSERT INTO hospitals (hospital_name, email, password_hash, salt, phone, hospital_code, license_number, 
                      address, city, state, zip_code, contact_person, contact_phone) VALUES
('BloodNet General Hospital', 'hospital@bloodnet.com', 'hashed_password_here', 'salt_here', 
 '+1-555-0123', 'HOSP001', 'LIC123456', '123 Medical Center Dr', 'New York', 'NY', '10001', 
 'Dr. Sarah Johnson', '+1-555-0124');

-- Insert sample blood inventory
INSERT INTO blood_inventory (hospital_id, blood_type, units_available, units_reserved) VALUES
(1, 'A+', 25, 5),
(1, 'A-', 8, 2),
(1, 'B+', 18, 3),
(1, 'B-', 6, 1),
(1, 'AB+', 12, 2),
(1, 'AB-', 3, 0),
(1, 'O+', 35, 8),
(1, 'O-', 7, 2);

-- =============================================
-- VIEWS FOR COMMON QUERIES
-- =============================================

-- View for active donors with eligibility status
CREATE VIEW active_donors_view AS
SELECT 
    d.donor_id,
    CONCAT(d.first_name, ' ', d.last_name) AS full_name,
    d.email,
    d.phone,
    d.blood_type,
    d.city,
    d.state,
    d.last_donation_date,
    CASE 
        WHEN d.last_donation_date IS NULL THEN TRUE
        WHEN d.last_donation_date <= DATE_SUB(CURDATE(), INTERVAL 56 DAY) THEN TRUE
        ELSE FALSE
    END AS is_eligible_to_donate,
    CASE 
        WHEN YEAR(CURDATE()) - YEAR(d.date_of_birth) BETWEEN 18 AND 65 THEN TRUE
        ELSE FALSE
    END AS is_eligible_age,
    d.created_at
FROM donors d
WHERE d.is_active = TRUE;

-- View for pending blood requests
CREATE VIEW pending_requests_view AS
SELECT 
    br.request_id,
    br.requester_type,
    br.blood_type,
    br.units_required,
    br.urgency,
    br.patient_name,
    br.hospital_name,
    br.city,
    br.state,
    br.contact_person,
    br.contact_phone,
    br.request_date,
    br.required_date,
    DATEDIFF(COALESCE(br.required_date, DATE_ADD(br.request_date, INTERVAL 7 DAY)), CURDATE()) AS days_remaining
FROM blood_requests br
WHERE br.status = 'pending'
ORDER BY 
    CASE br.urgency
        WHEN 'critical' THEN 1
        WHEN 'high' THEN 2
        WHEN 'medium' THEN 3
        WHEN 'low' THEN 4
    END,
    br.request_date;

-- =============================================
-- STORED PROCEDURES
-- =============================================

DELIMITER //

-- Procedure to find compatible donors for a blood request
CREATE PROCEDURE FindCompatibleDonors(
    IN p_blood_type VARCHAR(3),
    IN p_city VARCHAR(100),
    IN p_state VARCHAR(100),
    IN p_max_distance DECIMAL(8,2)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE donor_cursor CURSOR FOR
        SELECT 
            d.donor_id,
            CONCAT(d.first_name, ' ', d.last_name) AS full_name,
            d.blood_type,
            d.city,
            d.state,
            d.phone,
            d.last_donation_date,
            CASE 
                WHEN d.last_donation_date IS NULL THEN 100
                WHEN d.last_donation_date <= DATE_SUB(CURDATE(), INTERVAL 90 DAY) THEN 80
                WHEN d.last_donation_date <= DATE_SUB(CURDATE(), INTERVAL 56 DAY) THEN 60
                ELSE 0
            END AS eligibility_score
        FROM donors d
        WHERE d.is_active = TRUE
        AND d.city = p_city
        AND d.state = p_state
        AND (
            (p_blood_type = 'A+' AND d.blood_type IN ('A+', 'A-', 'O+', 'O-')) OR
            (p_blood_type = 'A-' AND d.blood_type IN ('A-', 'O-')) OR
            (p_blood_type = 'B+' AND d.blood_type IN ('B+', 'B-', 'O+', 'O-')) OR
            (p_blood_type = 'B-' AND d.blood_type IN ('B-', 'O-')) OR
            (p_blood_type = 'AB+' AND d.blood_type IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-')) OR
            (p_blood_type = 'AB-' AND d.blood_type IN ('A-', 'B-', 'AB-', 'O-')) OR
            (p_blood_type = 'O+' AND d.blood_type IN ('O+', 'O-')) OR
            (p_blood_type = 'O-' AND d.blood_type = 'O-')
        )
        AND (d.last_donation_date IS NULL OR d.last_donation_date <= DATE_SUB(CURDATE(), INTERVAL 56 DAY))
        ORDER BY eligibility_score DESC, d.created_at ASC;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN donor_cursor;
    read_loop: LOOP
        FETCH donor_cursor;
        IF done THEN
            LEAVE read_loop;
        END IF;
    END LOOP;
    CLOSE donor_cursor;
END //

DELIMITER ;

-- =============================================
-- TRIGGERS
-- =============================================

-- Trigger to update donor's last donation date when a request is fulfilled
DELIMITER //

CREATE TRIGGER update_donor_donation_date
AFTER UPDATE ON blood_requests
FOR EACH ROW
BEGIN
    IF NEW.status = 'fulfilled' AND OLD.status != 'fulfilled' THEN
        UPDATE donors d
        INNER JOIN donor_matches dm ON d.donor_id = dm.donor_id
        SET d.last_donation_date = CURDATE(),
            d.updated_at = CURRENT_TIMESTAMP
        WHERE dm.request_id = NEW.request_id
        AND dm.response_status = 'accepted';
    END IF;
END //

DELIMITER ;

-- =============================================
-- INDEXES FOR PERFORMANCE
-- =============================================

-- Additional composite indexes for better query performance
CREATE INDEX idx_donors_blood_location ON donors(blood_type, city, state, is_active);
CREATE INDEX idx_requests_blood_status ON blood_requests(blood_type, status, request_date);
CREATE INDEX idx_messages_request_sender ON chat_messages(request_id, sender_id, sent_at);

-- =============================================
-- GRANTS AND PERMISSIONS
-- =============================================

-- Create application user (replace 'bloodnet_user' and 'secure_password' with actual credentials)
-- CREATE USER 'bloodnet_user'@'localhost' IDENTIFIED BY 'secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON bloodnet_db.* TO 'bloodnet_user'@'localhost';
-- FLUSH PRIVILEGES;

-- =============================================
-- DATABASE COMPLETION MESSAGE
-- =============================================

SELECT 'BloodNet Database Schema Created Successfully!' AS Status;
SELECT 'Database: bloodnet_db' AS Database_Name;
SELECT 'Tables Created: 10' AS Table_Count;
SELECT 'Views Created: 2' AS View_Count;
SELECT 'Procedures Created: 1' AS Procedure_Count;
SELECT 'Triggers Created: 1' AS Trigger_Count;
