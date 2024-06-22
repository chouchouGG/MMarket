package cn.learn.domain.strategy.service.rule.chain.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.learn.types.common.Constants.RuleModel.RULE_BLACKLIST;

/**
 * @program: MMarket
 * @description: 黑名单责任链节点
 * @author: chouchouGG
 * @create: 2024-06-16 09:04
 **/
@Slf4j
@Component(value = RULE_BLACKLIST)
public class BlackListLogicChain extends AbstractLogicChain {

    @Resource
    IStrategyRepository repository;

    @Override
    public void handle(ProcessingContext context) {
        Long strategyId = context.getStrategyId();
        String userId = context.getUserId();
        String ruleModelName = getRuleModelName();

        log.info("用户【{}】，参与抽奖活动【{}】，进行【{}】", userId, strategyId, ruleModelName);

        // 查询规则值配置，（黑名单规则无需奖品ID）
        String ruleValue = repository.queryStrategyRuleValue(strategyId,null, ruleModelName);
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.parseInt(splitRuleValue[0]);

        // 黑名单抽奖判断
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String id : userBlackIds) {
            if (userId.equals(id)) {
                context.setAwardId(awardId);
                context.setStatus(ProcessingContext.ProcessStatus.TERMINATED);
                context.setRuleModel(RULE_BLACKLIST);
                context.setResultDesc("用户在黑名单中");
                log.info("抽奖责任链-【黑名单节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                        context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
                return;
            }
        }

        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
        context.setRuleModel(RULE_BLACKLIST);
        context.setResultDesc("用户不在黑名单中");
        log.info("抽奖责任链-【黑名单节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }
}
