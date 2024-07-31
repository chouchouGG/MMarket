package cn.learn.domain.strategy.service;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;
import java.util.Map;

/**
 * <h1>抽奖规则接口；提供对规则的业务功能查询</h1>
 */
public interface IRaffleRule {

    /**
     * 查询奖品的解锁次数的配置「部分奖品需要抽奖N次解锁」
     * Map键值对为：[awardID, RuleLockCount]
     */
    Map<Integer, Integer> queryAwardRuleLockCount(List<StrategyAwardEntity> strategyAwardEntities);

}