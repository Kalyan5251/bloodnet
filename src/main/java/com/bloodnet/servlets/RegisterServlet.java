package com.bloodnet.servlets;

import com.bloodnet.dao.DonorDAO;
import com.bloodnet.model.Donor;
import com.bloodnet.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * RegisterServlet - Handles donor registration
 * Processes registration form data and creates new donor accounts
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    
    private DonorDAO donorDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        donorDAO = new DonorDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Forward to registration page
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response content type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get form parameters
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String phone = request.getParameter("phone");
            String dateOfBirthStr = request.getParameter("dateOfBirth");
            String bloodType = request.getParameter("bloodType");
            String lastDonationStr = request.getParameter("lastDonation");
            String address = request.getParameter("address");
            String city = request.getParameter("city");
            String state = request.getParameter("state");
            String zipCode = request.getParameter("zipCode");
            
            // Validate required fields
            Map<String, String> validationErrors = validateRegistrationData(
                firstName, lastName, email, password, confirmPassword, 
                phone, dateOfBirthStr, bloodType, address, city, state, zipCode
            );
            
            if (!validationErrors.isEmpty()) {
                result.put("success", false);
                result.put("message", "Validation failed");
                result.put("errors", validationErrors);
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Check if email already exists
            if (donorDAO.emailExists(email)) {
                result.put("success", false);
                result.put("message", "Email already registered");
                result.put("errors", Map.of("email", "This email is already registered"));
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Parse dates
            LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate lastDonation = null;
            if (lastDonationStr != null && !lastDonationStr.trim().isEmpty()) {
                lastDonation = LocalDate.parse(lastDonationStr, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            // Validate age
            int age = LocalDate.now().getYear() - dateOfBirth.getYear();
            if (age < 18 || age > 65) {
                result.put("success", false);
                result.put("message", "Age must be between 18 and 65 years");
                result.put("errors", Map.of("dateOfBirth", "Age must be between 18 and 65 years"));
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Hash password
            String[] passwordData = PasswordUtil.hashPasswordWithSalt(password);
            String passwordHash = passwordData[0];
            String salt = passwordData[1];
            
            // Create donor object
            Donor donor = new Donor(firstName, lastName, email, phone, dateOfBirth, 
                                  bloodType, address, city, state, zipCode);
            donor.setPasswordHash(passwordHash);
            donor.setSalt(salt);
            donor.setLastDonationDate(lastDonation);
            
            // Set default coordinates (can be updated later with geocoding)
            donor.setLatitude(0.0);
            donor.setLongitude(0.0);
            
            // Register donor
            boolean registrationSuccess = donorDAO.registerDonor(donor);
            
            if (registrationSuccess) {
                // Create session for the new user
                HttpSession session = request.getSession();
                session.setAttribute("donorId", donor.getDonorId());
                session.setAttribute("donorName", donor.getFullName());
                session.setAttribute("donorEmail", donor.getEmail());
                session.setAttribute("userType", "donor");
                
                result.put("success", true);
                result.put("message", "Registration successful! Welcome to BloodNet!");
                result.put("redirectUrl", "dashboard.jsp");
                
            } else {
                result.put("success", false);
                result.put("message", "Registration failed. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "An error occurred during registration. Please try again.");
        }
        
        response.getWriter().write(convertToJson(result));
    }
    
    /**
     * Validate registration form data
     */
    private Map<String, String> validateRegistrationData(String firstName, String lastName, 
            String email, String password, String confirmPassword, String phone, 
            String dateOfBirth, String bloodType, String address, String city, 
            String state, String zipCode) {
        
        Map<String, String> errors = new HashMap<>();
        
        // Validate first name
        if (firstName == null || firstName.trim().isEmpty()) {
            errors.put("firstName", "First name is required");
        } else if (firstName.trim().length() < 2) {
            errors.put("firstName", "First name must be at least 2 characters");
        }
        
        // Validate last name
        if (lastName == null || lastName.trim().isEmpty()) {
            errors.put("lastName", "Last name is required");
        } else if (lastName.trim().length() < 2) {
            errors.put("lastName", "Last name must be at least 2 characters");
        }
        
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!isValidEmail(email)) {
            errors.put("email", "Please enter a valid email address");
        }
        
        // Validate password
        if (password == null || password.isEmpty()) {
            errors.put("password", "Password is required");
        } else {
            PasswordUtil.PasswordValidationResult validation = PasswordUtil.validatePassword(password);
            if (!validation.isValid()) {
                errors.put("password", validation.getMessage());
            }
        }
        
        // Validate confirm password
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            errors.put("confirmPassword", "Please confirm your password");
        } else if (!password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Passwords do not match");
        }
        
        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            errors.put("phone", "Phone number is required");
        } else if (!isValidPhone(phone)) {
            errors.put("phone", "Please enter a valid phone number");
        }
        
        // Validate date of birth
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            errors.put("dateOfBirth", "Date of birth is required");
        }
        
        // Validate blood type
        if (bloodType == null || bloodType.trim().isEmpty()) {
            errors.put("bloodType", "Blood type is required");
        } else if (!isValidBloodType(bloodType)) {
            errors.put("bloodType", "Please select a valid blood type");
        }
        
        // Validate address
        if (address == null || address.trim().isEmpty()) {
            errors.put("address", "Address is required");
        }
        
        // Validate city
        if (city == null || city.trim().isEmpty()) {
            errors.put("city", "City is required");
        }
        
        // Validate state
        if (state == null || state.trim().isEmpty()) {
            errors.put("state", "State is required");
        }
        
        // Validate zip code
        if (zipCode == null || zipCode.trim().isEmpty()) {
            errors.put("zipCode", "ZIP code is required");
        } else if (!isValidZipCode(zipCode)) {
            errors.put("zipCode", "Please enter a valid ZIP code");
        }
        
        return errors;
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
    
    /**
     * Validate phone number format
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("^[+]?[1-9]\\d{0,15}$");
    }
    
    /**
     * Validate blood type
     */
    private boolean isValidBloodType(String bloodType) {
        String[] validTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for (String type : validTypes) {
            if (type.equals(bloodType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate ZIP code format
     */
    private boolean isValidZipCode(String zipCode) {
        return zipCode.matches("^\\d{5}(-\\d{4})?$");
    }
    
    /**
     * Convert Map to JSON string (simple implementation)
     */
    private String convertToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Map) {
                json.append(convertMapToJson((Map<?, ?>) value));
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Convert nested Map to JSON string
     */
    private String convertMapToJson(Map<?, ?> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            json.append("\"").append(entry.getValue()).append("\"");
        }
        
        json.append("}");
        return json.toString();
    }
}
