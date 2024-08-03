package cn.learn.domain.rebate.service;

import cn.learn.domain.award.model.valobj.TaskStateVO;
import cn.learn.domain.rebate.event.SendRebateMessageEvent;
import cn.learn.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.learn.domain.rebate.model.entity.BehaviorEntity;
import cn.learn.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.learn.domain.rebate.model.entity.DailyBehaviorRebateEntity;
import cn.learn.domain.rebate.model.entity.TaskEntity;
import cn.learn.domain.rebate.repository.IBehaviorRebateRepository;
import cn.learn.types.common.Constants;
import cn.learn.types.event.BaseEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: MMarket
 * @description: <h1>行为返利服务实现</h1>
 * @author: chouchouGG
 * @create: 2024-08-03 13:42
 **/
@Service
public class BehaviorRebateService implements IBehaviorRebateService {

    @Resource
    private IBehaviorRebateRepository behaviorRebateRepository;
    @Resource
    private SendRebateMessageEvent sendRebateMessageEvent; // MQ消息构建器

    @Override
    public List<String> createBehaviorRewardOrder(BehaviorEntity behaviorEntity) {
        // 1. 查询当前行为触发的所有奖励配置
        List<DailyBehaviorRebateEntity> dailyBehaviorRebateConfigs = behaviorRebateRepository.queryDailyBehaviorRebateConfig(behaviorEntity.getBehaviorType());
        if (null == dailyBehaviorRebateConfigs || dailyBehaviorRebateConfigs.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> orderIds = new ArrayList<>(); // 返回给调用方所有返利订单的订单ID
        List<BehaviorRebateAggregate> behaviorRebateAggregates = new ArrayList<>(); // 内部进行事务，保存所有返利的聚合对象

        // 循环处理当前行为触发的所有奖励
        for (DailyBehaviorRebateEntity dailyBehaviorRebate : dailyBehaviorRebateConfigs) {
            // 1. 拼装订单业务ID；用户ID_返利类型_外部透穿的业务ID
            String bizId = concatBizId(behaviorEntity, dailyBehaviorRebate);

            // 2. 构建返利订单对象
            BehaviorRebateOrderEntity behaviorRebateOrderEntity = BehaviorRebateOrderEntity.builder()
                    .userId(behaviorEntity.getUserId())
                    .orderId(RandomStringUtils.randomNumeric(12))
                    .behaviorType(dailyBehaviorRebate.getBehaviorType())
                    .rebateDesc(dailyBehaviorRebate.getRebateDesc())
                    .rebateType(dailyBehaviorRebate.getRebateType())
                    .rebateConfig(dailyBehaviorRebate.getRebateConfig())
                    .bizId(bizId)
                    .build();
            orderIds.add(behaviorRebateOrderEntity.getOrderId());

            // 3. 构建任务对象，封装MQ消息
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage =
                    sendRebateMessageEvent.buildEventMessage(SendRebateMessageEvent.RebateMessage.builder()
                            .userId(behaviorEntity.getUserId())
                            .rebateType(dailyBehaviorRebate.getBehaviorType())
                            .rebateConfig(dailyBehaviorRebate.getRebateConfig())
                            .bizId(bizId)
                            .build());
            TaskEntity taskEntity = TaskEntity.builder()
                    .userId(behaviorEntity.getUserId())
                    .topic(sendRebateMessageEvent.topic())
                    .messageId(rebateMessageEventMessage.getId())
                    .message(rebateMessageEventMessage)
                    .state(TaskStateVO.create)
                    .build();

            // 4. 构建聚合对象，用于事务
            BehaviorRebateAggregate behaviorRebateAggregate = BehaviorRebateAggregate.builder()
                    .userId(behaviorEntity.getUserId())
                    .behaviorRebateOrderEntity(behaviorRebateOrderEntity)
                    .taskEntity(taskEntity)
                    .build();

            behaviorRebateAggregates.add(behaviorRebateAggregate);
        }

        // 存储聚合对象数据（事务）
        behaviorRebateRepository.saveUserRebateRecord(behaviorEntity.getUserId(), behaviorRebateAggregates);

        // 返回订单ID集合
        return orderIds;
    }

    /**
     * @return "用户id_返利类型_外部业务id"
     */
    private static String concatBizId(BehaviorEntity behaviorEntity, DailyBehaviorRebateEntity dailyBehaviorRebateVO) {
        return behaviorEntity.getUserId() +
                Constants.UNDERLINE +
                dailyBehaviorRebateVO.getRebateType() + 
                Constants.UNDERLINE + 
                behaviorEntity.getOutBusinessNo();
    }

}
