package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.learn.infrastructure.persistent.po.UserBehaviorRebateOrderPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <h1>用户行为返利订单流水</h1>
 **/
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserBehaviorRebateOrderDao {

    void insert(UserBehaviorRebateOrderPO userBehaviorRebateOrder);

}