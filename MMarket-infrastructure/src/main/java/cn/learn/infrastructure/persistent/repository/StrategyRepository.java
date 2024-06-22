package cn.learn.infrastructure.persistent.repository;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.entity.StrategyEntity;
import cn.learn.domain.strategy.model.entity.StrategyRuleEntity;
import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.infrastructure.persistent.dao.IStrategyAwardDao;
import cn.learn.infrastructure.persistent.dao.IStrategyDao;
import cn.learn.infrastructure.persistent.dao.IStrategyRuleDao;
import cn.learn.infrastructure.persistent.po.StrategyAwardPO;
import cn.learn.infrastructure.persistent.po.StrategyPO;
import cn.learn.infrastructure.persistent.po.StrategyRulePO;
import cn.learn.infrastructure.persistent.redis.IRedisService;
import cn.learn.types.common.Constants;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: MMarket
 * @description: 抽奖策略仓储功能的实现类
 * @author: chouchouGG
 * @create: 2024-05-30 14:53
 **/

/**
 *  Note: 工程模块的依赖关系是 infrastructure 模块依赖于 domain 模块，
 *   也就是说 infrastructure 模块的代码可以访问和使用 domain 模块中定义的类、接口或其他资源。
 *   所以 StrategyRepository（在 infrastructure中） 可以实现 IStrategyRepository 接口（在 domain中）
 *   */

/**
 * NOTE: 这是因为 @Autowired 注解依赖于 Spring 的组件扫描机制，需要确保包含 @Autowired 注解的类本身
 *  是由 Spring 管理的 Spring Bean。如果类没有被 Spring 管理，Spring 就无法自动注入其依赖项。
 */

//@Repository
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyDao strategyDao;

//    @Autowired
    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Resource
    private IStrategyRuleDao strategyRuleDao;

    /** redisClient 客户端 */
    @Resource
    private IRedisService redisService;

    /**
     * 缓存当前策略的奖品信息表
     * @param strategyId
     * @return
     */
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        // 1. 计算缓存的键
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
        // 2. 先查 redis 缓存
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);
        // 3. 判断缓存是否命中
        // - 3.1 缓存命中，直接返回
        if (strategyAwardEntities != null && !strategyAwardEntities.isEmpty()) {
            return strategyAwardEntities;
        }
        // - 3.2 未命中，查 mysql，将查询的 PO 对象转换为 Entity 对象
        // 【注意：mysql查询结果返回的不是domain层中的 entity 实体对象，而是 PO】
        List<StrategyAwardPO> strategyAwards = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        strategyAwardEntities = new ArrayList<>(strategyAwards.size());
        for (StrategyAwardPO strategyAward : strategyAwards) {
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                    .strategyId(strategyAward.getStrategyId())
                    .awardId(strategyAward.getAwardId())
                    .awardCount(strategyAward.getAwardCount())
                    .awardCountSurplus(strategyAward.getAwardCountSurplus())
                    .awardRate(strategyAward.getAwardRate())
                    .build();
            strategyAwardEntities.add(strategyAwardEntity);
        }
        // 4. 更新缓存
        redisService.setValue(cacheKey, strategyAwardEntities);
        // 5. 返回查询结果
        return strategyAwardEntities;
    }

    /**
     * 缓存【当前策略的随机数范围】和【随机数的映射表】
     * @param key
     * @param size
     * @param strategyAwardSearchRateTable 映射表
     */
    @Override
    public void storeStrategyAwardSearchRateTable(String key, Integer size, Map<Integer, Integer> strategyAwardSearchRateTable) {
        // 1. 缓存抽奖策略范围值【当前策略的随机数范围】，如 10000，用于生成 1000 以内的随机数
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, size);
        // 2. 缓存【抽奖表】
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        cacheRateTable.putAll(strategyAwardSearchRateTable);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return getRateRange(String.valueOf(strategyId));
    }

    @Override
    public int getRateRange(String key) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }

    @Override
    public Integer getStrategyAwardAssemble(String strategyId, Integer random) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, random);
    }

    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if (null != strategyEntity) {
            return strategyEntity;
        }
        // redis 不命中，查 mysql
        StrategyPO strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        // PO 对象转换为 Entity 对象
        strategyEntity = StrategyEntity.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .ruleModels(strategy.getRuleModels())
                .build();
        // 重新设置 redis 缓存
        redisService.setValue(cacheKey, strategyEntity);
        // 返回查询结果
        return strategyEntity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel) {
        // 将要查询的信息装在对象中
        StrategyRulePO strategyRule = new StrategyRulePO();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setRuleModel(ruleModel);
        // 查询数据库（这里为了🎯🎯🎯简化逻辑没有走缓存，直接走库）
        StrategyRulePO strategyRuleRes = strategyRuleDao.queryStrategyRule(strategyRule);
        if (strategyRuleRes == null) {
            return null;
        }
        return StrategyRuleEntity.builder()
                .strategyId(strategyRuleRes.getStrategyId())
                .awardId(strategyRuleRes.getAwardId())
                .ruleType(strategyRuleRes.getRuleType())
                .ruleModel(strategyRuleRes.getRuleModel())
                .ruleValue(strategyRuleRes.getRuleValue())
                .ruleDesc(strategyRuleRes.getRuleDesc())
                .build();
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRulePO strategyRule = StrategyRulePO.builder()
                .strategyId(strategyId)
                .awardId(awardId)
                .ruleModel(ruleModel)
                .build();

        return strategyRuleDao.queryStrategyRuleValue(strategyRule);
    }

    @Override
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId) {
        StrategyAwardPO strategyAwardPO = new StrategyAwardPO();
        strategyAwardPO.setStrategyId(strategyId);
        strategyAwardPO.setAwardId(awardId);
        String ruleModels = strategyAwardDao.queryStrategyAwardRuleModelVO(strategyAwardPO);
        return StrategyAwardRuleModelVO.builder().ruleModels(ruleModels).build();
    }
}




















