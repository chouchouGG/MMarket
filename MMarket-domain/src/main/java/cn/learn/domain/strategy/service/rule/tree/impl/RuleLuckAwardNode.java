package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.learn.domain.strategy.model.entity.ProcessingContext.ProcessStatus.TERMINATED;
import static cn.learn.types.common.Constants.RuleModel.RULE_LUCK_AWARD;

/**
 * @author: chouchouGG
 * @description 兜底奖励节点
 * @create: 2024-06-20 17:23
 */
@Slf4j
@Component(value = RULE_LUCK_AWARD)
public class RuleLuckAwardNode implements ILogicTreeNode {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public void execute(ProcessingContext context) {
        // 如果不需要走兜底，则直接返回
        if (!context.isNeedsFallbackAward()) {
            log.info("不需要走兜底处理");
            return;
        }

        String ruleValue = strategyRepository.queryStrategyRuleValue(context.getStrategyId(), context.getAwardId(), RULE_LUCK_AWARD);
        String[] split = ruleValue.split(Constants.COLON);
        if (split.length == 0) {
            String errormessage = String.format("奖品ID为【%d】的兜底奖品未配置", context.getAwardId());
            throw new RuntimeException(errormessage);
        }
        // 兜底奖励配置
        Integer awardId = Integer.valueOf(split[0]);
        String awardRuleValue = split.length > 1 ? split[1] : "";
        // 返回兜底奖品
        context.setAwardId(awardId);
        context.setAwardRuleValue(awardRuleValue);
        context.setRuleModel(RULE_LUCK_AWARD);
        context.setStatus(TERMINATED);
        context.setResultDesc("发放兜底奖品，处理流程终止");

        log.info("抽奖决策树-【兜底奖励节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }
}
