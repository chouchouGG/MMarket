package cn.learn.domain.activity.repository;

import cn.learn.domain.activity.model.entity.ActivityCountEntity;
import cn.learn.domain.activity.model.entity.ActivityEntity;
import cn.learn.domain.activity.model.entity.ActivitySkuEntity;

/**
 * @author chouchouGG
 * @description 活动仓储接口
 * @create 2024-03-16 10:31
 */
public interface IActivityRepository {

    /**
     * 查询活动 SKU 信息
     *
     * @param sku 活动 SKU 的唯一标识
     * @return 返回活动 SKU 实体信息
     */
    ActivitySkuEntity queryRaffleActivitySku(Long sku);

    /**
     * 根据活动 ID 查询抽奖活动信息
     *
     * @param activityId 活动 ID
     * @return 返回抽奖活动实体信息
     */
    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    /**
     * 根据活动次数 ID 查询抽奖活动次数信息
     *
     * @param activityCountId 活动次数 ID
     * @return 返回抽奖活动次数实体信息
     */
    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

}
