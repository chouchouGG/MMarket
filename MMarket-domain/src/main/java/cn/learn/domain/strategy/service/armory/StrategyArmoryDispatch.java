package cn.learn.domain.strategy.service.armory;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.model.entity.StrategyEntity;
import cn.learn.domain.strategy.model.entity.StrategyRuleEntity;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import cn.learn.types.common.Constants;
import cn.learn.types.enums.ResponseCode;
import cn.learn.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

/**
 * @program: MMarket
 * @description: 策略装配器的实现类
 * @author: chouchouGG
 * @create: 2024-05-30 14:07
 **/
@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory, IStrategyDispatch {

    /* NOTE: DDD 架构中，需要获取数据库中的某些值时，不用像 MVC 架构中直接去调用 DAO 层的接口，而是交给 infrastructure 基础层去完成。*/
    @Resource
    private IStrategyRepository strategyRepository;




/** 初始版本，默认抽奖策略的装配方案 ===========================
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 获取策略 id为 strategyId 的抽奖策略的配置列表
        List<StrategyAwardEntity> strategyAwardEntities = strategyRepository.queryStrategyAwardList(strategyId);
        // 2. 获取【最小概率值】
        BigDecimal minAwardProbability = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        // 3. 获取【概率值总和】
        BigDecimal totalAwardProbability = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

//        // fixme【已修改】: 概率装载的算法需要改进
//        BigDecimal remainder = minAwardProbability.remainder(BigDecimal.ONE);
//        // step: 小数点需要向右移动的次数
//        int step = 0;
//        while (minAwardProbability.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
//            minAwardProbability = minAwardProbability.movePointRight(1);
//            step++;
//        }

        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        // fixme【已修改】: 如下是xfg计算【策略奖品查找表】的方式，存在问题，如果两个商品概率分别为 0.3 和 0.7，
            最终【策略奖品查找表】的两件奖品的概率会变成 40% 和 60%，也就是说不能做到和给定的概率精确相等。
        // 4. 用 1 % 0.0001 获得概率范围，百分位、千分位、万分位
        BigDecimal rateRange = totalAwardProbability.divide(minAwardProbability, 0, RoundingMode.CEILING);

        // 5. 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
        List<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();
            // 计算出每个概率值需要存放到查找表的数量，循环填充
            for (int i = 0; i < rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue(); i++) {
                strategyAwardSearchRateTables.add(awardId);
            }
        }
        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


        // 6. 对存储的奖品进行乱序操作，（也是为了增加随机性）
        Collections.shuffle(strategyAwardSearchRateTables);

        // fixme: 直接使用 ArrayList 的下标作为映射是不是也可以？就不用放到 Map 里了
        // 7. 生成出Map集合，key值，对应的就是后续的概率值。通过概率来获得对应的奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new LinkedHashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTables.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i, strategyAwardSearchRateTables.get(i));
        }

        // note：和仓储有关的直接调用【 respository 仓储层】进行处理
        // 8. 存放到 Redis
        strategyRepository.storeStrategyAwardSearchRateTable(strategyId, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);

        return true;
    }
==================================================================*/


    /**
     * note: 抽奖策略装配分为两部分：1. 装配默认抽奖表，2. 装配幸运值抽奖表（权重抽奖表）
     */
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 获取当前策略对应的所有奖品列表
        List<StrategyAwardEntity> strategyAwardEntities = strategyRepository.queryStrategyAwardList(strategyId);

        // 2 缓存奖品库存【用于decr扣减库存使用】
        for (StrategyAwardEntity entity : strategyAwardEntities) {
            cacheStrategyAwardCount(strategyId, entity.getAwardId(), entity.getAwardCount());
        }

        // 3. 装配抽奖列表
        // 3.1. 装配默认的奖品列表
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntities);

        // 3.2. 装配幸运值抽奖表
        StrategyEntity strategyEntity = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        // 判断当前策略是否设置了权重规则，若没有设置权重规则，则直接返回，无需装配【幸运值抽奖表】（权重抽奖表）
        boolean isSetRuleWeight = strategyEntity.getRuleWeight();
        if (!isSetRuleWeight) {
            return true;
        }
        // 获取幸运值规则的配置
        StrategyRuleEntity strategyRuleEntity = strategyRepository.queryStrategyRule(strategyId, Constants.RuleModel.RULE_WEIGHT);
        // 提高代码健壮性：防止 strategy 表中配置了 rule_weight，但是 strategy_rule 中没有配置的异常
        if (null == strategyRuleEntity) {
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(),
                    ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }

        Map<String, List<Integer>> ruleWeightValueMap = strategyRuleEntity.getRuleWeightItem();
        Set<String> keys = ruleWeightValueMap.keySet();
        for (String key : keys) {
            List<Integer> ruleWeightValues = ruleWeightValueMap.get(key);
            ArrayList<StrategyAwardEntity> strategyAwardEntitiesClone = new ArrayList<>(strategyAwardEntities);
            strategyAwardEntitiesClone.removeIf(entity -> !ruleWeightValues.contains(entity.getAwardId()));
            // note: 权重规则可以理解为幸运值规则，不同程度的幸运值对应的不同的抽奖映射表
            // fixme：获取 reids 中 key 的操作应该专门作为一个方法放到一个类中
            String assembleKey = String.valueOf(strategyId).concat("_").concat(key);
            // 装配【幸运值抽奖表】🎯
            assembleLotteryStrategy(assembleKey, strategyAwardEntitiesClone);
        }

        return true;
    }


    /**
     * 组装抽奖策略
     *
     * @param key                     缓存的键
     * @param strategyAwardEntities   奖品实体列表
     */
    private void assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntities) {
        // 1. 获取最小概率值和概率值总和
        BigDecimal minAwardRate = getMinAwardRate(strategyAwardEntities);

        // 2. 获取最小奖品概率的标度，用于后续计算
        int stepToMove = getAwardRateScale(minAwardRate);

        // 3. 生成奖品概率查找表
        List<Integer> awardRateTable = getAwardRateSearchTable(strategyAwardEntities, stepToMove);

        // 4. 对存储的奖品进行乱序操作，以确保随机性
        Collections.shuffle(awardRateTable);

        // fixme: 直接使用 ArrayList 的下标作为映射是不是也可以？就不用放到 Map 里了？
        // 5. 生成映射表，key值对应概率值，通过概率来获得对应的奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new LinkedHashMap<>();
        for (int i = 0; i < awardRateTable.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i, awardRateTable.get(i));
        }

        // 6. 缓存，将映射表存放到 Redis
        strategyRepository.storeStrategyAwardSearchRateTable(key, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
    }

    /**
     * 生成奖品概率查找表
     *
     * @param strategyAwardEntities 奖品实体列表
     * @param stepToMove            小数点需要向右移动的次数
     * @return 奖品概率查找表
     */
    private static List<Integer> getAwardRateSearchTable(List<StrategyAwardEntity> strategyAwardEntities, int stepToMove) {
        // 参数校验
        if (strategyAwardEntities == null || strategyAwardEntities.isEmpty()) {
            throw new IllegalArgumentException("strategyAwardEntities 不能为 null 或空");
        }
        if (stepToMove < 0) {
            throw new IllegalArgumentException("stepToMove 为负数");
        }


        int room = 0;
        // 计算总的占位空间
        for (StrategyAwardEntity awardEntity : strategyAwardEntities) {
            room += awardEntity.getAwardRate().movePointRight(stepToMove).intValue();
        }

        // 4. 生成奖品概率表，需要在list集合中存放对应的奖品占位，空间换时间
        List<Integer> awardRateTable = new ArrayList<>(room);
        for (StrategyAwardEntity awardEntity : strategyAwardEntities) {
            Integer awardId = awardEntity.getAwardId();
            // 计算出每个概率值需要存放到查找表的数量，循环填充
            int count = awardEntity.getAwardRate().movePointRight(stepToMove).intValue();
            for (int i = 0; i < count; i++) {
                awardRateTable.add(awardId);
            }
        }
        return awardRateTable;
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        String key = String.valueOf(strategyId);
        // 获取当前策略对应的查找范围
        int size = strategyRepository.getRateRange(strategyId);
        // 根据随机值范围大小，获取一个随机值
        /* note: SecureRandom 是 Java 提供的一个类，用于生成强随机数（也称为安全随机数）。
            与 java.util.Random 不同，SecureRandom 使用加密算法来生成随机数，确保其不可预测性和高安全性。*/
        int random = new SecureRandom().nextInt(size);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        /* fixme: 如果【奖品映射表】（策略奖品查找表）已经被 redis 淘汰出内存，那么如下结果可能返回 null */
        return strategyRepository.getStrategyAwardAssemble(key, random);
    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        // fixme：获取 reids 中 key 的操作应该专门作为一个方法放到一个类中
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        int size = strategyRepository.getRateRange(key);
        int random = new SecureRandom().nextInt(size);
        /* fixme: 如果【奖品映射表】（策略奖品查找表）已经被 redis 淘汰出内存，那么如下结果可能返回 null */
        return strategyRepository.getStrategyAwardAssemble(key, random);
    }

    /**
     * 缓存奖品库存到Redis
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @param awardCount 奖品库存
     */
    private void cacheStrategyAwardCount(Long strategyId, Integer awardId, Integer awardCount) {
        String cacheKey = Constants.RedisKey.acquireKey_strategyAwardCount(strategyId, awardId);
        strategyRepository.cacheStrategyAwardCount(cacheKey, awardCount);
    }

    /**
     * 获取最小的奖励概率值
     *
     * @param strategyAwardEntities 奖励实体列表
     * @return 最小的奖励概率值，如果列表为空或 null，则返回 BigDecimal.ZERO
     */
    private static BigDecimal getMinAwardRate(List<StrategyAwardEntity> strategyAwardEntities) {
        // 1. 检查集合是否为空或空列表
        if (strategyAwardEntities == null || strategyAwardEntities.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 2. 初始化最小概率值为列表的第一个元素的概率值
        BigDecimal minAwardRate = strategyAwardEntities.get(0).getAwardRate();

        // 3. 遍历剩余的元素，寻找最小的概率值
        int size = strategyAwardEntities.size();
        for (int i = 1; i < size; i++) {
            StrategyAwardEntity entity = strategyAwardEntities.get(i);
            BigDecimal awardRate = entity.getAwardRate();
            // 4. 更新最小概率值
            if (awardRate.compareTo(minAwardRate) < 0) {
                minAwardRate = awardRate;
            }
        }
        // 5. 返回最小的概率值
        return minAwardRate;
    }


    /**
     * 转换小数为整数，并返回小数点向右移动的次数，即小数的标度scale。
     *
     * @param value 要转换的小数
     * @return 小数点向右移动的次数
     */
    private static int getAwardRateScale(BigDecimal value) {
        // scale: 小数点需要向右移动的次数
        int scale = 0;
        while (value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            value = value.movePointRight(1);
            scale++;
        }
        return scale;
    }

}
