package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.learn.infrastructure.persistent.po.RaffleActivityOrderPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description 【数据访问层】抽奖活动订单表
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IRaffleActivityOrderDao {

    @DBRouter(key = "userId")
    void insert(RaffleActivityOrderPO raffleActivityOrder);

    @DBRouter
    List<RaffleActivityOrderPO> queryRaffleActivityOrderByUserId(String userId);

}
