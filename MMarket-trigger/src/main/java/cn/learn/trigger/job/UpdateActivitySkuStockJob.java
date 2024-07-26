package cn.learn.trigger.job;

import cn.learn.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.learn.domain.activity.service.ISkuStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author chouchouGG
 * @description 更新活动sku库存任务
 * @create 2024-03-30 09:52
 */
@Slf4j
@Component()
public class UpdateActivitySkuStockJob {

    @Resource
    private ISkuStock skuStock;

    @Scheduled(cron = "0/5 * * * * ?")
    public void exec() {
        try {
            // 1. 获取延迟阻塞队列中的库存对象
            ActivitySkuStockKeyVO activitySkuStockKeyVO = skuStock.takeQueueValue();
            if (null == activitySkuStockKeyVO) {
                log.info("【定时任务-2】 - sku库存队列为空，无需更新库存");
                return;
            }
            // 2. 更新数据库中的库存信息
            skuStock.updateActivitySkuStock(activitySkuStockKeyVO.getSku());
            log.info("【定时任务-2】 - 更新活动sku库存，sku: {} activityId: {}", activitySkuStockKeyVO.getSku(), activitySkuStockKeyVO.getActivityId());
        } catch (Exception e) {
            log.error("【定时任务-2】，更新活动sku库存失败", e);
        }
    }

}
