package cn.learn.domain.activity.repository;

import cn.learn.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.learn.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.learn.domain.activity.model.entity.*;
import cn.learn.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;
import java.util.List;

/**
 * @author chouchouGG
 * @description 活动仓储接口
 * @create 2024-03-16 10:31
 */
public interface IActivityRepository {

    Integer querySkuStockCountSurplus(Long sku);

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


    /**
     * 保存订单信息
     *
     * @param createOrderAggregate 订单聚合对象，包含订单相关的所有信息
     */
    void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate);



    /**
     * 缓存活动 SKU 的库存数量。
     */
    void cacheActivitySkuStockCount(Long sku, Integer stockCount);

    /**
     * 扣减活动 SKU 的库存数量。
     *
     * @param sku SKU 的唯一标识。
     * @param cacheKey 用于缓存的键，通常包括 SKU 信息和其他标识符。
     * @param endDateTime 活动结束时间，根据结束时间设置加锁的key为结束时间
     * @return 如果库存扣减成功返回 true，否则返回 false。
     */
    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);




    /**
     * 发送活动 SKU 库存消费消息到延迟队列。
     *
     * @param activitySkuStockKeyVO 包含活动 SKU 库存信息的对象。
     */
    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    /**
     * 从活动 SKU 库存消耗队列中获取一个库存 Key 信息。
     *
     * @return 包含奖品库存 Key 信息的 ActivitySkuStockKeyVO 对象。
     */
    ActivitySkuStockKeyVO takeQueueValue();

    /**
     * 清空活动 SKU 库存消耗队列中的所有值。
     */
    void clearQueueValue();

    /**
     * 使用延迟队列和任务调度机制更新活动 SKU 的库存。
     *
     * @param sku 活动商品的 SKU。
     */
    void updateActivitySkuStock(Long sku);

    /**
     * 在缓存库存被消耗完毕后清空数据库中的库存记录。
     *
     * @param sku 活动商品的 SKU。
     */
    void clearActivitySkuStock(Long sku);

    /**
     * <h1>note:活动订单的核心逻辑</h1>
     */
    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);

    ActivityAccountEntity queryActivityAccount(String userId, Long activityId);

    ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day);

    UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    List<ActivitySkuEntity> queryActivitySkuListByActivityId(Long activityId);

    Integer queryAccountDayPartakeCount(Long activityId, String userId);
}
