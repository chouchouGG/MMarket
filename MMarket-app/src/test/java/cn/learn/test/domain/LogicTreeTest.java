package cn.learn.test.domain;

import cn.learn.domain.strategy.model.entity.ProcessingContext;
import cn.learn.domain.strategy.service.rule.tree.engine.IDecisionTreeEngine;
import cn.learn.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LogicTreeTest {

    @Resource
    private DefaultTreeFactory defaultTreeFactory;


    @Test
    public void test_tree_rule() {

        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree();

        ProcessingContext context = ProcessingContext.builder()
                .userId("joyboy")
                .strategyId(100001L)
                .status(ProcessingContext.ProcessStatus.CONTINUE)
                .awardId(100).build();

        treeEngine.process(context);

        log.info("测试结果：{}", JSON.toJSONString(context));

    }

}
