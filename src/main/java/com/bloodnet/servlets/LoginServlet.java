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
import java.util.HashMap;
import java.util.Map;

/**
 * LoginServlet - Handles user authentication
 * Verifies user credentials and manages user sessions
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    private DonorDAO donorDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        donorDAO = new DonorDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("donorId") != null) {
            response.sendRedirect("dashboard.jsp");
            return;
        }
        
        // Forward to login page
        request.getRequestDispatcher("/login.jsp").forward(request, response);
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
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String userType = request.getParameter("userType"); // "donor" or "hospital"
            String rememberMe = request.getParameter("rememberMe");
            
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Email is required");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            if (password == null || password.isEmpty()) {
                result.put("success", false);
                result.put("message", "Password is required");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            if (userType == null || (!userType.equals("donor") && !userType.equals("hospital"))) {
                result.put("success", false);
                result.put("message", "Please select user type");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Authenticate user
            if ("donor".equals(userType)) {
                authenticateDonor(email, password, request, response, result, rememberMe);
            } else if ("hospital".equals(userType)) {
                authenticateHospital(email, password, request, response, result, rememberMe);
            }
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "An error occurred during login. Please try again.");
            response.getWriter().write(convertToJson(result));
        }
    }
    
    /**
     * Authenticate donor login
     */
    private void authenticateDonor(String email, String password, HttpServletRequest request, 
                                 HttpServletResponse response, Map<String, Object> result, 
                                 String rememberMe) throws IOException {
        
        // Get donor by email
        Donor donor = donorDAO.getDonorByEmail(email);
        
        if (donor == null) {
            result.put("success", false);
            result.put("message", "Invalid email or password");
            response.getWriter().write(convertToJson(result));
            return;
        }
        
        // Verify password
        boolean passwordValid = PasswordUtil.verifyPassword(password, donor.getPasswordHash(), donor.getSalt());
        
        if (!passwordValid) {
            result.put("success", false);
            result.put("message", "Invalid email or password");
            response.getWriter().write(convertToJson(result));
            return;
        }
        
        // Check if donor is active
        if (!donor.isActive()) {
            result.put("success", false);
            result.put("message", "Your account has been deactivated. Please contact support.");
            response.getWriter().write(convertToJson(result));
            return;
        }
        
        // Create session
        HttpSession session = request.getSession();
        session.setAttribute("donorId", donor.getDonorId());
        session.setAttribute("donorName", donor.getFullName());
        session.setAttribute("donorEmail", donor.getEmail());
        session.setAttribute("donorBloodType", donor.getBloodType());
        session.setAttribute("donorCity", donor.getCity());
        session.setAttribute("donorState", donor.getState());
        session.setAttribute("userType", "donor");
        
        // Set session timeout
        if ("on".equals(rememberMe)) {
            session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 days
        } else {
            session.setMaxInactiveInterval(60 * 60); // 1 hour
        }
        
        result.put("success", true);
        result.put("message", "Login successful! Welcome back, " + donor.getFirstName() + "!");
        result.put("redirectUrl", "dashboard.jsp");
        result.put("userType", "donor");
        
        response.getWriter().write(convertToJson(result));
    }
    
    /**
     * Authenticate hospital login (placeholder implementation)
     */
    private void authenticateHospital(String email, String password, HttpServletRequest request, 
                                    HttpServletResponse response, Map<String, Object> result, 
                                    String rememberMe) throws IOException {
        
        // For now, implement basic hospital authentication
        // In a real application, you would have a HospitalDAO and Hospital model
        
        // Simple validation for demo purposes
        if ("hospital@bloodnet.com".equals(email) && "hospital123".equals(password)) {
            
            // Create session
            HttpSession session = request.getSession();
            session.setAttribute("hospitalId", 1);
            session.setAttribute("hospitalName", "BloodNet General Hospital");
            session.setAttribute("hospitalEmail", email);
            session.setAttribute("userType", "hospital");
            
            // Set session timeout
            if ("on".equals(rememberMe)) {
                session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 days
            } else {
                session.setMaxInactiveInterval(60 * 60); // 1 hour
            }
            
            result.put("success", true);
            result.put("message", "Login successful! Welcome to BloodNet Hospital Portal!");
            result.put("redirectUrl", "dashboard.jsp");
            result.put("userType", "hospital");
            
        } else {
            result.put("success", false);
            result.put("message", "Invalid hospital credentials");
        }
        
        response.getWriter().write(convertToJson(result));
    }
    
    /**
     * Logout functionality
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Logged out successfully");
        
        response.getWriter().write(convertToJson(result));
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
            } else if (value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
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
