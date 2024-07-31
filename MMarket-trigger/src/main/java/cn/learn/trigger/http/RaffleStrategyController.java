package cn.learn.trigger.http;

import cn.learn.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.learn.domain.strategy.model.entity.RaffleAwardEntity;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;
import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import cn.learn.domain.strategy.service.IRaffleAward;
import cn.learn.domain.strategy.service.IRaffleRule;
import cn.learn.domain.strategy.service.IRaffleStrategy;
import cn.learn.domain.strategy.service.armory.IStrategyArmory;
import cn.learn.trigger.api.IRaffleStrategyService;
import cn.learn.trigger.api.dto.RaffleAwardListReqDTO;
import cn.learn.trigger.api.dto.RaffleAwardListResDTO;
import cn.learn.trigger.api.dto.RaffleStrategyReqDTO;
import cn.learn.trigger.api.dto.RaffleStrategyResDTO;
import cn.learn.types.common.Constants;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import cn.learn.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: MMarket
 * @description: 营销抽奖服务api实现
 * @author: chouchouGG
 * @create: 2024-06-23 15:59
 **/
@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}") // 跨域服务
@RequestMapping("/api/${app.config.api-version}/raffle/strategy/")
public class RaffleStrategyController implements IRaffleStrategyService {

    @Resource
    private IRaffleAward raffleAward;
    
    @Resource
    private IRaffleStrategy raffleStrategy;
    
    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IRaffleRule raffleRule;

    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

    /**
     * 策略装配，将策略信息装配到缓存中
     * <a href="http://localhost:8091/api/v1/raffle/strategy/strategy_armory">/api/v1/raffle/strategy_armory</a>
     */
    @GetMapping("strategy_armory")
    @Override
    public Response<Boolean> strategyArmory(@RequestParam Long strategyId) {
        try {
            log.info("抽奖策略装配开始 strategyId：{}", strategyId);
            boolean armoryStatus = strategyArmory.assembleLotteryStrategy(strategyId);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(armoryStatus)
                    .build();
            log.info("抽奖策略【{}】装配完成 response: {}", strategyId, JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
            log.error("抽奖策略装配失败 strategyId：{}", strategyId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UNKNOW_ERROR.getCode())
                    .info(ResponseCode.UNKNOW_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 查询奖品列表
     *<a href="http://localhost:8091/api/v1/raffle/strategy/query_raffle_award_list">/api/v1/raffle/query_raffle_award_list</a>
     * 请求参数 raw json
     */
    @PostMapping("query_raffle_award_list")
    @Override
    public Response<List<RaffleAwardListResDTO>> queryRaffleAwardList(@RequestBody RaffleAwardListReqDTO request) {
        try {
            log.info("查询抽奖奖品列表配开始 userId:{} activityId：{}", request.getUserId(), request.getActivityId());
            // 1. 参数校验
            if (StringUtils.isBlank(request.getUserId()) || null == request.getActivityId()) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            // 2. 查询奖品配置
            List<StrategyAwardEntity> strategyAwardEntities = raffleAward.queryRaffleStrategyAwardListByActivityId(request.getActivityId());

            // 3. 获取解锁规则的解锁次数配置（也就是各个奖品的解锁次数，用于返回给前端进行展示）Map键值对为：[awardID, RuleLockCount]
            Map<Integer, Integer> ruleLockCountMap = raffleRule.queryAwardRuleLockCount(strategyAwardEntities);

            // 4. 查询日参与次数 - 用户已经参与的抽奖次数
            Integer dayPartakeCount = raffleActivityAccountQuotaService.queryAccountDayPartakeCount(request.getActivityId(), request.getUserId());

            // 5. 遍历填充数据
            List<RaffleAwardListResDTO> raffleAwardListResDTOs = new ArrayList<>(strategyAwardEntities.size());
            for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
                Integer awardRuleLockCount = ruleLockCountMap.get(strategyAward.getAwardId()); // 获取奖品ID对应的解锁次数
                raffleAwardListResDTOs.add(
                        RaffleAwardListResDTO.builder()
                                .awardId(strategyAward.getAwardId())
                                .awardTitle(strategyAward.getAwardTitle())
                                .awardSubtitle(strategyAward.getAwardSubtitle())
                                .sort(strategyAward.getSort())
                                .awardRuleLockCount(awardRuleLockCount)
                                .isAwardUnlock(null == awardRuleLockCount || dayPartakeCount >= awardRuleLockCount)
                                .waitUnLockCount((null == awardRuleLockCount || awardRuleLockCount <= dayPartakeCount) ? 0 : awardRuleLockCount - dayPartakeCount)
                                .build()
                );
            }
            Response<List<RaffleAwardListResDTO>> response = Response.<List<RaffleAwardListResDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(raffleAwardListResDTOs)
                    .build();
            log.info("查询抽奖奖品列表配置完成 userId:{} activityId：{} response: {}", request.getUserId(), request.getActivityId(), JSON.toJSONString(response));
            // 返回结果
            return response;
        } catch (Exception e) {
            log.error("查询抽奖奖品列表配置失败 userId:{} activityId：{}", request.getUserId(), request.getActivityId(), e);
            return Response.<List<RaffleAwardListResDTO>>builder()
                    .code(ResponseCode.UNKNOW_ERROR.getCode())
                    .info(ResponseCode.UNKNOW_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 随机抽奖接口
     * <a href="http://localhost:8091/api/v1/raffle/strategy/random_raffle">/api/v1/raffle/random_raffle</a>
     */
    @PostMapping("random_raffle")
    @Override
    public Response<RaffleStrategyResDTO> randomRaffle(@RequestBody RaffleStrategyReqDTO requestDTO) {
        try {
            log.info("随机抽奖开始 strategyId: {}", requestDTO.getStrategyId());
            // 调用抽奖接口
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(
                    RaffleFactorEntity.builder()
                            .userId("system")
                            .strategyId(requestDTO.getStrategyId())
                            .build()
            );
            // 封装返回结果
            Response<RaffleStrategyResDTO> response =
                    Response.<RaffleStrategyResDTO>builder()
                            .code(ResponseCode.SUCCESS.getCode())
                            .info(ResponseCode.SUCCESS.getInfo())
                            .data(RaffleStrategyResDTO.builder()
                                    .awardId(raffleAwardEntity.getAwardId())
                                    .awardIndex(raffleAwardEntity.getSort())
                                    .build())
                            .build();
            log.info("随机抽奖完成 strategyId: {} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            return response;
        } catch (AppException e) {
            log.error("随机抽奖失败 strategyId：{} {}", requestDTO.getStrategyId(), e.getInfo());
            return Response.<RaffleStrategyResDTO>builder().code(e.getCode()).info(e.getInfo()).build();
        } catch (Exception e) {
            log.error("随机抽奖失败 strategyId：{}", requestDTO.getStrategyId(), e);
            return Response.<RaffleStrategyResDTO>builder()
                    .code(ResponseCode.UNKNOW_ERROR.getCode())
                    .info(ResponseCode.UNKNOW_ERROR.getInfo())
                    .build();
        }
    }

}
