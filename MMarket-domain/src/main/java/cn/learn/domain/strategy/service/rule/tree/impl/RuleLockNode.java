package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.learn.types.common.Constants.RuleModel.RULE_LOCK;

/**
 * @program: MMarket
 * @description: 次数锁节点
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
        log.info("【奖品解锁节点】-现阶段未编写具体逻辑，默认通过【Continue】");
        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
        context.setRuleModel(RULE_LOCK);
        return;
    }

}
