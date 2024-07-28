package cn.learn.domain.award.service;

import cn.learn.domain.award.model.entity.UserAwardRecordEntity;

/**
 * <H1>奖品服务接口</H1>
 */
public interface IAwardService {

    /**
     * <p>该方法功能：</p>
     * <p>1. 根据传入的用户中奖记录实体对象，构建MQ奖品发放的任务对象，并组合为聚合对象。</p>
     * <p>2. 调用奖品仓储层进行核心逻辑。</p>
     * @param userAwardRecordEntity 用户中奖记录入参
     */
    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);

}