package cn.learn.domain.strategy.service.rule.chain.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: MMarket
 * @description: 黑名单责任链节点
 * @author: chouchouGG
 * @create: 2024-06-16 09:04
 **/
@Slf4j
@Component(value = Constants.RuleModel.RULE_BLACKLIST)
public class BlackListLogicChain extends AbstractLogicChain {

    @Resource
    IStrategyRepository repository;

    @Override
    public ProcessingContext handle(ProcessingContext context) {
        Long strategyId = context.getStrategyId();
        String userId = context.getUserId();
        String ruleModelName = getRuleModelName();

        log.info("抽奖责任链-黑名单开始 userId: {} strategyId: {} ruleModel: {}",
                userId, strategyId, ruleModelName);

        // 查询规则值配置，（黑名单规则无需奖品ID）
        String ruleValue = repository.queryStrategyRuleValue(strategyId,null, ruleModelName);
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.parseInt(splitRuleValue[0]);

        // 黑名单抽奖判断
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String id : userBlackIds) {
            if (userId.equals(id)) {
                context.setStatus(ProcessingContext.ProcessStatus.TERMINATED);
                context.setAwardId(awardId);
                log.info("抽奖责任链-【黑名单节点】 userId: {} strategyId: {} ruleModel: {} awardId: {}",
                        userId, strategyId, ruleModelName, awardId);
                return context;
            }
        }

        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
        log.info("抽奖责任链-【黑名单节点】 userId: {} strategyId: {} ruleModel: {} awardId: {}",
                userId, strategyId, ruleModelName, "没有在黑名单规则中");
        return context;
    }
}
