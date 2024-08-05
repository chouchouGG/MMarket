package cn.learn.domain.strategy.service;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.vo.RuleWeightVO;

import java.util.List;
import java.util.Map;

/**
 * <h1>抽奖规则接口；提供对规则的业务功能查询</h1>
 */
public interface IRaffleRule {

    /**
     * <p>查询奖品的解锁次数的配置「部分奖品需要抽奖N次解锁」</p>
     * <p>Map键值对为：[awardID, RuleLockCount]</p>
     */
    Map<Integer, Integer> queryAwardRuleLockCount(List<StrategyAwardEntity> strategyAwardEntities);

    /**
     * 查询奖品权重规则对应的奖品配置信息（通过活动ID，note：一般通过活动id查询的接口是直接关联到活动上的）
     */
    List<RuleWeightVO> queryAwardRuleWeightByActivityId(Long activityId);
}