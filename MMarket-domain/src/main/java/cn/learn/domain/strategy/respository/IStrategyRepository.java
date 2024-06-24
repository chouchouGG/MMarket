package cn.learn.domain.strategy.respository;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.entity.StrategyEntity;
import cn.learn.domain.strategy.model.entity.StrategyRuleEntity;
import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;

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
     * 该方法将【抽奖策略的随机数范围】和【抽奖表】缓存到 Redis 中，用于后续抽奖操作。
     *
     * @param assembleKey                       缓存键，用于标识当前策略
     * @param size                      当前策略的随机数范围值，如 10000
     * @param shuffleStrategyAwardSearchRateTable 抽奖表
     */
    void storeStrategyAwardSearchRateTable(String assembleKey, Integer size, Map<Integer, Integer> shuffleStrategyAwardSearchRateTable);

    /**
     * 获取映射范围
     */
    int getRateRange(Long strategyId);

    /**
     * 获取映射范围
     */
    int getRateRange(String assembleKey);

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


    /**
     * 查询策略奖品规则模型。
     *
     * @param strategyId 策略ID，用于标识具体的抽奖策略。
     * @param awardId    奖品ID，用于标识具体的奖品。
     * @return 返回对应的策略奖品规则模型对象。
     */
    StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId);


    /**
     * 缓存策略奖品的数量。
     *
     * @param cacheKey  缓存键，用于标识缓存条目的唯一标识符。
     * @param awardCount 奖品数量，用于记录奖品的数量。
     */
    void cacheStrategyAwardCount(String cacheKey, Integer awardCount);


    /**
     * 根据策略ID和奖品ID，扣减奖品缓存库存
     * <p>
     * 按照缓存键减去库存，并返回是否成功。如果库存不足，则返回 false。
     * 加锁机制确保在并发情况下不会出现超卖问题。
     * </p>
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 返回 true 表示库存减少成功，返回 false 表示库存不足，减少失败。
     */
    Boolean subtractionAwardStock(Long strategyId, Integer awardId);


    /**
     * 写入奖品库存消费队列
     *
     * @param strategyAwardStockKeyVO 对象值对象，包含策略id和奖品id
     */
    void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    /**
     * 从 Redis 阻塞队列(任务队列)中获取策略奖品库存更新任务。
     *
     * @return 返回从队列中获取到的 {@code StrategyAwardStockKeyVO} 对象，如果队列为空，则返回 {@code null}。
     */
    StrategyAwardStockKeyVO takeQueueValue();

    /**
     * 更新数据库中指定【策略ID】和【奖品ID】的奖品库存，将库存扣减 1
     * @param strategyId 策略ID
     * @param awardId 奖品ID
     */
    void updateStrategyAwardStock(Long strategyId, Integer awardId);

    /**
     * 查询策略奖品实体。
     * <p>
     * 根据策略ID和奖品ID查询对应的策略奖品实体信息。
     * </p>
     *
     * @param strategyId 策略ID，用于标识具体的抽奖策略。
     * @param awardId    奖品ID，用于标识具体的奖品。
     * @return 返回对应的策略奖品实体 {@code StrategyAwardEntity}。
     */
    StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId);
}
