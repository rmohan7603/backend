package com.example.model;

public class ChartData {
    private String label;
    private String userId;
    private int totalUsage;

    public ChartData(String label, String userId, int totalUsage) {
        this.label = label;
        this.userId = userId;
        this.totalUsage = totalUsage;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTotalUsage() {
        return totalUsage;
    }

    public void setTotalUsage(int totalUsage) {
        this.totalUsage = totalUsage;
    }
    
    public String toString() {
		return label+" "+userId+" "+totalUsage;
	}
}