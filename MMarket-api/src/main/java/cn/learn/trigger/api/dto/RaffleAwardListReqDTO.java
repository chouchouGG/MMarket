package cn.learn.trigger.api.dto;

import lombok.Data;

/**
 * @program: MMarket
 * @description: 抽奖奖品列表，请求对象
 * @author: chouchouGG
 * @create: 2024-06-23 15:45
 **/
@Data
public class RaffleAwardListReqDTO {

    // note: 对外提供给用户的是活动，具体的策略ID不需要用户关系，故这里进行优化
/*    // 抽奖策略ID
    private Long strategyId; */

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 抽奖活动ID
     */
    private Long activityId;


}