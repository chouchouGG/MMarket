package cn.learn.trigger.api;

import cn.learn.trigger.api.dto.ActivityDrawReqDTO;
import cn.learn.trigger.api.dto.ActivityDrawResDTO;
import cn.learn.types.model.Response;

/**
 *
 **/
public interface IRaffleActivityService {

    /**
     * 抽奖活动装配和抽奖策略装配，总的装配逻辑
     */
    Response<Boolean> armory(Long activityId);

    /**
     * 用户进行活动抽奖的接口
     */
    Response<ActivityDrawResDTO> draw(ActivityDrawReqDTO request);

}
