package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.RaffleActivityPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 【数据访问层】抽奖活动表
 */
@Mapper
public interface IRaffleActivityDao {

    /**
     * 根据活动ID查询抽奖活动信息。
     *
     * @param activityId 活动ID
     * @return 抽奖活动信息对象，如果不存在则返回null
     */
    RaffleActivityPO queryRaffleActivityByActivityId(Long activityId);

    /**
     * 根据活动ID查询策略ID。
     *
     * @param activityId 活动ID
     * @return 策略ID，如果未找到则返回null
     */
    Long queryStrategyIdByActivityId(Long activityId);

    /**
     * 根据策略ID查询活动ID。
     *
     * @param strategyId 策略ID
     * @return 活动ID，如果未找到则返回null
     */
    Long queryActivityIdByStrategyId(Long strategyId);


}
