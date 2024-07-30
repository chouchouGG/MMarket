package cn.learn.trigger.http;

import cn.learn.domain.strategy.model.entity.RaffleAwardEntity;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;
import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.service.IRaffleAward;
import cn.learn.domain.strategy.service.IRaffleStrategy;
import cn.learn.domain.strategy.service.armory.IStrategyArmory;
import cn.learn.trigger.api.IRaffleStrategyService;
import cn.learn.trigger.api.dto.RaffleAwardListReqDTO;
import cn.learn.trigger.api.dto.RaffleAwardListResDTO;
import cn.learn.trigger.api.dto.RaffleStrategyReqDTO;
import cn.learn.trigger.api.dto.RaffleStrategyResDTO;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import cn.learn.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    public Response<List<RaffleAwardListResDTO>> queryRaffleAwardList(@RequestBody RaffleAwardListReqDTO requestDTO) {
        try {
            log.info("查询抽奖奖品列表配开始 strategyId：{}", requestDTO.getStrategyId());
            // 查询奖品配置
            List<StrategyAwardEntity> strategyAwardEntities = raffleAward.queryRaffleStrategyAwardList(requestDTO.getStrategyId());
            List<RaffleAwardListResDTO> raffleAwardListResDTOs = new ArrayList<>(strategyAwardEntities.size());
            for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
                raffleAwardListResDTOs.add(
                        RaffleAwardListResDTO.builder()
                                .awardId(strategyAward.getAwardId())
                                .awardTitle(strategyAward.getAwardTitle())
                                .awardSubtitle(strategyAward.getAwardSubtitle())
                                .sort(strategyAward.getSort())
                                .build()
                );
            }
            Response<List<RaffleAwardListResDTO>> response =
                    Response.<List<RaffleAwardListResDTO>>builder()
                            .code(ResponseCode.SUCCESS.getCode())
                            .info(ResponseCode.SUCCESS.getInfo())
                            .data(raffleAwardListResDTOs)
                            .build();
            log.info("查询抽奖奖品列表配置完成 strategyId：{} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            // 返回结果
            return response;
        } catch (Exception e) {
            log.error("查询抽奖奖品列表配置失败 strategyId：{}", requestDTO.getStrategyId(), e);
            return Response.<List<RaffleAwardListResDTO>>builder().code(ResponseCode.UNKNOW_ERROR.getCode()).info(ResponseCode.UNKNOW_ERROR.getInfo()).build();
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
