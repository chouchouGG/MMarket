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
    private String ruleModels;

    public boolean getRuleWeight() {
        if (ruleModels == null) {
            return false;
        }
        String[] rules = ruleModels.split(Constants.SPLIT);
        for (String rule : rules) {
            if (Constants.RuleModel.RULE_WEIGHT.equals(rule)) {
                return true;
            }
        }
        return false;
    }
}
