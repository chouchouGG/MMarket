package cn.learn.domain.strategy.model.entity;

import cn.learn.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: MMarket
 * @description: 策略实体类
 * @author: chouchouGG
 * @create: 2024-05-31 19:00
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyEntity {
    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖策略描述 */
    private String strategyDesc;
    /** 规则模型 */
    private String ruleModels; // note：策略中配置的规则都是前置规则

    public String[] getSeperatedRuleModels() {
        if (ruleModels == null || ruleModels.isEmpty()) {
            return null;
        }
        return ruleModels.split(Constants.SPLIT);
    }


    public boolean getRuleWeight() {
        String[] rules = this.getSeperatedRuleModels();
        if (rules == null) {
            return false;
        }
        for (String rule : rules) {
            if (Constants.RuleModel.RULE_WEIGHT.equals(rule)) {
                return true;
            }
        }
        return false;
    }
}
