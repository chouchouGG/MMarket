package cn.learn.infrastructure.persistent.repository;

import cn.learn.domain.activity.model.entity.ActivityCountEntity;
import cn.learn.domain.activity.model.entity.ActivityEntity;
import cn.learn.domain.activity.model.entity.ActivitySkuEntity;
import cn.learn.domain.activity.model.valobj.ActivityStateVO;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.infrastructure.persistent.dao.IRaffleActivityCountDao;
import cn.learn.infrastructure.persistent.dao.IRaffleActivityDao;
import cn.learn.infrastructure.persistent.dao.IRaffleActivitySkuDao;
import cn.learn.infrastructure.persistent.po.RaffleActivityCountPO;
import cn.learn.infrastructure.persistent.po.RaffleActivityPO;
import cn.learn.infrastructure.persistent.po.RaffleActivitySkuPO;
import cn.learn.infrastructure.persistent.redis.IRedisService;
import cn.learn.types.common.Constants;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动仓储服务
 * @create 2024-03-16 11:03
 */
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IRedisService redisService;

    @Resource
    private IRaffleActivityDao raffleActivityDao;

    @Resource
    private IRaffleActivitySkuDao raffleActivitySkuDao;

    @Resource
    private IRaffleActivityCountDao raffleActivityCountDao;

    @Override
    public ActivitySkuEntity queryRaffleActivitySku(Long sku) {
        RaffleActivitySkuPO raffleActivitySku = raffleActivitySkuDao.queryActivitySku(sku);
        return ActivitySkuEntity.builder()
                .sku(raffleActivitySku.getSku())
                .activityId(raffleActivitySku.getActivityId())
                .activityCountId(raffleActivitySku.getActivityCountId())
                .stockCount(raffleActivitySku.getStockCount())
                .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                .build();
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
                .monthCount(raffleActivityCount.getMonthCount())
                .build();
        redisService.setValue(cacheKey, activityCountEntity);
        return activityCountEntity;
    }

}
