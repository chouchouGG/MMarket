package cn.learn.test.domain;

import cn.learn.domain.strategy.model.entity.RaffleAwardEntity;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;
import cn.learn.domain.strategy.service.IRaffleStrategy;
import cn.learn.domain.strategy.service.armory.StrategyArmoryDispatch;
import cn.learn.domain.strategy.service.rule.filter.impl.RuleLockLogicFilter;
import cn.learn.domain.strategy.service.rule.filter.impl.RuleWeightLogicFilter;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖策略测试
 * @create 2024-01-06 13:28
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleStrategyTest {

    @Resource
    StrategyArmoryDispatch strategyArmory;

    @Resource
    private IRaffleStrategy raffleStrategy;

    // note：用作mock测试（仿真测试），实际情况中用户的积分（幸运值）是变化的，这里用固定值进行模拟。
    @Resource
    private RuleWeightLogicFilter ruleWeightLogicFilter;

    @Resource
    private RuleLockLogicFilter ruleLockLogicFilter;

    @Before
    public void setUp() {
        log.info("策略装配结果：{}", strategyArmory.assembleLotteryStrategy(100001L));
        log.info("策略装配结果：{}", strategyArmory.assembleLotteryStrategy(100003L));

        long lucky_value = 40500L;
        ReflectionTestUtils.setField(ruleWeightLogicFilter, "userScore", lucky_value);
        log.info("当前用户幸运值为：{}", lucky_value);
    }

    @Test
    public void test_performRaffle() {
        RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
                .userId("joyboy")
                .strategyId(100001L)
                .build();


        // 连续抽奖 5 次
        for (int i = 0; i < 5; i++) {
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);
            // log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
            log.info("第{}次测试结果：{}", i, JSON.toJSONString(raffleAwardEntity));
        }
    }

    @Test
    public void test_performRaffle_blacklist() {
        RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
                .userId("user003")  // 黑名单用户 user001,user002,user003
                .strategyId(100001L)
                .build();

        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

        log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
        log.info("测试结果：{}", JSON.toJSONString(raffleAwardEntity));
    }

    @Test
    public void test_raffle_center_rule_lock() {
        // mock测试，设置用户的抽奖次数
        long numberOfRaffle = 2L;
        log.info("用户已经完成的抽奖次数: {}次", numberOfRaffle);
        ReflectionTestUtils.setField(ruleLockLogicFilter, "userRaffleCount", numberOfRaffle);

        RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
                .userId("joyboy")
                .strategyId(100003L)
                .build();


        log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
        // 连续抽奖 5 次
        for (int i = 0; i < 5; i++) {
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);
            // log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
            log.info("第{}次测试结果：{}", i, JSON.toJSONString(raffleAwardEntity));
        }
    }

}
