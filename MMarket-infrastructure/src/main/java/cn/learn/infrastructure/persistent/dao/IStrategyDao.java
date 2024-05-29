package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.StrategyPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-05-29 14:25
 **/
@Mapper
public interface IStrategyDao {

    List<StrategyPO> queryStrategyList();
}
