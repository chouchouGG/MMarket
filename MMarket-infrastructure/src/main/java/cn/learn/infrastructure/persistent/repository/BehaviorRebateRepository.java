package cn.learn.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.learn.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.learn.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.learn.domain.rebate.model.entity.DailyBehaviorRebateEntity;
import cn.learn.domain.rebate.model.entity.TaskEntity;
import cn.learn.domain.rebate.model.valobj.BehaviorTypeVO;
import cn.learn.domain.rebate.repository.IBehaviorRebateRepository;
import cn.learn.infrastructure.event.EventPublisher;
import cn.learn.infrastructure.persistent.dao.IDailyBehaviorRebateDao;
import cn.learn.infrastructure.persistent.dao.ITaskDao;
import cn.learn.infrastructure.persistent.dao.IUserBehaviorRebateOrderDao;
import cn.learn.infrastructure.persistent.po.DailyBehaviorRebatePO;
import cn.learn.infrastructure.persistent.po.TaskPO;
import cn.learn.infrastructure.persistent.po.UserBehaviorRebateOrderPO;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: MMarket
 * @description: 行为返利仓储实现
 * @author: chouchouGG
 * @create: 2024-08-03 13:59
 **/
@Slf4j
@Component
public class BehaviorRebateRepository implements IBehaviorRebateRepository {

    @Resource
    private IDailyBehaviorRebateDao dailyBehaviorRebateDao;
    @Resource
    private IUserBehaviorRebateOrderDao userBehaviorRebateOrderDao;
    @Resource
    private ITaskDao taskDao;
    @Resource
    private IDBRouterStrategy dbRouter;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private EventPublisher eventPublisher;

    @Override
    public List<DailyBehaviorRebateEntity> queryDailyBehaviorRebateConfig(BehaviorTypeVO behaviorTypeVO) {
        List<DailyBehaviorRebatePO> dailyBehaviorRebates = dailyBehaviorRebateDao.queryDailyBehaviorRebateByBehaviorType(behaviorTypeVO.getCode());
        List<DailyBehaviorRebateEntity> rebateConfigs = new ArrayList<>(dailyBehaviorRebates.size());
        for (DailyBehaviorRebatePO dailyBehaviorRebate : dailyBehaviorRebates) {
            rebateConfigs.add(DailyBehaviorRebateEntity.builder()
                    .behaviorType(dailyBehaviorRebate.getBehaviorType())
                    .rebateDesc(dailyBehaviorRebate.getRebateDesc())
                    .rebateType(dailyBehaviorRebate.getRebateType())
                    .rebateConfig(dailyBehaviorRebate.getRebateConfig())
                    .build()
            );
        }
        // 返回数据库配置的行为返利的配置
        return rebateConfigs;
    }

    @Override
    public void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates) {
        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
                        BehaviorRebateOrderEntity behaviorRebateOrderEntity = behaviorRebateAggregate.getBehaviorRebateOrderEntity();
                        // 写入行为返利订单记录
                        UserBehaviorRebateOrderPO userBehaviorRebateOrder = UserBehaviorRebateOrderPO.builder()
                                .userId(behaviorRebateOrderEntity.getUserId())
                                .orderId(behaviorRebateOrderEntity.getOrderId())
                                .behaviorType(behaviorRebateOrderEntity.getBehaviorType())
                                .rebateDesc(behaviorRebateOrderEntity.getRebateDesc())
                                .rebateType(behaviorRebateOrderEntity.getRebateType())
                                .rebateConfig(behaviorRebateOrderEntity.getRebateConfig())
                                .outBusinessNo(behaviorRebateOrderEntity.getOutBusinessNo())
                                .bizId(behaviorRebateOrderEntity.getBizId())
                                .build();
                        userBehaviorRebateOrderDao.insert(userBehaviorRebateOrder);

                        // 写入任务记录
                        TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
                        TaskPO task = TaskPO.builder()
                                .userId(taskEntity.getUserId())
                                .topic(taskEntity.getTopic())
                                .messageId(taskEntity.getMessageId())
                                .message(JSON.toJSONString(taskEntity.getMessage()))
                                .state(taskEntity.getState().getCode()).build();
                        taskDao.insert(task);
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入返利记录，唯一索引冲突 userId: {}", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), ResponseCode.INDEX_DUP.getInfo(), e);
                }
            });
        } finally {
            dbRouter.clear();
        }

        // 同步发送MQ消息
        for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
            TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
            TaskPO task = TaskPO.builder()
                    .userId(taskEntity.getUserId())
                    .messageId(taskEntity.getMessageId()).build();
            try {
                // 发送消息【在事务外执行，如果失败还有任务补偿】
                eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                // 更新数据库记录，task 任务表
                taskDao.updateTaskSendMessageCompleted(task);
            } catch (Exception e) {
                log.error("写入返利记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
                taskDao.updateTaskSendMessageFail(task);
            }
        }

    }

    @Override
    public List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
        // 1. 请求对象
        UserBehaviorRebateOrderPO userBehaviorRebateOrderReq = UserBehaviorRebateOrderPO.builder()
                .userId(userId)
                .outBusinessNo(outBusinessNo)
                .build();
        // 2. 查询结果
        List<UserBehaviorRebateOrderPO> userBehaviorRebateOrderResList = userBehaviorRebateOrderDao.queryOrderByOutBusinessNo(userBehaviorRebateOrderReq);
        List<BehaviorRebateOrderEntity> behaviorRebateOrderEntities = new ArrayList<>(userBehaviorRebateOrderResList.size());
        for (UserBehaviorRebateOrderPO userBehaviorRebateOrder : userBehaviorRebateOrderResList) {
            BehaviorRebateOrderEntity behaviorRebateOrderEntity = BehaviorRebateOrderEntity.builder()
                    .userId(userBehaviorRebateOrder.getUserId())
                    .orderId(userBehaviorRebateOrder.getOrderId())
                    .behaviorType(userBehaviorRebateOrder.getBehaviorType())
                    .rebateDesc(userBehaviorRebateOrder.getRebateDesc())
                    .rebateType(userBehaviorRebateOrder.getRebateType())
                    .rebateConfig(userBehaviorRebateOrder.getRebateConfig())
                    .outBusinessNo(userBehaviorRebateOrder.getOutBusinessNo())
                    .bizId(userBehaviorRebateOrder.getBizId())
                    .build();
            behaviorRebateOrderEntities.add(behaviorRebateOrderEntity);
        }
        return behaviorRebateOrderEntities;
    }

}
