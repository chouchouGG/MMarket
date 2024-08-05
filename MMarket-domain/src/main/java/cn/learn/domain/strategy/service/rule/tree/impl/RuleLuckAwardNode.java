package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.learn.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.learn.domain.strategy.model.entity.ProcessingContext.ProcessStatus.TERMINATED;
import static cn.learn.types.common.Constants.RuleModel.RULE_LUCK_AWARD;

/**
 * @author: chouchouGG
 * @description 兜底奖励节点
 * @create: 2024-06-20 17:23
 */
@Slf4j
@Component(value = RULE_LUCK_AWARD)
public class RuleLuckAwardNode implements ILogicTreeNode {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public void execute(ProcessingContext context) {
        // 如果不需要走兜底规则，则直接返回
        if (!context.isNeedsFallbackAward()) {
//            log.info("不需要走兜底处理");
            return;
        }
//        log.info("用户【{}】，参与抽奖活动【{}】，进行【{}】", context.getUserId(), context.getStrategyId(), RULE_LUCK_AWARD);

        // 解析兜底的随机积分值配置，数据示例："101:81,100"
        String[] split = parseRuleValueForRandomPoint(context.getStrategyId(), context.getAwardId(), RULE_LUCK_AWARD);
        Integer awardId = Integer.valueOf(split[0]);
        String awardRuleValue = split.length > 1 ? split[1] : "";

        // 写入延迟队列，延迟消费更新数据库记录。【在trigger的job；UpdateAwardStockJob 下消费队列，更新数据库记录】
        strategyRepository.awardStockConsumeSendQueue(StrategyAwardStockKeyVO.builder()
                .strategyId(context.getStrategyId())
                .awardId(awardId)
                .build());

        // 返回兜底奖品
        context.setAwardId(awardId);
        context.setAwardRuleValue(awardRuleValue);
        context.setRuleModel(RULE_LUCK_AWARD);
        context.setStatus(TERMINATED);
        context.setResultDesc("发放兜底奖品，处理流程终止");

//        log.info("抽奖决策树-【兜底奖励节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
//                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }

    // fixme：不同的奖品值的解析方法不同，这里可以使用策略模式提供多种不同的解析方式
    private String[] parseRuleValueForRandomPoint(Long strategyId, Integer awardId, String ruleModel) {
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, awardId, ruleModel);
        String[] split = ruleValue.split(Constants.COLON);
        if (split.length == 0) {
            String errormessage = String.format("奖品ID为【%d】的兜底奖品未配置", awardId);
            throw new RuntimeException(errormessage);
        }
        return split;
    }

}
