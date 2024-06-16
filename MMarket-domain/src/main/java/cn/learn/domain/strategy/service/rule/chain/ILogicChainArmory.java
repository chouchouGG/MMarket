package cn.learn.domain.strategy.service.rule.chain;

/**
 * @author 98389
 * @description 责任链装配接口
 */
public interface ILogicChainArmory {

    /**
     * 设置当前责任链节点的下一个节点
     * @param next 下一个责任链节点
     * @return 当前责任链节点本身，用于链式调用
     */
    ILogicChain setNext(ILogicChain next);


    /**
     * 获取下一个责任链节点
     * @return 下一个责任链节点
     */
    ILogicChain next();
}
