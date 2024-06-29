package cn.learn.domain.activity.service;

import cn.learn.domain.activity.model.aggregate.CreateOrderAggregate;
import cn.learn.domain.activity.model.entity.*;
import cn.learn.domain.activity.model.valobj.OrderStateVO;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.domain.activity.service.chain.factory.DefaultActivityChainFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动服务
 * @create 2024-03-16 08:41
 */
@Service
public class RaffleActivityService extends AbstractRaffleActivity {

    public RaffleActivityService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        super(activityRepository, defaultActivityChainFactory);
    }

    @Override
    protected CreateOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        // 创建活动订单实体对象
        ActivityOrderEntity activityOrderEntity = ActivityOrderEntity.builder()
                .userId(skuRechargeEntity.getUserId())                  // （1）设置用户ID
                .sku(skuRechargeEntity.getSku())                        // （2）设置商品SKU
                .activityId(activityEntity.getActivityId())             // （3）设置活动ID
                .activityName(activityEntity.getActivityName())         // （4）设置活动名称
                .strategyId(activityEntity.getStrategyId())             // （5）设置抽奖策略ID
                // fixme: 公司里一般会有专门的【雪花算法】UUID服务，我们这里直接生成个12位就可以了。
                .orderId(RandomStringUtils.randomNumeric(12))     // （6）生成12位的订单ID
                .orderTime(new Date())                                  // （7）设置下单时间为当前时间
                .totalCount(activityCountEntity.getTotalCount())        // （8）设置总次数
                .dayCount(activityCountEntity.getDayCount())            // （9）设置日次数
                .monthCount(activityCountEntity.getMonthCount())        // （10）设置月次数
                .state(OrderStateVO.completed)                          // （11）设置订单状态为已完成
                .outBusinessNo(skuRechargeEntity.getOutBusinessNo())    // （12）设置外部业务唯一标识
                .build();


        // 构建聚合对象
        return CreateOrderAggregate.builder()
                .totalCount(activityCountEntity.getTotalCount())    // （8）设置总次数
                .dayCount(activityCountEntity.getDayCount())        // （9）设置日次数
                .monthCount(activityCountEntity.getMonthCount())    // （10）设置月次数
                .activityOrderEntity(activityOrderEntity)           // 设置活动订单实体
                .build();
    }

    @Override
    protected void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
        activityRepository.doSaveOrder(createOrderAggregate);
    }
}
