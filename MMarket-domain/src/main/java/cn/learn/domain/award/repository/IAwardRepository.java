package cn.learn.domain.award.repository;

import cn.learn.domain.award.model.aggregate.UserAwardRecordAggregate;

/**
 * <h1>奖品仓储服务</h1>
 */
public interface IAwardRepository {

    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

}