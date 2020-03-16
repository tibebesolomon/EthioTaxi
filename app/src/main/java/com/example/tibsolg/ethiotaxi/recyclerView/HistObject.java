package com.example.tibsolg.ethiotaxi.recyclerView;

/**
 * Created by manel on 10/10/2017.
 */

public class HistObject {
    private String rideId;
    private String time;

    public HistObject(String rideId, String time){
        this.rideId = rideId;
        this.time = time;
    }

    public String getRideId(){return rideId;}
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime(){return time;}
    public void setTime(String time) {
        this.time = time;
    }
}
