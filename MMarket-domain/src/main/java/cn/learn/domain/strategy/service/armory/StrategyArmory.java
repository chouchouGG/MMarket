package cn.learn.domain.strategy.service.armory;

import cn.learn.domain.strategy.model.entity.StrategyAwardEntity;
import cn.learn.domain.strategy.respository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class StrategyArmory implements IStrategyArmory {

    /* NOTE: DDD 架构中，需要获取数据库中的某些值时，不用像 MVC 架构中直接
        去调用 DAO 层的接口，而是交给 infrastructure 基础层去完成。*/

    @Resource
    private IStrategyRepository strategyRepository;

    /**
     * NOTE: 如下是xfg计算【策略奖品查找表】的方式，存在问题，如果两个商品概率分别为 0.3 和 0.7，
     *  最终【策略奖品查找表】的两件奖品的概率会变成 40% 和 60%，也就是说不能做到和给定的概率精确相等。
     * <p>
     * // 4. 用 1 % 0.0001 获得概率范围，百分位、千分位、万分位
     * BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);
     * // 5. 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
     * List<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());
     * for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
     * Integer awardId = strategyAward.getAwardId();
     * BigDecimal awardRate = strategyAward.getAwardRate();
     * // 计算出每个概率值需要存放到查找表的数量，循环填充
     * for (int i = 0; i < rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue(); i++) {
     * strategyAwardSearchRateTables.add(awardId);
     * }
     * }
     *
     */

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

        // fixme: 概率装载的算法需要改进，问题可以参考上面的 NOTE 部分
/*        BigDecimal remainder = minAwardProbability.remainder(BigDecimal.ONE);
        // step: 小数点需要向右移动的次数
        int step = 0;
        while (minAwardProbability.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            minAwardProbability = minAwardProbability.movePointRight(1);
            step++;
        }*/

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

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int size = strategyRepository.getRateRange(strategyId);
        // 根据随机值范围大小，获取一个随机值
        /* note: SecureRandom 是 Java 提供的一个类，用于生成强随机数（也称为安全随机数）。
            与 java.util.Random 不同，SecureRandom 使用加密算法来生成随机数，确保其不可预测性和高安全性。*/
        int random = new SecureRandom().nextInt(size);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return strategyRepository.getStrategyAwardAssemble(strategyId, random);
    }
}
