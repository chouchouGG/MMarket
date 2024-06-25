package cn.learn.infrastructure.persistent.po;

import lombok.*;

import java.util.Date;

/**
 * @description 抽奖活动表 持久化对象
 */
@Data
public class RaffleActivityPO {

    private Long id;                // 自增ID
    private Long activityId;        // 活动ID
    private String activityName;    // 活动名称
    private String activityDesc;    // 活动描述
    private Date beginDateTime;     // 开始时间
    private Date endDateTime;       // 结束时间
    private Integer stockCount;     // 库存总量
    private Integer stockCountSurplus; // 剩余库存
    private Long activityCountId;   // 活动参与次数配置
    private Long strategyId;        // 抽奖策略ID
    private String state;           // 活动状态
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间


}
