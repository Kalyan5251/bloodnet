package com.bloodnet.servlets;

import com.bloodnet.model.BloodRequest;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * RequestBloodServlet - Handles blood request submissions
 * Processes blood requests from patients and hospitals
 */
@WebServlet("/requestBlood")
public class RequestBloodServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userType") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        // Forward to blood request form
        request.getRequestDispatcher("/requestBlood.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response content type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if user is logged in
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userType") == null) {
                result.put("success", false);
                result.put("message", "Please login to submit a blood request");
                result.put("redirectUrl", "login.jsp");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Get form parameters
            String bloodType = request.getParameter("bloodType");
            String unitsRequired = request.getParameter("unitsRequired");
            String urgency = request.getParameter("urgency");
            String patientName = request.getParameter("patientName");
            String patientAge = request.getParameter("patientAge");
            String medicalCondition = request.getParameter("medicalCondition");
            String hospitalName = request.getParameter("hospitalName");
            String hospitalAddress = request.getParameter("hospitalAddress");
            String city = request.getParameter("city");
            String state = request.getParameter("state");
            String zipCode = request.getParameter("zipCode");
            String contactPerson = request.getParameter("contactPerson");
            String contactPhone = request.getParameter("contactPhone");
            String contactEmail = request.getParameter("contactEmail");
            String requiredDateStr = request.getParameter("requiredDate");
            String additionalNotes = request.getParameter("additionalNotes");
            
            // Get user information from session
            String userType = (String) session.getAttribute("userType");
            Integer requesterId = null;
            
            if ("donor".equals(userType)) {
                requesterId = (Integer) session.getAttribute("donorId");
            } else if ("hospital".equals(userType)) {
                requesterId = (Integer) session.getAttribute("hospitalId");
            }
            
            // Validate required fields
            Map<String, String> validationErrors = validateBloodRequestData(
                bloodType, unitsRequired, urgency, city, state, zipCode, 
                contactPerson, contactPhone, contactEmail
            );
            
            if (!validationErrors.isEmpty()) {
                result.put("success", false);
                result.put("message", "Validation failed");
                result.put("errors", validationErrors);
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Parse and validate data
            int units = Integer.parseInt(unitsRequired);
            if (units <= 0 || units > 10) {
                result.put("success", false);
                result.put("message", "Units required must be between 1 and 10");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            LocalDateTime requiredDate = null;
            if (requiredDateStr != null && !requiredDateStr.trim().isEmpty()) {
                try {
                    requiredDate = LocalDateTime.parse(requiredDateStr + "T00:00:00");
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("message", "Invalid required date format");
                    response.getWriter().write(convertToJson(result));
                    return;
                }
            }
            
            // Create blood request object
            BloodRequest bloodRequest = new BloodRequest(userType, requesterId, bloodType, 
                                                       units, urgency, city, state);
            bloodRequest.setPatientName(patientName);
            bloodRequest.setPatientAge(patientAge);
            bloodRequest.setMedicalCondition(medicalCondition);
            bloodRequest.setHospitalName(hospitalName);
            bloodRequest.setHospitalAddress(hospitalAddress);
            bloodRequest.setZipCode(zipCode);
            bloodRequest.setContactPerson(contactPerson);
            bloodRequest.setContactPhone(contactPhone);
            bloodRequest.setContactEmail(contactEmail);
            bloodRequest.setRequiredDate(requiredDate);
            bloodRequest.setAdditionalNotes(additionalNotes);
            
            // Set default coordinates (can be updated with geocoding)
            bloodRequest.setLatitude(0.0);
            bloodRequest.setLongitude(0.0);
            
            // Save blood request to database
            boolean saveSuccess = saveBloodRequest(bloodRequest);
            
            if (saveSuccess) {
                result.put("success", true);
                result.put("message", "Blood request submitted successfully! We'll notify matching donors.");
                result.put("requestId", bloodRequest.getRequestId());
                result.put("redirectUrl", "requestStatus.jsp?requestId=" + bloodRequest.getRequestId());
                
                // Log the request
                System.out.println("Blood request submitted: " + bloodRequest.toString());
                
            } else {
                result.put("success", false);
                result.put("message", "Failed to submit blood request. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("Blood request error: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "An error occurred while submitting the request. Please try again.");
        }
        
        response.getWriter().write(convertToJson(result));
    }
    
    /**
     * Validate blood request form data
     */
    private Map<String, String> validateBloodRequestData(String bloodType, String unitsRequired, 
            String urgency, String city, String state, String zipCode, String contactPerson, 
            String contactPhone, String contactEmail) {
        
        Map<String, String> errors = new HashMap<>();
        
        // Validate blood type
        if (bloodType == null || bloodType.trim().isEmpty()) {
            errors.put("bloodType", "Blood type is required");
        } else if (!isValidBloodType(bloodType)) {
            errors.put("bloodType", "Please select a valid blood type");
        }
        
        // Validate units required
        if (unitsRequired == null || unitsRequired.trim().isEmpty()) {
            errors.put("unitsRequired", "Number of units is required");
        } else {
            try {
                int units = Integer.parseInt(unitsRequired);
                if (units <= 0 || units > 10) {
                    errors.put("unitsRequired", "Units must be between 1 and 10");
                }
            } catch (NumberFormatException e) {
                errors.put("unitsRequired", "Please enter a valid number");
            }
        }
        
        // Validate urgency
        if (urgency == null || urgency.trim().isEmpty()) {
            errors.put("urgency", "Urgency level is required");
        } else if (!isValidUrgency(urgency)) {
            errors.put("urgency", "Please select a valid urgency level");
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
        
        // Validate contact person
        if (contactPerson == null || contactPerson.trim().isEmpty()) {
            errors.put("contactPerson", "Contact person name is required");
        }
        
        // Validate contact phone
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            errors.put("contactPhone", "Contact phone number is required");
        } else if (!isValidPhone(contactPhone)) {
            errors.put("contactPhone", "Please enter a valid phone number");
        }
        
        // Validate contact email
        if (contactEmail == null || contactEmail.trim().isEmpty()) {
            errors.put("contactEmail", "Contact email is required");
        } else if (!isValidEmail(contactEmail)) {
            errors.put("contactEmail", "Please enter a valid email address");
        }
        
        return errors;
    }
    
    /**
     * Save blood request to database (placeholder implementation)
     * In a real application, this would use a BloodRequestDAO
     */
    private boolean saveBloodRequest(BloodRequest bloodRequest) {
        try {
            // For demo purposes, simulate database save
            // In real implementation, use BloodRequestDAO.saveBloodRequest(bloodRequest)
            
            // Generate a mock request ID
            bloodRequest.setRequestId((int) (System.currentTimeMillis() % 100000));
            
            // Simulate database operation delay
            Thread.sleep(100);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error saving blood request: " + e.getMessage());
            return false;
        }
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
     * Validate urgency level
     */
    private boolean isValidUrgency(String urgency) {
        String[] validUrgencies = {"low", "medium", "high", "critical"};
        for (String level : validUrgencies) {
            if (level.equals(urgency.toLowerCase())) {
                return true;
            }
        }
        return false;
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
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            } else if (value instanceof Boolean || value instanceof Integer) {
                json.append(value);
            } else if (value instanceof Map) {
                json.append(convertMapToJson((Map<?, ?>) value));
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
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
            json.append("\"").append(escapeJson(entry.getValue().toString())).append("\"");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\b", "\\b")
                 .replace("\f", "\\f")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}
