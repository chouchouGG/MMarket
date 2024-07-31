package cn.learn.domain.activity.service.quota;

import cn.learn.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.learn.domain.activity.model.entity.*;
import cn.learn.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.learn.domain.activity.model.valobj.OrderStateVO;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.domain.activity.service.IRaffleActivitySkuStockService;
import cn.learn.domain.activity.service.quota.chain.factory.DefaultActivityChainFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动服务
 * @create 2024-03-16 08:41
 */
@Service
public class RaffleActivityAccountQuotaService extends AbstractRaffleActivityAccountQuota implements IRaffleActivitySkuStockService {

    public RaffleActivityAccountQuotaService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        super(activityRepository, defaultActivityChainFactory);
    }

    @Override
    protected CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity,
                                                            ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        // 创建活动订单实体对象
        ActivityOrderEntity activityOrderEntity = ActivityOrderEntity.builder()
                .userId(skuRechargeEntity.getUserId())                  // （1）设置用户ID
                .sku(activitySkuEntity.getSku())                        // （2）设置商品SKU
                .activityId(activityEntity.getActivityId())             // （3）设置活动ID
                .activityName(activityEntity.getActivityName())         // （4）设置活动名称
                .strategyId(activityEntity.getStrategyId())             // （5）设置抽奖策略ID
                // fixme: 公司里一般会有专门的【雪花算法】UUID服务，我们这里直接生成个12位就可以了。
                .orderId(RandomStringUtils.randomNumeric(12))     // （6）生成12位的订单ID fixme：用于内部标识？
                .orderTime(new Date())                                  // （7）设置下单时间为当前时间
                .totalCount(activityCountEntity.getTotalCount())        // （8）设置总次数
                .dayCount(activityCountEntity.getDayCount())            // （9）设置日次数
                .monthCount(activityCountEntity.getMonthCount())        // （10）设置月次数
                .state(OrderStateVO.completed)                          // （11）设置订单状态为已完成
                .outBusinessNo(skuRechargeEntity.getOutBusinessNo())    // （12）设置外部业务唯一标识 fixme: 由外部生成？
                .build();


        // 构建聚合对象
        return CreateQuotaOrderAggregate.builder()
                .totalCount(activityCountEntity.getTotalCount())    // （8）设置总次数
                .dayCount(activityCountEntity.getDayCount())        // （9）设置日次数
                .monthCount(activityCountEntity.getMonthCount())    // （10）设置月次数
                .activityOrderEntity(activityOrderEntity)           // 设置活动订单实体
                .build();
    }

    @Override
    protected void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate) {
        activityRepository.doSaveOrder(createOrderAggregate);
    }

    @Override
    public ActivitySkuStockKeyVO takeQueueValue() {
        return activityRepository.takeQueueValue();
    }

    @Override
    public void clearQueueValue() {
        activityRepository.clearQueueValue();
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        activityRepository.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        activityRepository.clearActivitySkuStock(sku);
    }

    @Override
    public Integer queryAccountDayPartakeCount(Long activityId, String userId) {
        return activityRepository.queryAccountDayPartakeCount(activityId, userId);
    }
}
