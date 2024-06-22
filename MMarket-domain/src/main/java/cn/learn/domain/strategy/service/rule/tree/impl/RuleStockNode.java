package cn.learn.domain.strategy.service.rule.tree.impl;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.armory.IStrategyDispatch;
import cn.learn.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.learn.domain.strategy.model.entity.ProcessingContext.ProcessStatus.CONTINUE;
import static cn.learn.domain.strategy.model.entity.ProcessingContext.ProcessStatus.TERMINATED;
import static cn.learn.types.common.Constants.RuleModel.RULE_STOCK;

/**
 * @author: chouchouGG
 * @description 库存扣减节点
 *              note: 【库存扣减节点】的逻辑需要重点关注
 * @create: 2024-06-20 17:23
 */
@Slf4j
@Component(value = RULE_STOCK)
public class RuleStockNode implements ILogicTreeNode {

    @Autowired
    private IStrategyDispatch strategyDispatch;

    /**
     * fixme：当前通过 redis 的缓存操作，来实现发送异步的队列消息用于更新数据库中库存信息避免每次抽奖都访问数据库导致连接被过多占用，后期考虑加入MQ
     */
    @Autowired
    private IStrategyRepository repository;

    @Override
    public void execute(ProcessingContext context) {
        if (context.getStatus() == TERMINATED) {
            return;
        }

        // note: 奖品库存扣减是重点内容，需要深入理解整个流程
        // fixme: 个人理解 subtractionAwardStock 应该由仓储层直接调用，而不是 strategyDispatch，后续优化
        // 1. 扣减库存
        Boolean isSuccessful = strategyDispatch.subtractionAwardStock(context.getStrategyId(), context.getAwardId());

        // 2. 检查是否扣减成功
        context.setRuleModel(RULE_STOCK);
        if (!isSuccessful) {
            // 不成功
            context.setStatus(TERMINATED);
            context.setResultDesc("奖品库存数量不够");
            context.setNeedsFallbackAward(true);
        } else {
            // 成功
            context.setStatus(CONTINUE);
            context.setResultDesc("奖品库存数量足够，库存扣减成功");
            // 3. 写入延迟队列，延迟消费更新数据库记录。【在trigger的job；UpdateAwardStockJob 下消费队列，更新数据库记录】
            delayStockUpdate(context.getStrategyId(), context.getAwardId());
        }

        log.info("抽奖决策树-【库存扣减节点】 规则模型：{} 奖品ID：{} 执行状态：{} 结果描述：{}",
                context.getRuleModel(), context.getAwardId(), context.getStatus(), context.getResultDesc());
        return;
    }

    private void delayStockUpdate(Long strategyId, Integer awardId) {
        // 构建
        StrategyAwardStockKeyVO strategyAwardStockKeyVO = StrategyAwardStockKeyVO.builder()
                .strategyId(strategyId)
                .awardId(awardId).build();
        repository.awardStockConsumeSendQueue(strategyAwardStockKeyVO);
    }

}
