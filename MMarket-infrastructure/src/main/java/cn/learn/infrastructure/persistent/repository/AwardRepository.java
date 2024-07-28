package cn.learn.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.learn.domain.award.model.aggregate.UserAwardRecordAggregate;
import cn.learn.domain.award.model.entity.TaskEntity;
import cn.learn.domain.award.model.entity.UserAwardRecordEntity;
import cn.learn.domain.award.repository.IAwardRepository;
import cn.learn.infrastructure.event.EventPublisher;
import cn.learn.infrastructure.persistent.dao.ITaskDao;
import cn.learn.infrastructure.persistent.dao.IUserAwardRecordDao;
import cn.learn.infrastructure.persistent.po.TaskPO;
import cn.learn.infrastructure.persistent.po.UserAwardRecordPO;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
 * <h1>奖品仓储服务</h1>
 */
@Slf4j
@Component
public class AwardRepository implements IAwardRepository {

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IUserAwardRecordDao userAwardRecordDao;

    @Resource
    private IDBRouterStrategy dbRouter; // 分库分表

    @Resource
    private TransactionTemplate transactionTemplate; // 事务

    @Resource
    private EventPublisher eventPublisher; // 发送MQ消息

    @Override
    public void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate) {
        // 1. 获取到两个实体对象
        UserAwardRecordEntity userAwardRecordEntity = userAwardRecordAggregate.getUserAwardRecordEntity();
        TaskEntity taskEntity = userAwardRecordAggregate.getTaskEntity();

        // 2.【对象转换】：实体对象 -> 持久化对象
        UserAwardRecordPO userAwardRecord = UserAwardRecordPO.builder()
                .userId(userAwardRecordEntity.getUserId())
                .activityId(userAwardRecordEntity.getActivityId())
                .strategyId(userAwardRecordEntity.getStrategyId())
                .orderId(userAwardRecordEntity.getOrderId())
                .awardId(userAwardRecordEntity.getAwardId())
                .awardTitle(userAwardRecordEntity.getAwardTitle())
                .awardTime(userAwardRecordEntity.getAwardTime())
                .awardState(userAwardRecordEntity.getAwardState().getCode())
                .build();
        TaskPO task = TaskPO.builder()
                .userId(taskEntity.getUserId())
                .topic(taskEntity.getTopic())
                .messageId(taskEntity.getMessageId())
                .message(JSON.toJSONString(taskEntity.getMessage()))
                .state(taskEntity.getState().getCode())
                .build();

        // 获取后续逻辑会使用到的一些值
        String userId = userAwardRecordEntity.getUserId();
        Long activityId = userAwardRecordEntity.getActivityId();
        Integer awardId = userAwardRecordEntity.getAwardId();

        try {
            dbRouter.doRouter(userId);
            // 执行事务， 写入 1.用户中奖订单记录 2.MQ事件消息任务
            transactionTemplate.execute(
                    new TransactionCallback<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus status) {
                            try {
                                // 写入用户中奖订单记录
                                userAwardRecordDao.insert(userAwardRecord);
                                // 写入MQ事件消息任务
                                taskDao.insert(task);
                                return 1;
                            } catch (DuplicateKeyException e) {
                                status.setRollbackOnly();
                                log.error("写入中奖记录，唯一索引冲突 userId: {} activityId: {} awardId: {}", userId, activityId, awardId, e);
                                throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                            }
                        }
                    });
        } finally {
            dbRouter.clear();
        }

        // 【发送MQ消息】—— note：首次发送MQ消息，即使发送不成功也没关系，有后续的定时任务逻辑进行兜底操作。
        try {
            // fixme: 后期可以使用线程池MQ消息发送进行优化
            // 发送消息【在事务外执行，如果失败还有任务补偿】
            eventPublisher.publish(task.getTopic(), task.getMessage());
            // 消息发送成功后，及时更新消息任务的状态为-完成
            taskDao.updateTaskSendMessageCompleted(task);
        } catch (Exception e) {
            log.error("写入中奖记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            // 消息发送成功后，及时更新消息任务的状态为-失败（不过失败了也没有什么关系，有兜底策略）
            taskDao.updateTaskSendMessageFail(task);
        }

    }

}