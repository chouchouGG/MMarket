package cn.learn.domain.strategy.service.raffle;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.AbstractRaffleStrategy;
import cn.learn.domain.strategy.service.IRaffleAward;
import cn.learn.domain.strategy.service.IRaffleRule;
import cn.learn.domain.strategy.service.IRaffleStock;
import cn.learn.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.learn.domain.strategy.service.rule.chain.logicChain.ILogicChain;
import cn.learn.domain.strategy.service.rule.tree.engine.IDecisionTreeEngine;
import cn.learn.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-06-14 15:00
 **/
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleStock, IRaffleAward, IRaffleRule {

    public DefaultRaffleStrategy(DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory, IStrategyRepository repository) {
        super(defaultChainFactory, defaultTreeFactory, repository);
    }


    @Override
    public void raffleLogicChain(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            log.info("流程状态为TERMINATED，无需进行责任链流程");
            return;
        }
        // 获取抽奖责任链
        ILogicChain logicChain = defaultChainFactory.openLogicChain(context.getStrategyId());
        // 运行责任链
        logicChain.process(context);
    }


    @Override
    public void raffleLogicTree(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            log.info("流程状态为TERMINATED，无需进行决策树流程");
            return;
        }
        // 获取抽奖规则树
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(context.getStrategyId(), context.getAwardId());
        // 运行规则树
        treeEngine.process(context);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() {
        return repository.takeQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        repository.updateStrategyAwardStock(strategyId, awardId);
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId) {
        return repository.queryStrategyAwardList(strategyId);
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId) {
        Long strategyId = repository.queryStrategyIdByActivityId(activityId);
        return queryRaffleStrategyAwardList(strategyId);
    }

    @Override
    public Map<Integer, Integer> queryAwardRuleLockCount(List<StrategyAwardEntity> strategyAwardEntities) {
        return repository.queryAwardRuleLockCount(strategyAwardEntities);
    }
}
