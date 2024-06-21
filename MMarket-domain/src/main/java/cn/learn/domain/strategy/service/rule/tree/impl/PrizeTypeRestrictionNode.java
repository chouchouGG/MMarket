package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.learn.types.common.Constants.RuleModel.PRIZE_TYPE_RESTRICTION;

/**
 * @program: MMarket
 * @description: 奖品类型限制节点
 * @author: chouchouGG
 * @create: 2024-06-21 14:37
 **/
@Slf4j
@Component(value = PRIZE_TYPE_RESTRICTION)
public class PrizeTypeRestrictionNode implements ILogicTreeNode {
    @Override
    public void execute(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            return;
        }
        log.info("【奖品类型限制节点】-现阶段未编写具体逻辑，默认通过【Continue】");
        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
        context.setRuleModel(PRIZE_TYPE_RESTRICTION);
        return;
    }
}
