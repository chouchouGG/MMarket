package cn.learn.test.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-06-14 13:44
 **/
@Slf4j
public class RuleWeightLogicFilterTest {

    @Test
    public void test_getPrev() {
        ArrayList<Long> analyticalSortedKeys = new ArrayList<>();
        analyticalSortedKeys.add(5000L);
        analyticalSortedKeys.add(4000L);
        analyticalSortedKeys.add(6000L);
        log.info("积分阈值为：{}", analyticalSortedKeys);
        Collections.sort(analyticalSortedKeys);
        log.info("积分阈值排序后为：{}", analyticalSortedKeys);
        Long userScore = 5500L;

//        // 3. 找出最小符合的值，也就是【4500 积分，能找到 4000:102,103,104,105】【5000 积分，能找到 5000:102,103,104,105,106,107】
//        Long nextValue = analyticalSortedKeys.stream()
//                .filter(key -> userScore >= key)
//                .findFirst()
//                .orElse(null);

         Long prevValue = 0L;
         for (Long score : analyticalSortedKeys) {
             if (score > userScore) {
                break;
             }
             prevValue = score;
         }

        System.out.println("不超过 " + userScore + " 的最大值为：" + prevValue);
    }

}
