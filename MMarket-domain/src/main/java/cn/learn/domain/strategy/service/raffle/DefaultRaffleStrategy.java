package cn.learn.domain.strategy.service.raffle;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.AbstractRaffleStrategy;
import cn.learn.domain.strategy.service.armory.IStrategyDispatch;
import cn.learn.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.learn.domain.strategy.service.rule.chain.logicChain.ILogicChain;
import cn.learn.domain.strategy.service.rule.tree.engine.IDecisionTreeEngine;
import cn.learn.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-06-14 15:00
 **/
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {


    public DefaultRaffleStrategy(DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
        super(defaultChainFactory, defaultTreeFactory);
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

}
