package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.learn.types.common.Constants.RuleModel.RULE_LOCK;

/**
 * @program: MMarket
 * @description: 奖品解锁节点
 * @author: chouchouGG
 * @create: 2024-06-20 17:23
 **/
@Slf4j
@Component(value = RULE_LOCK)
public class RuleLockNode implements ILogicTreeNode {

    @Override
    public void execute(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            return;
        }
        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
        context.setRuleModel(RULE_LOCK);
        context.setResultDesc("奖品解锁条件达成，通过解锁节点 - 现阶段未编写具体逻辑，默认通过【Continue】");

        log.info("抽奖决策树-【奖品解锁节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }

}
