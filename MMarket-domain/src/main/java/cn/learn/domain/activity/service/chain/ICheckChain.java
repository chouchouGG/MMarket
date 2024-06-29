package cn.learn.domain.activity.service.chain;

import cn.learn.domain.activity.model.entity.ActivityCountEntity;
import cn.learn.domain.activity.model.entity.ActivityEntity;
import cn.learn.domain.activity.model.entity.ActivitySkuEntity;

/**
 * @program: MMarket
 * @description: 校验责任链的接口
 * @author: chouchouGG
 * @create: 2024-06-28 10:10
 **/
public interface ICheckChain extends ICheckChainArmory {

    boolean handle(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

}
