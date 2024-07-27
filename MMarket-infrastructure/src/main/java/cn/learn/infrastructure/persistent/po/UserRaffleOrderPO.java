package cn.learn.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <h1>用户抽奖订单表</h1>
 * <h2>表结构介绍：
 *  <ul>（user_id, activity_id）即（用户ID, 活动ID）设置了普通索引</ul>
 *  <ul>（order_id）即（订单ID）设置了唯一索引</ul>
 *  <ul>订单状态包括三种；create-创建、used-已使用、cancel-已作废	</ul>
 * </h2>
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRaffleOrderPO {

    private String id;
    /** 用户ID */
    private String userId;
    /** 活动ID */
    private Long activityId;
    /** 活动名称 */
    private String activityName;
    /** 抽奖策略ID */
    private Long strategyId;
    /** 订单ID */
    private String orderId;
    /** 下单时间 */
    private Date orderTime;
    /** 订单状态；create-创建、used-已使用、cancel-已作废 */
    private String orderState;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
