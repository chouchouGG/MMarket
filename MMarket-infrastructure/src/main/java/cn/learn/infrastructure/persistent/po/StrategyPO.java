package cn.learn.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * @program: MMarket
 * @description: 抽奖策略
 * @author: chouchouGG
 * @create: 2024-05-29 13:48
 **/
@Data
public class StrategyPO {

    /** 自增ID */
    private Long id;

    /** 抽奖策略ID */
    private Long strategyId;

    /** 抽奖策略描述 */
    private String strategyDesc;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
