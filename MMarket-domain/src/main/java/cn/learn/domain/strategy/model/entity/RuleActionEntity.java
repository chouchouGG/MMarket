package cn.learn.domain.strategy.model.entity;

/**
 * @program: MMarket
 * @description: 规则行为实体，规则过滤完成之后，应该返回的动作实体，动作实体决定后续规则引擎的具体执行流程。
 * @author: chouchouGG
 * @create: 2024-06-13 15:56
 **/

import cn.learn.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import lombok.*;

/**
 * note:
 *  @author 98389
 *
 * @param <T> RuleActionEntity传递过来的泛型一定是提前定义好的，做一定的限制
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleActionEntity<T extends RuleActionEntity.RaffleEntity> {
    @Builder.Default
    private String code = RuleLogicCheckTypeVO.ALLOW.getCode();
    @Builder.Default
    private String info = RuleLogicCheckTypeVO.ALLOW.getInfo();
    private String ruleModel;
    private T data;

    static public class RaffleEntity {

    }

    @EqualsAndHashCode(callSuper = true) // 暂时不知道放在这里的作用
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    // 抽奖之前
    static public class RaffleBeforeEntity extends RaffleEntity {

        //策略ID
        private Long strategyId;
        //权重值Key；用于抽奖时可以选择权重抽奖。
        private String ruleWeightValueKey;
        //奖品ID
        private Integer awardId;

    }


    // 抽奖之中
    static public class RaffleCenterEntity extends RaffleEntity {

    }

    // 抽奖之后
    static public class RaffleAfterEntity extends RaffleEntity {

    }
}
