package cn.learn.types.common;

public class Constants {

    public final static String SPLIT = ",";
    public final static String COLON = ":";
    public final static String SPACE = " ";

    public static class RedisKey {

        // 策略
        public static final String STRATEGY_KEY = "big_market_strategy_key_";
        // 策略奖品
        public static final String STRATEGY_AWARD_KEY = "big_market_strategy_award_key_";
        // 策略奖品列表
        public static final String STRATEGY_AWARD_LIST_KEY = "big_market_strategy_award_list_key_";
        // 策略抽奖表
        public static final String STRATEGY_RATE_TABLE_KEY = "big_market_strategy_rate_table_key_";
        // 奖品映射范围
        public static final String STRATEGY_RATE_RANGE_KEY = "big_market_strategy_rate_range_key_";
        // 奖品库存数量
        public static final String STRATEGY_AWARD_COUNT_KEY = "strategy_award_count_key_";
        // 奖品库存消耗的队列key
        public static final String STRATEGY_AWARD_COUNT_QUEUE_KEY = "strategy_award_count_queue_key";


        /**
         * 获取策略奖品概率表的缓存键。
         *
         * @param assembleKey 组装的键，用于标识具体的策略奖品概率表。
         * @return 返回策略奖品概率表的缓存键。
         */
        public static String acquireKey_strategyRateTable(String assembleKey) {
            return Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + assembleKey;
        }

        /**
         * 获取策略奖品概率范围的缓存键。
         *
         * @param assembleKey 组装的键，用于标识具体的策略奖品概率范围。
         * @return 返回策略奖品概率范围的缓存键。
         */
        public static String acquireKey_strategyRateRange(String assembleKey) {
            return STRATEGY_RATE_RANGE_KEY + assembleKey;
        }

        /**
         * 获取策略奖品实体的缓存键。
         *
         * @param strategyId 策略ID，用于标识具体的抽奖策略。
         * @param awardId    奖品ID，用于标识具体的奖品。
         * @return 返回策略奖品实体的缓存键。
         */
        public static String acquireKey_strategyAwardEntity(Long strategyId, Integer awardId) {
            return STRATEGY_AWARD_KEY + strategyId + "_" + awardId;
        }

        /**
         * 获取策略奖品列表的缓存键。
         *
         * @param strategyId 策略ID，用于标识具体的抽奖策略。
         * @return 返回策略奖品列表的缓存键。
         */
        public static String acquireKey_strategyAwardList(Long strategyId) {
            return STRATEGY_AWARD_LIST_KEY + strategyId;
        }

        // 获取奖品库存数量缓存的key
        public static String acquireKey_strategyAwardCount(Long strategyId, Integer awardId) {
            return STRATEGY_AWARD_COUNT_KEY + strategyId + "_" + awardId;
        }


        /**
         * 获取库存锁的key。
         * 通过加锁机制，确保在后续手动处理补加库存情况下，不会出现超卖的情况。
         *
         * @param cacheKey 缓存键，用于标识库存的唯一标识符。
         * @param surplus  减少后的库存值。
         * @return 生成的库存锁key。
         */
        public static String acquireKey_stockLock(String cacheKey, long surplus) {
            return cacheKey + "_" + surplus;
        }

        /**
         * @return 返回策略奖品库存计数队列的缓存键 {@code STRATEGY_AWARD_COUNT_QUEUE_KEY}。
         */
        public static String acquireStrategyAwardCountQueuekey() {
            return STRATEGY_AWARD_COUNT_QUEUE_KEY;
        }

    }

    public static class RuleModel {

        // 随机规则
        public static final String RULE_RANDOM = "rule_random";
        // 解锁规则
        public static final String RULE_LOCK = "rule_lock";
        // 兜底奖品规则
        public static final String RULE_LUCK_AWARD = "rule_luck_award";
        // 权重规则
        public static final String RULE_WEIGHT = "rule_weight";
        // 黑名单规则
        public static final String RULE_BLACKLIST = "rule_blacklist";
        // 默认抽奖规则
        public static final String DEFAULT = "default";
        // 库存规则
        public static final String RULE_STOCK = "rule_stock";
        // 中奖频率规则
        public static final String RULE_WIN_FREQUENCY = "rule_win_frequency";
        // 每日限额规则
        public static final String RULE_DAILY_LIMIT = "rule_daily_limit";
        // 奖品类型限制规则
        public static final String PRIZE_TYPE_RESTRICTION = "prize_type_restriction";

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
