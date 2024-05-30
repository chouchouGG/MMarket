package cn.learn.test.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @program: MMarket
 * @description:
 * @author: chouchouGG
 * @create: 2024-05-30 21:53
 **/
@Slf4j
public class BigDecimalTest {
    /**
     * 测试获取 BigDecimal 的精度和标度值
     */
    @Test
    public void test_precision_scale() {
        BigDecimal bd = new BigDecimal("15.6527");
        int precision = bd.precision();
        int scale = bd.scale();
        log.info("{} 的精度为: {}", bd, precision);
        log.info("{} 的标度为: {}", bd, scale);

        BigDecimal bd2 = new BigDecimal("15.60");
        int precision2 = bd2.precision();
        int scale2 = bd2.scale();
        log.info("{} 的精度为: {}", bd2, precision2);
        log.info("{} 的标度为: {}", bd2, scale2);
    }

    /**
     * 获取 BigDecimal 的小数部分
     */
    @Test
    public void test_remainder() {
        BigDecimal number = new BigDecimal("123.456");
        BigDecimal fractionPart = number.remainder(BigDecimal.ONE);
        System.out.println("小数部分: " + fractionPart);
    }

    /**
     * 获取 BigDecimal 的整数部分
     */
    @Test
    public void test_setScale() {
        BigDecimal number = new BigDecimal("123.456");
        BigDecimal integerPart = number.setScale(0, RoundingMode.FLOOR);
        System.out.println("整数部分为: " + integerPart);
    }

    @Test
    public void test_movePoint() {
        BigDecimal number = new BigDecimal("123.456");
        int step = 0;
        log.info("{}", number);
        while (number.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            number = number.movePointRight(1);
            step++;
            log.info("{}", number);
        }
        log.info("factor: {}", step); // step 是小数点向右移动的次数
    }

    @Test
    public void test_divide() {
        BigDecimal bd = new BigDecimal("0.3");
        BigDecimal bd2 = BigDecimal.ONE.divide(bd, 0, RoundingMode.CEILING);
        log.info("{}", bd2);
    }
}
