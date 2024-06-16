package cn.learn.domain.strategy.service.rule.filter.impl;

import cn.learn.domain.strategy.model.entity.RuleActionEntity;
import cn.learn.domain.strategy.model.entity.RuleMatterEntity;
import cn.learn.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.annotation.LogicStrategy;
import cn.learn.domain.strategy.service.rule.filter.ILogicFilter;
import cn.learn.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @program: MMarket
 * @description: 权重规则过滤器 ---> 已重构
 * @author: chouchouGG
 * @create: 2024-06-14 13:12
 **/

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_WIGHT)
@Deprecated
public class RuleWeightLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource
    private IStrategyRepository repository;

    // fixme: 当前测试阶段使用的是固定值
    private final Long userScore = 0L;

/**
     * 权重规则过滤；
     * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
     *
     * @param ruleMatterEntity 规则物料实体对象
     * @return 规则过滤结果
 **/


    @Override
    public RuleActionEntity filter(RuleMatterEntity ruleMatterEntity) {
        String userId = ruleMatterEntity.getUserId();
        Long strategyId = ruleMatterEntity.getStrategyId();
        Integer awardId = ruleMatterEntity.getAwardId();
        String ruleModel = ruleMatterEntity.getRuleModel();

        log.info("规则过滤-权重 userId:{} strategyId:{} ruleModel:{} info:{}",
                userId, strategyId, ruleModel, DefaultLogicFactory.LogicModel.RULE_WIGHT.getInfo());

        String ruleValue = repository.queryStrategyRuleValue(strategyId, awardId, ruleModel);

        // 1. fixme 根据用户ID查询用户抽奖消耗的积分值，本章节我们先写死为【固定的值 4500】。后续需要从数据库中查询。
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);
        // 如果解析后的 Map 为空，则返回一个设置为【放行】的规则行为实体对象 RuleActionEntity 。
        if (null == analyticalValueGroup || analyticalValueGroup.isEmpty()) {
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }

        // 2. 转换Keys值，并默认排序
        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);

        // 3. 找到不超过 userScore 的最大值存储在 prevValue中，也就是【4500 积分，能找到 4000:102,103,104,105】
        Long prevValue = null;
        for (Long score : analyticalSortedKeys) {
            if (score > userScore) {
                break;
            }
            prevValue = score;
        }

        // 获取类注解中的 logicMode 属性
        LogicStrategy logicStrategy = this.getClass().getAnnotation(LogicStrategy.class);
        DefaultLogicFactory.LogicModel logicModel = logicStrategy.logicMode();

        if (null != prevValue) {
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                    .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                    .ruleModel(logicModel.getCode())
                    .data(RuleActionEntity.RaffleBeforeEntity.builder()
                            .strategyId(strategyId)
                            .ruleWeightValueKey(analyticalValueGroup.get(prevValue))
                            .build())
                    .build();
        }

        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();
    }

    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : ruleValueGroups) {
            // 检查输入是否为空
            if (ruleValueKey == null || ruleValueKey.isEmpty()) {
                return ruleValueMap;
            }
            // 分割字符串以获取键和值
            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("规则模型rule_weight中的rule_value是无效的输入格式：" + ruleValueKey);
            }
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
        }
        return ruleValueMap;
    }
}
