package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.RaffleActivityCountPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 【数据访问层】抽奖活动次数表
 */
@Mapper
public interface IRaffleActivityCountDao {
    RaffleActivityCountPO queryRaffleActivityCountByActivityCountId(Long activityCountId);

}
