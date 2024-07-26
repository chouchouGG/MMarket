package cn.learn.domain.activity.service;

import cn.learn.domain.activity.model.entity.ActivityCountEntity;
import cn.learn.domain.activity.model.entity.ActivityEntity;
import cn.learn.domain.activity.model.entity.ActivitySkuEntity;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.domain.activity.service.chain.factory.DefaultActivityChainFactory;

/**
 * @author chouchouGG
 * @description 抽奖活动的支撑类，note：其与具体功能相关，和业务逻辑关联弱，所以单独抽离出来
 * @create 2024-03-23 09:27
 */
public class RaffleActivitySupport {

    protected IActivityRepository activityRepository;

    public RaffleActivitySupport(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        this.activityRepository = activityRepository;
        this.defaultActivityChainFactory = defaultActivityChainFactory;
    }

    protected DefaultActivityChainFactory defaultActivityChainFactory;


    public ActivitySkuEntity queryActivitySku(Long sku) {
        return activityRepository.queryRaffleActivitySku(sku);
    }

    Integer querySkuStockCountSurplus(Long sku) {
        return activityRepository.querySkuStockCountSurplus(sku);
    }

    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        return activityRepository.queryRaffleActivityByActivityId(activityId);
    }

    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
    }

}
