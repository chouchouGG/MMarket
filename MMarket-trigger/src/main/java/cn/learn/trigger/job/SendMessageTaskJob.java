package cn.learn.trigger.job;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.learn.domain.task.model.entity.TaskEntity;
import cn.learn.domain.task.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <h1>发送MQ消息任务队列</h1>
 */
@Slf4j
@Component
public class SendMessageTaskJob {

    @Resource
    private ITaskService taskService;

    @Resource
    private ThreadPoolExecutor executor;

    @Resource
    private IDBRouterStrategy dbRouter; // 分库分表

    @Scheduled(cron = "0/5 * * * * ?")
    public void exec() {
        try {
            // 获取分库数量
            int dbCount = dbRouter.dbCount();

            // 逐个库扫描表【每个库一个任务表】
            for (int dbIdx = 1; dbIdx <= dbCount; dbIdx++) {
                int finalDbIdx = dbIdx;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dbRouter.setDBKey(finalDbIdx);
                            dbRouter.setTBKey(0);

                            List<TaskEntity> taskEntities = taskService.queryNoSendMessageTaskList();
                            if (taskEntities.isEmpty()) {
                                // 没有待发送的任务
                                return;
                            }
                            // 【循环处理】：所有待处理的任务，执行发送MQ消息
                            for (TaskEntity taskEntity : taskEntities) {
                                // note: 【xfg建议】开启线程发送，提高发送效率。配置的线程池策略为 CallerRunsPolicy，在 ThreadPoolConfig 配置中有4个策略，面试中容易对比提问。可以检索下相关资料。
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // 发送消息
                                            taskService.sendMessage(taskEntity);
                                            // 更新状态（成功）
                                            taskService.updateTaskSendMessageCompleted(taskEntity.getUserId(), taskEntity.getMessageId());
                                            log.error("【定时任务】发送MQ消息 - 成功 userId: {} topic: {}", taskEntity.getUserId(), taskEntity.getTopic());
                                        } catch (Exception e) {
                                            log.error("【定时任务】发送MQ消息 - 失败 userId: {} topic: {}", taskEntity.getUserId(), taskEntity.getTopic());
                                            // 更新状态（失败）
                                            taskService.updateTaskSendMessageFail(taskEntity.getUserId(), taskEntity.getMessageId());
                                        }
                                    }
                                });
                            }
                        } finally {
                            dbRouter.clear();
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("定时任务，扫描MQ任务表发送消息失败。", e);
        }
    }

}
