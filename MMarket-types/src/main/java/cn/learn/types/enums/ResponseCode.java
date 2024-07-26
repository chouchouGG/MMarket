package cn.learn.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 98389
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UNKNOW_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    STRATEGY_RULE_WEIGHT_IS_NULL("ERR_BIZ_001", "业务异常，策略规则中 rule_weight 权重规则已适用但未配置"),
    UN_ASSEMBLED_STRATEGY_ARMORY("ERR_BIZ_002", "抽奖策略配置未装配，请通过IStrategyArmory完成装配"),

    INDEX_DUP("0003", "唯一索引冲突"),

    ACTIVITY_STATE_ERROR("ERR_BIZ_003", "活动未开启（非open状态），请检查数据库配置"),
    ACTIVITY_DATE_ERROR("ERR_BIZ_004", "非活动日期范围"),
    ACTIVITY_SKU_STOCK_ERROR("ERR_BIZ_005", "活动库存不足，请检查数据库配置"),


    // fixme：这里的状态定义的是异常错误码，是开始没有理解 ResponseCode 类是描述http响应状态的类
    //  后续需要将下面这两个异常描述拆分出去
    RULE_NOT_DEFINED("0003", "未定义规则对应的决策树节点"),
    PARSE_ERROR("parse error", "解析数据失败")
    ;

    private String code;
    private String info;

}
