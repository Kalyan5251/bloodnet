package com.bloodnet.servlets;

import com.bloodnet.model.ChatMessage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChatServlet - Handles donor-hospital communication
 * Manages message exchange between donors and hospitals for blood requests
 */
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userType") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        // Get request parameters
        String requestId = request.getParameter("requestId");
        String action = request.getParameter("action");
        
        if ("getMessages".equals(action)) {
            // Get chat messages for a specific request
            getChatMessages(requestId, response, session);
        } else {
            // Forward to chat page
            request.setAttribute("requestId", requestId);
            request.getRequestDispatcher("/chat.jsp").forward(request, response);
        }
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
            // Get form parameters
            String action = request.getParameter("action");
            String requestId = request.getParameter("requestId");
            String message = request.getParameter("message");
            String messageType = request.getParameter("messageType");
            
            // Get user information from session
            String userType = (String) session.getAttribute("userType");
            Integer senderId = null;
            String senderName = null;
            
            if ("donor".equals(userType)) {
                senderId = (Integer) session.getAttribute("donorId");
                senderName = (String) session.getAttribute("donorName");
            } else if ("hospital".equals(userType)) {
                senderId = (Integer) session.getAttribute("hospitalId");
                senderName = (String) session.getAttribute("hospitalName");
            }
            
            if (senderId == null || senderName == null) {
                result.put("success", false);
                result.put("message", "Invalid user session");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            if ("sendMessage".equals(action)) {
                // Send a new message
                sendMessage(requestId, senderId, userType, senderName, message, messageType, result);
            } else if ("markAsRead".equals(action)) {
                // Mark messages as read
                markMessagesAsRead(requestId, senderId, result);
            } else {
                result.put("success", false);
                result.put("message", "Invalid action");
            }
            
        } catch (Exception e) {
            System.err.println("Chat error: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "An error occurred. Please try again.");
        }
        
        response.getWriter().write(convertToJson(result));
    }
    
    /**
     * Get chat messages for a specific request
     */
    private void getChatMessages(String requestId, HttpServletResponse response, HttpSession session) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Request ID is required");
                response.getWriter().write(convertToJson(result));
                return;
            }
            
            // Get messages from database (placeholder implementation)
            List<ChatMessage> messages = getMessagesFromDatabase(requestId);
            
            // Convert to response format
            List<Map<String, Object>> messageList = new ArrayList<>();
            for (ChatMessage msg : messages) {
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("messageId", msg.getMessageId());
                messageData.put("senderId", msg.getSenderId());
                messageData.put("senderType", msg.getSenderType());
                messageData.put("senderName", msg.getSenderName());
                messageData.put("message", msg.getMessage());
                messageData.put("messageType", msg.getMessageType());
                messageData.put("isRead", msg.isRead());
                messageData.put("sentAt", msg.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                messageData.put("formattedTime", msg.getFormattedTime());
                messageData.put("isFromDonor", msg.isFromDonor());
                messageData.put("isFromHospital", msg.isFromHospital());
                
                messageList.add(messageData);
            }
            
            result.put("success", true);
            result.put("messages", messageList);
            result.put("requestId", requestId);
            result.put("totalMessages", messages.size());
            
        } catch (Exception e) {
            System.err.println("Error getting chat messages: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "Failed to retrieve messages");
        }
        
        response.getWriter().write(convertToJson(result));
    }
    
    /**
     * Send a new message
     */
    private void sendMessage(String requestId, Integer senderId, String userType, 
                           String senderName, String message, String messageType, 
                           Map<String, Object> result) {
        
        try {
            // Validate input
            if (requestId == null || requestId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Request ID is required");
                return;
            }
            
            if (message == null || message.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Message cannot be empty");
                return;
            }
            
            if (message.length() > 1000) {
                result.put("success", false);
                result.put("message", "Message is too long (maximum 1000 characters)");
                return;
            }
            
            // Create chat message
            ChatMessage chatMessage = new ChatMessage(
                Integer.parseInt(requestId), 
                senderId, 
                userType, 
                senderName, 
                message.trim()
            );
            
            if (messageType != null && !messageType.trim().isEmpty()) {
                chatMessage.setMessageType(messageType);
            }
            
            // Save message to database (placeholder implementation)
            boolean saveSuccess = saveMessageToDatabase(chatMessage);
            
            if (saveSuccess) {
                result.put("success", true);
                result.put("message", "Message sent successfully");
                result.put("messageId", chatMessage.getMessageId());
                result.put("sentAt", chatMessage.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                result.put("formattedTime", chatMessage.getFormattedTime());
                
                // Log the message
                System.out.println(String.format(
                    "Chat Message - RequestId: %s, Sender: %s (%s), Message: %s",
                    requestId, senderName, userType, message
                ));
                
            } else {
                result.put("success", false);
                result.put("message", "Failed to send message");
            }
            
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Invalid request ID");
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "An error occurred while sending the message");
        }
    }
    
    /**
     * Mark messages as read
     */
    private void markMessagesAsRead(String requestId, Integer userId, Map<String, Object> result) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Request ID is required");
                return;
            }
            
            // Mark messages as read in database (placeholder implementation)
            boolean updateSuccess = markMessagesAsReadInDatabase(requestId, userId);
            
            if (updateSuccess) {
                result.put("success", true);
                result.put("message", "Messages marked as read");
            } else {
                result.put("success", false);
                result.put("message", "Failed to mark messages as read");
            }
            
        } catch (Exception e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "An error occurred while marking messages as read");
        }
    }
    
    /**
     * Get messages from database (placeholder implementation)
     * In a real application, this would use a ChatDAO
     */
    private List<ChatMessage> getMessagesFromDatabase(String requestId) {
        List<ChatMessage> messages = new ArrayList<>();
        
        try {
            // For demo purposes, create some sample messages
            // In real implementation, use ChatDAO.getMessagesByRequestId(requestId)
            
            ChatMessage message1 = new ChatMessage(
                Integer.parseInt(requestId), 
                1, 
                "hospital", 
                "BloodNet General Hospital", 
                "Thank you for your interest in donating blood. We need 2 units of O+ blood urgently."
            );
            message1.setMessageId(1);
            message1.setSentAt(LocalDateTime.now().minusHours(2));
            message1.setRead(true);
            messages.add(message1);
            
            ChatMessage message2 = new ChatMessage(
                Integer.parseInt(requestId), 
                2, 
                "donor", 
                "John Doe", 
                "I'm available to donate. When would be the best time to come in?"
            );
            message2.setMessageId(2);
            message2.setSentAt(LocalDateTime.now().minusHours(1));
            message2.setRead(true);
            messages.add(message2);
            
            ChatMessage message3 = new ChatMessage(
                Integer.parseInt(requestId), 
                1, 
                "hospital", 
                "BloodNet General Hospital", 
                "We're open 24/7. You can come anytime today. Please bring a valid ID."
            );
            message3.setMessageId(3);
            message3.setSentAt(LocalDateTime.now().minusMinutes(30));
            message3.setRead(false);
            messages.add(message3);
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid request ID: " + requestId);
        }
        
        return messages;
    }
    
    /**
     * Save message to database (placeholder implementation)
     * In a real application, this would use a ChatDAO
     */
    private boolean saveMessageToDatabase(ChatMessage message) {
        try {
            // For demo purposes, simulate database save
            // In real implementation, use ChatDAO.saveMessage(message)
            
            // Generate a mock message ID
            message.setMessageId((int) (System.currentTimeMillis() % 100000));
            
            // Simulate database operation delay
            Thread.sleep(50);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error saving message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark messages as read in database (placeholder implementation)
     * In a real application, this would use a ChatDAO
     */
    private boolean markMessagesAsReadInDatabase(String requestId, Integer userId) {
        try {
            // For demo purposes, simulate database update
            // In real implementation, use ChatDAO.markMessagesAsRead(requestId, userId)
            
            // Simulate database operation delay
            Thread.sleep(50);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            return false;
        }
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
            } else if (value instanceof List) {
                json.append(convertListToJson((List<?>) value));
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
            } else if (value instanceof Boolean || value instanceof Integer) {
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
