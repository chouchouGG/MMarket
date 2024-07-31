package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.learn.infrastructure.persistent.po.RaffleActivityAccountDayPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动账户表-日次数
 */
@Mapper
public interface IRaffleActivityAccountDayDao {

    /**
     * 插入新的日账户记录。
     */
    void insertActivityAccountDay(RaffleActivityAccountDayPO raffleActivityAccountDay);

    /**
     * 更新日账户记录中的剩余额度。
     * 将记录中的 `day_count_surplus` 减少1。并且仅当 `day_count_surplus` 大于0时才进行更新。
     *
     * @return 更新成功的记录数。一般应为1，如果返回值不是1，则表示更新失败。
     */
    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDayPO raffleActivityAccountDay);

    /**
     * 查询日账户记录。
     *
     * @param raffleActivityAccountDayReq 查询条件对象。包含用户ID、活动ID、日期。
     * @return 查询到的日账户记录对象。如果没有找到匹配的记录，则返回null。
     */
    @DBRouter
    RaffleActivityAccountDayPO queryActivityAccountDay(RaffleActivityAccountDayPO raffleActivityAccountDayReq);

    @DBRouter
    Integer queryAccountDayPartakeCount(RaffleActivityAccountDayPO raffleActivityAccountDay);
}