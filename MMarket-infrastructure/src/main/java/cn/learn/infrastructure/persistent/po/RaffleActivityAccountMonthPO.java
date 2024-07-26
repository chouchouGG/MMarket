package cn.learn.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 账户月次数表
 */
@Data
public class RaffleActivityAccountMonthPO {

    /** 自增ID */
    private String id;
    /** 用户ID */
    private String userId;
    /** 活动ID */
    private Long activityId;
    /** 月（yyyy-mm） */
    private String month;
    /** 月总次数 */
    private Integer monthCount;
    /** 月剩余次数 */
    private Integer monthCountSurplus;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}