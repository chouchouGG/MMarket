package cn.learn.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @program: MMarket
 * @description: 策略奖品实体类
 * @author: chouchouGG
 * @create: 2024-05-30 15:20
 **/
@Data
@Builder // 用于为类生成建造者模式相关的代码
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardEntity {

    // 抽奖策略ID
    private Long strategyId;

    // 抽奖奖品ID
    private Integer awardId;

    // 抽奖奖品标题
    private String awardTitle;

    // 抽奖奖品副标题
    private String awardSubtitle;

    // 奖品库存总量
    private Integer awardCount;

    // 奖品库存剩余
    private Integer awardCountSurplus;

    // 奖品中奖概率
    private BigDecimal awardRate;

    // 排序
    private Integer sort;

}
