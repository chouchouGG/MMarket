package cn.learn.domain.activity.model.entity;

import cn.learn.domain.activity.model.valobj.ActivityStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chouchouGG
 * @description 活动实体对象，相比PO对象少了【自增id】、【创建时间】、【更新时间】三个属性
 * @create 2024-03-16 11:15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEntity {

    private Long activityId;        // 活动ID
    private String activityName;    // 活动名称
    private String activityDesc;    // 活动描述
    private Date beginDateTime;     // 开始时间
    private Date endDateTime;       // 结束时间
    private Long strategyId;        // 抽奖策略ID
    private ActivityStateVO state;  // 活动状态

}
