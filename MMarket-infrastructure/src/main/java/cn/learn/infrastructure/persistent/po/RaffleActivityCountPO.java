package cn.learn.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * @author 98389
 * @description 抽奖活动次数配置表 持久化对象
 */
@Data
public class RaffleActivityCountPO {

    private Long id;                // 自增ID
    private Long activityCountId;   // 活动次数编号
    private Integer totalCount;     // 总次数
    private Integer dayCount;       // 日次数
    private Integer monthCount;     // 月次数
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间

}


