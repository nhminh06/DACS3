package com.example.dacs3.admin.model;

import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
import com.google.cloud.firestore.annotation.PropertyName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Booking {
    private String id;
    private String tourId;
    private String userId;
    private String customerName;
    private String email;
    private String phone;
    private String address;
    private String startDate;
    private String note;
    private String status; // PENDING, CONFIRMED, CANCELLED

    @PropertyName("paymentMethod")
    private String paymentMethod; // QR, CASH

    @PropertyName("paymentStatus")
    private String paymentStatus; // cho_xac_nhan, da_thanh_toan, tu_choi

    @PropertyName("receiptUrl")
    private String paymentImage; // Trường lưu URL ảnh biên lai từ app

    private Long totalPrice;
    private Integer adults;
    private Integer children;
    private Integer infants;
    
    // Trip management fields
    private String tripStatus; // preparing, started, completed, cancelled
    private String startTime;
    private String endTime;
    private String guideId; // Legacy field for single guide
    private List<String> guideIds = new ArrayList<>(); // For multiple staff/guides
    private String tripNote;
    
    @Exclude
    private Date createdAt;
    
    @Exclude
    private Map<String, Object> tour;
    
    @Exclude
    private Map<String, Object> guide; // Legacy single guide data
    
    @Exclude
    private List<Map<String, Object>> guidesList = new ArrayList<>(); // Multiple guides data

    public Booking() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @PropertyName("paymentMethod")
    public String getPaymentMethod() { return paymentMethod; }
    @PropertyName("paymentMethod")
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @PropertyName("paymentStatus")
    public String getPaymentStatus() { 
        return paymentStatus != null ? paymentStatus : "cho_xac_nhan"; 
    }
    @PropertyName("paymentStatus")
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    @PropertyName("receiptUrl")
    public String getPaymentImage() { return paymentImage; }
    @PropertyName("receiptUrl")
    public void setPaymentImage(String paymentImage) { this.paymentImage = paymentImage; }

    public Long getTotalPrice() { return totalPrice != null ? totalPrice : 0L; }
    public void setTotalPrice(Long totalPrice) { this.totalPrice = totalPrice; }
    public Integer getAdults() { return adults; }
    public void setAdults(Integer adults) { this.adults = adults; }
    public Integer getChildren() { return children; }
    public void setChildren(Integer children) { this.children = children; }
    public Integer getInfants() { return infants; }
    public void setInfants(Integer infants) { this.infants = infants; }
    
    public String getTripStatus() { return tripStatus != null ? tripStatus : "preparing"; }
    public void setTripStatus(String tripStatus) { this.tripStatus = tripStatus; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public String getGuideId() { return guideId; }
    public void setGuideId(String guideId) { this.guideId = guideId; }
    
    public List<String> getGuideIds() { 
        if (guideIds == null) guideIds = new ArrayList<>();
        return guideIds; 
    }
    public void setGuideIds(List<String> guideIds) { this.guideIds = guideIds; }
    
    public String getTripNote() { return tripNote; }
    public void setTripNote(String tripNote) { this.tripNote = tripNote; }

    @Exclude
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    @Exclude
    public Map<String, Object> getTour() { return tour; }
    public void setTour(Map<String, Object> tour) { this.tour = tour; }
    
    @Exclude
    public Map<String, Object> getGuide() { return guide; }
    public void setGuide(Map<String, Object> guide) { this.guide = guide; }

    @Exclude
    public List<Map<String, Object>> getGuidesList() { return guidesList; }
    @Exclude
    public void setGuidesList(List<Map<String, Object>> guidesList) { this.guidesList = guidesList; }
}
