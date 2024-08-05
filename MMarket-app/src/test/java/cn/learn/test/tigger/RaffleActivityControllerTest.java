package cn.learn.test.tigger;

import cn.learn.trigger.api.IRaffleActivityService;
import cn.learn.trigger.api.dto.ActivityDrawReqDTO;
import cn.learn.trigger.api.dto.ActivityDrawResDTO;
import cn.learn.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动服务测试
 * @create 2024-04-20 11:02
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityControllerTest {

    @Resource
    private IRaffleActivityService raffleActivityService;

    @Test
    public void test_armory() {
        Response<Boolean> response = raffleActivityService.armory(100301L);
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    @Test
    public void test_draw() {
        ActivityDrawReqDTO request = new ActivityDrawReqDTO();
        request.setActivityId(100301L);
        request.setUserId("joyboy");
        Response<ActivityDrawResDTO> response = raffleActivityService.draw(request);

        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    /**
     * 进行签到
     */
    @Test
    public void test_calendarSignRebate() throws InterruptedException {
        Response<Boolean> response = raffleActivityService.calendarSignRebate("joyboy");
        // note：主线程等待MQ消息的处理线程处理完MQ消息，否则有可能看不到账户额度的变化
        new CountDownLatch(1).await();
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    /**
     * 检查是否进行了签到
     */
    @Test
    public void test_isCalendarSignRebate() {
        Response<Boolean> response = raffleActivityService.isCalendarSignRebate("joyboy");
        log.info("测试结果：{}", JSON.toJSONString(response));
    }
}
