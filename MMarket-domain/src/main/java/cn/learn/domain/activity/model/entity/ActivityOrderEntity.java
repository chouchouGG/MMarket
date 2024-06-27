package cn.learn.domain.activity.model.entity;

import cn.learn.domain.activity.model.valobj.OrderStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chouchouGG
 * @description 活动参与实体对象，相比PO对象少了【自增id】、【创建时间】、【更新时间】三个属性
 * @create 2024-03-16 09:02
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityOrderEntity {


    private String userId;         // 用户ID
    private Long activityId;       // 活动ID
    private String activityName;   // 活动名称
    private Long strategyId;       // 抽奖策略ID
    private String orderId;        // 订单ID
    private Date orderTime;        // 下单时间
    private Integer totalCount;    // 总次数
    private Integer dayCount;      // 日次数
    private Integer monthCount;    // 月次数
    private OrderStateVO state;    // 订单状态

}
