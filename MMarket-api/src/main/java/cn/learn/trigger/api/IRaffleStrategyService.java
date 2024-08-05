package cn.learn.trigger.api;

import cn.learn.trigger.api.dto.*;
import cn.learn.types.model.Response;

import java.util.List;

/**
 * @program: MMarket
 * @description: <h1>抽奖服务接口</h1>
 * @author: chouchouGG
 * @create: 2024-06-23 15:38
 **/
public interface IRaffleStrategyService {


    /**
     * 策略装配接口（前端装配策略的逻辑）（在与具体活动整合后，该接口废弃 {@Deprecated}）
     */
    @Deprecated
    Response<Boolean> strategyArmory(Long strategyId);

    /**
     * 抽奖接口（在与具体活动整合后，该接口废弃 {@Deprecated}）
     * @return 抽奖结果
     */
    @Deprecated
    Response<RaffleStrategyResDTO> randomRaffle(RaffleStrategyReqDTO request);

    /**
     * 查询抽奖奖品列表配置（用于前端页面渲染展示）
     * @return 奖品列表数据
     */
    Response<List<RaffleAwardListResDTO>> queryRaffleAwardList(RaffleAwardListReqDTO request);

    /**
     * 查询抽奖策略权重规则（用于前端页面渲染展示）
     * @return 权重奖品配置列表「这里会返回全部，前端可按需取用一条已达标的，或者一条要达标的」
     */
    Response<List<RaffleStrategyRuleWeightResDTO>> queryRaffleStrategyRuleWeight(RaffleStrategyRuleWeightReqDTO request);
}
