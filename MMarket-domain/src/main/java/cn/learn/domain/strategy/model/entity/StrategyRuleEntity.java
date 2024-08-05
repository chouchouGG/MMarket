package cn.learn.domain.strategy.model.entity;

import cn.learn.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: MMarket
 * @description: 策略规则实体
 * @author: chouchouGG
 * @create: 2024-05-31 20:17
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleEntity {
    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖奖品ID【规则类型为策略，则不需要奖品ID】 */
    private Integer awardId;
    /** 抽象规则类型；1-策略规则、2-奖品规则 */
    private Integer ruleType;
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】 */
    private String ruleModel;
    /** 抽奖规则比值 */
    private String ruleValue;
    /** 抽奖规则描述 */
    private String ruleDesc;


    /**
     * 解析并获取权重值，数据案例；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     * @return 返回解析后的权重规则
     * 返回的 Map 结构：{
     *                      '4000:102,103,104,105' : [102, 103, 104, 105],
     *                      '5000:102,103,104,105,106,107' : [102, 103, 104, 105, 106, 107],
     *                      '6000:102,103,104,105,106,107,108,109' : [102, 103, 104, 105, 106, 107, 108, 109]
     *               }
     */
    public Map<String, List<Integer>> getRuleWeightItem() {
        // 1. 检查 ruleModel 是否为 "rule_weight"，如果不是则返回 null
        if (!"rule_weight".equals(ruleModel)) {
            return null;
        }

        // 2. 按空格拆分 ruleValue 为多个 ruleValueGroup
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<String, List<Integer>> resultMap = new HashMap<>();

        // 3. 循环解析所有权重规则
        for (String ruleValueGroup : ruleValueGroups) {
            // 4. 参数校验，如果 ruleValueGroup 为空或 null，直接返回当前的 resultMap
            if (ruleValueGroup == null || ruleValueGroup.isEmpty()) {
                return resultMap;
            }

            // 5. 解析单个规则 '4000:102,103,104,105'，获取 [102, 103, 104, 105]
            List<Integer> values = parseRuleValueGroup(ruleValueGroup);

            // 6. 将键 '4000:102,103,104,105' 和值 [102, 103, 104, 105] 放入 resultMap
            resultMap.put(ruleValueGroup, values);
        }

        // 7. 返回结果
        return resultMap;
    }

    /**
     * 解析单个规则字符串，将其转换为整数列表
     *
     * @param ruleValueGroup 对应 '4000:102,103,104,105' 这样一组数据
     * @return 返回解析后的结果，是一个 list，其中包含 102, 103, 104, 105
     */
    private static List<Integer> parseRuleValueGroup(String ruleValueGroup) {
        // 1. 以冒号拆分 ruleValueGroup，确保其格式正确
        String[] parts = ruleValueGroup.split(Constants.COLON);
        if (parts.length != 2) {
            throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueGroup);
        }

        // 2. 以逗号拆分 parts[1]，得到每个具体的值
        String[] valueStrings = parts[1].split(Constants.SPLIT);
        List<Integer> values = new ArrayList<>();

        // 3. 遍历所有的值字符串，将其转换为整数并添加到 values 列表中
        for (String valueString : valueStrings) {
            values.add(Integer.parseInt(valueString));
        }

        // 4. 返回解析后的整数列表
        return values;
    }
}
