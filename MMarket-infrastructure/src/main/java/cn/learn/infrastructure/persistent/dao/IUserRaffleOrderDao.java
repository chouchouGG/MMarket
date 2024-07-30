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

    /**
     * 插入新的用户抽奖订单记录。
     */
    void insert(UserRaffleOrderPO userRaffleOrder);

    /**
     * 查询未使用的用户抽奖订单。
     */
    @DBRouter
    UserRaffleOrderPO queryNoUsedRaffleOrder(UserRaffleOrderPO userRaffleOrderReq);

    /**
     * 更新用户抽奖订单的状态为已使用。
     *
     * @return 更新成功的记录数
     */
    int updateUserRaffleOrderStateUsed(UserRaffleOrderPO userRaffleOrderReq);

}
