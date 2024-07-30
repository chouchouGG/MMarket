package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.learn.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.learn.types.common.Constants.RuleModel.RULE_LOCK;
import static cn.learn.types.enums.ResponseCode.PARSE_ERROR;

/**
 * @program: MMarket
 * @description: 奖品解锁节点
 * @author: chouchouGG
 * @create: 2024-06-20 17:23
 **/
@Slf4j
@Component(value = RULE_LOCK)
public class RuleLockNode implements ILogicTreeNode {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public void execute(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            return;
        }
        log.info("用户【{}】，参与抽奖活动【{}】，进行【{}】", context.getUserId(), context.getStrategyId(), RULE_LOCK);

        String userId = context.getUserId();
        Long strategyId = context.getStrategyId();
        Integer awardId = context.getAwardId();


        // 1. 获取数据库中配置的解锁次数
        // fixme：后期可以将奖品的解锁次数缓存到 reids 中，可以减少查表的次数，修改 queryStrategyRuleValue 方法，添加走缓存的逻辑
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, awardId, RULE_LOCK);
        long unlockThreshold = 0L;
        try {
            unlockThreshold = Long.parseLong(ruleValue);
        } catch (NumberFormatException e) {
            String errorMessage = String.format("解析奖品解锁阈值失败，规则值: %s", ruleValue);
            throw new AppException(PARSE_ERROR.getCode(), errorMessage, e);
        }

        // 2. 获取用户当日已经参与的抽奖次数
        Integer userRaffleCount = strategyRepository.queryTodayUserRaffleCount(userId, strategyId);

        // 2. 检查解锁次数
        context.setRuleModel(RULE_LOCK);
        if (userRaffleCount >= unlockThreshold) {
            log.info("规则过滤-次数锁【放行】 userId:{} strategyId:{} awardId:{} raffleCount:{} userRaffleCount:{}", userId, strategyId, awardId, userRaffleCount, userRaffleCount);
            // 放行：用户抽奖次数 >= 奖品解锁的阈值
            context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
            context.setResultDesc(String.format("奖品解锁条件达成，用户抽奖次数:%d >= 奖品解锁的阈值:%d", userRaffleCount, unlockThreshold));
        } else {
            log.info("规则过滤-次数锁【拦截】 userId:{} strategyId:{} awardId:{} raffleCount:{} userRaffleCount:{}", userId, strategyId, awardId, userRaffleCount, userRaffleCount);
            // 终止：用户抽奖次数 < 奖品解锁的阈值
            context.setStatus(ProcessingContext.ProcessStatus.TERMINATED);
            context.setResultDesc(String.format("奖品解锁条件未达成，用户抽奖次数:%d < 奖品解锁的阈值:%d", userRaffleCount, unlockThreshold));
            context.setNeedsFallbackAward(true);
        }

        log.info("抽奖决策树-【奖品解锁节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), awardId, context.getStatus(), context.getResultDesc());
        return;
    }

}
