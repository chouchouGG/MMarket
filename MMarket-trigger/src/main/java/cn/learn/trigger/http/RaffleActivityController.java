package cn.learn.trigger.http;

import cn.learn.domain.activity.model.entity.ActivityAccountEntity;
import cn.learn.domain.activity.model.entity.UserRaffleOrderEntity;
import cn.learn.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.learn.domain.activity.service.IRaffleActivityPartakeService;
import cn.learn.domain.activity.service.armory.IActivityArmory;
import cn.learn.domain.award.model.entity.UserAwardRecordEntity;
import cn.learn.domain.award.model.valobj.AwardStateVO;
import cn.learn.domain.award.service.IAwardService;
import cn.learn.domain.rebate.model.entity.BehaviorEntity;
import cn.learn.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.learn.domain.rebate.model.valobj.BehaviorTypeVO;
import cn.learn.domain.rebate.service.IBehaviorRebateService;
import cn.learn.domain.strategy.model.entity.RaffleAwardEntity;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;
import cn.learn.domain.strategy.service.IRaffleStrategy;
import cn.learn.domain.strategy.service.armory.IStrategyArmory;
import cn.learn.trigger.api.IRaffleActivityService;
import cn.learn.trigger.api.dto.ActivityDrawReqDTO;
import cn.learn.trigger.api.dto.ActivityDrawResDTO;
import cn.learn.trigger.api.dto.UserActivityAccountReqDTO;
import cn.learn.trigger.api.dto.UserActivityAccountResDTO;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import cn.learn.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-07-28 20:49
 **/
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/activity/")
public class RaffleActivityController implements IRaffleActivityService {

    /**
     * 参与抽奖
     */
    @Resource
    private IRaffleActivityPartakeService raffleActivityPartakeService;

    /**
     * 执行抽奖
     */
    @Resource
    private IRaffleStrategy raffleStrategy;

    /**
     * 中奖记录入库 & 发奖
     */
    @Resource
    private IAwardService awardService;

    /**
     * 装配
     */
    @Resource
    private IActivityArmory activityArmory; // 活动装配
    @Resource
    private IStrategyArmory strategyArmory; // 策略装配

    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

    @Resource
    private IBehaviorRebateService behaviorRebateService;

