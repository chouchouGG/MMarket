package cn.learn.trigger.http;

import cn.learn.domain.activity.model.entity.UserRaffleOrderEntity;
import cn.learn.domain.activity.service.IRaffleActivityPartakeService;
import cn.learn.domain.activity.service.armory.IActivityArmory;
import cn.learn.domain.award.model.entity.UserAwardRecordEntity;
import cn.learn.domain.award.model.valobj.AwardStateVO;
import cn.learn.domain.award.service.IAwardService;
import cn.learn.domain.strategy.model.entity.RaffleAwardEntity;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;
import cn.learn.domain.strategy.service.IRaffleStrategy;
import cn.learn.domain.strategy.service.armory.IStrategyArmory;
import cn.learn.trigger.api.IRaffleActivityService;
import cn.learn.trigger.api.dto.ActivityDrawReqDTO;
import cn.learn.trigger.api.dto.ActivityDrawResDTO;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import cn.learn.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

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
}
