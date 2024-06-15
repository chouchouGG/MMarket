package cn.learn.types.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Constants {

    public final static String SPLIT = ",";
    public final static String COLON = ":";
    public final static String SPACE = " ";

    public static class RedisKey {

        /** 策略前缀 */
        public static final String STRATEGY_KEY = "big_market_strategy_key_";
        /** 策略奖品前缀 */
        public static final String STRATEGY_AWARD_KEY = "big_market_strategy_award_key_";
        /** 策略比值前缀 */
        public static final String STRATEGY_RATE_TABLE_KEY = "big_market_strategy_rate_table_key_";
        /** 奖品映射范围前缀 */
        public static final String STRATEGY_RATE_RANGE_KEY = "big_market_strategy_rate_range_key_";
    }

    public static class RuleModel {
        public static final String RULE_RANDOM = "rule_random";
        public static final String RULE_LOCK = "rule_lock";
        public static final String RULE_LUCK_AWARD = "rule_luck_award";
        /** 幸运值规则（权重规则） */
        public static final String RULE_WEIGHT = "rule_weight";
        /* 黑名单规则 */
        public static final String RULE_BLACKLIST = "rule_blacklist";

    }

    /**
     * fixme：枚举类适用的场景：
     *  1. 需要类型安全。
     *  2. 需要附加方法、属性。
     *  3. 需要描述性信息的常量。
     */
//    @Getter
//    @AllArgsConstructor
//    public enum RuleModel {
//        RULE_RANDOM("rule_random"),
//        RULE_LOCK("rule_lock"),
//        RULE_LUCK_AWARD("rule_luck_award"),
//        RULE_WEIGHT("rule_weight"),
//        RULE_BLACKLIST("rule_blacklist")
//        ;
//
//         private final String name;
//    }


}
