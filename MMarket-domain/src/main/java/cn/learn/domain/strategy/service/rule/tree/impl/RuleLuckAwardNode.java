package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.learn.types.common.Constants.RuleModel.RULE_LUCK_AWARD;

/**
 * @author: chouchouGG
 * @description 兜底奖励节点
 * @create: 2024-06-20 17:23
 */
@Slf4j
@Component(value = RULE_LUCK_AWARD)
public class RuleLuckAwardNode implements ILogicTreeNode {

    @Override
    public void execute(ProcessingContext context) {
        log.info("【兜底奖励节点】- 发放兜底奖品，默认终止【TERMINATED】");
        context.setAwardId(101);
        context.setRuleModel(RULE_LUCK_AWARD);
        context.setStatus(ProcessingContext.ProcessStatus.TERMINATED);
        return;
    }
}
