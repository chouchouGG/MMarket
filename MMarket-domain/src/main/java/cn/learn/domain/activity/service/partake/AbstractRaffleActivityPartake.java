package cn.learn.domain.activity.service.partake;

import cn.learn.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.learn.domain.activity.model.entity.ActivityEntity;
import cn.learn.domain.activity.model.entity.PartakeRaffleActivityEntity;
import cn.learn.domain.activity.model.entity.UserRaffleOrderEntity;
import cn.learn.domain.activity.model.valobj.ActivityStateVO;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.domain.activity.service.IRaffleActivityPartakeService;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @program: MMarket
 * @description: 用户参与抽奖的抽象类（模板方法模式定义整个流程）
 * @author: chouchouGG
 * @create: 2024-07-26 18:40
 **/
@Slf4j
public abstract class AbstractRaffleActivityPartake implements IRaffleActivityPartakeService {

    protected final IActivityRepository activityRepository;

    public AbstractRaffleActivityPartake(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public UserRaffleOrderEntity createOrder(String userId, Long activityId) {
        return createOrder(
                PartakeRaffleActivityEntity.builder()
                        .userId(userId)
                        .activityId(activityId)
                        .build()
        );
    }

    // note：创建抽奖订单，类比于电商系统中的商品下单但是还未支付的状态
    @Override
    public UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        // 基础信息
        String userId = partakeRaffleActivityEntity.getUserId();
        Long activityId = partakeRaffleActivityEntity.getActivityId();
        Date currDate = new Date();

        // 活动查询
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activityId);

        // fixme：抽奖过程也需要活动校验？活动订单使用责任链进行了校验，这里因为校验逻辑不复杂，故没有使用责任链模式
        // 校验；活动状态开启（open）
        if (!ActivityStateVO.open.equals(activityEntity.getState())) {
            throw new AppException(ResponseCode.ACTIVITY_STATE_ERROR.getCode(), ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }

        // 校验；活动日期「开始时间 <- 当前时间 -> 结束时间」
        if (activityEntity.getBeginDateTime().after(currDate) || activityEntity.getEndDateTime().before(currDate)) {
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }

        // 2. 查询未被使用的活动参与订单（用户创建了一个订单，但是没有使用，则优先使用先前的订单）
        //    类似于商品下单但未支付，如果想要再次下单，需要先结束之前的流程。
        UserRaffleOrderEntity userRaffleOrderEntity = activityRepository.queryNoUsedRaffleOrder(partakeRaffleActivityEntity);
        if (null != userRaffleOrderEntity) {
            log.info("创建参与活动订单[存在未消费的抽奖订单] userId:{} activityId:{} userRaffleOrderEntity:{}", userId, activityId, JSON.toJSONString(userRaffleOrderEntity));
            return userRaffleOrderEntity;
        }

        // 3. 额度账户过滤 & 返回账户构建对象
        CreatePartakeOrderAggregate createPartakeOrderAggregate = this.doFilterAccount(userId, activityId, currDate);

        // 4. 构建订单
        UserRaffleOrderEntity userRaffleOrder = this.buildUserRaffleOrder(userId, activityId, currDate);

        // 5. 填充抽奖单实体对象
        createPartakeOrderAggregate.setUserRaffleOrderEntity(userRaffleOrder);

        // 6. 保存聚合对象 - 一个领域内的一个聚合是一个事务操作
        // note: 用户参与抽奖的账户额度更新流程
        activityRepository.saveCreatePartakeOrderAggregate(createPartakeOrderAggregate);

        // 7. 返回订单信息
        return userRaffleOrder;
    }

    protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate);

    protected abstract UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date currentDate);

}
