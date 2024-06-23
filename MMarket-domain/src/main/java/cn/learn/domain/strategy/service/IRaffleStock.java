package cn.learn.domain.strategy.service;

import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;

/**
 * @program: MMarket
 * @description: 抽奖库存相关服务，获取库存消耗队列
 * @author: chouchouGG
 * @create: 2024-06-22 17:40
 **/
public interface IRaffleStock {

    /**
     * 获取奖品库存消耗队列
     *
     * @return 奖品库存Key信息
     */
    StrategyAwardStockKeyVO takeQueueValue();


    /**
     * 更新奖品库存消耗记录
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     */
    void updateStrategyAwardStock(Long strategyId, Integer awardId);

}