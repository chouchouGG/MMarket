package cn.learn.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @program: MMarket
 * @description: 抽奖规则
 * @author: chouchouGG
 * @create: 2024-05-29 14:11
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyRulePO {

    /** 自增ID */
    private Long id;
    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖奖品ID【规则类型为策略，则不需要奖品ID】 */
    private Integer awardId;
    /**
     * 抽象规则类型；1-策略规则、2-奖品规则
     * fixme：后期可以将策略规则和奖品规则的数据库表进行分割
     **/
    private Integer ruleType;
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】 */
    private String ruleModel;
    /** 抽奖规则比值 - 举例：如果是抽奖3次后解锁，这里就需要配置为3 */
    private String ruleValue;
    /** 抽奖规则描述 */
    private String ruleDesc;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
