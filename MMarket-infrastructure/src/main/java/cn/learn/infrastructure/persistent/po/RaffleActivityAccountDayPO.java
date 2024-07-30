package cn.learn.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 账户日次数表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivityAccountDayPO {

    /** 自增ID */
    private String id;

    /** 用户ID */
    private String userId;

    /** 活动ID */
    private Long activityId;

    /** 日期（yyyy-mm-dd） */
    private String day;

    /** 日次数 */
    private Integer dayCount;

    /** 日次数-剩余 */
    private Integer dayCountSurplus;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /**
     * @return 当日的格式化日期：yyyy-MM-dd
     */
    public static String currentFormatedDay() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

}