    @GetMapping("armory")
    @Override
    public Response<Boolean> armory(@RequestParam Long activityId) {
        try {
            log.info("活动装配，数据预热，开始 activityId：{}", activityId);
            // note: 之前的装配都是基于模块自身的，比如抽奖策略模块是基于策略ID进行装配，抽奖活动模块基于SKU进行装配，此处对原有的装配接口进行扩展，即通过活动ID进行装配。
            // 1. 活动装配
            boolean activityArmorySuccess = activityArmory.assembleActivitySkuByActivityId(activityId);
            // 2. 策略装配
            boolean strategyArmorySuccess = strategyArmory.assembleLotteryStrategyByActivityId(activityId);
            if (activityArmorySuccess && strategyArmorySuccess) {
                return Response.<Boolean>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(true)
                        .build();
            } else {
                // 如果装配活动或策略失败，返回失败的响应
                return Response.<Boolean>builder()
                        .code("装配失败")
                        .info("活动装配或策略装配失败")
                        .data(false)
                        .build();
            }
        } catch (Exception e) {
            log.error("活动装配，数据预热，失败 activityId:{}", activityId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UNKNOW_ERROR.getCode())
                    .info(ResponseCode.UNKNOW_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("draw")
    @Override
    public Response<ActivityDrawResDTO> draw(@RequestBody ActivityDrawReqDTO request) {
        try {
            log.info("活动抽奖 userId:{} activityId:{}", request.getUserId(), request.getActivityId());

            // 1. 参数校验
            if (StringUtils.isBlank(request.getUserId()) || null == request.getActivityId()) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            // 2. 参与活动 - 创建参与记录订单
            UserRaffleOrderEntity orderEntity = raffleActivityPartakeService.createOrder(request.getUserId(), request.getActivityId());

            // 3. 抽奖策略 - 执行抽奖
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(
                    RaffleFactorEntity.builder()
                            .userId(orderEntity.getUserId())
                            .strategyId(orderEntity.getStrategyId())
                            .endDateTime(orderEntity.getEndDateTime())
                            .build()
            );

            // 4. 存放结果 - 写入中奖记录
            awardService.saveUserAwardRecord(
                    UserAwardRecordEntity.builder()
                            .userId(orderEntity.getUserId())
                            .activityId(orderEntity.getActivityId())
                            .strategyId(orderEntity.getStrategyId())
                            .orderId(orderEntity.getOrderId())
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardTitle(raffleAwardEntity.getAwardTitle())
                            .awardTime(new Date())
                            .awardState(AwardStateVO.create)
                            .build()
            );

            // 5. 返回结果
            return Response.<ActivityDrawResDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ActivityDrawResDTO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardTitle(raffleAwardEntity.getAwardTitle())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();
        // 业务异常
        } catch (AppException e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        // 系统异常
        } catch (Exception e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResDTO>builder()
                    .code(ResponseCode.UNKNOW_ERROR.getCode())
                    .info(ResponseCode.UNKNOW_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 日历签到返利接口
     *
     * @param userId 用户ID
     * @return 签到返利结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/calendar_sign_rebate">/api/v1/raffle/activity/calendar_sign_rebate</a>
     * 入参：joyboy
     * <p>
     * curl -X POST http://localhost:8091/api/v1/raffle/activity/calendar_sign_rebate -d "userId=xiaofuge" -H "Content-Type: application/x-www-form-urlencoded"
     */
    @RequestMapping(value = "calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> calendarSignRebate(@RequestParam String userId) {
        try {
            log.info("日历签到返利开始 userId:{}", userId);
            BehaviorEntity behaviorEntity = BehaviorEntity.builder()
                    .userId(userId)
                    .behaviorType(BehaviorTypeVO.SIGN)
                    .outBusinessNo(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                    .build();
            // 调用签到返利服务
            List<String> orderIds = behaviorRebateService.createBehaviorRewardOrder(behaviorEntity);

            log.info("日历签到返利完成 userId:{} orderIds: {}", userId, JSON.toJSONString(orderIds));
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (AppException e) {
            log.error("日历签到返利异常 userId:{} ", userId, e);
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("日历签到返利失败 userId:{}", userId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UNKNOW_ERROR.getCode())
                    .info(ResponseCode.UNKNOW_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    /**
     * 判断是否签到接口
     * <p>
     * curl -X POST http://localhost:8091/api/v1/raffle/activity/is_calendar_sign_rebate -d "userId=xiaofuge" -H "Content-Type: application/x-www-form-urlencoded"
     */
    @PostMapping(value = "is_calendar_sign_rebate")
    @Override
    public Response<Boolean> isCalendarSignRebate(String userId) {
        try {
            log.info("查询用户是否完成日历签到返利开始 userId:{}", userId);
            // note：签到返利的外部透传的业务ID是当日的日期时间
            String outBusinessNo = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            List<BehaviorRebateOrderEntity> behaviorRebateOrderEntities = behaviorRebateService.queryOrderByOutBusinessNo(userId, outBusinessNo);

            log.info("查询用户是否完成日历签到返利完成 userId:{} orders.size:{}", userId, behaviorRebateOrderEntities.size());
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(!behaviorRebateOrderEntities.isEmpty()) // 只要不为空，则表示已经做了签到
                    .build();
        } catch (Exception e) {
            log.error("查询用户是否完成日历签到返利失败 userId:{}", userId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UNKNOW_ERROR.getCode())
                    .info(ResponseCode.UNKNOW_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    /**
     * 查询账户额度
     * <p>
     * curl --request POST \
     * --url http://localhost:8091/api/v1/raffle/activity/query_user_activity_account \
     * --header 'content-type: application/json' \
     * --data '{
     * "userId":"xiaofuge",
     * "activityId": 100301
     * }'
     */
    @PostMapping(value = "query_user_activity_account")
    @Override
    public Response<UserActivityAccountResDTO> queryUserActivityAccount(UserActivityAccountReqDTO request) {
        try {
            log.info("查询用户活动账户开始 userId:{} activityId:{}", request.getUserId(), request.getActivityId());

            String userId = request.getUserId();
            Long activityId = request.getActivityId();

            // 1. 参数校验
            if (StringUtils.isBlank(userId) || null == activityId) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            ActivityAccountEntity activityAccountEntity = raffleActivityAccountQuotaService.queryActivityAccountEntity(activityId, userId);
            UserActivityAccountResDTO userActivityAccountResponseDTO = UserActivityAccountResDTO.builder()
                    .totalCount(activityAccountEntity.getTotalCount())
                    .totalCountSurplus(activityAccountEntity.getTotalCountSurplus())
                    .dayCount(activityAccountEntity.getDayCount())
                    .dayCountSurplus(activityAccountEntity.getDayCountSurplus())
                    .monthCount(activityAccountEntity.getMonthCount())
                    .monthCountSurplus(activityAccountEntity.getMonthCountSurplus())
                    .build();
            log.info("查询用户活动账户完成 userId:{} activityId:{} dto:{}",
                    request.getUserId(), request.getActivityId(), JSON.toJSONString(userActivityAccountResponseDTO));
            return Response.<UserActivityAccountResDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(userActivityAccountResponseDTO)
                    .build();
        } catch (Exception e) {
            log.error("查询用户活动账户失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<UserActivityAccountResDTO>builder()
                    .code(ResponseCode.UNKNOW_ERROR.getCode())
                    .info(ResponseCode.UNKNOW_ERROR.getInfo())
                    .build();
        }
    }
}
