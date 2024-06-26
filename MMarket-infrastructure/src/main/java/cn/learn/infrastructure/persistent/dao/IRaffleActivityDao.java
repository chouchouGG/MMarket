package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.RaffleActivityPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 【数据访问层】抽奖活动表
 */
@Mapper
public interface IRaffleActivityDao {

    RaffleActivityPO queryRaffleActivityByActivityId(Long activityId);

}
