package cn.learn.test.domain.strategy;

import cn.learn.domain.strategy.model.entity.RaffleAwardEntity;
import cn.learn.domain.strategy.model.entity.RaffleFactorEntity;
import cn.learn.domain.strategy.service.IRaffleStrategy;
import cn.learn.domain.strategy.service.armory.StrategyArmoryDispatch;
import cn.learn.domain.strategy.service.rule.chain.impl.RuleWeightLogicChain;
import cn.learn.domain.strategy.service.rule.tree.impl.RuleLockNode;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

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
    IRaffleStrategy raffleStrategy;

    @Resource
    RuleWeightLogicChain ruleWeightLogicChain;

    @Resource
    RuleLockNode ruleLockNode;


    @Before
    public void setUp() {
        log.info("策略装配结果：{}", strategyArmory.assembleLotteryStrategy(100001L));
        log.info("策略装配结果：{}", strategyArmory.assembleLotteryStrategy(100003L));
        // fixme：mock测试
        // 1. 设置用户累计积分
        long lucky_value = 100L;
        ReflectionTestUtils.setField(ruleWeightLogicChain, "userScore", lucky_value);
        log.info("当前用户幸运值为：{}", lucky_value);
        // 2. 设置用户抽奖次数
        long raffleCount = 1L;
        ReflectionTestUtils.setField(ruleLockNode, "userRaffleCount", raffleCount);
        log.info("当前用户抽奖次数为：{}", raffleCount);
    }

    @Test
    public void test_performRaffle() throws InterruptedException {
        RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
                .userId("joyboy")
                .strategyId(100001L).build();
        // 连续抽奖
        for (int i = 0; i < 1; i++) {
            log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);
            log.info("第{}次测试结果：{}", i, JSON.toJSONString(raffleAwardEntity));
        }

        // note: 每次记得重新加载reids的缓存
        // 让当前线程等待10秒，等待异步的数据库更新完成
        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void test_performRaffle_blacklist() {
        RaffleFactorEntity raffleFactorEntity = RaffleFactorEntity.builder()
                .userId("user003")  // 黑名单用户 user001,user002,user003
                .strategyId(100001L).build();

        log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);
        log.info("测试结果：{}", JSON.toJSONString(raffleAwardEntity));
    }


}
