package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: MMarket
 * @description: 复合节点，用于组合多个简单的单个节点进行分类
 * @author: chouchouGG
 * @create: 2024-06-21 10:06
 **/
public class CompositeNode implements ILogicTreeNode {

    private final List<ILogicTreeNode> children  = new ArrayList<>();

    /**
     * 为复合节点添加孩子节点
     * @param node 待添加的孩子节点
     */
    public void addChild(ILogicTreeNode node) {
        children.add(node);
    }

    @Override
    public void execute(ProcessingContext context) {
        // 依次处理复合节点中的每个孩子节点
        for (ILogicTreeNode child : children ) {
            // 先检查执行流状态，若为终止，则返回。
            if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
                break;
            }
            // 执行当前孩子节点的处理逻辑
            child.execute(context);
        }
        return;
    }

}
