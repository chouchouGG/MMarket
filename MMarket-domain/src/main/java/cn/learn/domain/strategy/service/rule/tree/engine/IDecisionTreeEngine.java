package cn.learn.domain.strategy.service.rule.tree.engine;

import cn.learn.domain.strategy.model.entity.ProcessingContext;

/**
 * @program: MMarket
 * @description: 决策树引擎接口
 * @author: chouchouGG
 * @create: 2024-06-20 18:01
 **/
public interface IDecisionTreeEngine {

    void process(ProcessingContext context);

}
