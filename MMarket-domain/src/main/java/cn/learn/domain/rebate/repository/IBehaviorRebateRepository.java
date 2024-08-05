package cn.learn.domain.rebate.repository;

import cn.learn.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.learn.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.learn.domain.rebate.model.entity.DailyBehaviorRebateEntity;
import cn.learn.domain.rebate.model.valobj.BehaviorTypeVO;

import java.util.List;

/**
 * 行为返利服务仓储接口
 */
public interface IBehaviorRebateRepository {

    List<DailyBehaviorRebateEntity> queryDailyBehaviorRebateConfig(BehaviorTypeVO behaviorTypeVO);

    /**
     *
     * @param userId 进行分库分表
     * @param behaviorRebateAggregates
     */
    void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates);


    List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo);

}
