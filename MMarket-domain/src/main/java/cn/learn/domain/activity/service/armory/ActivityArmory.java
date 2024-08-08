package cn.learn.domain.activity.service.armory;

import cn.learn.domain.activity.model.entity.ActivitySkuEntity;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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

        // note：这里缓存的是剩余库存，缓存总库存也是可以的
        activityRepository.cacheActivitySkuStockCount(sku, activitySkuEntity.getStockCountSurplus());

        // 预热活动【查询并预热到缓存】
        activityRepository.queryRaffleActivityByActivityId(activitySkuEntity.getActivityId());

        // 预热活动次数【查询并预热到缓存】
        activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        return true;
    }

    @Override
    public boolean assembleActivitySkuByActivityId(Long activityId) {
        // SKU和活动ID之间为一对多：一个SKU确定一个活动ID和次数ID，一个活动ID有可能对应多个SKU
        // note：（2024年7月29日-暂时理解）结果是查询出来的sku实体的列表中，活动ID都相同，次数ID不同，对应于不同的触发行为
        List<ActivitySkuEntity> activitySkuEntities = activityRepository.queryActivitySkuListByActivityId(activityId);
        // 循环处理所有的SKU
        for (ActivitySkuEntity activitySkuEntity : activitySkuEntities) {
            // 缓存SKU的库存剩余（note：sku库存是交给运维进行营销成本控制的）
            activityRepository.cacheActivitySkuStockCount(activitySkuEntity.getSku(), activitySkuEntity.getStockCountSurplus());
            // 预热活动对应的初始化次数（总、月、日）【查询时预热到缓存】
            activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());
        }
        // 预热活动（开始日期、截止日期、抽奖活动对应的抽奖策略Id）【查询时预热到缓存】
        activityRepository.queryRaffleActivityByActivityId(activityId);
        return true;
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
