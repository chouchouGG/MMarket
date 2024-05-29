package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.StrategyRulePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-05-29 14:26
 **/
@Mapper
public interface IStrategyRuleDao {

    List<StrategyRulePO> queryStrategyRuleList();
}
