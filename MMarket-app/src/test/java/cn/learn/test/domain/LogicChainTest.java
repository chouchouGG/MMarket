package cn.learn.test.domain;

import cn.learn.domain.strategy.model.entity.LogicChainContext;
import cn.learn.domain.strategy.service.armory.IStrategyArmory;
import cn.learn.domain.strategy.service.rule.chain.ILogicChain;
import cn.learn.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.learn.domain.strategy.service.rule.chain.impl.RuleWeightLogicChain;
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
 * @description 抽奖责任链测试，验证不同的规则走不同的责任链
 * @create 2024-01-20 11:20
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LogicChainTest {

    @Resource
    private IStrategyArmory strategyArmory;
    @Resource
    private RuleWeightLogicChain ruleWeightLogicChain;
    @Resource
    private DefaultChainFactory defaultChainFactory;

    @Before
    public void setUp() {
        // 策略装配 100001、100002、100003
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100001L));
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100002L));
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100003L));
    }

    @Test
    public void test_LogicChain_rule_blacklist() {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(100001L);
        LogicChainContext context = LogicChainContext.builder().userId("user001").strategyId(100001L).build();

        // 连续测试5次
        for (int i = 0; i < 5; i++) {
            context = logicChain.process(context);
            Integer awardId = context.getAwardId();
            log.info("测试结果：{}", awardId);
        }
    }

    @Test
    public void test_LogicChain_rule_weight() {
        // 通过反射 mock 规则中的值，模拟当前用户的积分值 userScore
        long userScore = 66666L;
        ReflectionTestUtils.setField(ruleWeightLogicChain, "userScore", userScore);
        // 策略100001的权重配置信息：4000:102,103,104,105 5000:102,103,104,105,106,107 6000:108,109

        long strategyID = 100001L;
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyID);
        LogicChainContext context = LogicChainContext.builder().userId("joyboy").strategyId(strategyID).build();

        // 连续测试5次
        for (int i = 0; i < 5; i++) {
            context = logicChain.process(context);
            Integer awardId = context.getAwardId();
            log.info("测试结果：{}", awardId);
        }
    }

    @Test
    public void test_LogicChain_rule_default() {
        long strategyID = 100001L;
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyID);
        LogicChainContext context = LogicChainContext.builder().userId("joyboy").strategyId(strategyID).build();

        // 连续测试5次
        for (int i = 0; i < 5; i++) {
            context = logicChain.process(context);
            Integer awardId = context.getAwardId();
            log.info("测试结果：{}", awardId);
        }
    }

}
