package cn.learn.test;

import com.esotericsoftware.minlog.Log;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.Test;

/**
 * @program: MMarket
 * @description: easyRandom工具测试
 * @author: chouchouGG
 * @create: 2024-06-26 13:17
 **/
@Slf4j
public class EasyRandomTest {

    private static final EasyRandom easyRandom;

    static {
        EasyRandomParameters parameters = new EasyRandomParameters()
                .seed(System.currentTimeMillis());
        easyRandom = new EasyRandom(parameters);
    }

    @Test
    public void test() {
        for (int i = 0; i < 10; i++) {
            String s = easyRandom.nextObject(String.class);
            log.info("第{}次随机生成字符串：{}", i, s);
        }
    }
}
