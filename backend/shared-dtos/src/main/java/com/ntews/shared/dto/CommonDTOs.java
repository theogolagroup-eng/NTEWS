package com.ntews.shared.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Shared DTOs to reduce code duplication across services
 * Optimized for immutability and thread safety
 */
public class CommonDTOs {
    
    // Base Response DTO - Immutable
    public static class ApiResponse<T> {
        private final String status;
        private final String message;
        private final T data;
        private final LocalDateTime timestamp;
        
        public ApiResponse() {
            this.status = "success";
            this.message = "Operation completed";
            this.data = null;
            this.timestamp = LocalDateTime.now();
        }
        
        public ApiResponse(String status, String message, T data) {
            this.status = Objects.requireNonNull(status, "Status cannot be null");
            this.message = Objects.requireNonNull(message, "Message cannot be null");
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }
        
        // Static factory methods
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>("success", "Operation completed", data);
        }
        
        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>("success", message, data);
        }
        
        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>("error", message, null);
        }
        
        // Getters only - immutable
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public T getData() { return data; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ApiResponse<?> that = (ApiResponse<?>) o;
            return Objects.equals(status, that.status) &&
                   Objects.equals(message, that.message) &&
                   Objects.equals(data, that.data);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(status, message, data);
        }
    }
    
    // Dashboard Summary Base - Optimized
    public static class DashboardSummaryBase {
        private final int totalItems;
        private final int activeItems;
        private final int criticalItems;
        private final int highItems;
        private final int mediumItems;
        private final int lowItems;
        private final LocalDateTime lastUpdated;
        
        public DashboardSummaryBase() {
            this.totalItems = 0;
            this.activeItems = 0;
            this.criticalItems = 0;
            this.highItems = 0;
            this.mediumItems = 0;
            this.lowItems = 0;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public DashboardSummaryBase(int totalItems, int activeItems, int criticalItems, 
                                 int highItems, int mediumItems, int lowItems) {
            if (totalItems < 0 || activeItems < 0 || criticalItems < 0 || 
                highItems < 0 || mediumItems < 0 || lowItems < 0) {
                throw new IllegalArgumentException("Item counts cannot be negative");
            }
            
            this.totalItems = totalItems;
            this.activeItems = activeItems;
            this.criticalItems = criticalItems;
            this.highItems = highItems;
            this.mediumItems = mediumItems;
            this.lowItems = lowItems;
            this.lastUpdated = LocalDateTime.now();
        }
        
        // Getters only - immutable
        public int getTotalItems() { return totalItems; }
        public int getActiveItems() { return activeItems; }
        public int getCriticalItems() { return criticalItems; }
        public int getHighItems() { return highItems; }
        public int getMediumItems() { return mediumItems; }
        public int getLowItems() { return lowItems; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        
        // Calculated properties
        public double getActivePercentage() {
            return totalItems > 0 ? (double) activeItems / totalItems * 100 : 0.0;
        }
        
        public int getHighPriorityTotal() {
            return criticalItems + highItems;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DashboardSummaryBase that = (DashboardSummaryBase) o;
            return totalItems == that.totalItems &&
                   activeItems == that.activeItems &&
                   criticalItems == that.criticalItems &&
                   highItems == that.highItems &&
                   mediumItems == that.mediumItems &&
                   lowItems == that.lowItems;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(totalItems, activeItems, criticalItems, highItems, mediumItems, lowItems);
        }
    }
    
    // Count by Category - Optimized
    public static class CategoryCount {
        private final String category;
        private final int count;
        
        public CategoryCount() {
            this.category = "";
            this.count = 0;
        }
        
        public CategoryCount(String category, int count) {
            this.category = Objects.requireNonNull(category, "Category cannot be null");
            if (count < 0) {
                throw new IllegalArgumentException("Count cannot be negative");
            }
            this.count = count;
        }
        
        // Getters only - immutable
        public String getCategory() { return category; }
        public int getCount() { return count; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CategoryCount that = (CategoryCount) o;
            return Objects.equals(category, that.category) && count == that.count;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(category, count);
        }
    }
    
    // Recent Item - Optimized
    public static class RecentItem {
        private final String id;
        private final String title;
        private final String severity;
        private final String status;
        private final LocalDateTime timestamp;
        private final String location;
        
        public RecentItem() {
            this.id = "";
            this.title = "";
            this.severity = "medium";
            this.status = "active";
            this.timestamp = LocalDateTime.now();
            this.location = "";
        }
        
        public RecentItem(String id, String title, String severity, String status, 
                        LocalDateTime timestamp, String location) {
            this.id = Objects.requireNonNull(id, "ID cannot be null");
            this.title = Objects.requireNonNull(title, "Title cannot be null");
            this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
            this.status = Objects.requireNonNull(status, "Status cannot be null");
            this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
            this.location = location != null ? location : "";
        }
        
        // Getters only - immutable
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getSeverity() { return severity; }
        public String getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getLocation() { return location; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecentItem that = (RecentItem) o;
            return Objects.equals(id, that.id) &&
                   Objects.equals(title, that.title) &&
                   Objects.equals(severity, that.severity) &&
                   Objects.equals(status, that.status) &&
                   Objects.equals(timestamp, that.timestamp) &&
                   Objects.equals(location, that.location);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(id, title, severity, status, timestamp, location);
        }
    }
    
    // Trend Data
    public static class TrendData {
        private String date;
        private int count;
        private double value;
        private double average;
        
        public TrendData() {}
        
        public TrendData(String date, int count, double value) {
            this.date = date;
            this.count = count;
            this.value = value;
        }
        
        public TrendData(String date, int count, double value, double average) {
            this(date, count, value);
            this.average = average;
        }
        
        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public double getAverage() { return average; }
        public void setAverage(double average) { this.average = average; }
    }
    
    // Location Data
    public static class LocationData {
        private String id;
        private String name;
        private String latitude;
        private String longitude;
        private String address;
        
        public LocationData() {}
        
        public LocationData(String id, String name, String latitude, String longitude) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
}
