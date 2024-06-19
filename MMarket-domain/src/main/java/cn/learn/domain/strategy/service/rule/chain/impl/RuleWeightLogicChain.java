package cn.learn.domain.strategy.service.rule.chain.impl;

import cn.learn.domain.strategy.model.entity.LogicChainContext;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.armory.IStrategyDispatch;
import cn.learn.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @program: MMarket
 * @description: 权重责任链节点
 * @author: chouchouGG
 * @create: 2024-06-16 09:39
 **/
@Slf4j
@Component(value = Constants.RuleModel.RULE_WEIGHT)
public class RuleWeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository repository;

    @Resource
    protected IStrategyDispatch strategyDispatch;

    // fixme: 当前测试阶段使用的是固定值,根据用户ID查询用户抽奖消耗的积分值，本章节我们先写死为固定的值。后续需要从数据库中查询。
    private final Long userScore = 0L;

    /**
     * 权重责任链过滤；
     * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:108,109
     * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
     */
    @Override
    public LogicChainContext handle(LogicChainContext context) {
        Long strategyId = context.getStrategyId();
        String userId = context.getUserId();
        String ruleModelName = getRuleModelName();

        log.info("抽奖责任链-权重开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModelName);

        String ruleValue = repository.queryStrategyRuleValue(strategyId, null, ruleModelName);

        // 1. 解析权重规则值 4000:102,103,104,105 拆解为；4000 -> 4000:102,103,104,105 便于比对判断
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);
        if (null == analyticalValueGroup || analyticalValueGroup.isEmpty()) {
            log.info("【策略{}】的权重规则配置信息为空，请检查数据库配置。", strategyId);
            context.setStatus(LogicChainContext.ProcessStatus.TERMINATED);
            return context;
        }

        // 2. 转换Keys值，并默认排序
        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);

        // 3. 找到不超过 userScore 的最大值存储在 prevValue中，也就是【4500 积分，能找到 4000:102,103,104,105】
        Long prevValue = null;
        for (Long score : analyticalSortedKeys) {
            if (userScore < score) {
                break;
            }
            prevValue = score;
        }
        log.info("当前用户的积分为{}，将采取权重规则：{}", userScore, analyticalValueGroup.get(prevValue));

        // 4. 权重抽奖
        if (null != prevValue) {
            Integer awardId = strategyDispatch.getRandomAwardId(strategyId, analyticalValueGroup.get(prevValue));
            log.info("抽奖责任链-【权重节点】 userId: {} strategyId: {} ruleModel: {} awardId: {}",
                    userId, strategyId, ruleModelName, awardId);
            // fixme: 权重抽中了也应该继续后续过滤流程（解锁、兜底...），而xfg在这里直接进行了返回
            context.setStatus(LogicChainContext.ProcessStatus.TERMINATED);
            context.setAwardId(awardId);
            return context;
        }

        // 5. 过滤其他责任链，（后续有默认抽奖规则过滤节点进行默认抽奖）
        log.info("抽奖责任链-【权重节点】 userId: {} strategyId: {} ruleModel: {}",
                userId, strategyId, ruleModelName);
        context.setStatus(LogicChainContext.ProcessStatus.CONTINUE);
        return context;
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
                throw new IllegalArgumentException("权重规则配置信息有误，请检查数据库配置。" + ruleValueKey);
            }
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
        }
        return ruleValueMap;
    }
}
