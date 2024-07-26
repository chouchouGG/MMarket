package cn.learn.infrastructure.persistent.dao;

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
     * @param raffleActivityAccount
     * @return
     */
    int updateAccountQuota(RaffleActivityAccountPO raffleActivityAccount);
}
