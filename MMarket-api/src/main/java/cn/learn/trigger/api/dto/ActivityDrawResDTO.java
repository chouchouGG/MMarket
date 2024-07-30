package cn.learn.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>活动抽奖的响应对象</h1>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDrawResDTO {

    /**
     * 奖品ID
      */
    private Integer awardId;

    /**
     * 奖品标题
     */
    private String awardTitle;

    /**
     * 排序编号【策略奖品配置的奖品顺序编号】(不是很重要)
     */
    private Integer awardIndex;

}
