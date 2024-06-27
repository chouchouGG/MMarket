package cn.learn.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chouchouGG
 * @description 活动次数实体对象，相比PO对象少了【自增id】、【创建时间】、【更新时间】三个属性
 * @create 2024-03-16 11:19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCountEntity {

    private Long activityCountId;  // 活动次数编号
    private Integer totalCount;    // 总次数
    private Integer dayCount;      // 日次数
    private Integer monthCount;    // 月次数

}
