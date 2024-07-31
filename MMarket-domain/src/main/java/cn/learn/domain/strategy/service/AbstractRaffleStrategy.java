package cn.learn.domain.strategy.service;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.model.entity.RaffleAwardEntity;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;
import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.learn.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: MMarket
 * @description: 抽奖策略抽象类，定义抽奖的标准流程，可以认为是规则引擎
 * note: 【模板方法模式】
 * @author: chouchouGG
 * @create: 2024-06-13 14:37
 **/
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    // 抽奖的责任链工厂 -> 从抽奖的规则中，解耦出前置规则为责任链处理
    protected final DefaultChainFactory defaultChainFactory;

    // 抽奖的决策树工厂 -> 负责抽奖中到抽奖后的规则过滤，如抽奖到A奖品ID，之后要做奖品解锁的判断和库存的扣减等。
    protected final DefaultTreeFactory defaultTreeFactory;

    protected final IStrategyRepository repository;

    @Autowired
    public AbstractRaffleStrategy(DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory, IStrategyRepository repository) {
        this.defaultChainFactory = defaultChainFactory;
        this.defaultTreeFactory = defaultTreeFactory;
        this.repository = repository;
    }

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {

        // 1. 参数校验: 检查 raffleFactorEntity 中的 userId 和 strategyId 是否为空
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 构建责任链节点和决策树节点的状态流转对象context
        ProcessingContext context = ProcessingContext.builder()
                .userId(userId)
                .strategyId(strategyId)
                .endDateTime(raffleFactorEntity.getEndDateTime())
                .build();

        // 3. 责任链抽奖计算
        raffleLogicChain(context);
        log.info("抽奖策略计算-责任链：用户ID: {} 策略ID: {} 奖品ID: {} 规则模型: {} 处理状态: {} 结果描述: {}",
                context.getUserId(), context.getStrategyId(), context.getAwardId(), context.getRuleModel(), context.getStatus().getInfo(), context.getResultDesc());

        // 4. 规则树抽奖过滤【奖品ID，会根据抽奖次数判断、库存判断、兜底兜里返回最终可获得的奖品信息】
        raffleLogicTree(context);
        log.info("抽奖策略计算-规则树：用户ID: {} 策略ID: {} 奖品ID: {} 规则模型: {} 处理状态: {} 结果描述: {}",
                context.getUserId(), context.getStrategyId(), context.getAwardId(), context.getRuleModel(), context.getStatus().getInfo(), context.getResultDesc());

        // 5. 返回抽奖结果
        StrategyAwardEntity entity = repository.queryStrategyAwardEntity(context.getStrategyId(), context.getAwardId());
        return RaffleAwardEntity.builder()
                .awardId(entity.getAwardId())
                .awardTitle(entity.getAwardTitle())
                .awardConfig(context.getAwardRuleValue())
                .sort(entity.getSort())
                .build();
    }


    // 抽奖计算，责任链抽象方法
    public abstract void raffleLogicChain(ProcessingContext context);

    // 抽奖结果过滤，决策树抽象方法
    public abstract void raffleLogicTree(ProcessingContext context);

}
