package cn.learn.infrastructure.persistent.repository;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.infrastructure.persistent.dao.IStrategyAwardDao;
import cn.learn.infrastructure.persistent.po.StrategyAwardPO;
import cn.learn.infrastructure.persistent.redis.IRedisService;
import cn.learn.types.common.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

//    @Autowired
    @Resource
    private IStrategyAwardDao strategyAwardDao;

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
     * @param strategyId
     * @param size
     * @param strategyAwardSearchRateTable 映射表
     */
    @Override
    public void storeStrategyAwardSearchRateTable(Long strategyId, Integer size, Map<Integer, Integer> strategyAwardSearchRateTable) {
        // 1. 存储抽奖策略范围值【当前策略的随机数范围】，如 10000，用于生成 1000 以内的随机数
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId, size);
        // 2. 存储【随机数的映射表】
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId);
        cacheRateTable.putAll(strategyAwardSearchRateTable);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
    }
    @Override
    public Integer getStrategyAwardAssemble(Long strategyId, int random) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, random);
    }
}




















