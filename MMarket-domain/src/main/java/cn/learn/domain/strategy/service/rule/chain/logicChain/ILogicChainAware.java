package cn.learn.domain.strategy.service.rule.chain.logicChain;

/**
 * @program: MMarket
 * @description: 提供【获取规则过滤的责任链节点的上下文或环境信息】方法
 * @author: chouchouGG
 * @create: 2024-06-16 10:37
 **/
public interface ILogicChainAware {

    /**
     * 获取规则过滤的责任链节点对应的 rule_model 名称。
     * @return rule_model 名称
     */
    String getRuleModelName();

}
