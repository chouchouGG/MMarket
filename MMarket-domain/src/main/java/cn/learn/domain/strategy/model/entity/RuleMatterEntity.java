package cn.learn.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: MMarket
 * @description: 规则物料实体类，保存过滤规则处理过程中的必要参数信息。
 * @author: chouchouGG
 * @create: 2024-06-13 14:43
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleMatterEntity {

    /** 用户ID */
    private String userId;
    /** 策略ID */
    private Long strategyId;
    /** 抽奖奖品ID */
    private Integer awardId;
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】 */
    private String ruleModel;

}

