package cn.learn.domain.strategy.service.rule.chain.factory;

import cn.learn.domain.strategy.model.entity.StrategyEntity;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.chain.logicChain.ILogicChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 工厂
 * @create 2024-01-20 10:54
 */
@Service
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
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategy.getSeperatedRuleModels();

        // 1. 如果未配置策略规则，则提供一个仅包含默认责任链节点的责任链
        if (null == ruleModels || 0 == ruleModels.length) {
            return logicChainGroup.get("default");
        }

        // 2. 按照配置顺序装填用户配置的责任链
        // fixme：可以提供一个策略规则的优先级表，用于依照各个规则的优先级生成责任链节点，而无需关心配置的策略规则顺序对责任链节点的影响
        // rule_weight,rule_blacklist 「注意此数据从Redis缓存中获取，如果更新库表，记得在测试阶段手动处理缓存」
        ILogicChain logicChain = logicChainGroup.get(ruleModels[0]);
        ILogicChain current = logicChain;
        for (int i = 1; i < ruleModels.length; i++) {
            ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
            current = current.setNext(nextChain);
        }

        // 3. 责任链的最后装填默认责任链
        current.setNext(logicChainGroup.get("default"));

        return logicChain;
    }

}
