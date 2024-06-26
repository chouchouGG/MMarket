package cn.learn.domain.activity.service;

import cn.learn.domain.activity.model.entity.SkuRechargeEntity;

/**
 * @author 98389
 * @description 抽奖活动订单接口
 * @create 2024-03-16 08:38
 */
public interface IRaffleOrder {

    /**
     * 创建 sku 账户充值订单，给用户增加抽奖次数
     * <p>
     * 1. 在【打卡、签到、分享、对话、积分兑换】等行为动作下，创建出活动订单，给用户的活动账户【日、月】充值可用的抽奖次数。
     * 2. 对于用户可获得的抽奖次数，比如登录赠送一次抽奖，则是依赖于运营配置的动作，在前端页面上。用户点击后，可以获得一次抽奖次数。
     *
     * @param skuRechargeEntity 活动商品充值实体对象
     * @return 活动ID
     */
    String createSkuRechargeOrder(SkuRechargeEntity skuRechargeEntity);

}
