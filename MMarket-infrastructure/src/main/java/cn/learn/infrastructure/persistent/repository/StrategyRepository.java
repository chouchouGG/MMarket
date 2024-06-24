package cn.learn.infrastructure.persistent.repository;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.entity.StrategyEntity;
import cn.learn.domain.strategy.model.entity.StrategyRuleEntity;
import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.infrastructure.persistent.dao.IStrategyAwardDao;
import cn.learn.infrastructure.persistent.dao.IStrategyDao;
import cn.learn.infrastructure.persistent.dao.IStrategyRuleDao;
import cn.learn.infrastructure.persistent.po.StrategyAwardPO;
import cn.learn.infrastructure.persistent.po.StrategyPO;
import cn.learn.infrastructure.persistent.po.StrategyRulePO;
import cn.learn.infrastructure.persistent.redis.IRedisService;
import cn.learn.types.common.Constants;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.learn.types.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;
import static com.sun.xml.internal.ws.api.message.Packet.Status.Response;

/*
 *  Note: 工程模块的依赖关系是 infrastructure 模块依赖于 domain 模块，
 *   也就是说 infrastructure 模块的代码可以访问和使用 domain 模块中定义的类、接口或其他资源。
 *   所以 StrategyRepository（在 infrastructure中） 可以实现 IStrategyRepository 接口（在 domain中）
 *   */

/*
 * NOTE: 这是因为 @Autowired 注解依赖于 Spring 的组件扫描机制，需要确保包含 @Autowired 注解的类本身
 *  是由 Spring 管理的 Spring Bean。如果类没有被 Spring 管理，Spring 就无法自动注入其依赖项。
 */

/**
 * @program: MMarket
 * @description: 抽奖策略仓储功能的实现类
 * @author: chouchouGG
 * @create: 2024-05-30 14:53
 **/
@Slf4j
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyDao strategyDao;

    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Resource
    private IStrategyRuleDao strategyRuleDao;

    // redisClient 客户端
    @Resource
    private IRedisService redisService;



    //缓存当前策略的奖品信息表
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        // 1. 计算缓存的键
        String cacheKey = Constants.RedisKey.acquireKey_strategyAwardList(strategyId);
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
                    .awardTitle(strategyAward.getAwardTitle())
                    .awardSubtitle(strategyAward.getAwardSubtitle())
                    .sort(strategyAward.getSort())
                    .build();
            strategyAwardEntities.add(strategyAwardEntity);
        }
        // 4. 更新缓存
        redisService.setValue(cacheKey, strategyAwardEntities);
        // 5. 返回查询结果
        return strategyAwardEntities;
    }

    @Override
    public void storeStrategyAwardSearchRateTable(String assembleKey, Integer size, Map<Integer, Integer> strategyAwardSearchRateTable) {
        // 1. 缓存抽奖策略范围值【当前策略的随机数范围】
        redisService.setValue(Constants.RedisKey.acquireKey_strategyRateRange(assembleKey), size);
        // 2. 缓存【抽奖表】
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.acquireKey_strategyRateTable(assembleKey));
        cacheRateTable.putAll(strategyAwardSearchRateTable);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return getRateRange(String.valueOf(strategyId));
    }

    @Override
    public int getRateRange(String assembleKey) {
        // 因为抽奖接口是直接提供给外部使用的，外部在调用抽奖之前可能没有先调用抽奖装配接口，所以先检查一下，如果没有装配就报错提醒
        String cacheKey = Constants.RedisKey.acquireKey_strategyRateRange(assembleKey);
        if (!redisService.isExists(cacheKey)) {
            throw new AppException(UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());
        }
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + assembleKey);
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

    @Override
    public void cacheStrategyAwardCount(String cacheKey, Integer awardCount) {
        if (redisService.isExists(cacheKey)) {
            return;
        }
        redisService.setAtomicLong(cacheKey, awardCount);
    }

    @Override
    public Boolean subtractionAwardStock(Long strategyId, Integer awardId) {
        // 使用策略ID和奖品ID生成唯一的库存缓存Key
        String cacheKey = Constants.RedisKey.acquireKey_strategyAwardCount(strategyId, awardId);

        // 使用Redis的 decr 方法对库存进行扣减，返回扣减后的库存值。
        long surplus = redisService.decr(cacheKey);

        // 如果扣减后的库存值小于0，说明库存不足，则重新将库存值恢复为0，并返回 false 表示扣减失败
        if (surplus < 0) {
            redisService.setValue(cacheKey, 0);
            return false;
        }
        // 使用 cacheKey 和当前库存值生成一个唯一的锁Key
        String lockKey = Constants.RedisKey.acquireKey_stockLock(cacheKey, surplus);
        // 通过每次扣减后都有一个唯一的锁Key实现防止超卖
        Boolean lock = redisService.setNx(lockKey);
        if (!lock) {
            log.info("策略奖品库存加锁失败 {}", lockKey);
        }
        return lock;
    }

    // note：【生产者】将任务放到阻塞队列中
    @Override
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        // 获取策略奖品库存任务队列的键
        String cacheQueueKey = Constants.RedisKey.acquireStrategyAwardCountQueuekey();
        // 获取 Redis 阻塞队列
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheQueueKey);
        // 获取 Redis 延迟队列，目的是降低库存信息更新到数据库的速度，减少连接的占用
        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        // offer 方法：将一个元素添加到队列中，元素将在指定的延迟时间后才会真正可用。
        // 将奖品库存消费信息添加到延迟队列中，在延迟时间 3 秒后才会在队列中可用。
        delayedQueue.offer(strategyAwardStockKeyVO, 3, TimeUnit.SECONDS);
    }

    // note：【消费者】从阻塞队列中取出任务
    @Override
    public StrategyAwardStockKeyVO takeQueueValue() {
        // 获取策略奖品库存任务队列的键
        String cacheQueueKey = Constants.RedisKey.acquireStrategyAwardCountQueuekey();
        // 获取 Redis 阻塞队列
        RBlockingQueue<StrategyAwardStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheQueueKey);
        // 从队列中取出元素，阻塞队列的 poll 方法会立即返回任务，如果队列为空则返回 null
        return destinationQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAwardPO strategyAward = new StrategyAwardPO();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId) {
        // 1. 从缓存获取数据
        String cacheKey = Constants.RedisKey.acquireKey_strategyAwardEntity(strategyId, awardId);
        StrategyAwardEntity strategyAwardEntity = redisService.getValue(cacheKey);
        if (null != strategyAwardEntity) {
            return strategyAwardEntity;
        }

        // 2. 从数据库查询数据
        StrategyAwardPO po = strategyAwardDao.queryStrategyAwardPO(strategyId, awardId);
        strategyAwardEntity = StrategyAwardEntity.builder()
                .sort(po.getSort())
                .awardSubtitle(po.getAwardSubtitle())
                .awardTitle(po.getAwardTitle())
                .awardCount(po.getAwardCount())
                .awardCountSurplus(po.getAwardCountSurplus())
                .awardId(po.getAwardId())
                .awardRate(po.getAwardRate())
                .strategyId(po.getStrategyId())
                .build();

        // 3. 缓存结果
        redisService.setValue(cacheKey, strategyAwardEntity);

        return strategyAwardEntity;
    }


}




















