package cn.learn.domain.strategy.service;

import cn.learn.domain.strategy.model.entity.*;
import cn.learn.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.armory.IStrategyDispatch;
import cn.learn.domain.strategy.service.rule.chain.logicChain.ILogicChain;
import cn.learn.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
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

    // 策略仓储服务 -> domain层像一个大厨，仓储层提供米面粮油
    protected IStrategyRepository repository;
    // 策略调度服务 -> 只负责抽奖处理，通过新增接口的方式，隔离职责，不需要使用方关心或者调用抽奖的初始化
    protected IStrategyDispatch strategyDispatch;
    // 抽奖的责任链工厂类 -> 从抽奖的规则中，解耦出前置规则为责任链处理
    private final DefaultChainFactory defaultChainFactory;

    @Autowired
    public AbstractRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory) {
        this.repository = repository;
        this.strategyDispatch = strategyDispatch;
        this.defaultChainFactory = defaultChainFactory;
    }

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {

        // 1. 参数校验: 检查 raffleFactorEntity 中的 userId 和 strategyId 是否为空
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),
                    ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 获取抽奖责任链 - 进行前置规则的责任链处理
        ILogicChain LogicChain = defaultChainFactory.openLogicChain(strategyId);

        ProcessingContext.LogicChainContextBuilder builder = ProcessingContext.builder();
        builder.userId(userId);
        builder.strategyId(strategyId);
        builder.awardId(raffleFactorEntity.getAwardId());
        ProcessingContext context = builder.build();

        // 3. 通过责任链获得，奖品ID
        ProcessingContext ret = LogicChain.process(context);
        Integer awardId = ret.getAwardId();

/** ==================== 【已重构的部分】 ====================
        // >>> fixme：使用责任链模式重构部分：起始 ========================================================================
        // 2. 策略查询
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModelsBefore = strategy.getSeperatedRuleModels();

        // 3. 抽奖前 - 规则过滤
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity =
                this.doCheckRaffleBeforeLogic(raffleFactorEntity, ruleModelsBefore);

        // note：判断抽奖【前】的规则过滤是否捕获到当前抽奖流程，如果捕获到，则由规则引擎进行接管。
        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionEntity.getCode())) {
            if (DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode().equals(ruleActionEntity.getRuleModel())) {
                // 黑名单返回固定的奖品ID
                return RaffleAwardEntity.builder()
                        .awardId(ruleActionEntity.getData().getAwardId())
                        .awardDesc("抽奖前黑名单规则拦截，发放黑名单奖品ID：" + ruleActionEntity.getData().getAwardId())
                        .build();
            } else if (DefaultLogicFactory.LogicModel.RULE_WIGHT.getCode().equals(ruleActionEntity.getRuleModel())) {
                // 权重根据返回的信息进行抽奖
                // fixme: 权重抽奖完后，应该也需要进行抽奖中的规则过滤（奖品解锁规则，兜底规则...）
                RuleActionEntity.RaffleBeforeEntity raffleBeforeEntity = ruleActionEntity.getData();
                String ruleWeightValueKey = raffleBeforeEntity.getRuleWeightValueKey();
                Integer awardId = strategyDispatch.getRandomAwardId(strategyId, ruleWeightValueKey);
                return RaffleAwardEntity.builder()
                        .awardId(awardId)
                        .awardDesc("抽奖前权重规则拦截，抽到的奖品ID为：" + awardId)
                        .build();
            }
        }

        // 4. 前置规则过滤完毕，进行【默认抽奖】流程
        // note：规则引擎的默认抽奖方式
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);
        // <<< fixme：使用责任链模式重构部分：结束 ========================================================================
*/

        // 4. 查询奖品规则，包括：【1. 抽奖中（拿到奖品ID时，过滤规则），2. 抽奖后（扣减完奖品库存后过滤，抽奖中拦截和无库存则走兜底）】
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelVO(strategyId, awardId);

        // 5. 抽奖中 - 规则过滤
        raffleFactorEntity.setAwardId(awardId);
        String[] ruleModelsCenter = strategyAwardRuleModelVO.raffleCenterRuleModelList();
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionCenterEntity =
                this.doCheckRaffleCenterLogic(raffleFactorEntity, ruleModelsCenter);

        // note: 判断抽奖中的规则过滤是否捕获到当前抽奖流程，如果捕获到，则由规则引擎进行接管
        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionCenterEntity.getCode())) {
            log.info("【临时日志】中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。");
            return RaffleAwardEntity.builder()
                    .awardDesc("抽奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。")
                    .build();
        }

        return RaffleAwardEntity.builder()
                .awardId(awardId)
                .build();
    }

    // fixme：通过责任链模式进行重构
    @Deprecated
    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics);

    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics);

}
