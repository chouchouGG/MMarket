package cn.learn.domain.strategy.service.armory;

/**
 * @program: MMarket
 * @description: 抽奖策略调度接口（比如随机抽取一个奖品id）
 * @author: chouchouGG
 * @create: 2024-05-31 18:19
 **/
public interface IStrategyDispatch {
    /**
     * 获取抽奖策略装配的随机结果
     *
     * @param strategyId 策略ID
     * @return 抽奖结果
     */
    Integer getRandomAwardId(Long strategyId);
}