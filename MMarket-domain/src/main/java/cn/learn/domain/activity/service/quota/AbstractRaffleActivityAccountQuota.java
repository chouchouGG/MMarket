package cn.learn.domain.activity.service.quota;

import cn.learn.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.learn.domain.activity.model.entity.ActivityCountEntity;
import cn.learn.domain.activity.model.entity.ActivityEntity;
import cn.learn.domain.activity.model.entity.ActivitySkuEntity;
import cn.learn.domain.activity.model.entity.SkuRechargeEntity;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.learn.domain.activity.service.quota.chain.factory.DefaultActivityChainFactory;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动抽象类，定义标准的流程
 * @create 2024-03-16 08:42
 */
@Slf4j
public abstract class AbstractRaffleActivityAccountQuota extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {

    public AbstractRaffleActivityAccountQuota(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        super(activityRepository, defaultActivityChainFactory);
    }

    /**
     * 标准流程的具体实现
     * @param skuRechargeEntity 活动商品充值实体对象
     * @return
     */
    @Override
    public String createAccountQuotaRechargeOrder(SkuRechargeEntity skuRechargeEntity) {
        // 1. 参数校验
        paramCheck(skuRechargeEntity);

        // 2. 查询基础信息（通过继承的 RaffleActivitySupport 类中方法获取）
        // note：对于这种和具体业务无关，与功能有关的方法，可以增加一个 Support 类将功能性的方法封装起来
        ActivitySkuEntity activitySku = super.queryActivitySku(skuRechargeEntity.getSku()); // 其中的剩余库存是从数据库中获取的
        activitySku.setStockCountSurplus(super.querySkuStockCountSurplus(skuRechargeEntity.getSku())); // 更新当前的剩余的sku库存为缓存中最新的数据

        ActivityEntity activity = super.queryRaffleActivityByActivityId(activitySku.getActivityId());

        ActivityCountEntity activityCount = super.queryRaffleActivityCountByActivityCountId(activitySku.getActivityCountId());

        // 3. 责任链处理（活动校验、sku库存扣减）「过滤失败则直接抛异常」(逻辑不是很重要)
        defaultActivityChainFactory.openActionChain().handle(activitySku, activity, activityCount);

        // 4. 构建订单聚合对象
        CreateQuotaOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeEntity, activity, activityCount);

        // 5. 两步操作，一个事务下完成（1.保存订单，2.更新账户）
        doSaveOrder(createOrderAggregate);

        // 6. 返回订单号
        return createOrderAggregate.getActivityOrderEntity().getOrderId();
    }

    private void paramCheck(SkuRechargeEntity skuRechargeEntity) {
        // 参数校验：取出商品充值实体 skuRechargeEntity 中的三个属性，进行非空检查
        String userId = skuRechargeEntity.getUserId();
        Long sku = skuRechargeEntity.getSku();
        String outBusinessNo = skuRechargeEntity.getOutBusinessNo();

        if (null == sku || StringUtils.isBlank(userId) || StringUtils.isBlank(outBusinessNo)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
    }

    protected abstract CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivityEntity activity, ActivityCountEntity activityCount);

    protected abstract void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate);

}
