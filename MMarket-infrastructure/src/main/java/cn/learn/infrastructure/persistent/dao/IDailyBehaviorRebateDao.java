package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.DailyBehaviorRebatePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <h1>日常行为返利活动配置</h1>
 */
@Mapper
public interface IDailyBehaviorRebateDao {

    /**
     * 查询出行为类型为给定类型且配置开启（open）的返利配置
     * @param behaviorType
     * @return
     */
    List<DailyBehaviorRebatePO> queryDailyBehaviorRebateByBehaviorType(String behaviorType);


}
