package cn.learn.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author 98389
 * @description 抽奖活动单 持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivityOrderPO {

    private Long id;                // 自增ID
    private Long sku;
    private String userId;          // 用户ID
    private Long activityId;        // 活动ID
    private String activityName;    // 活动名称
    private Long strategyId;        // 抽奖策略ID
    private String orderId;         // 订单ID
    private Date orderTime;         // 下单时间
    private Integer totalCount;     // 总次数 note：本质是 totalCountAdded，因为是从抽奖活动次数表里查出来的
    private Integer dayCount;       // 日次数 note：本质是 dayCountAdded，因为是从抽奖活动次数表里查出来的
    private Integer monthCount;     // 月次数 note：本质是 monthCountAdded，因为是从抽奖活动次数表里查出来的
    private String state;           // 订单状态（complete）
    private String outBusinessNo;   // fixme: 已经有了order_id且是唯一索引为什么还需要out_business_no？
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间


}
