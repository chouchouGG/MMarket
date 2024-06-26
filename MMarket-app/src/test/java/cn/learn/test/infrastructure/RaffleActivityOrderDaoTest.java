package cn.learn.test.infrastructure;

import cn.learn.infrastructure.persistent.dao.IRaffleActivityOrderDao;
import cn.learn.infrastructure.persistent.po.RaffleActivityOrderPO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @description 抽奖活动订单测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityOrderDaoTest {

    @Resource
    private IRaffleActivityOrderDao raffleActivityOrderDao;

    private static final EasyRandom easyRandom;

    static {
        EasyRandomParameters parameters = new EasyRandomParameters()
                .seed(System.currentTimeMillis());
        easyRandom = new EasyRandom(parameters);
    }

    @Test
    public void test_insert_random() {
        for (int i = 0; i < 5; i++) {
            RaffleActivityOrderPO raffleActivityOrder = new RaffleActivityOrderPO();
            raffleActivityOrder.setUserId(easyRandom.nextObject(String.class));
            raffleActivityOrder.setActivityId(100301L);
            raffleActivityOrder.setActivityName("测试活动");
            raffleActivityOrder.setStrategyId(100001L);
            raffleActivityOrder.setOrderId(RandomStringUtils.randomNumeric(12));
            raffleActivityOrder.setOrderTime(new Date());
            raffleActivityOrder.setState("not_used");
            // 插入数据
            raffleActivityOrderDao.insert(raffleActivityOrder);
        }
    }

    @Test
    public void test_insert() {
        RaffleActivityOrderPO raffleActivityOrder = new RaffleActivityOrderPO();
        raffleActivityOrder.setUserId("xiaofuge");
        raffleActivityOrder.setActivityId(100301L);
        raffleActivityOrder.setActivityName("测试活动");
        raffleActivityOrder.setStrategyId(100006L);
        raffleActivityOrder.setOrderId(RandomStringUtils.randomNumeric(12));
        raffleActivityOrder.setOrderTime(new Date());
        raffleActivityOrder.setState("not_used");
        // 插入数据
        raffleActivityOrderDao.insert(raffleActivityOrder);
    }

    @Test
    public void test_queryRaffleActivityOrderByUserId() {
        String userId = "xiaofuge";
        List<RaffleActivityOrderPO> raffleActivityOrders = raffleActivityOrderDao.queryRaffleActivityOrderByUserId(userId);
        log.info("测试结果：{}", JSON.toJSONString(raffleActivityOrders));
    }

}
