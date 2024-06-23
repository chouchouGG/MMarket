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

    // fixme: 先给个【固定值】用户抽奖次数，后续完成这部分流程开发的时候，从MySQL/Redis中读取
    private Long userRaffleCount = 0L;

    @Override
    public void execute(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            return;
        }
        log.info("用户【{}】，参与抽奖活动【{}】，进行【{}】", context.getUserId(), context.getStrategyId(), RULE_LOCK);


        // 1. 获取数据库中配置的解锁次数
        // fixme：通过将奖品的解锁次数缓存到reids中，可以减少查表的次数，修改queryStrategyRuleValue方法，添加走缓存的逻辑
        String ruleValue = strategyRepository.queryStrategyRuleValue(context.getStrategyId(), context.getAwardId(), RULE_LOCK);
        long unlockThreshold = 0L;
        try {
            unlockThreshold = Long.parseLong(ruleValue);
        } catch (NumberFormatException e) {
            String errorMessage = String.format("解析奖品解锁阈值失败，规则值: %s", ruleValue);
            throw new AppException(PARSE_ERROR.getCode(), errorMessage, e);
        }

        // 2. 检查解锁次数
        context.setRuleModel(RULE_LOCK);
        if (userRaffleCount >= unlockThreshold) {
            // 放行：用户抽奖次数 >= 奖品解锁的阈值
            context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
            context.setResultDesc(String.format("奖品解锁条件达成，用户抽奖次数:%d >= 奖品解锁的阈值:%d", userRaffleCount, unlockThreshold));
        } else {
            // 终止：用户抽奖次数 < 奖品解锁的阈值
            context.setStatus(ProcessingContext.ProcessStatus.TERMINATED);
            context.setResultDesc(String.format("奖品解锁条件未达成，用户抽奖次数:%d < 奖品解锁的阈值:%d", userRaffleCount, unlockThreshold));
            context.setNeedsFallbackAward(true);
        }

        log.info("抽奖决策树-【奖品解锁节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }

}
