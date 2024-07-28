package cn.learn.domain.task.service;


import cn.learn.domain.task.model.entity.TaskEntity;

import java.util.List;

/**
 * <h1>消息任务服务接口</h1>
 */
public interface ITaskService {

    /**
     * 查询发送MQ失败和超时1分钟未发送的MQ
     *
     * @return 未发送的任务消息列表10条
     */
    List<TaskEntity> queryNoSendMessageTaskList();

    /**
     * 调用 EventPublisherO（包装了RabbitTemplate）发布MQ消息
     * @param taskEntity {@link cn.learn.domain.task.model.entity.TaskEntity}
     */
    void sendMessage(TaskEntity taskEntity);

    /**
     * 更新任务的状态为 — 完成
     */
    void updateTaskSendMessageCompleted(String userId, String messageId);

    /**
     * 更新任务的状态为 — 失败
     */
    void updateTaskSendMessageFail(String userId, String messageId);

}