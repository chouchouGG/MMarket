package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.learn.infrastructure.persistent.po.UserRaffleOrderPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户抽奖订单表
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserRaffleOrderDao {

    void insert(UserRaffleOrderPO build);

    @DBRouter
    UserRaffleOrderPO queryNoUsedRaffleOrder(UserRaffleOrderPO userRaffleOrderReq);
}
