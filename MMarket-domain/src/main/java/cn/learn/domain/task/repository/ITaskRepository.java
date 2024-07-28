package cn.learn.domain.task.repository;

import cn.learn.domain.task.model.entity.TaskEntity;

import java.util.List;

/**
 * <h1>任务服务仓储接口</h1>
 * <h2>提供了4个方法，完成MQ消息发送的完整逻辑：
 * <ul>获取待处理的消息任务：{@link ITaskRepository#queryNoSendMessageTaskList()}</ul>
 * <ul>发送MQ消息：{@link ITaskRepository#sendMessage(TaskEntity)}</ul>
 * <ul>更新消息任务状态（成功）：{@link ITaskRepository#updateTaskSendMessageCompleted(String, String)}</ul>
 * <ul>更新消息任务状态（失败）：{@link ITaskRepository#updateTaskSendMessageFail(String, String)}</ul>
 * </h2>
 */
public interface ITaskRepository {

    List<TaskEntity> queryNoSendMessageTaskList();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskSendMessageCompleted(String userId, String messageId);

    void updateTaskSendMessageFail(String userId, String messageId);

}
