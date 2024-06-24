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

    // 抽奖策略ID
    private Long strategyId;

}