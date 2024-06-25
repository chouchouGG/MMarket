package cn.learn.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * @author 98389
 * @description 抽奖活动账户流水表 持久化对象
 */
@Data
public class RaffleActivityAccountFlowPO {

    private Integer id;             // 自增ID
    private String userId;          // 用户ID
    private Long activityId;        // 活动ID
    private Integer totalCount;     // 总次数
    private Integer dayCount;       // 日次数
    private Integer monthCount;     // 月次数
    private String flowId;          // 流水ID - 生成的唯一ID
    private String flowChannel;     // 流水渠道（activity-活动领取、sale-购买、redeem-兑换、free-免费赠送）
    private String bizId;           // 业务ID（外部透传，活动ID、订单ID）
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间


}
