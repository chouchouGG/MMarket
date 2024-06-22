package cn.learn.domain.strategy.service.rule.chain.logicChain;

import cn.learn.domain.strategy.model.entity.ProcessingContext;

/**
 * @author 98389
 * @description 责任链启动接口
 * note：【责任链模式】
 *  1. 解耦请求处理流程：
 *      将每个处理流程抽取为一个单独的类，称为“处理者”，每个处理者在接收到请求后决定是否自行处理该请求，或沿着链进行传递该请求。
 *  2. 动态组合处理流程：
 *      可以在运行时动态地组合责任链中的处理对象。通过添加或删除处理对象，灵活地改变请求的处理流程。
 *  3. 增加处理对象的可扩展性：
 *      新的处理对象可以很容易地添加到责任链中，而不会影响其他处理对象。
 */
public interface ILogicChainHandler extends ILogicChainArmory {

    /**
     * process 方法用于定义当前节点处理流程:
     * 1. 调用当前责任链节点的处理方法
     * 2. 根据具体的业务逻辑自定义是否调用下一个责任链节点的处理流程
     */
    void process(ProcessingContext context);

    /**
     * 当前责任链节点的具体处理过程
     */
    void handle(ProcessingContext context);

}
