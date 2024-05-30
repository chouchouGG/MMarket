package cn.learn.domain.strategy.respository;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;
import java.util.Map;

/**
 * @program: MMarket
 * @description: 策略的仓储接口
 * @author: chouchouGG
 * @create: 2024-05-30 14:44
 **/
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategyAwardSearchRateTable(Long strategyId,
                                           Integer size,
                                           Map<Integer, Integer> shuffleStrategyAwardSearchRateTable);

    int getRateRange(Long strategyId);

    Integer getStrategyAwardAssemble(Long strategyId, int random);
}
