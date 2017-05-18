package com.neal.adpannel.interf;


import com.neal.adpannel.entity.AdEntity;

import java.util.List;

/**
 * 切换广告接口
 * Created by lichao on 17/5/12.
 */

public interface onSwitchAd {
    /**
     * 切换广告
     * @param singleList 广告组
     * @param index 广告下标
     */
    void onSwitch(List<AdEntity> singleList, int index);
}
