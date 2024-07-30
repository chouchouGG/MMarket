package cn.learn.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: MMarket
 * @description: 抽奖奖品实体，不做奖品发放，记录最终返回给调用方拿到的是什么奖品
 * @author: chouchouGG
 * @create: 2024-06-13 14:24
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardEntity {

    /**
     * 奖品ID
     */
    private Integer awardId;

    /**
     * 抽奖奖品标题
     */
    private String awardTitle;

    /**
     * 奖品配置信息
     */
    private String awardConfig;

    /**
     * 奖品顺序号
     */
    private Integer sort;

}
