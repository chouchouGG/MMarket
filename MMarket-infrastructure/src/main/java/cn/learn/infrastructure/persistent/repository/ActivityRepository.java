package cn.learn.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.learn.domain.activity.event.ActivitySkuStockZeroMessageEvent;
import cn.learn.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.learn.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.learn.domain.activity.model.entity.*;
import cn.learn.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.learn.domain.activity.model.valobj.ActivityStateVO;
import cn.learn.domain.activity.model.valobj.UserRaffleOrderStateVO;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.infrastructure.event.EventPublisher;
import cn.learn.infrastructure.persistent.dao.*;
import cn.learn.infrastructure.persistent.po.*;
import cn.learn.infrastructure.persistent.redis.IRedisService;
import cn.learn.types.common.Constants;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.event.BaseEvent;
import cn.learn.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 98389
 * @description 活动仓储服务
 * @create 2024-03-16 11:03
 */
@Slf4j
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IRaffleActivityDao raffleActivityDao; // 活动

    @Resource
    private IRaffleActivityCountDao raffleActivityCountDao; // 次数

    @Resource
    private IRaffleActivitySkuDao raffleActivitySkuDao; // sku

    @Resource
    private IRaffleActivityOrderDao raffleActivityOrderDao; // 订单

    @Resource
    private IRaffleActivityAccountDao raffleActivityAccountDao; // 账户

    @Resource
    private TransactionTemplate transactionTemplate; // 事务

    @Resource
    private IRedisService redisService;

    @Resource
    private IDBRouterStrategy dbRouter; // 分库分表组件

    @Resource
    private EventPublisher eventPublisher; // RabbitMQ的消息发送器

    @Resource
    private ActivitySkuStockZeroMessageEvent activitySkuStockZeroMessageEvent; // 要发送的消息标准定义


    @Resource
    private IRaffleActivityAccountMonthDao raffleActivityAccountMonthDao;
    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao;
    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;

    @Override
    public Integer querySkuStockCountSurplus(Long sku) {
        // 由于延迟更新的缘故，缓存中的库存的实时性比数据库中实时性更高
        String cacheKey = Constants.RedisKey.acquireKey_skuStockCount(sku);
        Long cacheSkuStock = redisService.getAtomicLong(cacheKey);
        if (null == cacheSkuStock || 0 == cacheSkuStock) {
            cacheSkuStock = 0L;
        }
        return cacheSkuStock.intValue();
    }

    @Override
    public ActivitySkuEntity queryRaffleActivitySku(Long sku) {
        String cacheKey = Constants.RedisKey.acquireKey_activitySku(sku);
        ActivitySkuEntity skuEntity = redisService.getValue(cacheKey);
        if (skuEntity != null) {
            return skuEntity;
        }
//        // 由于延迟更新的缘故，缓存中的库存的实时性比数据库中实时性更高
//        String cacheKey = Constants.RedisKey.acquireKey_skuStockCount(sku);
//        Long cacheSkuStock = redisService.getAtomicLong(cacheKey);
//        if (null == cacheSkuStock || 0 == cacheSkuStock) {
//            cacheSkuStock = 0L;
//        }
        RaffleActivitySkuPO raffleActivitySku = raffleActivitySkuDao.queryActivitySku(sku);
        skuEntity = ActivitySkuEntity.builder().sku(raffleActivitySku.getSku())                                // sku编号
                .activityId(raffleActivitySku.getActivityId())                  // 活动id
                .activityCountId(raffleActivitySku.getActivityCountId())        // 次数id
                .stockCount(raffleActivitySku.getStockCount())                  // 总库存
                .stockCountSurplus(raffleActivitySku.getStockCountSurplus())    // 剩余库存(从缓存中获取)
                .build();

        redisService.setValue(cacheKey, skuEntity);
        return skuEntity;
    }


    @Override
    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.acquireKey_activity(activityId);
        ActivityEntity activityEntity = redisService.getValue(cacheKey);
        if (null != activityEntity) {
            return activityEntity;
        }
        // 从库中获取数据
        RaffleActivityPO raffleActivity = raffleActivityDao.queryRaffleActivityByActivityId(activityId);
        activityEntity = ActivityEntity.builder()
                .activityId(raffleActivity.getActivityId())
                .activityName(raffleActivity.getActivityName())
                .activityDesc(raffleActivity.getActivityDesc())
                .beginDateTime(raffleActivity.getBeginDateTime())
                .endDateTime(raffleActivity.getEndDateTime())
                .strategyId(raffleActivity.getStrategyId())
                .state(ActivityStateVO.valueOf(raffleActivity.getState()))
                .build();
        redisService.setValue(cacheKey, activityEntity);
        return activityEntity;
    }

    @Override
    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.acquireKey_activityCount(activityCountId);
        ActivityCountEntity activityCountEntity = redisService.getValue(cacheKey);
        if (null != activityCountEntity) {
            return activityCountEntity;
        }
        // 从库中获取数据
        RaffleActivityCountPO raffleActivityCount = raffleActivityCountDao.queryRaffleActivityCountByActivityCountId(activityCountId);
        activityCountEntity = ActivityCountEntity.builder()
                .activityCountId(raffleActivityCount.getActivityCountId())
                .totalCount(raffleActivityCount.getTotalCount())
                .dayCount(raffleActivityCount.getDayCount())
                .monthCount(raffleActivityCount.getMonthCount()).build();
        redisService.setValue(cacheKey, activityCountEntity);
        return activityCountEntity;
    }

    @Override
    public void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate) {
        try {
            ActivityOrderEntity activityOrderEntity = createOrderAggregate.getActivityOrderEntity();
            String userId = createOrderAggregate.getUserId();
            Long activityId = createOrderAggregate.getActivityId();
            Integer totalCount = createOrderAggregate.getTotalCount();
            Integer monthCount = createOrderAggregate.getMonthCount();
            Integer dayCount = createOrderAggregate.getDayCount();

            // 抽奖活动订单（额度增加订单）PO对象
            RaffleActivityOrderPO raffleActivityOrder = RaffleActivityOrderPO.builder()
                    .userId(userId)
                    .activityId(activityId)
                    .totalCount(totalCount) // 设置本次订单增加的总次数
                    .monthCount(monthCount) // 设置本次订单增加的月次数
                    .dayCount(dayCount)     // 设置本次订单增加的日次数
                    .sku(activityOrderEntity.getSku())
                    .activityName(activityOrderEntity.getActivityName())
                    .strategyId(activityOrderEntity.getStrategyId())
                    .orderId(activityOrderEntity.getOrderId())
                    .orderTime(activityOrderEntity.getOrderTime())
                    .state(activityOrderEntity.getState().getCode())
                    .outBusinessNo(activityOrderEntity.getOutBusinessNo())
                    .build();

            // 总账户PO对象
            RaffleActivityAccountPO raffleActivityAccount = RaffleActivityAccountPO.builder()
                    .userId(userId)
                    .activityId(activityId)
                    .totalCount(totalCount)
                    .totalCountSurplus(totalCount)
                    .monthCount(monthCount)
                    .monthCountSurplus(monthCount)
                    .dayCount(dayCount)
                    .dayCountSurplus(dayCount)
                    .build();

            // 月账户PO对象
            RaffleActivityAccountMonthPO raffleActivityAccountMonth = RaffleActivityAccountMonthPO.builder()
                    .userId(userId)
                    .activityId(activityId)
                    .month(RaffleActivityAccountMonthPO.currentFormatedMonth())
                    .monthCount(monthCount)
                    .monthCountSurplus(monthCount)
                    .build();

            // 账户对象 - 日
            RaffleActivityAccountDayPO raffleActivityAccountDay = RaffleActivityAccountDayPO.builder()
                    .userId(userId)
                    .activityId(activityId)
                    .day(RaffleActivityAccountDayPO.currentFormatedDay())
                    .dayCount(dayCount)
                    .dayCountSurplus(dayCount)
                    .build();


            // 以用户ID作为切分键，通过 doRouter 设定路由【这样就保证了下面的操作，都是同一个链接下，也就保证了事务的特性】
            dbRouter.doRouter(activityOrderEntity.getUserId());

            // 编程式事务
            transactionTemplate.execute(status -> {
                try {
                    // 1. 订单写入
                    raffleActivityOrderDao.insert(raffleActivityOrder);
                    // 2. 账户更新 - 总：note（尝试更新表 raffle_activity_account，updata 语句的返回值为受影响的行数 count）
                    // 如果受影响的行数 count 为 0，则账户不存在，需要为创新新账户
                    // note：更新是基于用户id和活动id，这两个属性有联合唯一索引，所以count的返回值要么为1，要么为0
                    int count = raffleActivityAccountDao.updateAccountQuota(raffleActivityAccount);
                    if (0 == count) {
                        raffleActivityAccountDao.insert(raffleActivityAccount);
                    }
                    // 4. 更新账户 - 月
                    raffleActivityAccountMonthDao.addAccountQuota(raffleActivityAccountMonth);
                    // 5. 更新账户 - 日
                    raffleActivityAccountDayDao.addAccountQuota(raffleActivityAccountDay);
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入订单记录，唯一索引冲突 userId: {} activityId: {} sku: {}",
                            activityOrderEntity.getUserId(), activityOrderEntity.getActivityId(), activityOrderEntity.getSku(), e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public void cacheActivitySkuStockCount(Long sku, Integer stockCount) {
        String cacheKey = Constants.RedisKey.acquireKey_skuStockCount(sku);
        if (redisService.isExists(cacheKey)) {
            return;
        }
        redisService.setAtomicLong(cacheKey, stockCount);
    }

    /**
     * note：库存扣减的核心逻辑
     *
     * @param sku         SKU 的唯一标识。
     * @param cacheKey    用于缓存的键，通常包括 SKU 信息和其他标识符。
     * @param endDateTime 活动结束时间，根据结束时间设置加锁的key为结束时间
     * @return
     */
    @Override
    public boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime) {
        long surplus = redisService.decr(cacheKey);

        if (surplus == 0) {
            // note：库存消耗没了以后，发送 MQ 消息，直接更新数据库库存（中断趋势更新流程）
            String topic = activitySkuStockZeroMessageEvent.topic(); // 获取topic
            BaseEvent.EventMessage<Long> eventMessage = activitySkuStockZeroMessageEvent.buildEventMessage(sku); // 构建消息
            eventPublisher.publish(topic, eventMessage);
        } else if (surplus < 0) {
            // note：库存小于0，恢复为0个（有可能是有一些并发同时进入了，造成库存为负数的情况）
            redisService.setAtomicLong(cacheKey, 0);
            return false;
        }

        // 1. 按照 decr 后的值，如果原本库存有 100 个，缓存 key 分别从 99、98 ... 0 和 key 组成为库存锁的key进行使用。
        // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等【运营是人来操作，会有这种情况发放，系统要做防护】，也不会超卖。因为所有的可用库存key，都被加锁了。
        // 3. 设置加锁时间为活动到期 + 延迟1天
        String lockKey = cacheKey + "_" + surplus;
        long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        Boolean lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        if (!lock) {
            log.info("活动sku库存加锁失败 {}", lockKey);
        }
        return lock;
    }

    @Override
    public void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        String cacheKey = Constants.RedisKey.acquireKey_skuCountQueue();
        RBlockingQueue<ActivitySkuStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);      // 获取阻塞队列
        RDelayedQueue<ActivitySkuStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);    // 获取延迟队列
        delayedQueue.offer(activitySkuStockKeyVO, 3, TimeUnit.SECONDS);
    }

    @Override
    public ActivitySkuStockKeyVO takeQueueValue() {
        String cacheKey = Constants.RedisKey.acquireKey_skuCountQueue();
        RBlockingQueue<ActivitySkuStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        return destinationQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = Constants.RedisKey.acquireKey_skuCountQueue();
        RBlockingQueue<ActivitySkuStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        blockingQueue.clear();
        RDelayedQueue<ActivitySkuStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);    // 获取延迟队列
        delayedQueue.clear();
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        raffleActivitySkuDao.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        raffleActivitySkuDao.clearActivitySkuStock(sku);
    }

    @Override
    public void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate) {
        try {
            // 取出所有属性
            String userId = createPartakeOrderAggregate.getUserId();
            Long activityId = createPartakeOrderAggregate.getActivityId();
            ActivityAccountEntity activityAccountEty = createPartakeOrderAggregate.getActivityAccountEntity();
            ActivityAccountMonthEntity activityAccountMonthEty = createPartakeOrderAggregate.getActivityAccountMonthEntity();
            ActivityAccountDayEntity activityAccountDayEty = createPartakeOrderAggregate.getActivityAccountDayEntity();
            UserRaffleOrderEntity userRaffleOrderEty = createPartakeOrderAggregate.getUserRaffleOrderEntity();


            String month = activityAccountMonthEty.getMonth();
            Integer monthCount = activityAccountMonthEty.getMonthCount();
            Integer monthCountSurplus = activityAccountMonthEty.getMonthCountSurplus();

            String day = activityAccountDayEty.getDay();
            Integer dayCount = activityAccountDayEty.getDayCount();
            Integer dayCountSurplus = activityAccountDayEty.getDayCountSurplus();

            // 基于用户ID（userId）进行路由
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    // 1. 更新总账户，返回受影响的行数
                    int totalCount = raffleActivityAccountDao.updateActivityAccountSubtractionQuota(
                            RaffleActivityAccountPO.builder()
                                    .userId(userId)
                                    .activityId(activityId)
                                    .build());
                    // 判断总额度是否收到影响，1：收到影响，0：未收到影响
                    if (1 != totalCount) {
                        status.setRollbackOnly();
                        log.warn("写入创建参与活动记录，更新总账户额度不足，异常 userId: {} activityId: {}", userId, activityId);
                        throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
                    }

                    // 2. 检查之前是否存在月账户：
                    // true - 存在则更新，
                    if (createPartakeOrderAggregate.isExistAccountMonth()) {
                        int updateMonthCount = raffleActivityAccountMonthDao.updateActivityAccountMonthSubtractionQuota(
                                RaffleActivityAccountMonthPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .month(month)
                                        .build());
                        if (1 != updateMonthCount) {
                            status.setRollbackOnly();
                            log.warn("写入创建参与活动记录，更新月账户额度不足，异常 userId: {} activityId: {} month: {}", userId, activityId, month);
                            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
                        }
                        // 更新总账户中月镜像库存
                        raffleActivityAccountDao.updateActivityAccountMonthSubtractionQuota(
                                RaffleActivityAccountPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .build());
                        // false - 不存在则创建（插入）
                    } else {
                        // note： '账户月次数表' 中还未配置，应该采用 insert 操作。'账户日次数表' 同理。
                        raffleActivityAccountMonthDao.insertActivityAccountMonth(
                                RaffleActivityAccountMonthPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .month(month)
                                        .monthCount(monthCount)
                                        .monthCountSurplus(monthCountSurplus - 1)  // 额度减一
                                        .build());
                        // 创建新的月账户，更新总账表中月镜像额度 // fixme： 不懂这里为什么要更新总表
                        raffleActivityAccountDao.updateActivityAccountMonthSurplusImageQuota(
                                RaffleActivityAccountPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .monthCountSurplus(monthCountSurplus)
                                        .build());
                    }

                    // 3. 创建或更新日账户，true - 存在则更新，false - 不存在则插入
                    if (createPartakeOrderAggregate.isExistAccountDay()) {
                        int updateDayCount = raffleActivityAccountDayDao.updateActivityAccountDaySubtractionQuota(
                                RaffleActivityAccountDayPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .day(day)
                                        .build());
                        if (1 != updateDayCount) {
                            // 未更新成功则回滚
                            status.setRollbackOnly();
                            log.warn("写入创建参与活动记录，更新日账户额度不足，异常 userId: {} activityId: {} day: {}", userId, activityId, day);
                            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
                        }
                        // 更新总账户中日镜像库存
                        raffleActivityAccountDao.updateActivityAccountDaySubtractionQuota(
                                RaffleActivityAccountPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .build());
                    } else {
                        raffleActivityAccountDayDao.insertActivityAccountDay(
                                RaffleActivityAccountDayPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .day(day)
                                        .dayCount(dayCount)
                                        .dayCountSurplus(dayCountSurplus - 1)
                                        .build());
                        // 创建新的日账户，更新总账表中日镜像额度 // fixme： 不懂这里为什么要更新总表
                        raffleActivityAccountDao.updateActivityAccountDaySurplusImageQuota(
                                RaffleActivityAccountPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .dayCountSurplus(dayCountSurplus)
                                        .build());
                    }

                    // 4. 写入【用户抽奖参与订单】
                    userRaffleOrderDao.insert(UserRaffleOrderPO.builder()
                            .userId(userId)
                            .activityId(activityId)
                            .activityName(userRaffleOrderEty.getActivityName())
                            .strategyId(userRaffleOrderEty.getStrategyId())
                            .orderId(userRaffleOrderEty.getOrderId())
                            .orderTime(userRaffleOrderEty.getOrderTime())
                            .orderState(userRaffleOrderEty.getOrderState().getCode())
                            .build());

                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入创建参与活动记录，唯一索引冲突 userId: {} activityId: {}", userId, activityId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public ActivityAccountEntity queryActivityAccount(String userId, Long activityId) {
        // 1. 查询账户
        RaffleActivityAccountPO res = raffleActivityAccountDao.queryActivityAccount(RaffleActivityAccountPO.builder()
                .userId(userId)
                .activityId(activityId)
                .build());
        if (null == res) {  // 用户没有参与活动的话返回null
            return null;
        }
        // 2. 转换对象
        return ActivityAccountEntity.builder()
                .userId(res.getUserId())
                .activityId(res.getActivityId())
                .totalCount(res.getTotalCount())
                .totalCountSurplus(res.getTotalCountSurplus())
                .dayCount(res.getDayCount())
                .dayCountSurplus(res.getDayCountSurplus())
                .monthCount(res.getMonthCount())
                .monthCountSurplus(res.getMonthCountSurplus())
                .build();
    }

    @Override
    public ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month) {
        // 1. 查询账户
        RaffleActivityAccountMonthPO param = new RaffleActivityAccountMonthPO();
        param.setUserId(userId);
        param.setActivityId(activityId);
        param.setMonth(month);
        RaffleActivityAccountMonthPO res = raffleActivityAccountMonthDao.queryActivityAccountMonth(param);
        if (null == res) {
            return null;
        }
        // 2. 转换对象
        return ActivityAccountMonthEntity.builder()
                .userId(res.getUserId())
                .activityId(res.getActivityId())
                .month(res.getMonth())
                .monthCount(res.getMonthCount())
                .monthCountSurplus(res.getMonthCountSurplus())
                .build();
    }

    @Override
    public ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day) {
        // 1. 查询账户
        RaffleActivityAccountDayPO param = new RaffleActivityAccountDayPO();
        param.setUserId(userId);
        param.setActivityId(activityId);
        param.setDay(day);
        RaffleActivityAccountDayPO res = raffleActivityAccountDayDao.queryActivityAccountDay(param);
        if (null == res) {
            return null;
        }
        // 2. 转换对象
        return ActivityAccountDayEntity.builder()
                .userId(res.getUserId())
                .activityId(res.getActivityId())
                .day(res.getDay())
                .dayCount(res.getDayCount())
                .dayCountSurplus(res.getDayCountSurplus())
                .build();
    }

    @Override
    public UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEty) {
        // 查询数据
        UserRaffleOrderPO param = new UserRaffleOrderPO();
        param.setUserId(partakeRaffleActivityEty.getUserId());
        param.setActivityId(partakeRaffleActivityEty.getActivityId());
        UserRaffleOrderPO res = userRaffleOrderDao.queryNoUsedRaffleOrder(param);
        if (null == res) {
            return null;
        }
        // 2. 转换对象
        return UserRaffleOrderEntity.builder()
                .userId(res.getUserId())
                .activityId(res.getActivityId())
                .activityName(res.getActivityName())
                .strategyId(res.getStrategyId())
                .orderId(res.getOrderId())
                .orderTime(res.getOrderTime())
                .orderState(UserRaffleOrderStateVO.valueOf(res.getOrderState()))
                .build();
    }

    @Override
    public List<ActivitySkuEntity> queryActivitySkuListByActivityId(Long activityId) {
        List<RaffleActivitySkuPO> raffleActivitySkus = raffleActivitySkuDao.queryActivitySkuListByActivityId(activityId);
        List<ActivitySkuEntity> list = new ArrayList<>(raffleActivitySkus.size());
        for (RaffleActivitySkuPO raffleActivitySku : raffleActivitySkus) {
            ActivitySkuEntity activitySkuEntity = ActivitySkuEntity.builder()
                    .sku(raffleActivitySku.getSku())
                    .activityCountId(raffleActivitySku.getActivityCountId())
                    .stockCount(raffleActivitySku.getStockCount())
                    .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                    .build();
            list.add(activitySkuEntity);
        }
        return list;
    }

    @Override
    public Integer queryAccountDayPartakeCount(Long activityId, String userId) {
        RaffleActivityAccountDayPO raffleActivityAccountDay = RaffleActivityAccountDayPO.builder()
                .activityId(activityId)
                .userId(userId)
                .day(RaffleActivityAccountDayPO.currentFormatedDay())
                .build();
        Integer dayPartakeCount = raffleActivityAccountDayDao.queryAccountDayPartakeCount(raffleActivityAccountDay);
        // 当日未参与抽奖则默认为 0 次
        return null == dayPartakeCount ? 0 : dayPartakeCount;
    }

    @Override
    public ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId) {
        // 1. 查询总账户额度
        RaffleActivityAccountPO raffleActivityAccount =
                raffleActivityAccountDao.queryActivityAccount(RaffleActivityAccountPO.builder()
                        .activityId(activityId)
                        .userId(userId)
                        .build());

        if (null == raffleActivityAccount) {
            return ActivityAccountEntity.builder()
                    .activityId(activityId)
                    .userId(userId)
                    .totalCount(0)
                    .totalCountSurplus(0)
                    .monthCount(0)
                    .monthCountSurplus(0)
                    .dayCount(0)
                    .dayCountSurplus(0)
                    .build();
        }

        // 2. 查询月账户额度
        RaffleActivityAccountMonthPO raffleActivityAccountMonth =
                raffleActivityAccountMonthDao.queryActivityAccountMonth(RaffleActivityAccountMonthPO.builder()
                        .activityId(activityId)
                        .userId(userId)
                        .build());

        // 3. 查询日账户额度
        RaffleActivityAccountDayPO raffleActivityAccountDay =
                raffleActivityAccountDayDao.queryActivityAccountDay(RaffleActivityAccountDayPO.builder()
                        .activityId(activityId)
                        .userId(userId)
                        .build());

        // 组装对象
        ActivityAccountEntity activityAccountEntity = new ActivityAccountEntity();
        activityAccountEntity.setUserId(userId);
        activityAccountEntity.setActivityId(activityId);
        activityAccountEntity.setTotalCount(raffleActivityAccount.getTotalCount());
        activityAccountEntity.setTotalCountSurplus(raffleActivityAccount.getTotalCountSurplus());

        // 如果没有创建日账户，则从总账户中获取日总额度填充。
        if (null == raffleActivityAccountDay) {
            activityAccountEntity.setDayCount(raffleActivityAccount.getDayCount());
            activityAccountEntity.setDayCountSurplus(raffleActivityAccount.getDayCount());
        } else {
            activityAccountEntity.setDayCount(raffleActivityAccountDay.getDayCount());
            activityAccountEntity.setDayCountSurplus(raffleActivityAccountDay.getDayCountSurplus());
        }

        // 如果没有创建月账户，则从总账户中获取月总额度填充。「当新创建日账户时，会获得总账户额度」
        if (null == raffleActivityAccountMonth) {
            activityAccountEntity.setMonthCount(raffleActivityAccount.getMonthCount());
            activityAccountEntity.setMonthCountSurplus(raffleActivityAccount.getMonthCount());
        } else {
            activityAccountEntity.setMonthCount(raffleActivityAccountMonth.getMonthCount());
            activityAccountEntity.setMonthCountSurplus(raffleActivityAccountMonth.getMonthCountSurplus());
        }

        return activityAccountEntity;
    }

    @Override
    public Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId) {
        RaffleActivityAccountPO raffleActivityAccount =
                raffleActivityAccountDao.queryActivityAccount(RaffleActivityAccountPO.builder()
                        .activityId(activityId)
                        .userId(userId)
                        .build());
        // 用户的抽奖次数为总次数 - 总剩余次数。（这种实现方式还存在一定bug，不好重新记录参与次数，改进思路见如下fixme）
        // fixme：后续可以为用户账户增加一个属性专门用于记录用户的总参与次数，这样就方便在达到一定抽奖次数完成权重值抽奖后直接将参与次数清零，并重新开始记录总参与次数。
        return raffleActivityAccount.getTotalCount() - raffleActivityAccount.getTotalCountSurplus();
    }
}
