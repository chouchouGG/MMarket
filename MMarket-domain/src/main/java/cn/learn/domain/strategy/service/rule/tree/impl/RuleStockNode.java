package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: chouchouGG
 * @description 库存扣减节点
 * @create: 2024-06-20 17:23
 */
@Slf4j
@Component(value = Constants.RuleModel.RULE_STOCK)
public class RuleStockNode implements ILogicTreeNode {

    @Override
    public void execute(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            return;
        }

        log.info("【库存扣减节点】- 开始检查库存...");

        log.info("【库存扣减节点】- 现阶段未编写具体逻辑，默认通过【TERMINATED】，库存不够哦");

        context.setRuleModel(Constants.RuleModel.RULE_STOCK);
        context.setStatus(ProcessingContext.ProcessStatus.TERMINATED);

        log.info("【库存扣减节点】- ...库存检查结束");
        return;
    }
}
