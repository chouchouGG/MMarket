package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.StrategyRulePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-05-29 14:26
 **/
@Mapper
public interface IStrategyRuleDao {

    /**
     * note：规则分为两种：（数据库中通过 ‘rule_type’ 进行区分）
     *  1. ’策略规则‘；
     *  2. ’奖品规则‘
     */

//    List<StrategyRulePO> queryStrategyRuleList();


    /**
     * 获取策略规则
     */
    StrategyRulePO queryStrategyRule(StrategyRulePO strategyRule);

    /**
     * 通过策略ID、奖品ID、规则模型查询到规则配置的值
     */
    String queryStrategyRuleValue(StrategyRulePO strategyRule);

    /**
     * 获取奖品规则——解锁规则的解锁次数。
     */
    List<StrategyRulePO> queryAwardRuleLockCount(Long strategyId);
}
