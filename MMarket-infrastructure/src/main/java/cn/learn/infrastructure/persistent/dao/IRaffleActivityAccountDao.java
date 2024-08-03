package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.learn.infrastructure.persistent.po.RaffleActivityAccountPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 98389
 * @description 【数据访问层】抽奖活动账户表
 */
@Mapper
public interface IRaffleActivityAccountDao {
    void insert(RaffleActivityAccountPO raffleActivityAccount);

    /**
     * 更新操作是将增量直接添加
     */
    int updateAccountQuota(RaffleActivityAccountPO raffleActivityAccount);

    /**
     * 更新账户总额度
     * <p>操作：总剩余额度 - 1</p>
     */
    int updateActivityAccountSubtractionQuota(RaffleActivityAccountPO raffleActivityAccount);

    /**
     * 更新账户总额度
     * <p>操作：月剩余额度 - 1</p>
     */
    int updateActivityAccountMonthSubtractionQuota(RaffleActivityAccountPO raffleActivityAccount);

    /**
     * 更新账户总额度
     * <p>操作：日剩余额度 - 1</p>
     */
    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountPO raffleActivityAccount);

    /**
     * 更新账户月的镜像额度
     * <p>操作：传入参数的月剩余额度 - 1</p>
     */
    void updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccountPO raffleActivityAccount);

    /**
     * 更新账户日的镜像额度
     * <p>操作：传入参数的日剩余额度 - 1</p>
     */
    void updateActivityAccountDaySurplusImageQuota(RaffleActivityAccountPO raffleActivityAccount);

    @DBRouter // 分库分表路由
    RaffleActivityAccountPO queryActivityAccount(RaffleActivityAccountPO param);

}
