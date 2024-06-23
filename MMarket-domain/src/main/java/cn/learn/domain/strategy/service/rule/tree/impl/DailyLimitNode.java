package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.learn.types.common.Constants.RuleModel.RULE_DAILY_LIMIT;

/**
 * @program: MMarket
 * @description: 每日限额节点
 * @author: chouchouGG
 * @create: 2024-06-21 11:55
 **/
@Slf4j
@Component(value = RULE_DAILY_LIMIT)
public class DailyLimitNode implements ILogicTreeNode {
    @Override
    public void execute(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            return;
        }
        log.info("用户【{}】，参与抽奖活动【{}】，进行【{}】", context.getUserId(), context.getStrategyId(), RULE_DAILY_LIMIT);


        // fixme: 用户中奖频率验证逻辑，暂未实现
        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
        context.setRuleModel(RULE_DAILY_LIMIT);
        context.setResultDesc("暂未实现，默认通过【CONTINUE】");


        log.info("抽奖决策树-【每日限额节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }

}
