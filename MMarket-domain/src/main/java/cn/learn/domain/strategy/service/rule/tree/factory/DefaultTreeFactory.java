package cn.learn.domain.strategy.service.rule.tree.factory;

import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.learn.domain.strategy.service.rule.tree.engine.IDecisionTreeEngine;
import cn.learn.domain.strategy.service.rule.tree.engine.impl.RuleDecisionTreeEngine;
import cn.learn.domain.strategy.service.rule.tree.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Map;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-06-20 17:32
 **/
@Service
public class DefaultTreeFactory {

    private final Map<String, ILogicTreeNode> decisionTreeNodes;

    @Autowired
    public DefaultTreeFactory(Map<String, ILogicTreeNode> logicTreeNodeGroup) {
        this.decisionTreeNodes = logicTreeNodeGroup;
    }

    public ILogicTreeNode getSpecificTreeNode(String treeNodeName) {
        return decisionTreeNodes.get(treeNodeName);
    }

    public IDecisionTreeEngine openLogicTree() {
        // 用户属性验证组
        CompositeNode userAttributeGroup = new CompositeNode();
        userAttributeGroup.addChild(new DailyLimitNode());
        userAttributeGroup.addChild(new WinFrequencyNode());

        // 奖品属性验证组
        CompositeNode prizeAttributeGroup = new CompositeNode();
        prizeAttributeGroup.addChild(new RuleLockNode());
        prizeAttributeGroup.addChild(new RuleStockNode());
        prizeAttributeGroup.addChild(new PrizeTypeRestrictionNode());
        prizeAttributeGroup.addChild(new RuleLuckAwardNode());

        // 根节点
        CompositeNode rootNode = new CompositeNode();
        rootNode.addChild(userAttributeGroup);
        rootNode.addChild(prizeAttributeGroup);

        return new RuleDecisionTreeEngine(rootNode, this);
    }

}
