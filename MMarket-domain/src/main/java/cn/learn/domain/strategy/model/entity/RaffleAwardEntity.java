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

    // 抽奖奖品ID - 内部流转使用
    private Integer awardId;

    // 奖品配置信息
    private String awardRuleValue;

    // 规则模型
    private String ruleModel;

    // 处理流程的结果描述，用于提供反馈
    private String resultDesc;

    // 排序
    private Integer sort;

}
