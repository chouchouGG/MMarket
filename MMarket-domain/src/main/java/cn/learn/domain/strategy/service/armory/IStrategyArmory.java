package cn.learn.domain.strategy.service.armory;

/**
 * @program: MMarket
 * @description: 装配器，抽奖策略概率的装配功能
 * @author: chouchouGG
 * @create: 2024-05-30 13:59
 **/

public interface IStrategyArmory {
    /**
     * 一个活动对应一种抽奖策略，抽奖策略概率的装配实现以空间换时间。
     *
     * @return
     */
    boolean assembleLotteryStrategy(Long strategyId);


}
