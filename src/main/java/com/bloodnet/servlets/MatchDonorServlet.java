package com.bloodnet.servlets;

import com.bloodnet.dao.DonorDAO;
import com.bloodnet.model.Donor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MatchDonorServlet - Handles donor matching for blood requests
 * Finds and returns compatible donors based on blood type and location
 */
@WebServlet("/matchDonors")
public class MatchDonorServlet extends HttpServlet {
    
    private DonorDAO donorDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        donorDAO = new DonorDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response content type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get request parameters
            String bloodType = request.getParameter("bloodType");
            String city = request.getParameter("city");
            String state = request.getParameter("state");
            String maxDistance = request.getParameter("maxDistance");
            String urgency = request.getParameter("urgency");
            
            // Validate required parameters
            if (bloodType == null || bloodType.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Blood type is required");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            if (city == null || city.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "City is required");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            if (state == null || state.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "State is required");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Parse max distance (default to 50 km if not provided)
            double maxDist = 50.0;
            if (maxDistance != null && !maxDistance.trim().isEmpty()) {
                try {
                    maxDist = Double.parseDouble(maxDistance);
                    if (maxDist <= 0 || maxDist > 500) {
                        maxDist = 50.0; // Reset to default if invalid
                    }
                } catch (NumberFormatException e) {
                    maxDist = 50.0; // Use default if parsing fails
                }
            }
            
            // Find eligible donors
            List<Donor> eligibleDonors = donorDAO.findEligibleDonors(bloodType, city, state, maxDist);
            
            // Filter and sort donors based on criteria
            List<Map<String, Object>> donorMatches = processDonorMatches(eligibleDonors, urgency, maxDist);
            
            if (donorMatches.isEmpty()) {
                result.put("success", true);
                result.put("message", "No eligible donors found in the specified area. Try expanding your search radius.");
                result.put("donors", donorMatches);
                result.put("totalFound", 0);
            } else {
                result.put("success", true);
                result.put("message", "Found " + donorMatches.size() + " eligible donor(s)");
                result.put("donors", donorMatches);
                result.put("totalFound", donorMatches.size());
            }
            
            // Add search criteria to response
            result.put("searchCriteria", Map.of(
                "bloodType", bloodType,
                "city", city,
                "state", state,
                "maxDistance", maxDist,
                "urgency", urgency != null ? urgency : "medium"
            ));
            
        } catch (Exception e) {
            System.err.println("Donor matching error: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "An error occurred while searching for donors. Please try again.");
        }
        
        response.getWriter().write(convertToJson(result));
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userType") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        // Set response content type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get request parameters
            String bloodType = request.getParameter("bloodType");
            String city = request.getParameter("city");
            String state = request.getParameter("state");
            String maxDistance = request.getParameter("maxDistance");
            String urgency = request.getParameter("urgency");
            String requestId = request.getParameter("requestId");
            
            // Validate required parameters
            Map<String, String> validationErrors = validateSearchParameters(
                bloodType, city, state, maxDistance
            );
            
            if (!validationErrors.isEmpty()) {
                result.put("success", false);
                result.put("message", "Validation failed");
                result.put("errors", validationErrors);
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Parse max distance
            double maxDist = Double.parseDouble(maxDistance);
            
            // Find eligible donors
            List<Donor> eligibleDonors = donorDAO.findEligibleDonors(bloodType, city, state, maxDist);
            
            // Process and rank donors
            List<Map<String, Object>> donorMatches = processDonorMatches(eligibleDonors, urgency, maxDist);
            
            // Log the search for analytics
            logDonorSearch(session, bloodType, city, state, maxDist, donorMatches.size());
            
            result.put("success", true);
            result.put("message", "Search completed successfully");
            result.put("donors", donorMatches);
            result.put("totalFound", donorMatches.size());
            result.put("requestId", requestId);
            
        } catch (Exception e) {
            System.err.println("Donor search error: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "An error occurred during the search. Please try again.");
        }
        
        response.getWriter().write(convertToJson(result));
    }
    
    /**
     * Process and rank donor matches
     */
    private List<Map<String, Object>> processDonorMatches(List<Donor> donors, String urgency, double maxDistance) {
        return donors.stream()
                .map(donor -> createDonorMatch(donor, urgency, maxDistance))
                .sorted((a, b) -> {
                    // Sort by priority score (higher is better)
                    int scoreA = (Integer) a.get("priorityScore");
                    int scoreB = (Integer) b.get("priorityScore");
                    return Integer.compare(scoreB, scoreA);
                })
                .limit(20) // Limit to top 20 matches
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Create donor match object with additional information
     */
    private Map<String, Object> createDonorMatch(Donor donor, String urgency, double maxDistance) {
        Map<String, Object> match = new HashMap<>();
        
        // Basic donor information
        match.put("donorId", donor.getDonorId());
        match.put("name", donor.getFullName());
        match.put("bloodType", donor.getBloodType());
        match.put("city", donor.getCity());
        match.put("state", donor.getState());
        match.put("phone", maskPhoneNumber(donor.getPhone()));
        match.put("email", maskEmail(donor.getEmail()));
        
        // Eligibility information
        match.put("isEligible", donor.isEligibleToDonate());
        match.put("isEligibleAge", donor.isEligibleAge());
        match.put("age", donor.getAge());
        match.put("lastDonationDate", donor.getLastDonationDate());
        
        // Calculate priority score
        int priorityScore = calculatePriorityScore(donor, urgency);
        match.put("priorityScore", priorityScore);
        
        // Distance information (placeholder - would use geocoding in real app)
        match.put("estimatedDistance", calculateEstimatedDistance(donor, maxDistance));
        match.put("distanceUnit", "km");
        
        // Availability status
        match.put("availability", determineAvailability(donor));
        match.put("responseTime", estimateResponseTime(donor));
        
        return match;
    }
    
    /**
     * Calculate priority score for donor ranking
     */
    private int calculatePriorityScore(Donor donor, String urgency) {
        int score = 0;
        
        // Base score for being eligible
        if (donor.isEligibleToDonate()) {
            score += 100;
        }
        
        // Age eligibility bonus
        if (donor.isEligibleAge()) {
            score += 50;
        }
        
        // Urgency multiplier
        if (urgency != null) {
            switch (urgency.toLowerCase()) {
                case "critical":
                    score += 200;
                    break;
                case "high":
                    score += 150;
                    break;
                case "medium":
                    score += 100;
                    break;
                case "low":
                    score += 50;
                    break;
            }
        }
        
        // Last donation date bonus (longer time since last donation = higher score)
        if (donor.getLastDonationDate() == null) {
            score += 100; // Never donated before
        } else {
            long daysSinceDonation = java.time.temporal.ChronoUnit.DAYS.between(
                donor.getLastDonationDate(), java.time.LocalDate.now()
            );
            if (daysSinceDonation >= 56) {
                score += 80;
            } else if (daysSinceDonation >= 30) {
                score += 40;
            }
        }
        
        // Age preference (prefer donors in prime age range)
        int age = donor.getAge();
        if (age >= 25 && age <= 45) {
            score += 30;
        } else if (age >= 18 && age <= 65) {
            score += 20;
        }
        
        return score;
    }
    
    /**
     * Calculate estimated distance (placeholder implementation)
     */
    private double calculateEstimatedDistance(Donor donor, double maxDistance) {
        // In a real application, this would use geocoding APIs
        // For demo purposes, return a random distance within the max range
        return Math.random() * maxDistance;
    }
    
    /**
     * Determine donor availability status
     */
    private String determineAvailability(Donor donor) {
        if (!donor.isEligibleToDonate()) {
            return "Not Available";
        }
        
        if (!donor.isEligibleAge()) {
            return "Age Ineligible";
        }
        
        return "Available";
    }
    
    /**
     * Estimate response time
     */
    private String estimateResponseTime(Donor donor) {
        // Simple estimation based on last activity
        if (donor.getLastDonationDate() == null) {
            return "Quick Response Expected";
        }
        
        long daysSinceDonation = java.time.temporal.ChronoUnit.DAYS.between(
            donor.getLastDonationDate(), java.time.LocalDate.now()
        );
        
        if (daysSinceDonation >= 90) {
            return "Quick Response Expected";
        } else if (daysSinceDonation >= 56) {
            return "Normal Response Time";
        } else {
            return "May Take Time";
        }
    }
    
    /**
     * Mask phone number for privacy
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***-***-****";
        }
        return "***-***-" + phone.substring(phone.length() - 4);
    }
    
    /**
     * Mask email for privacy
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "***@" + domain;
        }
        
        return username.substring(0, 2) + "***@" + domain;
    }
    
    /**
     * Validate search parameters
     */
    private Map<String, String> validateSearchParameters(String bloodType, String city, 
            String state, String maxDistance) {
        
        Map<String, String> errors = new HashMap<>();
        
        if (bloodType == null || bloodType.trim().isEmpty()) {
            errors.put("bloodType", "Blood type is required");
        }
        
        if (city == null || city.trim().isEmpty()) {
            errors.put("city", "City is required");
        }
        
        if (state == null || state.trim().isEmpty()) {
            errors.put("state", "State is required");
        }
        
        if (maxDistance == null || maxDistance.trim().isEmpty()) {
            errors.put("maxDistance", "Maximum distance is required");
        } else {
            try {
                double distance = Double.parseDouble(maxDistance);
                if (distance <= 0 || distance > 500) {
                    errors.put("maxDistance", "Distance must be between 1 and 500 km");
                }
            } catch (NumberFormatException e) {
                errors.put("maxDistance", "Please enter a valid distance");
            }
        }
        
        return errors;
    }
    
    /**
     * Log donor search for analytics
     */
    private void logDonorSearch(HttpSession session, String bloodType, String city, 
                              String state, double maxDistance, int resultsCount) {
        
        String userType = (String) session.getAttribute("userType");
        String userId = session.getAttribute("donorId") != null ? 
                       session.getAttribute("donorId").toString() : 
                       session.getAttribute("hospitalId").toString();
        
        System.out.println(String.format(
            "Donor Search - User: %s (%s), BloodType: %s, Location: %s, %s, MaxDistance: %.1f km, Results: %d",
            userId, userType, bloodType, city, state, maxDistance, resultsCount
        ));
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
            } else if (value instanceof Boolean || value instanceof Integer || value instanceof Double) {
                json.append(value);
            } else if (value instanceof List) {
                json.append(convertListToJson((List<?>) value));
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
     * Convert List to JSON string
     */
    private String convertListToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        for (Object item : list) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            if (item instanceof Map) {
                json.append(convertMapToJson((Map<?, ?>) item));
            } else if (item instanceof String) {
                json.append("\"").append(escapeJson(item.toString())).append("\"");
            } else {
                json.append(item);
            }
        }
        
        json.append("]");
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
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            } else {
                json.append(value);
            }
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
