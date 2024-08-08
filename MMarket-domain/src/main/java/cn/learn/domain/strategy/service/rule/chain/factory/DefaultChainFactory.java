package cn.learn.domain.strategy.service.rule.chain.factory;

import cn.learn.domain.strategy.model.entity.StrategyEntity;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.chain.logicChain.ILogicChain;
import cn.learn.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

import static cn.learn.types.enums.ResponseCode.RULE_NOT_DEFINED;


/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 工厂
 * @create 2024-01-20 10:54
 */
@Service
@Slf4j
public class DefaultChainFactory {

    private final Map<String, ILogicChain> logicChainGroup;

    protected IStrategyRepository repository;

    @Autowired
    public DefaultChainFactory(Map<String, ILogicChain> logicChainGroup, IStrategyRepository repository) {
        this.logicChainGroup = logicChainGroup;
        this.repository = repository;
    }

    /**
     * 通过策略ID，根据数据库中的配置信息，构建责任链并返回该责任链
     *
     * @param strategyId 策略ID
     * @return LogicChain
     */
    public ILogicChain openLogicChain(Long strategyId) {
        // fixme：每次抽取都要重新生成 strategyId 决定的责任链，待优化
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategy.getSeperatedRuleModels();
        log.info("策略ID：" + strategyId + " 的规则模型为：" + Arrays.toString(ruleModels));

//        log.info(">>>>>>>>>>>>>>>>>>>>开始为策略ID：" + strategyId + " 构建责任链");
        // 1. 如果未配置策略规则，则提供一个仅包含默认责任链节点的责任链
        if (null == ruleModels || 0 == ruleModels.length) {
            log.info("\t未配置策略规则，返回默认责任链");
            return logicChainGroup.get("default");
        }

        // 2. 按照配置顺序装填用户配置的责任链
        // fixme：可以提供一个策略规则的优先级表，用于依照各个规则的优先级生成责任链节点，而无需关心配置的策略规则顺序对责任链节点的影响
        // rule_weight,rule_blacklist 「注意此数据从Redis缓存中获取，如果更新库表，记得在测试阶段手动处理缓存」
//        log.info("按照配置顺序装填用户配置的责任链");
        ILogicChain logicChain = logicChainGroup.get(ruleModels[0]);
        if (logicChain == null) {
//            log.warn("\t未找到规则模型对应的责任链节点：" + ruleModels[0]);
            throw new AppException(RULE_NOT_DEFINED.getCode(), "未找到规则模型对应的责任链节点：" + ruleModels[0]);
        }
        log.info("\t初始化责任链节点：" + ruleModels[0]);
        ILogicChain current = logicChain;
        for (int i = 1; i < ruleModels.length; i++) {
            ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
            if (nextChain == null) {
//                log.warn("\t未找到规则模型对应的责任链节点：" + ruleModels[i]);
                throw new AppException(RULE_NOT_DEFINED.getCode(), "未找到规则模型对应的责任链节点：" + ruleModels[i]);
            }
//            log.info("添加责任链节点：" + ruleModels[i]);
            current = current.setNext(nextChain);
        }

        // 3. 责任链的最后装填默认责任链
//        log.info("\t添加默认责任链节点：default");
        current.setNext(logicChainGroup.get("default"));

//        log.info("<<<<<<<<<<<<<<<<<<<<责任链构建完成");
        return logicChain;
    }

}
