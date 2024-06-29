package cn.learn.domain.activity.service.chain;

import cn.learn.domain.strategy.service.rule.chain.logicChain.ILogicChain;

import java.util.Set;

/**
 * @program: MMarket
 * @description: 校验责任链的装配接口
 * @author: chouchouGG
 * @create: 2024-06-28 10:13
 **/
public interface ICheckChainArmory {

    ICheckChain setNext(ICheckChain next);


    /**
     * 获取下一个责任链节点
     * @return 下一个责任链节点
     */
    ICheckChain next();
}
