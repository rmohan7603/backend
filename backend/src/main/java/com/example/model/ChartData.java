package com.example.model;

public class ChartData {
    private int userId;
    private int usageValue;
    
    public ChartData() {}
    public ChartData(int userId, int usageValue) {
        this.userId = userId;
        this.usageValue = usageValue;
    }

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUsageValue() {
		return usageValue;
	}

	public void setUsageValue(int usageValue) {
		this.usageValue = usageValue;
	}
}