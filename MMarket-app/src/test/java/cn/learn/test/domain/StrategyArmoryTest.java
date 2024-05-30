package cn.learn.test.domain;

import cn.learn.domain.strategy.service.armory.IStrategyArmory;
import cn.learn.domain.strategy.service.armory.StrategyArmory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
public class StrategyArmoryTest {

    @Resource
    private IStrategyArmory strategyArmory;

    @Test
    public void test_assembleLotteryStrategy() {
        strategyArmory.assembleLotteryStrategy(100002L);
    }

    @Test
    public void test_getRandomAwardId() {
        for (int i = 0; i < 5; i++) {
            log.info("第 {} 次随机抽取的奖品id为：{}", i, strategyArmory.getRandomAwardId(100002L));
        }
    }
}
