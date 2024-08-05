package cn.learn.trigger.api.dto;

import lombok.Data;

/**
 * 抽奖策略规则，权重配置，查询N次抽奖可解锁奖品范围，请求对象
 */
@Data
public class RaffleStrategyRuleWeightReqDTO {

    /**
     * 用户ID
      */
    private String userId;

    /**
     * 抽奖活动ID
     */
    private Long activityId;

}