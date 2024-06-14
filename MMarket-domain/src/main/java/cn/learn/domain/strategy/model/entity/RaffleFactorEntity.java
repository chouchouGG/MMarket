package cn.learn.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: MMarket
 * @description: 抽奖计算因子实体
 * @author: chouchouGG
 * @create: 2024-06-13 14:24
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleFactorEntity {
    /** 用户ID */
    private String userId;
    /** 策略ID */
    private Long strategyId;
}
