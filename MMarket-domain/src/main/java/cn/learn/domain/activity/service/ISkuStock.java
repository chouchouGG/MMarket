package cn.learn.domain.activity.service;


import cn.learn.domain.activity.model.valobj.ActivitySkuStockKeyVO;

/**
 * @author chouchouGG
 * @description sku 库存处理接口
 */
public interface ISkuStock {
    /*
      note：
       （1）获取和更新库存消耗队列（进行趋势更新）：
            takeQueueValue 方法用于从队列中获取需要消耗的库存信息。（缓存）
            updateActivitySkuStock 方法用于更新 SKU 的库存，通常会结合延迟队列和任务调度机制。（数据库）
       （2）清空队列和清空库存：（配合 MQ 消息使用）：
            clearQueueValue 方法用于清空库存队列中的所有值。（缓存）
            clearActivitySkuStock 方法用于清空数据库中的库存记录，当缓存中的库存被完全消耗时使用。（数据库）
     */

    /**
     * 获取活动sku库存消耗队列
     *
     * @return 奖品库存Key信息
     */
    ActivitySkuStockKeyVO takeQueueValue();

    /**
     * 清空队列
     */
    void clearQueueValue();

    /**
     * 延迟队列 + 任务趋势更新活动sku库存
     *
     * @param sku 活动商品
     */
    void updateActivitySkuStock(Long sku);

    /**
     * 缓存库存以消耗完毕，清空数据库库存
     *
     * @param sku 活动商品
     */
    void clearActivitySkuStock(Long sku);

}
