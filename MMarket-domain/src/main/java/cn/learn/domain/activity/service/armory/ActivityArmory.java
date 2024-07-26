package cn.learn.domain.activity.service.armory;

import cn.learn.domain.activity.model.entity.ActivitySkuEntity;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动sku预热
 * @create 2024-03-30 09:12
 */
@Slf4j
@Service
public class ActivityArmory implements IActivityArmory, IActivityDispatch {

    // 仓储层
    @Resource
    private IActivityRepository activityRepository;

    @Override
    public boolean assembleActivitySku(Long sku) {
        // 预热活动sku库存
        ActivitySkuEntity activitySkuEntity = activityRepository.queryRaffleActivitySku(sku);

        // note：这里缓存的是总库存，缓存剩余库存也是可以的
        cacheActivitySkuStockCount(sku, activitySkuEntity.getStockCountSurplus());

        // 预热活动【查询并预热到缓存】
        activityRepository.queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());

        // 预热活动次数【查询并预热到缓存】
        activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        return true;
    }

    /**
     * 缓存库存
     */
    private void cacheActivitySkuStockCount(Long sku, Integer stockCountSurplus) {
        activityRepository.cacheActivitySkuStockCount(Constants.RedisKey.acquireKey_skuStockCount(sku), stockCountSurplus);
    }

    /**
     * 库存扣减，涉及MQ消息发送，保证最终一致性
     * @param sku 互动SKU
     * @param endDateTime 活动结束时间，根据结束时间设置加锁的key为结束时间
     * @return
     */
    @Override
    public boolean subtractionActivitySkuStock(Long sku, Date endDateTime) {
        return activityRepository.subtractionActivitySkuStock(sku, Constants.RedisKey.acquireKey_skuStockCount(sku), endDateTime);
    }

}
