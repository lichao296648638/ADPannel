package com.neal.adpannel.entity;

import org.litepal.crud.DataSupport;

/**
 * 广告编辑事件时间戳实体类
 * Created by lichao on 17/5/11.
 */

public class EventEntity extends DataSupport{

    /**
     * time_stamp : 2017101010101
     */

    private String time_stamp;

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }
}
