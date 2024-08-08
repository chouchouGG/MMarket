package cn.learn.domain.activity.service.partake;

import cn.learn.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.learn.domain.activity.model.entity.*;
import cn.learn.domain.activity.model.valobj.UserRaffleOrderStateVO;
import cn.learn.domain.activity.repository.IActivityRepository;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link RaffleActivityPartakeService} 可以理解为处理用户参与抽奖活动的服务，负责管理和处理用户如何参与到抽奖活动中的相关业务逻辑。
 */
@Service
public class RaffleActivityPartakeService extends AbstractRaffleActivityPartake {

    private final SimpleDateFormat dateFormatMonth = new SimpleDateFormat("yyyy-MM");

    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");

    public RaffleActivityPartakeService(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    protected CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate) {
        // 查询用户在该活动中的总账户额度
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccount(userId, activityId);

        // 总额度判断（检查总额度是否存在或总剩余额度是否大于0：用户没有参与当前活动 || 账户总剩余额度不足）
        if (null == activityAccountEntity || activityAccountEntity.getTotalCountSurplus() <= 0) {
            throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
        }

        // 格式化当前日期为月和日
        String month = dateFormatMonth.format(currentDate);
        String day = dateFormatDay.format(currentDate);

        /*
            note: 因为月账户额度、日账户额度需要在每月、每日进行重置，为了避免定时任务轮询重置额度的操作，
             在设计上使用了两个新的表【账户日次数表】、【账户月次数表】作为镜像，后续对于日额度、月额度的变化只关心这两个表。
         */
        // 查询用户在该活动中的月账户额度
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonth(userId, activityId, month);
        if (null != activityAccountMonthEntity && activityAccountMonthEntity.getMonthCountSurplus() <= 0) {
            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
        }
        // 创建月额度账户；true = 存在月账户、false = 不存在月账户
        boolean isExistAccountMonth = (null != activityAccountMonthEntity);
        if (null == activityAccountMonthEntity) {
            activityAccountMonthEntity = ActivityAccountMonthEntity.builder()
                                            .userId(userId)
                                            .activityId(activityId)
                                            .month(month)
                                            .monthCount(activityAccountEntity.getMonthCount())
                                            .monthCountSurplus(activityAccountEntity.getMonthCount())
                                            .build();
        }

        // 查询出账户配置的【日账户额度】
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, day);
        if (null != activityAccountDayEntity && activityAccountDayEntity.getDayCountSurplus() <= 0) {
            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
        }
        // 创建日账户额度；true = 存在日账户、false = 不存在日账户
        boolean isExistAccountDay = (null != activityAccountDayEntity);
        if (null == activityAccountDayEntity) {
            activityAccountDayEntity = ActivityAccountDayEntity.builder()
                                            .userId(userId)
                                            .activityId(activityId)
                                            .day(day)
                                            .dayCount(activityAccountEntity.getDayCount())
                                            .dayCountSurplus(activityAccountEntity.getDayCount())
                                            .build();
        }

        // 构建对象
        return CreatePartakeOrderAggregate.builder()
                .userId(userId)
                .activityId(activityId)
                .activityAccountEntity(activityAccountEntity)
                .isExistAccountMonth(isExistAccountMonth)
                .activityAccountMonthEntity(activityAccountMonthEntity)
                .isExistAccountDay(isExistAccountDay)
                .activityAccountDayEntity(activityAccountDayEntity)
                .build();
    }

    @Override
    protected UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date currentDate) {
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activityId);
        // 构建订单
        return UserRaffleOrderEntity.builder()
                .userId(userId)
                .activityId(activityId)
                .activityName(activityEntity.getActivityName())
                .strategyId(activityEntity.getStrategyId())
                .orderId(RandomStringUtils.randomNumeric(12)) // fixme: 后期对于这种订单ID都可以使用雪花算法进行优化
                .orderTime(currentDate)
                .orderState(UserRaffleOrderStateVO.create) // 【用户抽奖参与订单】状态为：create
                .endDateTime(activityEntity.getEndDateTime())
                .build();
    }


}
