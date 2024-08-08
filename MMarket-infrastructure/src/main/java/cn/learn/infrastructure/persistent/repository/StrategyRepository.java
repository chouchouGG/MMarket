package cn.learn.infrastructure.persistent.repository;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.entity.StrategyEntity;
import cn.learn.domain.strategy.model.entity.StrategyRuleEntity;
import cn.learn.domain.strategy.model.vo.RuleWeightVO;
import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.learn.infrastructure.persistent.dao.*;
import cn.learn.infrastructure.persistent.po.RaffleActivityAccountDayPO;
import cn.learn.infrastructure.persistent.po.StrategyAwardPO;
import cn.learn.infrastructure.persistent.po.StrategyPO;
import cn.learn.infrastructure.persistent.po.StrategyRulePO;
import cn.learn.infrastructure.persistent.redis.IRedisService;
import cn.learn.types.common.Constants;
import cn.learn.types.event.BaseEvent;
import cn.learn.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static cn.learn.types.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;

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

    // note：为组合抽奖活动接口和抽奖策略接口使用
    @Resource
    private IRaffleActivityDao raffleActivityDao;

    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao; // 用于查询当日次数相关



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
                    .ruleModels(strategyAward.getRuleModels())
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
        return subtractionAwardStock(strategyId, awardId, null);

    }

    @Override
    public Boolean subtractionAwardStock(Long strategyId, Integer awardId, Date endDateTime) {
        // 使用策略ID和奖品ID生成唯一的库存缓存Key
        String cacheKey = Constants.RedisKey.acquireKey_strategyAwardCount(strategyId, awardId);

        // 使用Redis的 decr 方法对库存进行扣减，返回扣减后的库存值。
        long surplus = redisService.decr(cacheKey);

        // 如果扣减后的库存值小于0，说明库存不足，则重新将库存值恢复为0，并返回 false 表示扣减失败
        if (surplus < 0) {
            redisService.setValue(cacheKey, 0);
            return false;
        } else if (surplus == 0) {
            // fixme：库存消耗没了以后，发送 MQ 消息，直接更新数据库库存（中断趋势更新流程）
        }

        // 使用 cacheKey 和当前库存值生成一个唯一的锁Key
        String lockKey = Constants.RedisKey.acquireKey_stockLock(cacheKey, surplus);
        // 通过每次扣减后都有一个唯一的锁Key实现防止超卖
        Boolean lock = false;
        if (null != endDateTime) {
            long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
            lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        } else {
            lock = redisService.setNx(lockKey);
        }

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
    @Override
    public Long queryStrategyIdByActivityId(Long activityId) {
        return raffleActivityDao.queryStrategyIdByActivityId(activityId);
    }

    @Override
    public Integer queryTodayUserRaffleCount(String userId, Long strategyId) {
        // 活动ID
        Long activityId = raffleActivityDao.queryActivityIdByStrategyId(strategyId);
        // 查询用户日剩余次数
        RaffleActivityAccountDayPO raffleActivityAccountDay = raffleActivityAccountDayDao.queryActivityAccountDay(
                RaffleActivityAccountDayPO.builder()
                        .userId(userId)
                        .activityId(activityId)
                        .day(RaffleActivityAccountDayPO.currentFormatedDay())
                        .build()
        );
        if (null == raffleActivityAccountDay) {
            return 0;
        }
        // 今日参与抽奖次数 = 日限额次数 - 日剩余次数
        return raffleActivityAccountDay.getDayCount() - raffleActivityAccountDay.getDayCountSurplus();
    }

    @Override
    public Map<Integer, Integer> queryAwardRuleLockCount(List<StrategyAwardEntity> strategyAwardEntities) {
        if (null == strategyAwardEntities || strategyAwardEntities.isEmpty()) {
            return new HashMap<>();
        }

        // 获取所有配置的解锁规则的奖品ID，以及与之对应的解锁次数
        List<StrategyRulePO> awardRuleLockCountList = null;
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            // 只要有一个奖品设置了解锁规则，就将当前策略下所有的解锁奖品全都查出来
            if (strategyAwardEntity.getRuleModels().contains(Constants.RuleModel.RULE_LOCK)) {
                awardRuleLockCountList = strategyRuleDao.queryAwardRuleLockCount(strategyAwardEntity.getStrategyId());
                break;
            }
        }

        // 将奖品ID与解锁次数以键值对的形式保存
        if (awardRuleLockCountList == null) {
            return new HashMap<>();
        }
        Map<Integer, Integer> resultMap = new HashMap<>();
        for (StrategyRulePO awardRuleLockCount : awardRuleLockCountList) {
            resultMap.put(awardRuleLockCount.getAwardId(), Integer.valueOf(awardRuleLockCount.getRuleValue()));
        }
        return resultMap;
    }


    @Override
    public List<RuleWeightVO> queryAwardRuleWeight(Long strategyId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.acquireKey_strategyRuleWeight(strategyId);
        List<RuleWeightVO> ruleWeightVOS = redisService.getValue(cacheKey);
        // 缓存命中直接返回
        if (null != ruleWeightVOS) {
            return ruleWeightVOS;
        }

        // 【数据库查询流程】
        ruleWeightVOS = new ArrayList<>();
        // 1. 查询权重规则配置的字段值
        String ruleWeightValue = strategyRuleDao.queryStrategyRuleValue(StrategyRulePO.builder()
                .strategyId(strategyId)
                .ruleModel(Constants.RuleModel.RULE_WEIGHT)
                .build());

        // 2. 解析权重规则配置的字段值（借助借助 StrategyRuleEntity 实体对象进行解析）
        Map<String, List<Integer>> ruleWeightValues = StrategyRuleEntity.builder()
                .ruleModel(Constants.RuleModel.RULE_WEIGHT)
                .ruleValue(ruleWeightValue)
                .build().getRuleWeightItem();

        // 3. 根据解析的奖品ID，组装完整的奖品信息
        for (String ruleWeightKey : ruleWeightValues.keySet()) {
            // ruleWeightKey 对应 '4000:102,103,104,105'，awardIds 对应 [102, 103, 104, 105]
            List<Integer> awardIds = ruleWeightValues.get(ruleWeightKey);
            List<RuleWeightVO.Award> awardList = new ArrayList<>();
            // TODO: 后续可以修改为一次性从数据库查询
            for (Integer awardId : awardIds) {
                StrategyAwardPO strategyAward = strategyAwardDao.queryStrategyAward(StrategyAwardPO.builder()
                        .strategyId(strategyId)
                        .awardId(awardId)
                        .build());
                awardList.add(RuleWeightVO.Award.builder()
                        .awardId(strategyAward.getAwardId())
                        .awardTitle(strategyAward.getAwardTitle())
                        .build());
            }

            ruleWeightVOS.add(
                    RuleWeightVO.builder()
                            .ruleValue(ruleWeightKey)
                            .weight(Integer.valueOf(ruleWeightKey.split(Constants.COLON)[0]))  // 抽奖次数
                            .awardIds(awardIds)
                            .awardList(awardList)
                            .build()
            );
        }

        // 设置缓存 - 实际场景中，这类数据，可以在活动下架的时候统一清空缓存。
        redisService.setValue(cacheKey, ruleWeightVOS);

        return ruleWeightVOS;
    }
}




















