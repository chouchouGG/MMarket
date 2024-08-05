package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.learn.infrastructure.persistent.po.RaffleActivityAccountMonthPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动账户表 - 月次数
 */
@Mapper
public interface IRaffleActivityAccountMonthDao {
    /**
     * <h1>更新账户的月额度</h1>
     * <h2>具体操作：月额度账户中的月剩余额度 - 1</h2>
     */
    int updateActivityAccountMonthSubtractionQuota(RaffleActivityAccountMonthPO raffleActivityAccountMonth);

    void insertActivityAccountMonth(RaffleActivityAccountMonthPO raffleActivityAccountMonth);

    @DBRouter
    RaffleActivityAccountMonthPO queryActivityAccountMonth(RaffleActivityAccountMonthPO raffleActivityAccountMonthReq);

    void addAccountQuota(RaffleActivityAccountMonthPO raffleActivityAccountMonth);

}