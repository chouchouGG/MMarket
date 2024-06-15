package cn.learn.domain.strategy.respository;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.entity.StrategyEntity;
import cn.learn.domain.strategy.model.entity.StrategyRuleEntity;
import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;

import java.util.List;
import java.util.Map;

/**
 * @program: MMarket
 * @description: 策略的仓储接口
 * @author: chouchouGG
 * @create: 2024-05-30 14:44
 **/
public interface IStrategyRepository {

    /**
     * 根据策略id获取奖品配置信息
     */
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    /**
     * 缓存【抽奖表】、【映射范围】
     */
    void storeStrategyAwardSearchRateTable(String key, Integer size, Map<Integer, Integer> shuffleStrategyAwardSearchRateTable);

    /**
     * 获取映射范围
     */
    int getRateRange(Long strategyId);

    int getRateRange(String key);

    /**
     * 根据【抽奖策略】和【随机值】获取对应的奖品id
     */
    Integer getStrategyAwardAssemble(String key, Integer random);

    /**
     * 通过策略 id 查询策略表中的条目
     */
    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    /**
     * 根据【策略id】和【规则模型】获取对应的【策略规则实体】
     */
    StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel);

    /**
     * 从 mysql 中查询 rule_value
     */
    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);


    StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId);
}
