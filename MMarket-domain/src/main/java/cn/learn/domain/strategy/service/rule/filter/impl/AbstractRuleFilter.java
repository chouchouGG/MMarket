package cn.learn.domain.strategy.service.rule.filter.impl;

import cn.learn.domain.strategy.model.entity.RuleActionEntity;
import cn.learn.domain.strategy.service.rule.filter.ILogicFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @program: MMarket
 * @description: note：抽象的规则过滤器，采用模板方法模式，将各种不同规则过滤器的过滤流程按步骤抽取出来。
 *
 * @author: chouchouGG
 * @create: 2024-06-15 10:15
 **/
@Slf4j
public abstract class AbstractRuleFilter implements ILogicFilter<RuleActionEntity.RaffleEntity> {

    // fixme：该类还未实现，之后可以使用【模板方法模式】进行优化。

}
