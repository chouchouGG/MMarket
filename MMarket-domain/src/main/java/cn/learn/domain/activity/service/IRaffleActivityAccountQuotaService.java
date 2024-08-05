package cn.learn.domain.activity.service;

import cn.learn.domain.activity.model.entity.ActivityAccountEntity;
import cn.learn.domain.activity.model.entity.SkuRechargeEntity;

/**
 * @author 98389
 * @description 抽奖活动账户额度服务接口
 * @create 2024-03-16 08:38
 */
public interface IRaffleActivityAccountQuotaService {

    /**
     * <p>创建用户账户额度充值订单 —— 给用户增加抽奖额度次数。</p>
     * <p>note 在【签到、分享、积分兑换】等行为动作下，创建出活动订单，给用户账户的【总、日、月】额度充值可用的抽奖次数。</p>
     *
     */
    String createAccountQuotaRechargeOrder(SkuRechargeEntity skuRechargeEntity);

    /**
     * 查询用户账户「总、月、日」额度
     */
    ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId);

    /**
     * 查询用户账户的【日参与次数】
     */
    Integer queryAccountDayPartakeCount(Long activityId, String userId);

    /**
     * 查询用户账户的【总参与次数】
     */
    Integer queryAccountTotalPartakeCount(Long activityId, String userId);

}
