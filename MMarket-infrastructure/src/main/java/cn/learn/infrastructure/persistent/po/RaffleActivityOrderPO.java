package cn.learn.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * @author 98389
 * @description 抽奖活动单 持久化对象
 */
@Data
public class RaffleActivityOrderPO {

    // fixme：和库表相比少了一个sku字段
    private Long id;                // 自增ID
    private String userId;          // 用户ID
    private Long activityId;        // 活动ID
    private String activityName;    // 活动名称
    private Long strategyId;        // 抽奖策略ID
    private String orderId;         // 订单ID
    private Date orderTime;         // 下单时间
    private Integer totalCount; // 总次数
    private Integer dayCount; // 日次数
    private Integer monthCount; // 月次数
    private String state;           // 订单状态（complete）
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间


}
