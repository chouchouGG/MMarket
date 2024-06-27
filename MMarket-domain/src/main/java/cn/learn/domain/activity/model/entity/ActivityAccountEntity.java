package cn.learn.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chouchouGG
 * @description 活动账户实体对象，相比PO对象少了【自增id】、【创建时间】、【更新时间】三个属性
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAccountEntity {

    private String userId;          // 用户ID
    private Long activityId;        // 活动ID
    private Integer totalCount;     // 总次数
    private Integer totalCountSurplus; // 总次数-剩余
    private Integer dayCount;       // 日次数
    private Integer dayCountSurplus; // 日次数-剩余
    private Integer monthCount;     // 月次数
    private Integer monthCountSurplus; // 月次数-剩余


}
