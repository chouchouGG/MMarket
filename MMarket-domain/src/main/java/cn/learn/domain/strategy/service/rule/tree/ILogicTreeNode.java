package cn.learn.domain.strategy.service.rule.tree;

import cn.learn.domain.strategy.model.entity.ProcessingContext;

/**
 * @program: MMarket
 * @description: 【组合模式】的接口
 * @author: chouchouGG
 * @create: 2024-06-19 15:41
 **/
public interface ILogicTreeNode {

    /**
     * note：
     *  · 组合设计模式，将流程中的每个步骤作为一个节点
     *  · 将库存扣减也抽象为树节点
     *  ·
     *
     */

    void execute(ProcessingContext context);


}
