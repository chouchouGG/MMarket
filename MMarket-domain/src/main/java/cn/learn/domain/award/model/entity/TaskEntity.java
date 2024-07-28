package cn.learn.domain.award.model.entity;

import cn.learn.domain.award.event.SendAwardMessageEvent;
import cn.learn.domain.award.model.valobj.TaskStateVO;
import cn.learn.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * note：虽然一般情况下，Entity 实体类和 PO 持久化类是一对一的，但是其类型却有可能是有差异的。
 *  原因在于：实体对象偏向于具体的业务实现，而持久化对象是与数据库进行对应。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity {

    /** 活动ID */
    private String userId;
    /** 消息主题 */
    private String topic;
    /** 消息编号 */
    private String messageId;
    /** 消息主体 */
    private BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> message;
    /** 任务状态；create-创建、completed-完成、fail-失败 */
    private TaskStateVO state;

}