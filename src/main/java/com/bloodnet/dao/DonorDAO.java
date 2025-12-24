package com.bloodnet.dao;

import com.bloodnet.model.Donor;
import com.bloodnet.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Donor operations
 * Handles all database operations related to donors
 */
public class DonorDAO {
    
    /**
     * Register a new donor
     * @param donor Donor object with registration data
     * @return true if registration successful, false otherwise
     */
    public boolean registerDonor(Donor donor) {
        String sql = "INSERT INTO donors (first_name, last_name, email, password_hash, salt, " +
                    "phone, date_of_birth, blood_type, last_donation_date, address, city, " +
                    "state, zip_code, latitude, longitude, is_active, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, donor.getFirstName());
            stmt.setString(2, donor.getLastName());
            stmt.setString(3, donor.getEmail());
            stmt.setString(4, donor.getPasswordHash());
            stmt.setString(5, donor.getSalt());
            stmt.setString(6, donor.getPhone());
            stmt.setDate(7, Date.valueOf(donor.getDateOfBirth()));
            stmt.setString(8, donor.getBloodType());
            stmt.setDate(9, donor.getLastDonationDate() != null ? 
                        Date.valueOf(donor.getLastDonationDate()) : null);
            stmt.setString(10, donor.getAddress());
            stmt.setString(11, donor.getCity());
            stmt.setString(12, donor.getState());
            stmt.setString(13, donor.getZipCode());
            stmt.setDouble(14, donor.getLatitude());
            stmt.setDouble(15, donor.getLongitude());
            stmt.setBoolean(16, donor.isActive());
            stmt.setTimestamp(17, Timestamp.valueOf(donor.getCreatedAt()));
            stmt.setTimestamp(18, Timestamp.valueOf(donor.getUpdatedAt()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        donor.setDonorId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error registering donor: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Authenticate donor login
     * @param email Donor email
     * @param passwordHash Hashed password
     * @return Donor object if authentication successful, null otherwise
     */
    public Donor authenticateDonor(String email, String passwordHash) {
        String sql = "SELECT * FROM donors WHERE email = ? AND password_hash = ? AND is_active = true";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDonor(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error authenticating donor: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get donor by email
     * @param email Donor email
     * @return Donor object if found, null otherwise
     */
    public Donor getDonorByEmail(String email) {
        String sql = "SELECT * FROM donors WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDonor(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting donor by email: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get donor by ID
     * @param donorId Donor ID
     * @return Donor object if found, null otherwise
     */
    public Donor getDonorById(int donorId) {
        String sql = "SELECT * FROM donors WHERE donor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, donorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDonor(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting donor by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find eligible donors for blood request
     * @param bloodType Required blood type
     * @param city City to search in
     * @param state State to search in
     * @param maxDistance Maximum distance in kilometers
     * @return List of eligible donors
     */
    public List<Donor> findEligibleDonors(String bloodType, String city, String state, double maxDistance) {
        List<Donor> donors = new ArrayList<>();
        
        // Get compatible blood types
        List<String> compatibleTypes = getCompatibleBloodTypes(bloodType);
        
        String sql = "SELECT * FROM donors WHERE blood_type IN (" + 
                    String.join(",", compatibleTypes.stream().map(s -> "?").toArray(String[]::new)) +
                    ") AND city = ? AND state = ? AND is_active = true " +
                    "AND (last_donation_date IS NULL OR last_donation_date <= DATE_SUB(CURDATE(), INTERVAL 56 DAY)) " +
                    "ORDER BY last_donation_date ASC, created_at ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            for (String type : compatibleTypes) {
                stmt.setString(paramIndex++, type);
            }
            stmt.setString(paramIndex++, city);
            stmt.setString(paramIndex++, state);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Donor donor = mapResultSetToDonor(rs);
                    donors.add(donor);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding eligible donors: " + e.getMessage());
        }
        
        return donors;
    }
    
    /**
     * Update donor's last donation date
     * @param donorId Donor ID
     * @param donationDate Date of donation
     * @return true if update successful, false otherwise
     */
    public boolean updateLastDonationDate(int donorId, LocalDate donationDate) {
        String sql = "UPDATE donors SET last_donation_date = ?, updated_at = ? WHERE donor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(donationDate));
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, donorId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating last donation date: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update donor profile
     * @param donor Donor object with updated data
     * @return true if update successful, false otherwise
     */
    public boolean updateDonor(Donor donor) {
        String sql = "UPDATE donors SET first_name = ?, last_name = ?, phone = ?, " +
                    "address = ?, city = ?, state = ?, zip_code = ?, latitude = ?, " +
                    "longitude = ?, updated_at = ? WHERE donor_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, donor.getFirstName());
            stmt.setString(2, donor.getLastName());
            stmt.setString(3, donor.getPhone());
            stmt.setString(4, donor.getAddress());
            stmt.setString(5, donor.getCity());
            stmt.setString(6, donor.getState());
            stmt.setString(7, donor.getZipCode());
            stmt.setDouble(8, donor.getLatitude());
            stmt.setDouble(9, donor.getLongitude());
            stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(11, donor.getDonorId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating donor: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if email already exists
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM donors WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Donor object
     * @param rs ResultSet
     * @return Donor object
     * @throws SQLException if database error occurs
     */
    private Donor mapResultSetToDonor(ResultSet rs) throws SQLException {
        Donor donor = new Donor();
        donor.setDonorId(rs.getInt("donor_id"));
        donor.setFirstName(rs.getString("first_name"));
        donor.setLastName(rs.getString("last_name"));
        donor.setEmail(rs.getString("email"));
        donor.setPasswordHash(rs.getString("password_hash"));
        donor.setSalt(rs.getString("salt"));
        donor.setPhone(rs.getString("phone"));
        donor.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        donor.setBloodType(rs.getString("blood_type"));
        
        Date lastDonation = rs.getDate("last_donation_date");
        if (lastDonation != null) {
            donor.setLastDonationDate(lastDonation.toLocalDate());
        }
        
        donor.setAddress(rs.getString("address"));
        donor.setCity(rs.getString("city"));
        donor.setState(rs.getString("state"));
        donor.setZipCode(rs.getString("zip_code"));
        donor.setLatitude(rs.getDouble("latitude"));
        donor.setLongitude(rs.getDouble("longitude"));
        donor.setActive(rs.getBoolean("is_active"));
        donor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        donor.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return donor;
    }
    
    /**
     * Get compatible blood types for a given blood type
     * @param bloodType Blood type to find compatible types for
     * @return List of compatible blood types
     */
    private List<String> getCompatibleBloodTypes(String bloodType) {
        List<String> compatible = new ArrayList<>();
        
        switch (bloodType.toUpperCase()) {
            case "A+":
                compatible.add("A+");
                compatible.add("A-");
                compatible.add("O+");
                compatible.add("O-");
                break;
            case "A-":
                compatible.add("A-");
                compatible.add("O-");
                break;
            case "B+":
                compatible.add("B+");
                compatible.add("B-");
                compatible.add("O+");
                compatible.add("O-");
                break;
            case "B-":
                compatible.add("B-");
                compatible.add("O-");
                break;
            case "AB+":
                compatible.add("A+");
                compatible.add("A-");
                compatible.add("B+");
                compatible.add("B-");
                compatible.add("AB+");
                compatible.add("AB-");
                compatible.add("O+");
                compatible.add("O-");
                break;
            case "AB-":
                compatible.add("A-");
                compatible.add("B-");
                compatible.add("AB-");
                compatible.add("O-");
                break;
            case "O+":
                compatible.add("O+");
                compatible.add("O-");
                break;
            case "O-":
                compatible.add("O-");
                break;
        }
        
        return compatible;
    }
}
