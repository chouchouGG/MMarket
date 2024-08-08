package cn.learn.trigger.api;

import cn.learn.trigger.api.dto.ActivityDrawReqDTO;
import cn.learn.trigger.api.dto.ActivityDrawResDTO;
import cn.learn.trigger.api.dto.UserActivityAccountReqDTO;
import cn.learn.trigger.api.dto.UserActivityAccountResDTO;
import cn.learn.types.model.Response;

/**
 * 抽奖活动服务接口
 **/
public interface IRaffleActivityService {

    /**
     * 抽奖活动装配和抽奖策略装配，总的装配逻辑
     */
    Response<Boolean> armory(Long activityId);

    /**
     * 用户进行活动抽奖的接口
     */
    Response<ActivityDrawResDTO> draw(ActivityDrawReqDTO request);

    /**
     * 签到行为返利接口
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    Response<Boolean> calendarSignRebate(String userId);

    /**
     * 判断是否完成日历签到返利接口（根据这个接口来渲染前端签到按钮）
     *
     * @param userId 用户ID
     * @return 签到结果 true 已签到，false 未签到
     */
    @Deprecated
    Response<Boolean> isCalendarSignRebate(String userId);

    /**
     * 查询用户活动账户
     *
     * @param request 请求对象「活动ID、用户ID」
     * @return 返回结果「总额度、月额度、日额度」
     */
    @Deprecated
    Response<UserActivityAccountResDTO> queryUserActivityAccount(UserActivityAccountReqDTO request);

}
