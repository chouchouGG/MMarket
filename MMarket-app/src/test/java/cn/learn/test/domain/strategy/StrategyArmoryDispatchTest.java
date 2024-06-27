package cn.learn.test.domain.strategy;

import cn.learn.domain.strategy.service.armory.IStrategyArmory;
import cn.learn.domain.strategy.service.armory.IStrategyDispatch;
import cn.learn.domain.strategy.service.armory.StrategyArmoryDispatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @program: MMarket
 * @description: 对StrategyArmory进行单元测试
 * @author: chouchouGG
 * @create: 2024-05-31 03:03
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyArmoryDispatchTest {

    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IStrategyDispatch strategyDispatch;

    @Before
    public void test_assembleLotteryStrategy() {
        boolean success = strategyArmory.assembleLotteryStrategy(100001L);
        log.info("test_assembleLotteryStrategy 测试结果：{}", success);
    }

    @Test
    public void test_getRandomAwardId() {
        for (int i = 0; i < 5; i++) {
            log.info("第 {} 次随机抽取的奖品id为：{}", i, strategyDispatch.getRandomAwardId(100001L));
        }
    }

    @Test
    public void test_getRandomAwardId_ruleWeightValue() {
        log.info("测试结果：{} - 4000 策略配置", strategyDispatch.getRandomAwardId(100001L, "4000:102,103,104,105"));
        log.info("测试结果：{} - 5000 策略配置", strategyDispatch.getRandomAwardId(100001L, "5000:102,103,104,105,106,107"));
        log.info("测试结果：{} - 6000 策略配置", strategyDispatch.getRandomAwardId(100001L, "6000:108,109"));
    }
}
