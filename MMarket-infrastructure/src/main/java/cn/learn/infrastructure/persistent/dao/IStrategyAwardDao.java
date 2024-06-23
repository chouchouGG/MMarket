package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.StrategyAwardPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-05-29 14:22
 **/
@Mapper
public interface IStrategyAwardDao {

    List<StrategyAwardPO> queryStrategyAwardList();

    List<StrategyAwardPO> queryStrategyAwardListByStrategyId(Long strategyId);

    String queryStrategyAwardRuleModelVO(StrategyAwardPO strategyAward);

    void updateStrategyAwardStock(StrategyAwardPO strategyAward);
}
