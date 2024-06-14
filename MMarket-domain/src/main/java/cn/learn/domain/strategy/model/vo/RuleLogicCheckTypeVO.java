package cn.learn.domain.strategy.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @program: MMarket
 * @description: 规则过滤后返回的具体的值对象，包含在【规则行为实体类 RuleActionEntity】中返回
 * @author: chouchouGG
 * @create: 2024-06-13 16:20
 **/
@Getter
@AllArgsConstructor
public enum RuleLogicCheckTypeVO {

    ALLOW("0000", "放行；执行后续的流程，不受规则引擎影响"),
    TAKE_OVER("0001","接管；后续的流程，受规则引擎执行结果影响"),
    ;

    private final String code;
    private final String info;

}
