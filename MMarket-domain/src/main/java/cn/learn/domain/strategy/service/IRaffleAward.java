package cn.learn.domain.strategy.service;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

/**
 * @program: MMarket
 * @description: 抽奖策略奖品相关
 * @author: chouchouGG
 * @create: 2024-06-23 16:31
 **/
public interface IRaffleAward {

    /**
     * 根据策略ID查询抽奖奖品列表配置
     *
     * @param strategyId 策略ID
     * @return 奖品列表
     */
    List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId);

}
