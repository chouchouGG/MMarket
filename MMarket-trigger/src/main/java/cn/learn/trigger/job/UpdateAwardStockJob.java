package cn.learn.trigger.job;

import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import cn.learn.domain.strategy.service.IRaffleStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author chouchouGG
 * @description 更新奖品库存任务；为了不让更新库存的压力打到数据库中，这里采用了redis更新缓存库存，异步队列更新数据库，数据库表最终一致即可。
 */
@Slf4j
@Component
public class UpdateAwardStockJob {

    @Resource
    private IRaffleStock raffleStock;

    /**
     * 延迟任务队列获取，降低对数据库的更新频次，减少连接占用。
     * <p>
     * 每隔 5 秒钟执行一次，检查奖品消耗库存队列，并更新数据库中的库存信息。
     * </p>
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void exec() {
        try {
            // 1. 获取延迟阻塞任务队列中的【奖品消耗库存对象】strategyAwardStockKeyVO
            StrategyAwardStockKeyVO strategyAwardStockKeyVO = raffleStock.takeQueueValue();
            if (strategyAwardStockKeyVO == null) {
                log.info("【定时任务-1】 - 奖品库存队列为空，无需更新库存");
                return;
            }
            log.info("【定时任务-1】 - 正在更新奖品消耗库存，strategyId: {} awardId: {}", strategyAwardStockKeyVO.getStrategyId(), strategyAwardStockKeyVO.getAwardId());
            // 2. 更新数据库中的库存信息
            raffleStock.updateStrategyAwardStock(strategyAwardStockKeyVO.getStrategyId(), strategyAwardStockKeyVO.getAwardId());
        } catch (Exception e) {
            log.error("【定时任务-1】，更新奖品消耗库存失败", e);
        }
    }

}
