package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.AwardPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @program: MMarket
 * @description: 奖品 dao
 * @author: chouchouGG
 * @create: 2024-05-29 14:08
 **/
@Mapper
public interface IAwardDao {

    List<AwardPO> queryAwardList();
}
