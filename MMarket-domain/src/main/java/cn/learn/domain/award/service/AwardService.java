package cn.learn.domain.award.service;

import cn.learn.domain.award.event.SendAwardMessageEvent;
import cn.learn.domain.award.model.aggregate.UserAwardRecordAggregate;
import cn.learn.domain.award.model.entity.TaskEntity;
import cn.learn.domain.award.model.entity.UserAwardRecordEntity;
import cn.learn.domain.award.model.valobj.TaskStateVO;
import cn.learn.domain.award.repository.IAwardRepository;
import cn.learn.types.event.BaseEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: MMarket
 * @description: <h1>奖品服务</h1>
 * @author: chouchouGG
 * @create: 2024-07-27 23:28
 **/
@Service
public class AwardService implements IAwardService {

    @Resource
    private IAwardRepository awardRepository;

    @Resource
    private SendAwardMessageEvent sendAwardMessageEvent;

    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {
        // 1. 构建MQ消息事件对象（为构建任务对象使用）
        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> sendAwardMessageEventMessage = sendAwardMessageEvent.buildEventMessage(
                SendAwardMessageEvent.SendAwardMessage.builder()
                        .userId(userAwardRecordEntity.getUserId())
                        .awardId(userAwardRecordEntity.getAwardId())
                        .awardTitle(userAwardRecordEntity.getAwardTitle())
                        .build()
        );

        // 2. 构建消息发送的任务对象
        TaskEntity taskEntity = TaskEntity.builder()
                .userId(userAwardRecordEntity.getUserId())
                .topic(sendAwardMessageEvent.topic())
                .messageId(sendAwardMessageEventMessage.getId())
                .message(sendAwardMessageEventMessage)
                .state(TaskStateVO.create)
                .build();

        /**
         * note：通过MQ消息进行奖品发放，但是为了避免由于网络原因造成MQ消息接收失败，
         *  所以将订单入库和消息任务合并为一个事务先写入到数据库，之后可以通过定时任务对消息任务进行兜底。
         */
        // 3.构建聚合对象
        UserAwardRecordAggregate userAwardRecordAggregate = UserAwardRecordAggregate.builder()
                .taskEntity(taskEntity)
                .userAwardRecordEntity(userAwardRecordEntity)
                .build();

        // 4.存储聚合对象（一个事务下，用户的中奖记录）
        awardRepository.saveUserAwardRecord(userAwardRecordAggregate);
    }
}
