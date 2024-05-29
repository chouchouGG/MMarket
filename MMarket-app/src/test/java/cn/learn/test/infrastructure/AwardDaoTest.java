package cn.learn.test.infrastructure;

import cn.learn.infrastructure.persistent.dao.IAwardDao;
import cn.learn.infrastructure.persistent.po.AwardPO;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @program: MMarket
 * @description: 奖品持久化单元测试
 * @author: chouchouGG
 * @create: 2024-05-29 20:20
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AwardDaoTest {

    @Autowired
    IAwardDao iAwardDao;

    @Test
    public void test_queryAwardList() {
         List<AwardPO> awardList = iAwardDao.queryAwardList();
         log.info("测试结果：{}", JSON.toJSONString(awardList));
    }

}
