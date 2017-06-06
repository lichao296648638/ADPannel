package com.neal.adpannel.entity;

/**
 * 电梯状态实体类
 * Created by lichao on 17/6/4.
 */

public class StatusEntity {


    /**
     * station : 32
     * status : 1
     * direction : 0
     * safety : 1
     * overload : 0
     * power : 1
     * brk : 0
     * temp : 25
     * hum : 50
     */

    private int station;
    private int status;
    private int direction;
    private int safety;
    private int overload;
    private int power;
    private int brk;
    private int temp;
    private int hum;

    public int getStation() {
        return station;
    }

    public void setStation(int station) {
        this.station = station;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getSafety() {
        return safety;
    }

    public void setSafety(int safety) {
        this.safety = safety;
    }

    public int getOverload() {
        return overload;
    }

    public void setOverload(int overload) {
        this.overload = overload;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getBrk() {
        return brk;
    }

    public void setBrk(int brk) {
        this.brk = brk;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getHum() {
        return hum;
    }

    public void setHum(int hum) {
        this.hum = hum;
    }
}
