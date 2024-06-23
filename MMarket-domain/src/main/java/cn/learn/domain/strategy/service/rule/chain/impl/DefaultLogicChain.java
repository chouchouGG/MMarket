package cn.learn.domain.strategy.service.rule.chain.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.armory.IStrategyDispatch;
import cn.learn.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.learn.domain.strategy.model.entity.ProcessingContext.ProcessStatus.CONTINUE;
import static cn.learn.types.common.Constants.RuleModel.DEFAULT;

/**
 * @program: MMarket
 * @description: 默认规则的责任链节点（兜底规则）
 * @author: chouchouGG
 * @create: 2024-06-16 09:40
 **/
@Slf4j
@Component(value = DEFAULT)
public class DefaultLogicChain extends AbstractLogicChain {

    @Resource
    IStrategyDispatch strategyDispatch;

    @Override
    public void handle(ProcessingContext context) {
        Long strategyId = context.getStrategyId();
        String userId = context.getUserId();
        String ruleModelName = getRuleModelName();

        log.info("用户【{}】，参与抽奖活动【{}】，进行【{}】", userId, strategyId, ruleModelName);

        // 默认抽奖方式
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);
        context.setAwardId(awardId);
        context.setStatus(CONTINUE);
        context.setRuleModel(ruleModelName);
        context.setResultDesc("用户参与默认抽奖规则，从默认的奖品规则中抽取奖品");
        log.info("抽奖责任链-【默认抽奖节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }
}
