package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.learn.infrastructure.persistent.po.TaskPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <h1>任务表，发送MQ消息</h1>
 */
@Mapper
public interface ITaskDao {

    void insert(TaskPO task);

    @DBRouter
    void updateTaskSendMessageCompleted(TaskPO task);

    @DBRouter
    void updateTaskSendMessageFail(TaskPO task);

    /**
     * 查询未发送的消息任务
     */
    List<TaskPO> queryNoSendMessageTaskList();

}
