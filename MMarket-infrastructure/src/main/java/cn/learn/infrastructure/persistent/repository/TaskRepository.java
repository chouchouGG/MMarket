package cn.learn.infrastructure.persistent.repository;

import cn.learn.domain.task.model.entity.TaskEntity;
import cn.learn.domain.task.repository.ITaskRepository;
import cn.learn.infrastructure.event.EventPublisher;
import cn.learn.infrastructure.persistent.dao.ITaskDao;
import cn.learn.infrastructure.persistent.po.TaskPO;
import javafx.concurrent.Task;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>任务服务仓储实现</h1>
 */
@Repository
public class TaskRepository implements ITaskRepository {

    @Resource
    private ITaskDao taskDao;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public List<TaskEntity> queryNoSendMessageTaskList() {
        List<TaskPO> taskPOs = taskDao.queryNoSendMessageTaskList();
        // 存储满足条件的消息任务实体：
        // SQL中的条件：where state = 'fail' or (state = 'create' and now() - update_time > 60000)
        List<TaskEntity> taskEntities = new ArrayList<>(taskPOs.size());
        for (TaskPO taskPO : taskPOs) {
            TaskEntity taskEntity = TaskEntity.builder()
                    .userId(taskPO.getUserId())
                    .topic(taskPO.getTopic())
                    .messageId(taskPO.getMessageId())
                    .message(taskPO.getMessage())
                    .build();
            taskEntities.add(taskEntity);
        }
        return taskEntities;
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
    }

    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        TaskPO task = TaskPO.builder().userId(userId).messageId(messageId).build();
        taskDao.updateTaskSendMessageCompleted(task);
    }

    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        TaskPO task = TaskPO.builder().userId(userId).messageId(messageId).build();
        taskDao.updateTaskSendMessageFail(task);
    }

}
