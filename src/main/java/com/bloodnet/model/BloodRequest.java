package com.bloodnet.model;

import java.time.LocalDateTime;

/**
 * Blood Request Model Class for BloodNet Application
 * Represents a blood request from patients or hospitals
 */
public class BloodRequest {
    
    private int requestId;
    private String requesterType; // "patient" or "hospital"
    private int requesterId; // patient_id or hospital_id
    private String bloodType;
    private int unitsRequired;
    private String urgency; // "low", "medium", "high", "critical"
    private String patientName;
    private String patientAge;
    private String medicalCondition;
    private String hospitalName;
    private String hospitalAddress;
    private String city;
    private String state;
    private String zipCode;
    private double latitude;
    private double longitude;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String additionalNotes;
    private String status; // "pending", "matched", "fulfilled", "cancelled"
    private LocalDateTime requestDate;
    private LocalDateTime requiredDate;
    private LocalDateTime fulfilledDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public BloodRequest() {}
    
    // Constructor for new request
    public BloodRequest(String requesterType, int requesterId, String bloodType, 
                       int unitsRequired, String urgency, String city, String state) {
        this.requesterType = requesterType;
        this.requesterId = requesterId;
        this.bloodType = bloodType;
        this.unitsRequired = unitsRequired;
        this.urgency = urgency;
        this.city = city;
        this.state = state;
        this.status = "pending";
        this.requestDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }
    
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
    
    public String getRequesterType() {
        return requesterType;
    }
    
    public void setRequesterType(String requesterType) {
        this.requesterType = requesterType;
    }
    
    public int getRequesterId() {
        return requesterId;
    }
    
    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }
    
    public String getBloodType() {
        return bloodType;
    }
    
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    
    public int getUnitsRequired() {
        return unitsRequired;
    }
    
    public void setUnitsRequired(int unitsRequired) {
        this.unitsRequired = unitsRequired;
    }
    
    public String getUrgency() {
        return urgency;
    }
    
    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getPatientAge() {
        return patientAge;
    }
    
    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }
    
    public String getMedicalCondition() {
        return medicalCondition;
    }
    
    public void setMedicalCondition(String medicalCondition) {
        this.medicalCondition = medicalCondition;
    }
    
    public String getHospitalName() {
        return hospitalName;
    }
    
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
    
    public String getHospitalAddress() {
        return hospitalAddress;
    }
    
    public void setHospitalAddress(String hospitalAddress) {
        this.hospitalAddress = hospitalAddress;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
    
    public LocalDateTime getRequiredDate() {
        return requiredDate;
    }
    
    public void setRequiredDate(LocalDateTime requiredDate) {
        this.requiredDate = requiredDate;
    }
    
    public LocalDateTime getFulfilledDate() {
        return fulfilledDate;
    }
    
    public void setFulfilledDate(LocalDateTime fulfilledDate) {
        this.fulfilledDate = fulfilledDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if request is urgent
     * @return true if urgency is high or critical
     */
    public boolean isUrgent() {
        return "high".equals(urgency) || "critical".equals(urgency);
    }
    
    /**
     * Get urgency priority for sorting
     * @return priority number (higher = more urgent)
     */
    public int getUrgencyPriority() {
        switch (urgency.toLowerCase()) {
            case "critical": return 4;
            case "high": return 3;
            case "medium": return 2;
            case "low": return 1;
            default: return 0;
        }
    }
    
    /**
     * Get full location string
     * @return formatted location string
     */
    public String getFullLocation() {
        return city + ", " + state + " " + zipCode;
    }
    
    @Override
    public String toString() {
        return "BloodRequest{" +
                "requestId=" + requestId +
                ", requesterType='" + requesterType + '\'' +
                ", bloodType='" + bloodType + '\'' +
                ", unitsRequired=" + unitsRequired +
                ", urgency='" + urgency + '\'' +
                ", status='" + status + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
