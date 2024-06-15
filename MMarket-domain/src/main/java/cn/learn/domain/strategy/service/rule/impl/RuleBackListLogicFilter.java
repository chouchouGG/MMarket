package cn.learn.domain.strategy.service.rule.impl;

import cn.learn.domain.strategy.model.entity.RuleActionEntity;
import cn.learn.domain.strategy.model.entity.RuleMatterEntity;
import cn.learn.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.annotation.LogicStrategy;
import cn.learn.domain.strategy.service.rule.ILogicFilter;
import cn.learn.domain.strategy.service.rule.factory.DefaultLogicFactory;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 【抽奖前规则】黑名单用户过滤规则
 * @create 2024-01-06 13:19
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_BLACKLIST) // 自定义注解，
public class RuleBackListLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource
    private IStrategyRepository repository;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        String userId = ruleMatterEntity.getUserId();
        Long strategyId = ruleMatterEntity.getStrategyId();
        String ruleModel = ruleMatterEntity.getRuleModel();


        log.info("规则过滤-黑名单 userId:{} strategyId:{} ruleModel:{} info:{}",
                userId,
                strategyId,
                ruleModel,
                DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getInfo());


        // 查询规则值 rule_value
        String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());

        // 获取类注解中的 logicMode 属性
        LogicStrategy logicStrategy = this.getClass().getAnnotation(LogicStrategy.class);
        DefaultLogicFactory.LogicModel logicModel = logicStrategy.logicMode();

        // 数据案例；100:user001,user002,user003
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        // note：黑名单用户的默认奖品ID
        Integer awardId = Integer.parseInt(splitRuleValue[0]);
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String userBlackId : userBlackIds) {
            if (userId.equals(userBlackId)) {
                // 用户在黑名单中
                return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                        .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                        .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                        .ruleModel(logicModel.getCode())
                        .data(RuleActionEntity.RaffleBeforeEntity.builder()
                                        .strategyId(ruleMatterEntity.getStrategyId())
                                        .awardId(awardId)
                                        .build())
                        .build();
            }
        }

        // 用户不在黑名单中
        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();
    }

}
