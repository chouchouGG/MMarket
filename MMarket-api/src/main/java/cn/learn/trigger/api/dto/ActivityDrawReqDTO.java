package cn.learn.trigger.api.dto;

import lombok.Data;

/**
 * <h1>活动抽奖的请求对象</h1>
 */
@Data
public class ActivityDrawReqDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}