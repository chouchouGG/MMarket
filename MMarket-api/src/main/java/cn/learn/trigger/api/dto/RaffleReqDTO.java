package cn.learn.trigger.api.dto;

import lombok.Data;

/**
 * @program: MMarket
 * @description: 抽奖请求参数
 * @author: chouchouGG
 * @create: 2024-06-23 15:50
 **/
@Data
public class RaffleReqDTO {

    // 抽奖策略ID
    private Long strategyId;

}