package cn.learn.domain.strategy.service.armory;

/**
 * @program: MMarket
 * @description: 抽奖策略调度接口（比如随机抽取一个奖品id）
 * @author: chouchouGG
 * @create: 2024-05-31 18:19
 **/
public interface IStrategyDispatch {
    /**
     * 基于【默认抽奖表】获取一个随机奖品
     *
     * @param strategyId 策略ID
     * @return 抽奖结果
     */
    Integer getRandomAwardId(Long strategyId);

    /**
     * 基于【幸运值抽奖表】获取一个随机奖品
     * @param strategyId
     * @param ruleWeightValue
     * @return
     */
    Integer getRandomAwardId(Long strategyId, String ruleWeightValue);

    /**
     * 根据策略ID和奖品ID，扣减奖品缓存库存
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 扣减结果
     */
    Boolean subtractionAwardStock(Long strategyId, Integer awardId);
}