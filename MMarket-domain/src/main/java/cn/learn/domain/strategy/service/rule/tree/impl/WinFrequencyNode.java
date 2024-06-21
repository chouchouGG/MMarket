package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cn.learn.types.common.Constants.RuleModel.RULE_WIN_FREQUENCY;

/**
 * @program: MMarket
 * @description: 中奖频率节点
 * @author: chouchouGG
 * @create: 2024-06-21 11:40
 **/
@Slf4j
@Component(value = RULE_WIN_FREQUENCY)
public class WinFrequencyNode implements ILogicTreeNode {

    @Override
    public void execute(ProcessingContext context) {
        if (context.getStatus() == ProcessingContext.ProcessStatus.TERMINATED) {
            return;
        }

        // fixme: 用户中奖频率验证逻辑，暂未实现
        context.setStatus(ProcessingContext.ProcessStatus.CONTINUE);
        log.info("【中奖频率节点】- 暂未实现，默认通过【CONTINUE】");
        return;
    }

}