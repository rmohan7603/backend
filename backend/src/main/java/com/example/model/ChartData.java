package com.example.model;

public class ChartData {
    private String usageDate;
    private int usageValue;

    public ChartData() {}
    public ChartData(String usageDate, int usageValue) {
        this.usageDate = usageDate;
        this.usageValue = usageValue;
    }

    public String getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(String usageDate) {
        this.usageDate = usageDate;
    }

    public int getUsageValue() {
        return usageValue;
    }

    public void setUsageValue(int usageValue) {
        this.usageValue = usageValue;
    }
    
    public String toString() {
		return usageDate+" "+usageValue;
	}
}
