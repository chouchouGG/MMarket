package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.learn.infrastructure.persistent.po.RaffleActivityAccountDayPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动账户表-日次数
 */
@Mapper
public interface IRaffleActivityAccountDayDao {
    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDayPO raffleActivityAccountDay);

    void insertActivityAccountDay(RaffleActivityAccountDayPO raffleActivityAccountDay);

    @DBRouter
    RaffleActivityAccountDayPO queryActivityAccountDay(RaffleActivityAccountDayPO raffleActivityAccountDayReq);

}