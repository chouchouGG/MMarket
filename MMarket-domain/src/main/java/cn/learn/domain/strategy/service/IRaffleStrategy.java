package cn.learn.domain.strategy.service;

import cn.learn.domain.strategy.model.entity.RaffleAwardEntity;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;

/**
 * @program: MMarket
 * @description: 抽奖接口
 * @author: chouchouGG
 * @create: 2024-06-03 07:55
 **/
public interface IRaffleStrategy {
    /**
     * 执行抽奖；抽奖因子入参，执行抽奖计算，返回具体的奖品信息
     *
     * @param raffleFactorEntity 抽奖因子实体对象，根据入参信息计算抽奖结果
     * @return 抽奖的奖品
     */
    RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity);
}
