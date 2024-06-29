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
    // note：抽奖活动次数表中的这些次数，对应于用户分享，签到，实名认证等不同行为对用户抽奖次数的次数增加的配置
    private Integer totalCount;     // 总次数 note：本质是 totalCountAdded
    private Integer dayCount;       // 日次数 note：本质是 dayCountAdded
    private Integer monthCount;     // 月次数 note：本质是 monthCountAdded
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间

}


