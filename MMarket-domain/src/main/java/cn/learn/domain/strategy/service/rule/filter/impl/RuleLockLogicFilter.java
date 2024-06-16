package cn.learn.domain.strategy.service.rule.filter.impl;

import cn.learn.domain.strategy.model.entity.RuleActionEntity;
import cn.learn.domain.strategy.model.entity.RuleMatterEntity;
import cn.learn.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.annotation.LogicStrategy;
import cn.learn.domain.strategy.service.rule.filter.ILogicFilter;
import cn.learn.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: MMarket
 * @description: 用户抽奖n次后，对应奖品可解锁抽奖
 * @author: chouchouGG
 * @create: 2024-06-14 22:42
 **/
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_LOCK)
public class RuleLockLogicFilter implements ILogicFilter<RuleActionEntity.RaffleCenterEntity> {

    @Resource
    private IStrategyRepository repository;

    // fixme: 先给个【固定值】用户抽奖次数，后续完成这部分流程开发的时候，从数据库/Redis中读取
    private Long userRaffleCount = 0L;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleCenterEntity> filter(RuleMatterEntity ruleMatterEntity) {
        Long strategyId = ruleMatterEntity.getStrategyId();
        Integer awardId = ruleMatterEntity.getAwardId();
        String ruleModel = ruleMatterEntity.getRuleModel();

        log.info("规则过滤-次数：用户ID：{} 策略ID：{} 规则模型：{} 规则信息：{}",
                ruleMatterEntity.getUserId(),
                strategyId,
                ruleModel,
                DefaultLogicFactory.LogicModel.RULE_LOCK.getInfo());

        // 获取数据库中配置的奖品解锁规则模型的解锁次数
        String ruleValue = repository.queryStrategyRuleValue(strategyId, awardId, ruleModel);
        long unlockThreshold = Long.parseLong(ruleValue);

        if (userRaffleCount >= unlockThreshold) {
            // 用户抽奖次数 >= 奖品解锁的阈值，则放行
            log.info("用户抽奖次数:{} >= 奖品解锁的阈值:{}", userRaffleCount, unlockThreshold);
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        } else {
            // 用户抽奖次数 < 奖品解锁的阈值，则由规则引擎接管拦截
            log.info("用户抽奖次数:{} < 奖品解锁的阈值:{}", userRaffleCount, unlockThreshold);
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                    .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                    .build();
        }
    }
}
