package cn.learn.test.domain.strategy;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.domain.strategy.service.IRaffleAward;
import cn.learn.domain.strategy.service.IRaffleRule;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-07-30 22:23
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class IRaffleRuleTest {

    @Resource
    IRaffleAward raffleAward;

    @Resource
    IRaffleRule raffleRule;

    @Test
    public void test_queryAwardRuleLockCount() {
        List<StrategyAwardEntity> strategyAwardEntities = raffleAward.queryRaffleStrategyAwardListByActivityId(100301L);
        Map<Integer, Integer> awardRuleLockCountMap = raffleRule.queryAwardRuleLockCount(strategyAwardEntities);
        System.out.println(JSON.toJSON(awardRuleLockCountMap));
    }



}
