package com.qhiehome.ihome.bean;

public class PublishBean {

    private String parkingId;

    private String startTime;

    private String endTime;

    public PublishBean(String parkingId, String startTime, String endTime) {
        this.parkingId = parkingId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getParkingId() {
        return parkingId;
    }

    public void setParkingId(String parkingId) {
        this.parkingId = parkingId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}