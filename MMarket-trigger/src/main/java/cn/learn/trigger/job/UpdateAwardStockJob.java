package cn.learn.trigger.job;

import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import cn.learn.domain.strategy.service.IRaffleStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 98389
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
        log.info("【定时任务】 - 开始更新奖品消耗库存");

        // 1. 获取延迟任务队列中的奖品消耗库存键值对象
        StrategyAwardStockKeyVO strategyAwardStockKeyVO = raffleStock.takeQueueValue();
        if (strategyAwardStockKeyVO == null) {
            log.info("【定时任务】 - 队列为空，无需更新库存");
            return;
        }

        log.info("【定时任务】 - 更新奖品消耗库存，strategyId: {} awardId: {}",
                strategyAwardStockKeyVO.getStrategyId(), strategyAwardStockKeyVO.getAwardId());

        // 2. 更新数据库中的库存信息
        raffleStock.updateStrategyAwardStock(strategyAwardStockKeyVO.getStrategyId(), strategyAwardStockKeyVO.getAwardId());

        log.info("【定时任务】 - 完成奖品消耗库存更新，strategyId: {} awardId: {}",
                strategyAwardStockKeyVO.getStrategyId(), strategyAwardStockKeyVO.getAwardId());
    }

}
