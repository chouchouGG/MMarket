package cn.learn.domain.activity.service.armory;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 活动装配预热，也就是将活动相关的数据缓存起来
 * @create 2024-03-30 09:09
 */
public interface IActivityArmory {

    boolean assembleActivitySku(Long sku);

    /**
     * 通过活动ID进行装配
     */
    boolean assembleActivitySkuByActivityId(Long activityId);

}
