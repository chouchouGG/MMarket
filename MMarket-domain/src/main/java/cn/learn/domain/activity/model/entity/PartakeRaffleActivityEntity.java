package cn.learn.domain.activity.model.entity;

import lombok.Data;

/**
 * 用户参与抽奖的实体对象
 */
@Data
public class PartakeRaffleActivityEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}