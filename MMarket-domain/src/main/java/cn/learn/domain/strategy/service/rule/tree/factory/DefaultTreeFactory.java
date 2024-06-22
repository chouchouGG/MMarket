package cn.learn.domain.strategy.service.rule.tree.factory;

import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.learn.domain.strategy.service.rule.tree.engine.IDecisionTreeEngine;
import cn.learn.domain.strategy.service.rule.tree.engine.impl.RuleDecisionTreeEngine;
import cn.learn.domain.strategy.service.rule.tree.impl.*;
import cn.learn.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

import static cn.learn.types.enums.ResponseCode.RULE_NOT_DEFINED;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-06-20 17:32
 **/
@Slf4j
@Service
public class DefaultTreeFactory {

    private final IStrategyRepository repository;

    private final Map<String, ILogicTreeNode> decisionTreeNodes;

    @Autowired
    public DefaultTreeFactory(IStrategyRepository repository, Map<String, ILogicTreeNode> decisionTreeNodes) {
        this.repository = repository;
        this.decisionTreeNodes = decisionTreeNodes;
    }


    public ILogicTreeNode getSpecificTreeNode(String treeNodeName) {
        return decisionTreeNodes.get(treeNodeName);
    }

    public IDecisionTreeEngine openLogicTree(Long strategyId, Integer awardId) {
        // 1. 获取策略奖品配置的抽奖规则
        log.info("开始获取策略奖品配置的抽奖规则，策略ID：{}，奖品ID：{}", strategyId, awardId);
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelVO(strategyId, awardId);
        log.info("当前奖品【{}】的规则列表为：{}", awardId, strategyAwardRuleModelVO.getRuleModels());
        log.info("开始构建决策树>>>>>>>>>>>>>>>>");
        // 2. 默认构建【用户属性验证组】
        log.info("开始构建用户属性验证组");
        CompositeNode userAttributeGroup = new CompositeNode();
        userAttributeGroup.addChild(new DailyLimitNode());
        log.info("\t用户属性验证组添加 DailyLimitNode 节点");
        userAttributeGroup.addChild(new WinFrequencyNode());
        log.info("\t用户属性验证组添加 WinFrequencyNode 节点");

        // 3. 根据数据库配置进行构建【奖品属性验证组】
        log.info("开始构建奖品属性验证组");
        CompositeNode prizeAttributeGroup = new CompositeNode();
        // 默认添加：奖品类型限制规则
        prizeAttributeGroup.addChild(new PrizeTypeRestrictionNode());
        log.info("\t奖品属性验证组添加 PrizeTypeRestrictionNode 节点");
        // 默认添加：库存规则
        prizeAttributeGroup.addChild(new RuleStockNode());
        log.info("\t奖品属性验证组添加 RuleStockNode 节点");

        // 从策略奖品配置中获取规则模型数组
        String[] ruleModels = strategyAwardRuleModelVO.getRuleModelsArray();

        for (String ruleModel : ruleModels) {
            ILogicTreeNode node = decisionTreeNodes.get(ruleModel);
            if (node == null) {
                log.info("warn: 未定义规则【{}】对应的决策树节点", ruleModel);
                throw new AppException(RULE_NOT_DEFINED.getCode(), "未定义规则【" + ruleModel + "】对应的决策树节点");
            } else {
                prizeAttributeGroup.addChild(node);
                log.info("\t奖品属性验证组添加 {} 节点", ruleModel);
            }
        }

        // 4. 构建根节点
        log.info("开始构建根节点");
        CompositeNode rootNode = new CompositeNode();
        rootNode.addChild(userAttributeGroup);
        log.info("\t根节点添加->用户属性验证组");
        rootNode.addChild(prizeAttributeGroup);
        log.info("\t根节点添加->奖品属性验证组");

        log.info("<<<<<<<<<<<<<<<<<<<<构建决策树完成");
        return new RuleDecisionTreeEngine(rootNode, this);
    }

}
