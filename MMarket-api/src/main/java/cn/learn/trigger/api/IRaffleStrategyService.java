package cn.learn.trigger.api;

import cn.learn.trigger.api.dto.RaffleAwardListReqDTO;
import cn.learn.trigger.api.dto.RaffleAwardListResDTO;
import cn.learn.trigger.api.dto.RaffleStrategyReqDTO;
import cn.learn.trigger.api.dto.RaffleStrategyResDTO;
import cn.learn.types.model.Response;

import java.util.List;

/**
 * @program: MMarket
 * @description: 抽奖服务接口
 * @author: chouchouGG
 * @create: 2024-06-23 15:38
 **/
public interface IRaffleStrategyService {


    /**
     * 策略装配接口
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    Response<Boolean> strategyArmory(Long strategyId);

    /**
     * 查询抽奖奖品列表配置
     *
     * @param requestDTO 抽奖奖品列表查询请求参数
     * @return 奖品列表数据
     */
    Response<List<RaffleAwardListResDTO>> queryRaffleAwardList(RaffleAwardListReqDTO requestDTO);

    /**
     * 抽奖接口
     *
     * @param requestDTO 请求参数
     * @return 抽奖结果
     */
    Response<RaffleStrategyResDTO> randomRaffle(RaffleStrategyReqDTO requestDTO);
}
