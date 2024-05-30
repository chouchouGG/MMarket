package cn.learn.test;

import cn.learn.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Autowired
    private IRedisService redisService;

    /**
     * @description: 测试以空间换时间的抽奖概率策略，抽奖本质上是通过随机值来实现，提前在 redis 中生成出每个随机值对应的奖品 id。
     * @description: 随机值实现的抽奖概率策略，如果某个事件的概率是万分位描述的，那么其范围是从 0 到 10000，共有 10000 个可能的结果。
     */
    @Test
    public void test() {
        RMap<Object, Object> map = redisService.getMap("strategy_id_100001");
        map.put(0, 101);
        map.put(1, 101);
        map.put(2, 102);
        map.put(3, 102);
        map.put(4, 102);
        map.put(5, 102);
        map.put(6, 103);
        map.put(7, 103);
        map.put(8, 103);
        map.put(9, 103);
        log.info("测试结果：{}", redisService.getFromMap("strategy_id_100001", 1).toString());
    }

}
