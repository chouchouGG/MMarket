package cn.learn.domain.strategy.service.rule.tree.engine.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.engine.IDecisionTreeEngine;
import cn.learn.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.learn.domain.strategy.service.rule.tree.impl.CompositeNode;
import lombok.extern.slf4j.Slf4j;

/**
 * @program: MMarket
 * @description: 决策树引擎
 * @author: chouchouGG
 * @create: 2024-06-20 18:02
 **/
@Slf4j
public class RuleDecisionTreeEngine implements IDecisionTreeEngine {

    /**
     * 决策树的根节点：
     * 决策树的起始节点(根节点)，从这里开始执行决策树的处理逻辑。
     * 根节点通常是一个组合节点（CompositeTreeNode），它包含了多个子节点，
     * 每个子节点都可以是其他[组合节点]或者[叶子节点]，形成树形结构。
     */
    private final CompositeNode rootNode;


    /**
     * 构造函数，初始化决策树引擎。
     *
     * @param rootNode 决策树的根节点
     */
    public RuleDecisionTreeEngine(CompositeNode rootNode) {
        this.rootNode = rootNode;
    }


    /**
     * 处理决策树的入口方法。
     * 从根节点开始执行决策树，依次处理各个节点，直至终止条件或完成所有节点的处理。
     *
     * @param context 传入的处理上下文对象，包含了执行所需的所有信息和状态。
     * @return 返回处理后的上下文对象，包含最终的处理结果和状态信息。
     */
    @Override
    public void process(ProcessingContext context) {
        // 从根节点开始执行决策树逻辑
        rootNode.execute(context);
        log.info("最终决策树节点：{} 处理结果信息: {}", context.getRuleModel(), context.getStatus().getInfo());
    }

}
