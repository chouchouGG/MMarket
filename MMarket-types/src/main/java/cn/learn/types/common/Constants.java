package cn.learn.types.common;

public class Constants {

    public final static String SPLIT = ",";
    public final static String COLON = ":";
    public final static String SPACE = " ";

    public static class RedisKey {

        /** 策略前缀 */
        public static String STRATEGY_KEY = "big_market_strategy_key_";
        /** 策略奖品前缀 */
        public static String STRATEGY_AWARD_KEY = "big_market_strategy_award_key_";
        /** 策略比值前缀 */
        public static String STRATEGY_RATE_TABLE_KEY = "big_market_strategy_rate_table_key_";
        /** 奖品映射范围前缀 */
        public static String STRATEGY_RATE_RANGE_KEY = "big_market_strategy_rate_range_key_";
    }

    public static class RuleModel {
        public static String RULE_RANDOM = "rule_random";
        public static String RULE_LOCK = "rule_lock";
        public static String RULE_LUCK_AWARD = "rule_luck_award";
        /** 幸运值规则（权重规则） */
        public static String RULE_WEIGHT = "rule_weight";
        public static String RULE_BLACKLIST = "rule_blacklist";

    }


}
