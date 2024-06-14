package cn.learn.domain.strategy.service.rule;

import cn.learn.domain.strategy.model.entity.RuleActionEntity;
import cn.learn.domain.strategy.model.entity.RuleMatterEntity;

/**
 * @program: MMarket
 * @description: 抽奖规则的过滤接口
 * @author: chouchouGG
 * @create: 2024-06-13 14:42
 **/
public interface ILogicFilter<T extends RuleActionEntity.RaffleEntity> {
    RuleActionEntity<T> filter(RuleMatterEntity ruleMatterEntity);
}
