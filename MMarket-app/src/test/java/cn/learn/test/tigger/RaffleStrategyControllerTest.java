package cn.learn.test.tigger;

import cn.learn.trigger.api.IRaffleStrategyService;
import cn.learn.trigger.api.dto.RaffleAwardListReqDTO;
import cn.learn.trigger.api.dto.RaffleAwardListResDTO;
import cn.learn.trigger.api.dto.RaffleStrategyRuleWeightReqDTO;
import cn.learn.trigger.api.dto.RaffleStrategyRuleWeightResDTO;
import cn.learn.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 营销抽奖服务测试
 * @create 2024-04-20 10:41
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleStrategyControllerTest {

    @Resource
    private IRaffleStrategyService raffleStrategyService;

    @Test
    public void test_queryRaffleAwardList() {
        RaffleAwardListReqDTO request = new RaffleAwardListReqDTO();
        request.setUserId("joyboy");
        request.setActivityId(100301L);

        Response<List<RaffleAwardListResDTO>> response = raffleStrategyService.queryRaffleAwardList(request);

        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    @Test
    public void test_queryRaffleStrategyRuleWeight() {
        RaffleStrategyRuleWeightReqDTO request = new RaffleStrategyRuleWeightReqDTO();
        request.setUserId("joyboy");
        request.setActivityId(100301L);

        Response<List<RaffleStrategyRuleWeightResDTO>> response = raffleStrategyService.queryRaffleStrategyRuleWeight(request);
        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }
}
