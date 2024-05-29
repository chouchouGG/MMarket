package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.StrategyAwardPO;
import org.apache.ibatis.annotations.Mapper;

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
}
