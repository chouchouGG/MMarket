package cn.learn.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.learn.infrastructure.persistent.po.UserAwardRecordPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户中奖记录表
 */
@Mapper
@DBRouterStrategy(splitTable = true) // 分表
public interface IUserAwardRecordDao {

    void insert(UserAwardRecordPO userAwardRecord);

}