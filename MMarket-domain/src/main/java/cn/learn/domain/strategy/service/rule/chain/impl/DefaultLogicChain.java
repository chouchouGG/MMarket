package cn.learn.domain.strategy.service.rule.chain.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.armory.IStrategyDispatch;
import cn.learn.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: MMarket
 * @description: 默认规则的责任链节点（兜底规则）
 * @author: chouchouGG
 * @create: 2024-06-16 09:40
 **/
@Slf4j
@Component(value = Constants.RuleModel.DEFAULT)
public class DefaultLogicChain extends AbstractLogicChain {

    @Resource
    IStrategyDispatch strategyDispatch;

    @Override
    public ProcessingContext handle(ProcessingContext context) {
        Long strategyId = context.getStrategyId();
        String userId = context.getUserId();
        String ruleModelName = getRuleModelName();

        // 默认抽奖方式
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);
        context.setAwardId(awardId);
        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);

        log.info("抽奖责任链-【默认处理节点】 userId: {} strategyId: {} ruleModel: {} awardId: {}",
                userId, strategyId, ruleModelName, awardId);
        return context;
    }
}
