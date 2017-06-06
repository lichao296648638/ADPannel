package com.neal.adpannel.interf;

import com.neal.adpannel.entity.StatusEntity;

/**
 * 电梯状态更新接口
 * Created by lichao on 17/6/4.
 */

public interface onStatusChangedListener {
    /**
     * 更新电梯状态
     * @param status 状态信息
     */
    void update(StatusEntity status);
}
