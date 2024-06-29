package cn.learn.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * @description 抽奖活动账户表 持久化对象
 */
@Data
public class RaffleActivityAccountPO {

    private Long id;                // 自增ID
    private String userId;          // 用户ID
    private Long activityId;        // 活动ID
    private Integer totalCount;         // 总次数 （用于用户订单的核对）
    private Integer totalCountSurplus;  // 总次数-剩余（用户可用次数）
    private Integer dayCount;           // 日次数
    private Integer dayCountSurplus;    // 日次数-剩余（用户可用次数）
    private Integer monthCount;         // 月次数
    private Integer monthCountSurplus;  // 月次数-剩余（用户可用次数）
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间


}
