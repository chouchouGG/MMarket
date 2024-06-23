package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.learn.types.common.Constants.RuleModel.PRIZE_TYPE_RESTRICTION;
import static cn.learn.types.common.Constants.RuleModel.RULE_DAILY_LIMIT;

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
        log.info("用户【{}】，参与抽奖活动【{}】，进行【{}】", context.getUserId(), context.getStrategyId(), PRIZE_TYPE_RESTRICTION);


        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
        context.setRuleModel(PRIZE_TYPE_RESTRICTION);
        context.setResultDesc("暂未实现，现阶段未编写具体逻辑，默认通过【Continue】");

        log.info("抽奖决策树-【奖品类型限制节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }
}
