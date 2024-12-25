package com.example.model;

public class UsageData {
    private String userId;
    private int usageValue;
    private long epoch;

    public UsageData() {}

    public UsageData(String userId, int usageValue, long epoch) {
        this.userId = userId;
        this.usageValue = usageValue;
        this.epoch = epoch;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getUsageValue() {
        return usageValue;
    }

    public void setUsageValue(int usageValue) {
        this.usageValue = usageValue;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }
}